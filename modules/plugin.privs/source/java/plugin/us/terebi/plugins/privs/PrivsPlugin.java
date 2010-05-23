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

package us.terebi.plugins.privs;

import java.util.Properties;

import us.terebi.engine.config.Config;
import us.terebi.engine.plugin.AbstractPlugin;
import us.terebi.engine.plugin.Plugin;
import us.terebi.lang.lpc.runtime.jvm.context.Efuns;
import us.terebi.lang.lpc.runtime.jvm.context.SystemContext;
import us.terebi.plugins.privs.efun.QueryPrivsEfun;

/**
 * @version $Revision$
 */
public class PrivsPlugin extends AbstractPlugin implements Plugin
{
    public void load(Config config, SystemContext context, Properties properties)
    {
        Efuns efuns = context.efuns();
        efuns.define("query_privs", new QueryPrivsEfun());
    }

    public void init(SystemContext context)
    {
        context.objectManager().addListener(new PrivsListener());
    }
}

