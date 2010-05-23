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

package us.terebi.plugins.net;

import java.util.Properties;

import us.terebi.engine.config.Config;
import us.terebi.engine.plugin.AbstractPlugin;
import us.terebi.engine.plugin.Plugin;
import us.terebi.lang.lpc.runtime.jvm.context.Efuns;
import us.terebi.lang.lpc.runtime.jvm.context.SystemContext;
import us.terebi.plugins.net.efun.ResolveEfun;
import us.terebi.plugins.net.efun.SocketCloseEfun;
import us.terebi.plugins.net.efun.SocketCreateEfun;
import us.terebi.plugins.net.efun.SocketStatusEfun;

/**
 */
public class NetPlugin extends AbstractPlugin implements Plugin
{
    public void load(Config config, SystemContext context, Properties properties)
    {
        //        CompileOptions compileOptions = context.attachments().get(CompileOptions.class);
        //        compileOptions.defineTrue("__PACKAGE_SOCKETS__"); // Compat with (Mud|Fluff)OS

        Efuns efuns = context.efuns();
        efuns.define("resolve", new ResolveEfun());
        efuns.define("socket_create", new SocketCreateEfun());
        efuns.define("socket_status", new SocketStatusEfun());
        efuns.define("socket_close", new SocketCloseEfun());

        context.attachments().put(SocketManager.class, new SocketManager(context));
    }
}
