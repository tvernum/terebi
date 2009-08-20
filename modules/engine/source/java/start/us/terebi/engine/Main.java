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

package us.terebi.engine;

import java.io.File;

import org.apache.log4j.Logger;

import us.terebi.engine.config.PropertiesConfig;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public class Main
{
    private static final Logger LOG = Logger.getLogger(Main.class);

    public static void main(String[] args) throws Exception
    {
        if (args.length != 1)
        {
            throw new RuntimeException("Usage: <java> " + Main.class.getName() + " <config-file>");
        }
        File configFile = new File(args[0]);
        PropertiesConfig config = new PropertiesConfig(configFile);
        try
        {
            new Engine(config).run();
        }
        catch (InterruptedException e)
        {
            LOG.fatal("Interrupted");
        }
        catch (Exception e)
        {
            LOG.fatal("Shutting down server", e);
        }
    }

}
