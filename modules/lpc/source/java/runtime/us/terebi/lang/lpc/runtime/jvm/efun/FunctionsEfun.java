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
import java.util.Collection;
import java.util.List;
import java.util.Set;

import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.MethodDefinition;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.ArrayBuilder;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;

/**
 * 
 */
public class FunctionsEfun extends AbstractEfun implements FunctionSignature, Callable
{
    // string array functions(object, int default: 0);
    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        ArrayList<ArgumentDefinition> list = new ArrayList<ArgumentDefinition>();
        list.add(new ArgumentSpec("obj", Types.OBJECT));
        list.add(new ArgumentSpec("flag", Types.INT));
        return list;
    }

    public boolean acceptsLessArguments()
    {
        return true;
    }

    public LpcType getReturnType()
    {
        return Types.MIXED_ARRAY;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        checkArguments(arguments, 1);
        ObjectInstance object = arguments.get(0).asObject();
        long flag = 0;
        if (arguments.size() > 1)
        {
            flag = arguments.get(1).asLong();
        }
        if (flag == 0)
        {
            return function_names(object).toArray();
        }
        else
        {
            return function_info(object).toArray();
        }
    }

    private ArrayBuilder function_names(ObjectInstance object)
    {
        Set<String> names = object.getDefinition().getMethods().keySet();
        ArrayBuilder builder = new ArrayBuilder(Types.STRING_ARRAY, names.size());
        for (String name : names)
        {
            builder.add(name);
        }
        return builder;
    }

    private ArrayBuilder function_info(ObjectInstance object)
    {
        Collection< ? extends MethodDefinition> definitions = object.getDefinition().getMethods().values();
        ArrayBuilder values = new ArrayBuilder(Types.arrayOf(Types.MIXED_ARRAY), definitions.size());
        for (MethodDefinition definition : definitions)
        {
            FunctionSignature signature = definition.getSignature();
            ArrayBuilder elements = new ArrayBuilder(Types.MIXED_ARRAY, 3 + signature.getArguments().size());
            elements.add(definition.getName());
            elements.add(signature.getArguments().size());
            elements.add(TypeofEfun.getTypeString(signature.getReturnType()));
            for (ArgumentDefinition argument : signature.getArguments())
            {
                elements.add(TypeofEfun.getTypeString(argument.getType()));
            }
            values.add(elements);
        }
        return values;
    }
}
