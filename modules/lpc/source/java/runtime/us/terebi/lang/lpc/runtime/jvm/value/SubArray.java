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

import java.util.List;

import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.LpcConstants;
import us.terebi.lang.lpc.runtime.jvm.LpcReference;
import us.terebi.lang.lpc.runtime.jvm.type.Types;

/**
 * 
 */
public class SubArray implements LpcReference
{
    private final LpcReference _array;
    private final Index _start;
    private final Index _end;

    public SubArray(LpcReference string, Index start, Index end)
    {
        _array = string;
        _start = start;
        _end = end;
    }

    public LpcValue get()
    {
        return subArray(_array.get(), _start, _end);
    }

    public static ArrayValue subArray(LpcValue value, Index startIndex, Index endIndex)
    {
        List<LpcValue> list = value.asList();
        int start = getIndex(list, startIndex);
        int end = getIndex(list, endIndex);
        if (start > end || start >= list.size())
        {
            return LpcConstants.ARRAY.EMPTY;
        }
        if (start < 0)
        {
            start = 0;
        }
        if (end >= list.size())
        {
            end = list.size();
        }
        else
        {
            end += 1;
        }
        return new ArrayValue(value.getActualType(), list.subList(start, end));
    }

    private static int getIndex(List< ? extends LpcValue> list, Index idx)
    {
        if (idx.index == -1)
        {
            return list.size() - 1;
        }
        long index = idx.index;
        if (idx.reverse)
        {
            index = list.size() - index;
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

    public LpcValue set(LpcValue value)
    {
        //      eval for ( int i=0; i<10; i++) { string s = "0123456789" ; s[5..i] = "#@!" ; write("[5.." + i + "] " + s); }
        //      [5..0] 01234#@!123456789
        //      [5..1] 01234#@!23456789
        //      [5..2] 01234#@!3456789
        //      [5..3] 01234#@!456789
        //      [5..4] 01234#@!56789
        //      [5..5] 01234#@!6789
        //      [5..6] 01234#@!789
        //      [5..7] 01234#@!89
        //      [5..8] 01234#@!9
        //      [5..9] 01234#@!
        throw new UnsupportedOperationException("set - Not implemented");
    }

}
