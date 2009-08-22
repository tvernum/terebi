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

package us.terebi.lang.lpc.runtime.util.type;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import us.terebi.lang.lpc.runtime.ClassDefinition;
import us.terebi.lang.lpc.runtime.ClassInstance;
import us.terebi.lang.lpc.runtime.FieldDefinition;
import us.terebi.lang.lpc.runtime.LpcValue;

/**
 * 
 */
public class DynamicClassInstance implements ClassInstance, DynamicUserType
{
    private final ClassDefinition _definition;
    private final Map<FieldDefinition, LpcValue> _fields;

    public DynamicClassInstance(ClassDefinition definition)
    {
        _definition = definition;
        _fields = new HashMap<FieldDefinition, LpcValue>();
    }

    public ClassDefinition getDefinition()
    {
        return _definition;
    }

    public Map<FieldDefinition, LpcValue> getFieldValues()
    {
        return Collections.unmodifiableMap(_fields);
    }

    public LpcValue getFieldValue(FieldDefinition field)
    {
        return _fields.get(field);
    }

    public void setFieldValue(FieldDefinition field, LpcValue value)
    {
        _fields.put(field, value);
    }

}
