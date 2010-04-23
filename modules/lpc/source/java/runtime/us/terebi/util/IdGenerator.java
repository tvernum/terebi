/* ------------------------------------------------------------------------
 * The Terebi (LPC) Game Engine
 * Copyright 2010 Tim Vernum
 * ------------------------------------------------------------------------
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ------------------------------------------------------------------------
 */

package us.terebi.util;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public class IdGenerator
{
    private int _id;
    private final int _first;
    private final int _last;

    public IdGenerator()
    {
        this(1, Integer.MAX_VALUE);
    }

    public IdGenerator(int start, int max)
    {
        _id = start;
        _first = start;
        _last = max;
    }

    public synchronized int next()
    {
        if (_id == _last)
        {
            _id = _first;
            return _last;
        }
        return _id++;
    }
}
