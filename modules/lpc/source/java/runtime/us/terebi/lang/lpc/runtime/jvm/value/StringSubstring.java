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
import us.terebi.lang.lpc.runtime.jvm.LpcConstants;
import us.terebi.lang.lpc.runtime.jvm.LpcReference;
import us.terebi.lang.lpc.runtime.jvm.type.Types;

/**
 * 
 */
public class StringSubstring implements LpcReference
{
    private final LpcReference _string;
    private final Index _start;
    private final Index _end;

    public StringSubstring(LpcReference string, LpcValue startIndex, boolean startReverse, LpcValue endIndex, boolean endReverse)
    {
        _string = string;
        _start = new Index(startIndex == null ? -1 : startIndex.asLong(), startReverse);
        _end = new Index(endIndex == null ? Integer.MAX_VALUE : endIndex.asLong(), endReverse);
    }

    public StringSubstring(LpcReference string, Index start, Index end)
    {
        _string = string;
        _start = start;
        _end = end;
    }

    public LpcValue get()
    {
        return substring(_string.get(), _start, _end);
    }

    public static StringValue substring(LpcValue value, Index startIndex, Index endIndex)
    {
        //      eval for ( int i=0; i<12; i++) { string s = "0123456789" ; write("[5.." + i + "] " + s[5..i]); }
        //        [5..0] 
        //        [5..1] 
        //        [5..2] 
        //        [5..3] 
        //        [5..4] 
        //        [5..5] 5
        //        [5..6] 56
        //        [5..7] 567
        //        [5..8] 5678
        //        [5..9] 56789
        //        [5..10] 56789
        //        [5..11] 56789
        //      eval for ( int i=0; i<12; i++) { string s = "0123456789" ; write("[" + i + "..5] " + s[i..5]); }
        //        [0..5] 012345
        //        [1..5] 12345
        //        [2..5] 2345
        //        [3..5] 345
        //        [4..5] 45
        //        [5..5] 5
        //        [6..5] 
        //        [7..5] 
        //        [8..5] 
        //        [9..5] 
        //        [10..5] 
        //        [11..5] 
        //      eval for ( int i=0; i<12; i++) { string s = "0123456789" ; write("[" + i + ".." + i + "] " + s[i..i]); }
        //        [0..0] 0
        //        [1..1] 1
        //        [2..2] 2
        //        [3..3] 3
        //        [4..4] 4
        //        [5..5] 5
        //        [6..6] 6
        //        [7..7] 7
        //        [8..8] 8
        //        [9..9] 9
        //        [10..10] 
        //        [11..11] 
        String str = value.asString();
        int start = getIndex(str, startIndex);
        int end = getIndex(str, endIndex);
        if (start > end || start >= str.length())
        {
            return LpcConstants.STRING.BLANK;
        }
        if (start < 0)
        {
            start = 0;
        }
        if (end >= str.length())
        {
            end = str.length();
        }
        else
        {
            end += 1;
        }
        return new StringValue(str.substring(start, end));
    }

    private static int getIndex(String str, Index idx)
    {
        long index = idx.index;
        if (idx.reverse)
        {
            index = str.length() - index;
        }
        //        if (index < 0)
        //        {
        //            throw new LpcRuntimeException("String index (" + index + ") out of bounds");
        //        }
        //        if (index > str.length())
        //        {
        //            throw new LpcRuntimeException("String index (" + index + ") out of bounds (" + str.length() + ")");
        //        }
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
