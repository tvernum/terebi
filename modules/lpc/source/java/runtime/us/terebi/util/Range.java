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

package us.terebi.util;

/**
 * 
 */
public class Range<T extends Comparable<T>>
{
    private final T _start;
    private final T _end;

    public Range(T start, T end)
    {
        if (start == null)
        {
            throw new NullPointerException("Cannot start a range at null");
        }
        if (end == null)
        {
            throw new NullPointerException("Cannot end a range at null");
        }
        if (start.compareTo(end) > 0)
        {
            throw new IllegalArgumentException("Range [" + start + " to " + end + "] is inverted");
        }
        _start = start;
        _end = end;
    }

    public T getStart()
    {
        return _start;
    }

    public T getEnd()
    {
        return _end;
    }

    public boolean inRange(T value)
    {
        if (value == null)
        {
            throw new NullPointerException("Cannot test 'null' as being inRange");
        }
        if (value.compareTo(_start) < 0)
        {
            return false;
        }
        if (value.compareTo(_end) > 0)
        {
            return false;
        }
        return true;
    }

    public String toString()
    {
        if (_start.equals(_end))
        {
            return _start.toString();
        }
        else
        {
            return "between " + _start + " and " + _end;
        }
    }
}
