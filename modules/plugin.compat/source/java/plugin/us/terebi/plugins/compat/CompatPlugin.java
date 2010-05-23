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

import java.util.Properties;

import us.terebi.engine.config.Config;
import us.terebi.engine.config.ConfigException;
import us.terebi.engine.config.ConfigNames;
import us.terebi.engine.objects.CompileOptions;
import us.terebi.engine.plugin.AbstractPlugin;
import us.terebi.engine.plugin.Plugin;
import us.terebi.lang.lpc.runtime.jvm.context.Efuns;
import us.terebi.lang.lpc.runtime.jvm.context.SystemContext;
import us.terebi.plugins.compat.efun.MaxEvalCostEfun;
import us.terebi.plugins.compat.efun.ResetEvalCostEfun;
import us.terebi.plugins.compat.efun.SetEvalLimitEfun;

/**
 * @version $Revision$
 */
public class CompatPlugin extends AbstractPlugin implements Plugin
{
    public void load(Config config, SystemContext context, Properties properties)
    {
        CompileOptions compileOptions = context.attachments().get(CompileOptions.class);
        if (compileOptions == null)
        {
            throw new ConfigException("No " + CompileOptions.class + " attached to " + context);
        }

        String mudName = config.getString("compat.mud.name");
        compileOptions.defineString("MUD_NAME", mudName);
        compileOptions.defineString("__VERSION__", "Terebi-" + properties.getProperty("plugin.version"));

        long[] ports = config.getLongs(ConfigNames.TELNET_PORT);
        if (ports != null && ports.length > 0)
        {
            compileOptions.defineLong("__PORT__", ports[0]);
        }

        compileOptions.defineLong("__LARGEST_PRINTABLE_STRING__", config.getLong("compat.largest.printable.string", 8192));

        Efuns efuns = context.efuns();
        efuns.define("max_eval_cost", new MaxEvalCostEfun());
        efuns.define("reset_eval_cost", new ResetEvalCostEfun());
        efuns.define("set_eval_limit", new SetEvalLimitEfun());
    }
}
