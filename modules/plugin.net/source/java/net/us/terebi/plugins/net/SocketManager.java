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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.context.RuntimeContext;
import us.terebi.lang.lpc.runtime.jvm.context.SystemContext;
import us.terebi.lang.lpc.runtime.jvm.efun.ThisObjectEfun;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcSecurityException;
import us.terebi.lang.lpc.runtime.jvm.support.ValueSupport;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.ArrayBuilder;
import us.terebi.lang.lpc.runtime.jvm.value.StringValue;
import us.terebi.lang.lpc.runtime.util.Apply;
import us.terebi.util.IdGenerator;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public class SocketManager
{
    private static final StringValue SOCKET_OPEN = new StringValue("socket_open");

    public enum SocketMode
    {
        MUD, STREAM, DATAGRAM, STREAM_BINARY, DATAGRAM_BINARY
    }

    public enum SocketState
    {
        CLOSED, CLOSING, UNBOUND, BOUND, LISTEN, DATA_XFER
    }

    public class SocketInfo
    {
        public final long handle;
        public final SocketMode mode;
        public SocketState status;
        public Callable close;
        public Callable read;
        public Callable write;
        public ObjectInstance owner;
        public Socket client;
        public ServerSocket server;

        public SocketInfo(long id, SocketMode socketMode)
        {
            handle = id;
            mode = socketMode;
        }

        public String localAddress()
        {
            if (client != null)
            {
                return client.getLocalAddress().getHostAddress() + " " + client.getLocalPort();
            }
            if (server != null)
            {
                return server.getInetAddress().getHostAddress() + " " + server.getLocalPort();
            }
            return "";
        }

        public String remoteAddress(boolean includePort)
        {
            if (client != null)
            {
                if (includePort)
                {
                    return client.getInetAddress().getHostAddress() + " " + client.getPort();
                }
                else
                {
                    return client.getInetAddress().getHostAddress();
                }
            }
            return "";
        }

        public int port()
        {
            if (client != null)
            {
                return client.getPort();
            }
            if (server != null)
            {
                return server.getLocalPort();
            }
            return 0;
        }
    }

    public static final Apply VALID_SOCKET = new Apply("valid_socket");

    private final SystemContext _context;
    private final IdGenerator _id;
    private final Map<Long, SocketInfo> _sockets;

    public SocketManager(SystemContext context)
    {
        _context = context;
        _id = new IdGenerator();
        _sockets = new HashMap<Long, SocketInfo>();
    }

    public long open(SocketMode mode, Callable read_callback, Callable close_callback, ObjectInstance owner)
    {
        final long id = _id.next();
        SocketInfo info = new SocketInfo(id, mode);
        info.close = close_callback;
        info.read = read_callback;
        info.owner = owner;
        info.status = SocketState.UNBOUND;
        info.client = null;
        info.server = null;
        _sockets.put(id, info);
        security_check(info, SOCKET_OPEN);
        return id;
    }

    private void security_check(SocketInfo info, StringValue efun)
    {
        if (!RuntimeContext.hasActiveContext())
        {
            RuntimeContext.activate(_context);
        }

        ObjectInstance master = _context.objectManager().getMasterObject();
        ObjectInstance caller = ThisObjectEfun.this_object();
        ArrayBuilder args = new ArrayBuilder(Types.MIXED_ARRAY, 4);
        args.add(info.handle);
        args.add(info.owner);
        args.add(info.remoteAddress(false));
        args.add(info.port());
        LpcValue check = VALID_SOCKET.invoke(master, caller.asValue(), efun, args.toArray());
        if (!check.asBoolean())
        {
            throw new LpcSecurityException(VALID_SOCKET + " returned " + check);
        }
    }

    public SocketInfo getSocketInfo(long socket)
    {
        return _sockets.get(Long.valueOf(socket));
    }

    public Collection<SocketInfo> getSockets()
    {
        return _sockets.values();
    }

    public void close(long socket)
    {
        SocketInfo info = _sockets.get(Long.valueOf(socket));
        if (info == null)
        {
            throw new NoSuchElementException("No socket " + socket);
        }
        // TODO Security check
        switch (info.status)
        {
            case BOUND:
            case DATA_XFER:
            case LISTEN:
            case UNBOUND:
                close(info);
        }
    }

    private void close(SocketInfo info)
    {
        if (info.close != null)
        {
            info.close.execute(ValueSupport.intValue(info.handle));
        }
        if (info.client != null)
        {
            close(info.client);
        }
        if (info.server != null)
        {
            close(info.server);
        }
    }

    private void close(Socket socket)
    {
        try
        {
            socket.close();
        }
        catch (IOException e)
        {
            // Ignore
        }
    }

    private void close(ServerSocket socket)
    {
        try
        {
            socket.close();
        }
        catch (IOException e)
        {
            // Ignore
        }
    }

}
