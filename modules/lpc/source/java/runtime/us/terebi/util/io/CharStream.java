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

package us.terebi.util.io;

import java.util.NoSuchElementException;

/**
 * 
 */
public class CharStream
{
    private final CharSequence _string;
    private int _index;
    private final int _end;

    public CharStream(CharSequence string)
    {
        this(string, 0);
    }

    public CharStream(CharSequence string, int start)
    {
        this(string, start, string.length());
    }

    public CharStream(CharSequence string, int start, int end)
    {
        if (end > string.length())
        {
            throw new IllegalArgumentException("Attempt to set stream end point ("
                    + end
                    + ") after end of string ("
                    + string
                    + ")");
        }
        _string = string;
        _index = start;
        _end = end;
    }

    public boolean eof()
    {
        return _index == _end;
    }

    public char read()
    {
        char ch = peek();
        _index++;
        return ch;
    }

    public char peek()
    {
        if (eof())
        {
            throw new NoSuchElementException("Attempt to read past the end of " + this);
        }
        return _string.charAt(_index);
    }

    public String toString()
    {
        return getClass().getSimpleName() + ":" + _string + "[@" + _index + ".." + _end + "]";
    }

}
