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

package us.terebi.lang.lpc.runtime.jvm.efun;

import java.util.ArrayList;
import java.util.List;

import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.ArgumentSemantics;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.ClassDefinition;
import us.terebi.lang.lpc.runtime.ClassInstance;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectDefinition;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.context.ObjectManager;
import us.terebi.lang.lpc.runtime.jvm.context.RuntimeContext;
import us.terebi.lang.lpc.runtime.jvm.context.SystemContext;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.ClassReference;
import us.terebi.lang.lpc.runtime.jvm.value.ClassValue;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;

import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isClassReference;
import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isString;

/**
 * 
 */
public class CloneObjectEfun extends AbstractEfun implements FunctionSignature, Callable
{
    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        ArrayList<ArgumentDefinition> list = new ArrayList<ArgumentDefinition>();
        list.add(new ArgumentSpec("name", Types.MIXED));
        list.add(new ArgumentSpec("args", Types.MIXED_ARRAY, true, ArgumentSemantics.BY_VALUE));
        return list;
    }

    public LpcType getReturnType()
    {
        return Types.MIXED;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        checkArguments(arguments);
        LpcValue type = arguments.get(0);
        if (isString(type))
        {
            return cloneObject(type.asString(), arguments.get(1).asList());
        }
        if (isClassReference(type))
        {
            return newClass(type.asExtension(ClassReference.class));
        }
        return badArgumentType(1, type.getActualType(), Types.STRING, Types.CLASS_REFERENCE);
    }

    private LpcValue newClass(ClassReference ref)
    {
        ClassDefinition definition = ref.getClassDefinition();
        ClassInstance instance = definition.newInstance(ref.getOwner());
        return new ClassValue(instance);
    }

    private LpcValue cloneObject(String name, List< ? extends LpcValue> arguments)
    {
        SystemContext system = RuntimeContext.obtain().system();
        ObjectManager objectManager = system.objectManager();
        if (objectManager == null)
        {
            throw new IllegalStateException("No object manager in context " + system);
        }
        ObjectDefinition definition = objectManager.findObject(name, true);
        if (definition == null)
        {
            throw new LpcRuntimeException("Object not found - " + name);
        }
        ObjectInstance instance = definition.newInstance(arguments);
        return instance.asValue();
    }

}
