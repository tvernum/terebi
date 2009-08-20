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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * 
 */
public class ArrayStack<E> implements Stack<E>
{
    private final List<E> _list;

    public ArrayStack()
    {
        _list = new ArrayList<E>();
    }

    public E peek()
    {
        return peek(0);
    }

    public E peek(int offset)
    {
        return _list.get(_list.size() - offset - 1);
    }

    public E pop()
    {
        return _list.remove(_list.size() - 1);
    }

    public void push(E element)
    {
        _list.add(element);
    }

    public boolean add(E o)
    {
        return _list.add(o);
    }

    public boolean addAll(Collection< ? extends E> c)
    {
        return _list.addAll(c);
    }

    public boolean addAll(int index, Collection< ? extends E> c)
    {
        return _list.addAll(index, c);
    }

    public void clear()
    {
        _list.clear();
    }

    public boolean contains(Object o)
    {
        return _list.contains(o);
    }

    public boolean containsAll(Collection< ? > c)
    {
        return _list.containsAll(c);
    }

    public int hashCode()
    {
        return _list.hashCode();
    }

    public boolean isEmpty()
    {
        return _list.isEmpty();
    }

    /**
     * Return an iterator over the elements in the stack. The iterator will be in a FIFO order - the most recently pushed element will be retrieved first.
     */
    public Iterator<E> iterator()
    {
        return new ReverseIterator<E>(_list.listIterator(size()));
    }

    public boolean remove(Object o)
    {
        return _list.remove(o);
    }

    public boolean removeAll(Collection< ? > c)
    {
        return _list.removeAll(c);
    }

    public boolean retainAll(Collection< ? > c)
    {
        return _list.retainAll(c);
    }

    public int size()
    {
        return _list.size();
    }

    public Object[] toArray()
    {
        return _list.toArray();
    }

    public <T> T[] toArray(T[] a)
    {
        return _list.toArray(a);
    }

    public E top()
    {
        return _list.get(0);
    }
    
    public String toString()
    {
        return _list.toString();
    }
}
