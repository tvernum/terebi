/* ------------------------------------------------------------------------
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

package us.terebi.plugins.compat;

import us.terebi.engine.config.Config;
import us.terebi.engine.config.ConfigException;
import us.terebi.engine.objects.CompileOptions;
import us.terebi.engine.plugin.Plugin;
import us.terebi.engine.server.TerebiServer;
import us.terebi.lang.lpc.runtime.jvm.context.SystemContext;

/**
 * @version $Revision$
 */
public class CompatPlugin implements Plugin
{
    public void init(SystemContext context)
    {
        // No-op
    }

    public void load(Config config, SystemContext context)
    {
        CompileOptions compileOptions = context.attachments().get(CompileOptions.class);
        if (compileOptions == null)
        {
            throw new ConfigException("No " + CompileOptions.class + " attached to " + context);
        }

        String mudName = config.getString("compat.mud.name");
        compileOptions.defineString("MUD_NAME", mudName);

        long[] ports = config.getLongs(TerebiServer.TELNET_PORT_KEY);
        if (ports != null && ports.length > 0)
        {
            compileOptions.defineLong("__PORT__", ports[0]);
        }

        compileOptions.defineLong("__LARGEST_PRINTABLE_STRING__", config.getLong("compat.largest.printable.string", 8192));
    }

    public void run(SystemContext context)
    {
        // No-op
    }

    public void start(SystemContext context)
    {
        // No-op
    }

}
