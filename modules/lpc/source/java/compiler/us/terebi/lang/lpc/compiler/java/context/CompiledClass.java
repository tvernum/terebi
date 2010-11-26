/* ------------------------------------------------------------------------
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

package us.terebi.lang.lpc.compiler.java.context;

import java.util.HashMap;
import java.util.Map;

import us.terebi.lang.lpc.runtime.ClassDefinition;
import us.terebi.lang.lpc.runtime.FieldDefinition;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.LpcClass;

/**
 * 
 */
public class CompiledClass implements CompiledClassInstance
{
    private final ClassDefinition _definition;
    private final LpcClass _object;

    public CompiledClass(ClassDefinition definition, LpcClass object)
    {
        _definition = definition;
        _object = object;
    }

    public LpcClass getImplementingObject()
    {
        return _object;
    }

    public ClassDefinition getDefinition()
    {
        return _definition;
    }

    public Map<FieldDefinition, LpcValue> getFieldValues()
    {
        Map<FieldDefinition, LpcValue> values = new HashMap<FieldDefinition, LpcValue>();
        for (FieldDefinition field : _definition.getFields().values())
        {
            values.put(field, field.getValue(this));
        }
        return values;
    }

}
