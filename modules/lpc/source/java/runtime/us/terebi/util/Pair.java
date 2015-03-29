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
public class Pair<T1, T2>
{
    private final T1 _first;
    private final T2 _second;

    public Pair(final T1 first, final T2 second)
    {
        _first = first;
        _second = second;
    }
    
    public T1 getFirst()
    {
        return _first;
    }
    
    public T2 getSecond()
    {
        return _second;
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + '{' + _first + " , " + _second + '}';
    }
}
