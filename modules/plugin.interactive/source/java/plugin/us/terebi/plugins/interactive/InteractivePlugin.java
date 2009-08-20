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

import us.terebi.engine.config.Config;
import us.terebi.engine.plugin.Plugin;
import us.terebi.lang.lpc.runtime.jvm.context.Efuns;
import us.terebi.lang.lpc.runtime.jvm.context.SystemContext;
import us.terebi.plugins.interactive.efun.InputToEfun;
import us.terebi.plugins.interactive.efun.InteractiveEfun;
import us.terebi.plugins.interactive.efun.ThisPlayerEfun;
import us.terebi.plugins.interactive.efun.WriteEfun;

/**
 * @version $Revision$
 */
public class InteractivePlugin implements Plugin
{
    public void init(SystemContext context)
    {
        Efuns efuns = context.efuns();
        efuns.define("interactive", new InteractiveEfun());
        efuns.define("this_player", new ThisPlayerEfun());
        efuns.define("write", new WriteEfun());
        efuns.define("input_to", new InputToEfun());
    }

    public void load(Config config, SystemContext context)
    {
        // No-op
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
