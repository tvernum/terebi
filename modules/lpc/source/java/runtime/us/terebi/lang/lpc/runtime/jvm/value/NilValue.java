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

import java.util.Collections;
import java.util.List;

import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.type.Types;

/**
 * 
 */
public class NilValue extends AbstractValue implements LpcValue
{
    public static final NilValue INSTANCE = new NilValue(Types.MIXED);

    private final LpcType _expectedType;

    public NilValue(LpcType expectedType)
    {
        _expectedType = expectedType;
    }

    public LpcType getExpectedType()
    {
        return _expectedType;
    }

    protected CharSequence getDescription()
    {
        return "(nil:" + _expectedType + ")";
    }

    public LpcType getActualType()
    {
        return Types.NIL;
    }

    public boolean asBoolean()
    {
        return false;
    }

    public long asLong()
    {
        return 0;
    }

    public double asDouble()
    {
        return 0;
    }

    public String asString()
    {
        return "";
    }

    public List<LpcValue> asList()
    {
        return Collections.emptyList();
    }

    protected boolean valueEquals(LpcValue other)
    {
        return true;
    }

    protected int valueHashCode()
    {
        return 0x79c;
    }

    public CharSequence debugInfo()
    {
        return "0";
    }

}
