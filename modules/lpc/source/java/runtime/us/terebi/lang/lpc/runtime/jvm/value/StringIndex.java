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
import us.terebi.lang.lpc.runtime.jvm.LpcReference;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;
import us.terebi.lang.lpc.runtime.jvm.type.Types;

/**
 * 
 */
public class StringIndex implements LpcReference
{
    private final LpcReference _string;
    private final LpcValue _index;
    private final boolean _reverse;

    public StringIndex(LpcReference string, LpcValue index, boolean reverse)
    {
        _string = string;
        _index = index;
        _reverse = reverse;
    }

    public LpcValue get()
    {
        String str = _string.get().asString();
        int index = getIndex(str);
        if (index == str.length())
        {
            return NilValue.INSTANCE;
        }
        return new IntValue(str.charAt(index));
    }

    private int getIndex(String str)
    {
        long index = _index.asLong();
        if (_reverse)
        {
            index = str.length() - index;
        }
        if (index < 0)
        {
            throw new LpcRuntimeException("String index (" + index + ") out of bounds");
        }
        if (index > str.length())
        {
            throw new LpcRuntimeException("String index (" + index + ") out of bounds (" + str.length() + ")");
        }
        return (int) index;
    }

    public LpcType getType()
    {
        return Types.INT;
    }

    public boolean isSet()
    {
        return true;
    }

    public void set(LpcValue value)
    {
        String str = _string.get().asString();
        int index = getIndex(str);
        if (index == str.length())
        {
            throw new LpcRuntimeException("String index (" + index + ") out of bounds (" + str.length() + ") for assignment");
        }
        char ch = (char) value.asLong();
        str = str.substring(0, index - 1) + ch + str.substring(index + 1);
        _string.set(new StringValue(str));
    }

}
