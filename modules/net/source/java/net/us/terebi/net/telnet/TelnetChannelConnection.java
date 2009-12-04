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

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import us.terebi.net.core.AttributeSet;
import us.terebi.net.core.Connection;
import us.terebi.net.core.ConnectionInfo;
import us.terebi.net.core.FeatureSet;
import us.terebi.net.core.InputInfo;
import us.terebi.net.core.NetException;
import us.terebi.net.core.NetworkFeatures;
import us.terebi.net.core.Shell;
import us.terebi.net.core.impl.Attributes;
import us.terebi.net.core.impl.Features;
import us.terebi.net.core.impl.InputData;
import us.terebi.net.core.impl.PropertyListener;
import us.terebi.net.io.NonBlockingChannelOutputStream;
import us.terebi.net.server.impl.ChannelListener;
import us.terebi.net.server.impl.ConnectionData;
import us.terebi.net.server.impl.ShellAttributeNotifier;
import us.terebi.net.server.impl.ShellFeatureNotifier;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public class TelnetChannelConnection implements Connection
{
    /**
     * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
     */
    final class FeatureListener implements PropertyListener<Boolean>
    {
        private final ShellFeatureNotifier _notifier;

        public FeatureListener()
        {
            this(new ShellFeatureNotifier(TelnetChannelConnection.this));
        }

        public FeatureListener(ShellFeatureNotifier notifier)
        {
            _notifier = notifier;
        }

        public void propertyChanged(String property, Boolean oldValue, Boolean newValue)
        {
            if (NetworkFeatures.CLIENT_ECHO.equals(property) && newValue != null)
            {
                TelnetChannelConnection.this.setClientEcho(newValue);
            }
            _notifier.propertyChanged(property, oldValue, newValue);
        }

        public void propertyRemoved(String property, Boolean oldValue)
        {
            _notifier.propertyRemoved(property, oldValue);
        }
    }

    private final Logger LOG = Logger.getLogger(TelnetChannelConnection.class);

    private final SocketChannel _channel;
    private final ByteBuffer _telnetBuffer;

    private final ConnectionInfo _info;
    private final OutputStream _stream;
    private PrintWriter _writer;
    private final AttributeSet _attributes;
    private final Features _features;

    private Shell _shell;

    private SelectionKey _selectionKey;

    private List<Byte> _will;
    private List<Byte> _wont;

    public TelnetChannelConnection(SocketChannel channel, ChannelListener listener)
    {
        _channel = channel;
        _telnetBuffer = ByteBuffer.allocate(256);

        // For testing support
        if (_channel == null)
        {
            _info = null;
            _stream = null;
        }
        else
        {
            _info = new ConnectionData(channel.socket(), listener, TelnetProtocol.INSTANCE);
            _stream = new NonBlockingChannelOutputStream(_channel);
        }

        _attributes = new Attributes(new ShellAttributeNotifier(this));
        _features = new Features(new FeatureListener());
        _shell = null;

        _will = new ArrayList<Byte>();
        _wont = new ArrayList<Byte>();
    }

    protected void setClientEcho(boolean clientEcho)
    {
        if (clientEcho)
        {
            wont(TelnetCodes.OPT_SERVER_ECHO);
        }
        else
        {
            will(TelnetCodes.OPT_SERVER_ECHO);
        }
    }

    private void will(byte opt)
    {
        _will.add(opt);
        writeBytes(TelnetCodes.IAC, TelnetCodes.WILL, opt);
    }

    private void wont(byte opt)
    {
        _wont.add(opt);
        writeBytes(TelnetCodes.IAC, TelnetCodes.WONT, opt);
    }

    public void bind(Shell shell)
    {
        _shell = shell;
    }

    public Shell boundTo()
    {
        return _shell;
    }

    public void close()
    {
        flush(_writer);
        flush(_stream);
        if (_selectionKey != null)
        {
            _selectionKey.cancel();
        }
        close(_channel);
    }

    private void close(Closeable closeable)
    {
        if (closeable == null)
        {
            return;
        }
        try
        {
            closeable.close();
        }
        catch (IOException e)
        {
            // Ignore
        }
    }

    private void flush(Flushable closeable)
    {
        if (closeable == null)
        {
            return;
        }
        try
        {
            closeable.flush();
        }
        catch (IOException e)
        {
            // Ignore
        }
    }

    public AttributeSet getAttributes()
    {
        return _attributes;
    }

    public FeatureSet getFeatures()
    {
        return _features;
    }

    public ConnectionInfo getInfo()
    {
        return _info;
    }

    public OutputStream getOutputStream()
    {
        return _stream;
    }

    public void setSelectionKey(SelectionKey selectionKey)
    {
        _selectionKey = selectionKey;
    }

    public synchronized void readInput() throws NetException
    {
        if (!_channel.isConnected())
        {
            return;
        }

        InputInfo info = new InputData();

        try
        {
            _channel.read(_telnetBuffer);
            _telnetBuffer.flip();
            ByteBuffer userBuffer = processTelnetBuffer();

            if (_shell == null)
            {
                return;
            }

            userBuffer.flip();
            if (userBuffer.hasRemaining())
            {
                _shell.inputReceived(userBuffer, info, this);
            }
        }
        catch (ClosedChannelException e)
        {
            close();
        }
        catch (IOException e)
        {
            close();
            throw new NetException(e);
        }
    }

    protected ByteBuffer processTelnetBuffer()
    {
        ByteBuffer userBuffer = ByteBuffer.allocate(_telnetBuffer.remaining());
        while (_telnetBuffer.hasRemaining())
        {
            _telnetBuffer.mark();

            byte b = _telnetBuffer.get();
            if (b == TelnetCodes.IAC)
            {
                if (!_telnetBuffer.hasRemaining())
                {
                    _telnetBuffer.reset();
                    break;
                }
                b = _telnetBuffer.get();
                if (b == TelnetCodes.IAC)
                {
                    userBuffer.put(b);
                }
                else
                {
                    if (!handleTelnetCommand(b))
                    {
                        _telnetBuffer.reset();
                        break;
                    }
                }
            }
            else
            {
                userBuffer.put(b);
            }
        }
        _telnetBuffer.compact();
        return userBuffer;
    }

    private boolean handleTelnetCommand(byte b)
    {
        switch (b)
        {
            case TelnetCodes.SB:
                {
                    while (_telnetBuffer.hasRemaining())
                    {
                        if (_telnetBuffer.get() == TelnetCodes.SE)
                        {
                            return true;
                        }
                    }
                    return false;
                }
            case TelnetCodes.NOP:
            case TelnetCodes.DM:
            case TelnetCodes.BRK:
            case TelnetCodes.IP:
            case TelnetCodes.AO:
            case TelnetCodes.AYT:
            case TelnetCodes.EC:
            case TelnetCodes.EL:
            case TelnetCodes.GA:
                return true;

            case TelnetCodes.WILL:
                return rejectOptionIfNotRequested(TelnetCodes.WONT, Collections.<Byte> emptyList());
            case TelnetCodes.DO:
                return rejectOptionIfNotRequested(TelnetCodes.DONT, _will);
            case TelnetCodes.WONT:
            case TelnetCodes.DONT:
                return ignoreOption();
        }
        return true;
    }

    private boolean rejectOptionIfNotRequested(byte type, List<Byte> requested)
    {
        if (_telnetBuffer.hasRemaining())
        {
            byte opt = _telnetBuffer.get();
            if (requested.contains(opt))
            {
                requested.remove(new Byte(opt));
            }
            else
            {
                writeBytes(TelnetCodes.IAC, type, opt);
            }
            return true;
        }
        return false;
    }

    private void writeBytes(byte... bytes)
    {
        ByteBuffer output = ByteBuffer.allocate(bytes.length);
        for (byte b : bytes)
        {
            output.put(b);
        }
        output.flip();
        try
        {
            _channel.write(output);
        }
        catch (IOException e)
        {
            LOG.info("Failed to write telnet codes " + toString(bytes), e);
        }
    }

    private CharSequence toString(byte[] bytes)
    {
        StringBuilder builder = new StringBuilder(bytes.length * 3);
        for (byte b : bytes)
        {
            builder.append(Integer.toHexString(b));
            builder.append(' ');
        }
        return builder;
    }

    private boolean ignoreOption()
    {
        if (_telnetBuffer.hasRemaining())
        {
            _telnetBuffer.get();
            return true;
        }
        return false;
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + "{" + _channel + "}";
    }

    ByteBuffer getInternalBufferForTesting()
    {
        return _telnetBuffer;
    }

    public synchronized PrintWriter getWriter()
    {
        if (_writer == null)
        {
            _writer = new PrintWriter(getOutputStream());
        }
        return _writer;
    }

    public boolean isOpen()
    {
        return _channel.isConnected() && _channel.isOpen();
    }

}
