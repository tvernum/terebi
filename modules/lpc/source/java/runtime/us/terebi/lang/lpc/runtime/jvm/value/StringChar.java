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
public class StringChar implements LpcReference
{
    private final LpcReference _string;
    private final Index _index;

    public StringChar(LpcReference string, Index index)
    {
        _string = string;
        _index = index;
    }

    public LpcValue get()
    {
        return get(_string.get(), _index);
    }

    public static LpcValue get(LpcValue value, Index index)
    {
        String str = value.asString();
        int i = getIndex(str, index);
        if (i == str.length())
        {
            return NilValue.INSTANCE;
        }
        return new IntValue(str.charAt(i));
    }

    private static int getIndex(String str, Index index)
    {
        long i = index.evaluate(str.length());
        if (i < 0)
        {
            throw new LpcRuntimeException("String index (" + i + ") out of bounds");
        }
        if (i > str.length())
        {
            throw new LpcRuntimeException("String index (" + i + ") out of bounds (" + str.length() + ")");
        }
        return (int) i;
    }

    public LpcType getType()
    {
        return Types.INT;
    }

    public boolean isSet()
    {
        return true;
    }

    public LpcValue set(LpcValue value)
    {
        String str = _string.get().asString();
        int index = getIndex(str, _index);
        if (index == str.length())
        {
            throw new LpcRuntimeException("String index (" + index + ") out of bounds (" + str.length() + ") for assignment");
        }
        char ch = (char) value.asLong();
        str = str.substring(0, index - 1) + ch + str.substring(index + 1);
        _string.set(new StringValue(str));
        return get();
    }

    public String toString()
    {
        return getClass().getSimpleName() + ":" + _string + "[" + _index + "]";
    }

}
