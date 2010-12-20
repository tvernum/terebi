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

package us.terebi.net.telnet;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.log4j.Logger;

import us.terebi.net.core.Connection;
import us.terebi.net.core.NetException;
import us.terebi.net.core.Shell;
import us.terebi.net.core.impl.AbstractComponent;
import us.terebi.net.core.impl.NoChildren;
import us.terebi.net.server.impl.ChannelConnectionHandler;
import us.terebi.net.server.impl.ChannelListener;
import us.terebi.net.thread.LoggingExecutor;
import us.terebi.net.thread.SimpleThreadFactory;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public class TelnetChannelConnectionHandler extends AbstractComponent<NoChildren> implements ChannelConnectionHandler
{
    final Logger LOG = Logger.getLogger(TelnetChannelConnectionHandler.class);

    private final Selector _selector;

    private ThreadFactory _threadFactory;
    private Executor _executor;
    private int _maxErrors;
    private Shell _shell;
    private final Set<Connection> _connections;

    public TelnetChannelConnectionHandler() throws IOException
    {
        _selector = Selector.open();
        _threadFactory = null;
        _executor = null;
        _maxErrors = 0;
        _connections = new HashSet<Connection>();
    }

    public void setThreadFactory(ThreadFactory threadFactory)
    {
        _threadFactory = threadFactory;
    }

    public void setExecutor(Executor executor)
    {
        _executor = executor;
    }

    public void setMaxErrors(int maxErrors)
    {
        _maxErrors = maxErrors;
    }

    public void setShell(Shell shell)
    {
        _shell = shell;
    }

    @Override
    protected void preBegin()
    {
        if (_executor == null)
        {
            if (_threadFactory == null)
            {
                _executor = Executors.newFixedThreadPool(10);
            }
            else
            {
                _executor = Executors.newFixedThreadPool(10, _threadFactory);
            }
            _executor = new LoggingExecutor(_executor);
        }
        if (_threadFactory == null)
        {
            SimpleThreadFactory simpleThreadFactory = new SimpleThreadFactory();
            simpleThreadFactory.setDaemon(true);
            simpleThreadFactory.setPrefix(getClass().getSimpleName() + "[" + System.identityHashCode(this) + "]");
            setThreadFactory(simpleThreadFactory);
        }
        if (_maxErrors == 0)
        {
            setMaxErrors(75);
        }
    }

    @Override
    protected void postBegin()
    {
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                try
                {
                    select();
                }
                catch (InterruptedException e)
                {
                    LOG.fatal("Select thread interrupted", e);
                }
            }
        };
        _threadFactory.newThread(runnable).start();
    }

    protected void select() throws InterruptedException
    {
        int errors = 0;
        for (;;)
        {
            try
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Selecting " + _selector + " : " + _selector.keys());
                }
                _selector.select(12500);
                readConnections(_selector.selectedKeys());
            }
            catch (IOException e)
            {
                LOG.error("Failed to select readable sockets", e);
                errors++;
                if (errors >= _maxErrors)
                {
                    LOG.fatal("Select errors have reached " + errors + " - terminating handler " + this);
                    return;
                }
                Thread.sleep(25);
            }
        }
    }

    private final class ConnectionReader implements Runnable
    {
        private final SelectionKey _key;
        private final TelnetChannelConnection _connection;

        ConnectionReader(SelectionKey key, TelnetChannelConnection connection)
        {
            _key = key;
            _connection = connection;
        }

        public void run()
        {
            if (!_connection.isOpen())
            {
                _key.interestOps(0);
                return;
            }
            _key.interestOps(SelectionKey.OP_READ);
            readConnection(_connection);
        }

        @Override
        public String toString()
        {
            return TelnetChannelConnectionHandler.class.getSimpleName() + "$" + getClass().getSimpleName() + "{" + _key + "}";
        }
    }

    private void readConnections(Set<SelectionKey> keys)
    {
        LOG.debug("Processing keys " + keys);
        Iterator<SelectionKey> iterator = keys.iterator();
        while (iterator.hasNext())
        {
            final SelectionKey key = iterator.next();
            // Not interested in read signals on this key until we've finished reading the connection
            key.interestOps(0);
            iterator.remove();

            Object attachment = key.attachment();
            assert attachment instanceof TelnetChannelConnection;
            final TelnetChannelConnection connection = (TelnetChannelConnection) attachment;
            if (connection.isOpen())
            {
                Runnable runnable = new ConnectionReader(key, connection);
                _executor.execute(runnable);
            }
            else
            {
                key.cancel();
            }
        }
    }

    void readConnection(TelnetChannelConnection connection)
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Processing " + connection);
        }
        try
        {
            connection.readInput();
        }
        catch (NetException e)
        {
            LOG.info("Net exception from connection " + connection, e);
        }
        _selector.wakeup();
    }

    public void newConnection(SocketChannel socket, ChannelListener listener) throws NetException
    {
        TelnetChannelConnection connection = new TelnetChannelConnection(socket, listener);
        if (_shell != null)
        {
            connection.bind(_shell);
        }

        _connections.add(connection);

        LOG.info("New connection: " + connection);
        try
        {
            _shell.connectionCreated(connection);
            socket.configureBlocking(false);
            SelectionKey key = socket.register(_selector, SelectionKey.OP_READ, connection);
            connection.setSelectionKey(key);
            _selector.wakeup();
        }
        catch (ClosedChannelException e)
        {
            throw new NetException("Socket was closed before it could be registered", e);
        }
        catch (IOException e)
        {
            throw new NetException("Unexpected I/O Exception on " + socket, e);
        }
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + "{" + _threadFactory + ";" + _shell + "}";
    }

    public Set<Connection> connections()
    {
        return Collections.unmodifiableSet(_connections);
    }
}
