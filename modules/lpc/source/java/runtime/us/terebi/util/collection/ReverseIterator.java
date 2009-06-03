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

package us.terebi.util.collection;

import java.util.Iterator;
import java.util.ListIterator;

/**
 * 
 */
public class ReverseIterator<E> implements Iterator<E>
{
    private final ListIterator<E> _listIterator;

    public ReverseIterator(ListIterator<E> listIterator)
    {
        _listIterator = listIterator;
    }

    public boolean hasNext()
    {
        return _listIterator.hasPrevious();
    }

    public E next()
    {
        return _listIterator.previous();
    }

    public void remove()
    {
        _listIterator.remove();
    }

}
