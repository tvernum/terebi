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

import java.util.Collections;
import java.util.List;

import us.terebi.lang.lpc.compiler.java.context.ObjectId;
import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.context.ObjectManager;
import us.terebi.lang.lpc.runtime.jvm.context.RuntimeContext;
import us.terebi.lang.lpc.runtime.jvm.context.ThreadContext;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.NilValue;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;

/**
 * 
 */
public class FindObjectEfun extends AbstractEfun implements FunctionSignature, Callable
{
    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        return Collections.singletonList(new ArgumentSpec("name", Types.STRING));
    }

    public LpcType getReturnType()
    {
        return Types.OBJECT;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        checkArguments(arguments);
        String name = arguments.get(0).asString();

        ObjectInstance object = find_object(name);
        if (object == null)
        {
            return NilValue.INSTANCE;
        }
        else
        {
            return object.asValue();
        }
    }

    public static ObjectInstance find_object(String name)
    {
        ThreadContext context = RuntimeContext.obtain();
        ObjectManager manager = context.system().objectManager();
        try
        {
            ObjectInstance object = manager.findObject(new ObjectId(name));
            return object;
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

}
