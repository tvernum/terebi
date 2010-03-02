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

import java.util.Arrays;
import java.util.List;

import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.FieldDefinition;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.VoidValue;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;

/**
 * 
 */
public class StoreVariableEfun extends AbstractEfun implements Efun
{
    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        return Arrays.asList( //
                new ArgumentSpec("name", Types.STRING), //
                new ArgumentSpec("value", Types.MIXED) //
        );
    }

    public LpcType getReturnType()
    {
        return Types.VOID;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        checkArguments(arguments);

        ObjectInstance thisObject = ThisObjectEfun.this_object();
        String name = arguments.get(0).asString();
        LpcValue value = arguments.get(1);

        FieldDefinition field = thisObject.getDefinition().getFields().get(name);
        field.setValue(thisObject, value);
        
        return VoidValue.INSTANCE;
    }

}
