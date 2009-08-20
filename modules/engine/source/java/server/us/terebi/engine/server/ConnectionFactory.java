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

import org.apache.log4j.Logger;

import us.terebi.engine.server.TerebiServer.ConnectionObjectFactory;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;
import us.terebi.lang.lpc.runtime.jvm.value.NilValue;
import us.terebi.net.core.Connection;

import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isObject;
import static us.terebi.lang.lpc.runtime.jvm.support.ValueSupport.intValue;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public class ConnectionFactory implements ConnectionObjectFactory
{
    private final Logger LOG = Logger.getLogger(ConnectionFactory.class);

    private final Callable _callable;

    public ConnectionFactory(Callable callable)
    {
        _callable = callable;
    }

    public ObjectInstance getConnectionObject(Connection connection)
    {
        int port = connection.getInfo().getLocalAddress().getPort();
        LpcValue value = execute(port);
        if (isObject(value))
        {
            return value.asObject();
        }
        if (value.asBoolean())
        {
            LOG.error("Non object " + value + "returned from calling " + _callable);
        }
        return null;
    }

    private LpcValue execute(int port)
    {
        try
        {
            return _callable.execute(intValue(port));
        }
        catch (LpcRuntimeException e)
        {
            LOG.error("Cannot create connection object", e);
            return NilValue.INSTANCE;
        }
    }

}
