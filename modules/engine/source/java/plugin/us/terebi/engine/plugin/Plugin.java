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

import java.util.Properties;

import us.terebi.engine.config.Config;
import us.terebi.lang.lpc.runtime.jvm.context.SystemContext;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public interface Plugin
{
    /**
     * Called very early in the initialisation process, before the master object or simul-efun objects are loaded.
     * The provided {@link SystemContext} will have efuns, and some attachments (such as {@link us.terebi.engine.objects.CompileOptions},
     * but will not have an object manager or master/simul-efun objects.
     * This is the appropriate place to configure new efuns (so they can be used in the master object) and new preprocessor directives
     * @param properties @TODO
     */
    public void load(Config config, SystemContext context, Properties properties);

    /**
     * Called during the initialisation process, after the object manager is loaded, but before the master object and simul-efun objects are loaded
     * This is the appropriate place to register listeners with the object manager
     */
    public void init(SystemContext context);

    /**
     * Called during the initialisation process, after the master object and simul-efun objects are loaded, but before epilog is called in the master object
     * This is the appropriate place to load any objects, or to verify that the correct applies have been defined in the master object
     */
    public void epilog(SystemContext context);

    /**
     * Called after the initialisation process, before external connections are opened.
     * This is the last chance to do anything before connections are opened
     */
    public void start(SystemContext context);

    /**
     * Called at the end of the start-up process, after external connections are opened.
     * The driver is now running and listening, it's really too late to do anything useful
     */
    public void run(SystemContext context);
}
