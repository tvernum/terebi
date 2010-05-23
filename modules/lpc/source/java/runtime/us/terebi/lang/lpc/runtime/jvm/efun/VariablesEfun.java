/* ------------------------------------------------------------------------
 * Copyright 2010 Tim Vernum
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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.FieldDefinition;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.MemberDefinition.Modifier;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.ArrayValue;
import us.terebi.lang.lpc.runtime.jvm.value.StringValue;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;

/**
 * 
 */
public class VariablesEfun extends AbstractEfun implements Efun
{
    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        return Arrays.asList( //
                new ArgumentSpec("name", Types.OBJECT), //
                new ArgumentSpec("flag", Types.INT) //
        );
    }

    public LpcType getReturnType()
    {
        return Types.MIXED_ARRAY;
    }

    public boolean acceptsLessArguments()
    {
        return true;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        checkArguments(arguments);

        ObjectInstance object = arguments.get(0).asObject();
        boolean flag = arguments.get(1).asBoolean();

        Collection< ? extends FieldDefinition> fields = object.getDefinition().getFields().values();
        List<LpcValue> result = new ArrayList<LpcValue>(fields.size());
        for (FieldDefinition field : fields)
        {
            if (flag)
            {
                result.add(new ArrayValue(Types.STRING_ARRAY, new StringValue(field.getName()), new StringValue(describe(field.getType(),
                        field.getModifiers()))));
            }
            else
            {
                result.add(new StringValue(field.getName()));
            }
        }
        
        return new ArrayValue(flag ? Types.STRING_ARRAY_ARRAY : Types.STRING_ARRAY, result);
    }

    private CharSequence describe(LpcType type, Set< ? extends Modifier> modifiers)
    {
        if (modifiers.isEmpty())
        {
            return type.toString();
        }

        final int capacity = (modifiers.size() + 1) * 8;
        StringBuilder builder = new StringBuilder(capacity);
        
        for (Modifier modifier : modifiers)
        {
            builder.append(modifier.toString().toLowerCase());
            builder.append(' ');
        }
        builder.append(type);
        return builder;
    }

}
