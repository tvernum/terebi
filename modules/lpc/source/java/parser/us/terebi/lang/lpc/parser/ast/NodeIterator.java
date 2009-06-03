/* ------------------------------------------------------------------------
 * $Id$
 * Copyright 2008 Tim Vernum
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

package us.terebi.lang.lpc.parser.ast;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * 
 */
public class NodeIterator implements Iterator<SimpleNode>
{
    private final SimpleNode _base;
    private int _index;

    public NodeIterator(SimpleNode base)
    {
        _base = base;
        _index = 0;
    }

    public boolean hasNext()
    {
        return _index < _base.jjtGetNumChildren();
    }

    public SimpleNode next()
    {
        if (_index < _base.jjtGetNumChildren())
        {
            Node n = _base.jjtGetChild(_index);
            _index++;
            return (SimpleNode) n;
        }
        else
        {
            throw new NoSuchElementException("No child " + _index + " in " + _base);
        }
    }

    public void remove()
    {
        throw new UnsupportedOperationException("remove - Not implemented");
    }

}
