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
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.context.ObjectManager;
import us.terebi.lang.lpc.runtime.jvm.context.RuntimeContext;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.ArrayBuilder;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;

import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isNil;

/**
 * 
 */
public class ObjectsEfun extends AbstractEfun implements FunctionSignature, Callable
{
    //    object array objects();
    //    object array objects( string func, object ob );
    //    object array objects( function f);

    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        ArrayList<ArgumentDefinition> list = new ArrayList<ArgumentDefinition>();
        list.add(new ArgumentSpec("func", Types.MIXED));
        list.add(new ArgumentSpec("obj", Types.OBJECT));
        return list;
    }

    public boolean acceptsLessArguments()
    {
        return true;
    }

    public LpcType getReturnType()
    {
        return Types.OBJECT_ARRAY;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        ObjectManager objectManager = RuntimeContext.obtain().system().objectManager();

        Callable filter = null;
        LpcValue a1 = getArgument(arguments, 0);
        LpcValue a2 = getArgument(arguments, 1);

        int resultSize = objectManager.objectCount();
        if (!isNil(a1))
        {
            filter = getFunctionReference(a1, a2);
            resultSize /= 5;
            /* The 1/5 is chosen because array list increments by 3/2 when it grows, 
             * and this is a good approximation that allows for 4 growth events and
             * ends up at approx the right size, if all objects are filtered 'in' */
        }

        ArrayBuilder result = new ArrayBuilder(Types.OBJECT_ARRAY, resultSize);
        
        for (ObjectInstance instance : objectManager.objects())
        {
            LpcValue object = instance.asValue();

            if (filter != null)
            {
                LpcValue check = filter.execute(object);
                if (!check.asBoolean())
                {
                    continue;
                }
            }
            result.add(object);
        }
        
        return result.toArray();
    }

}
