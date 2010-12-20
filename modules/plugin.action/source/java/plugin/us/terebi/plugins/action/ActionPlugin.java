/* ------------------------------------------------------------------------
 * Copyright 2010 Tim Vernum
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

package us.terebi.plugins.action;

import java.util.Properties;

import us.terebi.engine.config.Config;
import us.terebi.engine.plugin.AbstractPlugin;
import us.terebi.engine.plugin.Plugin;
import us.terebi.lang.lpc.runtime.jvm.context.Efuns;
import us.terebi.lang.lpc.runtime.jvm.context.SystemContext;
import us.terebi.plugins.action.efun.AddActionEfun;
import us.terebi.plugins.action.efun.CommandEfun;
import us.terebi.plugins.action.efun.CommandsEfun;
import us.terebi.plugins.action.efun.EnableCommandsEfun;
import us.terebi.plugins.action.efun.QueryVerbEfun;
import us.terebi.plugins.action.efun.SetLivingNameEfun;

/**
 * @version $Revision$
 */
public class ActionPlugin extends AbstractPlugin implements Plugin
{
    public void load(Config config, SystemContext context, Properties properties)
    {
        Efuns efuns = context.efuns();
        efuns.define("query_verb", new QueryVerbEfun());
        efuns.define("add_action", new AddActionEfun());
        efuns.define("enable_commands", new EnableCommandsEfun());
        efuns.define("command", new CommandEfun());
        efuns.define("commands", new CommandsEfun());
        efuns.define("set_living_name", new SetLivingNameEfun());
    }
}
