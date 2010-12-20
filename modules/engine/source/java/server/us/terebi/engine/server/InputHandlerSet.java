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

package us.terebi.engine.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public class InputHandlerSet
{
    private final List<InputHandler> _handlers;

    public InputHandlerSet(InputHandler... handlers)
    {
        _handlers = new ArrayList<InputHandler>(Arrays.asList(handlers));
    }

    public void prepend(InputHandler handler)
    {
        _handlers.add(0, handler);
    }

    public void append(InputHandler handler)
    {
        _handlers.add(handler);
    }

    public void remove(InputHandler handler)
    {
        _handlers.remove(handler);
    }

    public Collection<InputHandler> handlers()
    {
        return Collections.unmodifiableCollection(_handlers);
    }

    public boolean contains(Class< ? extends InputHandler> type)
    {
        for (InputHandler handler : this._handlers)
        {
            if (type.isInstance(handler))
            {
                return true;
            }
        }
        return false;
    }
}
