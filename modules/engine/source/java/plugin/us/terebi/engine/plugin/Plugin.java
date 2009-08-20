/* ------------------------------------------------------------------------
 * The Terebi (LPC) Game Engine
 * Copyright 2009 Tim Vernum
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

package us.terebi.engine.plugin;

import us.terebi.engine.config.Config;
import us.terebi.lang.lpc.runtime.jvm.context.SystemContext;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public interface Plugin
{
    public void load(Config config, SystemContext context);
    public void init(SystemContext context);
    public void start(SystemContext context);
    public void run(SystemContext context);
}
