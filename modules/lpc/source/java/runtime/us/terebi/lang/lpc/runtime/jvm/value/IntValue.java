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

import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.support.MiscSupport;
import us.terebi.lang.lpc.runtime.jvm.type.Types;

/**
 * 
 */
public class IntValue extends AbstractValue
{
    private final long _value;

    public IntValue(long value)
    {
        _value = value;
    }

    protected CharSequence getDescription()
    {
        return "int (" + _value + ")";
    }

    public LpcType getActualType()
    {
        return Types.INT;
    }

    public long asLong()
    {
        return _value;
    }

    public double asDouble()
    {
        return _value;
    }

    public boolean asBoolean()
    {
        return _value != 0;
    }

    public String asString()
    {
        return String.valueOf(_value);
    }

    protected boolean equalsOther(LpcValue other)
    {
        if (super.equalsOther(other))
        {
            return true;
        }
        if (_value == 0 && MiscSupport.isNothing(other))
        {
            return true;
        }
        return false;
    }

    protected boolean valueEquals(LpcValue other)
    {
        return this.asLong() == other.asLong();
    }

    protected int valueHashCode()
    {
        return (int) (_value ^ (_value >>> 32));
    }

    public CharSequence debugInfo()
    {
        return String.valueOf(_value);
    }
}
