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

import us.terebi.lang.lpc.compiler.java.context.ObjectId;
import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectDefinition;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.context.ObjectManager;
import us.terebi.lang.lpc.runtime.jvm.context.RuntimeContext;
import us.terebi.lang.lpc.runtime.jvm.support.ValueSupport;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;

/**
 * 
 */
public class InheritsEfun extends AbstractEfun implements FunctionSignature, Callable
{
    //    int inherits( string file, object obj );
    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        ArrayList<ArgumentDefinition> list = new ArrayList<ArgumentDefinition>();
        list.add(new ArgumentSpec("file", Types.STRING));
        list.add(new ArgumentSpec("obj", Types.OBJECT));
        return list;
    }

    public LpcType getReturnType()
    {
        return Types.INT;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        checkArguments(arguments);
        String file = ObjectId.normalise(arguments.get(0).asString());
        ObjectInstance object = arguments.get(1).asObject();
        ObjectDefinition definition = object.getDefinition();

        ObjectManager objectManager = RuntimeContext.obtain().system().objectManager();

        int result = inherits(file, definition, objectManager);
        return ValueSupport.intValue(result);
    }

    private int inherits(String file, ObjectDefinition definition, ObjectManager objectManager)
    {
        if (definition.getName().equals(file))
        {
            ObjectDefinition current = objectManager.findObject(file, true);
            if (current == definition)
            {
                return 1;
            }
            else
            {
                return 2;
            }
        }
        for (ObjectDefinition parent : definition.getInheritedObjects().values())
        {
            int inherit = inherits(file, parent, objectManager);
            if (inherit != 0)
            {
                return inherit;
            }
        }
        return 0;
    }

}
