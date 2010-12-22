/* ------------------------------------------------------------------------
 * $Id$
 * Copyright 2009 Tim Vernum
 * ------------------------------------------------------------------------
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ------------------------------------------------------------------------
 */

package us.terebi.plugins.interactive.efun;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import us.terebi.engine.server.ObjectShell;
import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.efun.AbstractEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.FindObjectEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.environment.Environment;
import us.terebi.lang.lpc.runtime.jvm.support.MiscSupport;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.VoidValue;
import us.terebi.lang.lpc.runtime.util.Apply;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;

/**
 * 
 */
public class MessageEfun extends AbstractEfun implements FunctionSignature, Callable
{
    //    void message( mixed class, mixed message, mixed target, mixed exclude );

    private static final Apply APPLY = new Apply("receive_message");

    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        ArrayList<ArgumentDefinition> list = new ArrayList<ArgumentDefinition>();
        list.add(new ArgumentSpec("class", Types.MIXED));
        list.add(new ArgumentSpec("message", Types.MIXED));
        list.add(new ArgumentSpec("target", Types.MIXED));
        list.add(new ArgumentSpec("exclude", Types.MIXED));
        return list;
    }

    public boolean acceptsLessArguments()
    {
        return true;
    }

    public LpcType getReturnType()
    {
        return Types.VOID;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        LpcValue messageClass = getArgument(arguments, 0);
        LpcValue messageContent = getArgument(arguments, 1);
        LpcValue target = getArgument(arguments, 2);
        LpcValue exclude = getArgument(arguments, 3);

        Collection<ObjectInstance> targets = getTargets(target);
        Collection<ObjectInstance> excludes = getObjects(exclude);
        targets.removeAll(excludes);

        arguments = Arrays.asList(messageClass, messageContent);
        for (ObjectInstance objectInstance : targets)
        {
            APPLY.invoke(objectInstance, arguments);
        }

        return VoidValue.INSTANCE;
    }

    private Collection<ObjectInstance> getTargets(LpcValue target)
    {
        // FluffOS does some magic here if target is zero. We don't (yet?)
        if (MiscSupport.isArray(target))
        {
            return getTargets(target.asList());
        }
        else
        {
            return getTargets(Collections.singletonList(target));
        }
    }

    private Collection<ObjectInstance> getTargets(List<LpcValue> elements)
    {
        List<ObjectInstance> list = new ArrayList<ObjectInstance>(elements.size());
        for (LpcValue element : elements)
        {
            ObjectInstance instance;
            if (MiscSupport.isString(element))
            {
                instance = FindObjectEfun.find_object(element.asString());
            }
            else
            {
                instance = element.asObject();
            }
            if (ObjectShell.isConnectionObject(instance) || APPLY.existsIn(instance))
            {
                list.add(instance);
            }
            else
            {
                list.addAll(Environment.getInventory(instance, false));
            }
        }
        return list;
    }

    private Collection<ObjectInstance> getObjects(LpcValue value)
    {
        if (MiscSupport.isArray(value))
        {
            List<ObjectInstance> list = new ArrayList<ObjectInstance>();
            for (LpcValue el : value.asList())
            {
                list.add(el.asObject());
            }
            return list;
        }
        if (MiscSupport.isNothing(value))
        {
            return Collections.emptyList();
        }
        return Collections.singleton(value.asObject());
    }

}
