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

package us.terebi.lang.lpc.runtime.jvm.object;

import us.terebi.lang.lpc.runtime.FieldDefinition;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.UserTypeInstance;
import us.terebi.lang.lpc.runtime.jvm.LpcReference;

/**
 * 
 */
public class FieldReference implements LpcReference
{
    private final UserTypeInstance _instance;
    private final FieldDefinition _field;

    public FieldReference(UserTypeInstance instance, FieldDefinition field)
    {
        _instance = instance;
        _field = field;
    }

    public LpcValue get()
    {
        return _field.getValue(_instance);
    }

    public LpcType getType()
    {
        return _field.getType();
    }

    public boolean isSet()
    {
        return _field.getValue(_instance) != null;
    }

    public LpcValue set(LpcValue value)
    {
        _field.setValue(_instance, value);
        return value;
    }

}
