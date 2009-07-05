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
import java.util.NoSuchElementException;

import us.terebi.util.Predicate;

public final class PredicateIterator<T> implements Iterator<T>
{
    private boolean _hasNext;
    private T _nextElement;
    private final Predicate< ? super T> _predicate;
    private final Iterator< ? extends T> _iterator;

    public PredicateIterator(Predicate< ? super T> predicate, Iterator< ? extends T> iterator)
    {
        _predicate = predicate;
        _iterator = iterator;
        advance();
    }

    private boolean advance()
    {
        while (true)
        {
            if (!_iterator.hasNext())
            {
                _nextElement = null;
                _hasNext = false;
                return false;
            }
            _nextElement = _iterator.next();
            if (_predicate.test(_nextElement))
            {
                return true;
            }
        }
    }

    public boolean hasNext()
    {
        return _hasNext;
    }

    public T next()
    {
        checkNext();
        T element = _nextElement;
        advance();
        return element;
    }

    private void checkNext()
    {
        if (!_hasNext)
        {
            throw new NoSuchElementException();
        }
    }

    public void remove()
    {
        throw new UnsupportedOperationException("remove - Not supported on " + getClass());
    }
}