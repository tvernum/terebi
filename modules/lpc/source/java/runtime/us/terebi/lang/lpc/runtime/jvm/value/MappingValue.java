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

package us.terebi.lang.lpc.runtime.jvm.value;

import java.util.HashMap;
import java.util.Map;

import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.type.Types;

/**
 * 
 */
public class MappingValue extends AbstractValue implements LpcValue
{
    private final Map<LpcValue, LpcValue> _map;

    public MappingValue(int initialSize)
    {
        this(new HashMap<LpcValue, LpcValue>(initialSize));
    }
    
    public MappingValue(Map<LpcValue,LpcValue> map)
    {
        _map = map;
    }

    public Map<LpcValue, LpcValue> asMap()
    {
        return _map;
    }

    protected CharSequence getDescription()
    {
        return "mapping";
    }

    public LpcType getActualType()
    {
        return Types.MAPPING;
    }

    protected boolean valueEquals(LpcValue other)
    {
        return this.asMap().equals(other.asMap());
    }

    protected int valueHashCode()
    {
        return this.asMap().hashCode();
    }

}
