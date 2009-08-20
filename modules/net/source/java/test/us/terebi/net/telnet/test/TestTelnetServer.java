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

package us.terebi.net.telnet.test;

import java.io.IOException;

import us.terebi.net.core.NetException;
import us.terebi.net.server.impl.ChannelListener;
import us.terebi.net.server.impl.NetServer;
import us.terebi.net.shell.PrintShell;
import us.terebi.net.telnet.TelnetChannelConnectionHandler;

/**
 * @version $Revision$
 */
public class TestTelnetServer
{
    public static void main(String[] args) throws NetException, IOException, InterruptedException
    {
        TelnetChannelConnectionHandler handler = new TelnetChannelConnectionHandler();
        handler.setShell(new PrintShell(System.err));

        ChannelListener listener = new ChannelListener();
        listener.setAddress(4321);
        listener.setBacklog(25);
        listener.setHandler(handler);

        NetServer server = new NetServer();
        server.addListener(listener);
        
        server.begin();
        synchronized (server)
        {
            server.wait();
        }
    }

}
