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

package us.terebi.net.server.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.concurrent.ThreadFactory;

import org.apache.log4j.Logger;

import us.terebi.net.core.Component;
import us.terebi.net.core.Connection;
import us.terebi.net.core.NetException;
import us.terebi.net.core.impl.AbstractComponent;
import us.terebi.net.server.ConnectionListener;
import us.terebi.net.server.Server;
import us.terebi.net.thread.SimpleThreadFactory;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public class ChannelListener extends AbstractComponent<ChannelConnectionHandler> implements ConnectionListener
{
    private final Logger LOG = Logger.getLogger(ChannelListener.class);

    /* Connections to other objects */
    @SuppressWarnings("unused")
    private Server _server;
    private ThreadFactory _threadFactory;
    private ChannelConnectionHandler _handler;

    /* Configurable settings */
    private InetSocketAddress _address;
    private int _backlog;
    private int _maxErrors;

    /* Internal State */
    private ServerSocketChannel _channel;

    public ChannelListener()
    {
        _server = null;
        _threadFactory = null;
        _address = null;
        _backlog = -1;
    }

    public void attachedToParent(Component parent)
    {
        super.attachedToParent(parent);
        attachedToServer(findAncestor(Server.class));
    }

    public void attachedToServer(Server server)
    {
        _server = server;
    }

    public void setAddress(InetSocketAddress address)
    {
        _address = address;
    }

    public void setAddress(String hostname, int port)
    {
        setAddress(new InetSocketAddress(hostname, port));
    }

    public void setAddress(int port)
    {
        setAddress(new InetSocketAddress(port));
    }

    public InetSocketAddress getAddress()
    {
        return _address;
    }

    public void setBacklog(int backlog)
    {
        _backlog = backlog;
    }

    public void setThreadFactory(ThreadFactory threadFactory)
    {
        _threadFactory = threadFactory;
    }

    public void setHandler(ChannelConnectionHandler handler)
    {
        removeChild(handler);
        _handler = handler;
        addChild(handler);
    }
    
    public ChannelConnectionHandler getHandler()
    {
        return _handler;
    }

    public void setMaxErrors(int maxErrors)
    {
        _maxErrors = maxErrors;
    }

    @Override
    protected void preBegin()
    {
        if (_address == null)
        {
            setAddress(7777);
        }
        if (_backlog == -1)
        {
            setBacklog(10);
        }
        if (_threadFactory == null)
        {
            SimpleThreadFactory simpleThreadFactory = new SimpleThreadFactory();
            simpleThreadFactory.setPrefix("Listener(" + _address + ")");
            simpleThreadFactory.setDaemon(true);
            setThreadFactory(simpleThreadFactory);
        }
        if (_maxErrors == 0)
        {
            setMaxErrors(25);
        }
    }

    @Override
    protected void postBegin() throws NetException
    {
        try
        {
            listen();
        }
        catch (IOException e)
        {
            throw new NetException(e);
        }
    }

    public void listen() throws IOException
    {
        LOG.info("Listening on " + _address);
        _channel = ServerSocketChannel.open();
        _channel.socket().bind(_address, _backlog);
        _channel.configureBlocking(true);

        Runnable runnable = new Runnable()
        {
            public void run()
            {
                acceptConnections();
            }
        };
        Thread thread = _threadFactory.newThread(runnable);
        thread.start();
    }

    protected void acceptConnections()
    {
        int errorCount = 0;
        while (true)
        {
            try
            {
                SocketChannel accept = _channel.accept();
                errorCount = 0;
                _handler.newConnection(accept, this);
            }
            catch (ClosedChannelException e)
            {
                LOG.error("Socket " + _address + " is closed", e);
                return;
            }
            catch (IOException e)
            {
                LOG.error("Socket " + _address + " has IO error", e);
                errorCount++;
                if (errorCount >= _maxErrors)
                {
                    LOG.fatal("Socket errors have reached " + errorCount + " - terminating listener " + this);
                    return;
                }
            }
            catch (NetException e)
            {
                LOG.info("Exception when establishing new connection", e);
                continue;
            }
        }
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + "[" + _address + "]";
    }
    
    public Set<Connection> getConnections()
    {
        return _handler.connections();
    }

}
