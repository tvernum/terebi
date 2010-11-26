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

package us.terebi.plugins.interactive;

import java.util.Properties;

import us.terebi.engine.config.Config;
import us.terebi.engine.plugin.AbstractPlugin;
import us.terebi.engine.plugin.Plugin;
import us.terebi.lang.lpc.runtime.jvm.context.Efuns;
import us.terebi.lang.lpc.runtime.jvm.context.SystemContext;
import us.terebi.plugins.interactive.efun.ExecEfun;
import us.terebi.plugins.interactive.efun.InputToEfun;
import us.terebi.plugins.interactive.efun.InteractiveEfun;
import us.terebi.plugins.interactive.efun.ReceiveEfun;
import us.terebi.plugins.interactive.efun.TellObjectEfun;
import us.terebi.plugins.interactive.efun.ThisPlayerEfun;
import us.terebi.plugins.interactive.efun.UsersEfun;
import us.terebi.plugins.interactive.efun.WriteEfun;

/**
 * @version $Revision$
 */
public class InteractivePlugin extends AbstractPlugin implements Plugin
{
    public void load(Config config, SystemContext context, Properties properties)
    {
        Efuns efuns = context.efuns();
        efuns.define("interactive", new InteractiveEfun());
        efuns.define("exec", new ExecEfun());
        efuns.define("users", new UsersEfun());
        efuns.define("this_player", new ThisPlayerEfun());
        efuns.define("write", new WriteEfun());
        efuns.define("input_to", new InputToEfun());
        efuns.define("tell_object", new TellObjectEfun(config.getBoolean("plugin.interactive.catch_tell", false)));
        efuns.define("receive", new ReceiveEfun());
    }
}
