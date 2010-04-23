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

package us.terebi.engine.server;

import java.io.IOException;

import us.terebi.engine.config.Config;
import us.terebi.engine.config.ConfigException;
import us.terebi.engine.config.ConfigNames;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.context.SystemContext;
import us.terebi.net.core.Connection;
import us.terebi.net.core.NetException;
import us.terebi.net.server.impl.ChannelListener;
import us.terebi.net.server.impl.NetServer;
import us.terebi.net.telnet.TelnetChannelConnectionHandler;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public class TerebiServer
{
    private final NetServer _net;

    public interface ConnectionObjectFactory
    {
        public ObjectInstance getConnectionObject(Connection connection);
    }

    public TerebiServer(ConnectionObjectFactory factory, Config config, SystemContext context) throws IOException
    {
        _net = new NetServer();
        ObjectShell shell = new ObjectShell(factory, context);
        long[] ports = config.getLongs(ConfigNames.TELNET_PORT);
        if (ports.length == 0)
        {
            throw new ConfigException("No telnet ports configured under key '" + ConfigNames.TELNET_PORT + "'");
        }
        for (long port : ports)
        {
            if (port > Short.MAX_VALUE)
            {
                throw new ConfigException("Port number '" + port + "' is too large");
            }

            TelnetChannelConnectionHandler handler = new TelnetChannelConnectionHandler();
            handler.setShell(shell);

            ChannelListener listener = new ChannelListener();
            listener.setAddress((short) port);
            listener.setHandler(handler);

            _net.addListener(listener);
        }
    }

    public void start() throws NetException
    {
        _net.begin();
    }

    public void stop() throws NetException
    {
        _net.destroy();
    }
    
    public NetServer getNetServer()
    {
        return _net;
    }
}
