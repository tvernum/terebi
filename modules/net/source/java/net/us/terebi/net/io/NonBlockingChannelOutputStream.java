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

package us.terebi.net.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public class NonBlockingChannelOutputStream extends OutputStream
{
    private final WritableByteChannel _channel;
    private ByteBuffer _buffer;

    public NonBlockingChannelOutputStream(WritableByteChannel channel)
    {
        _channel = channel;
    }

    @Override
    public void write(int b) throws IOException
    {
        ByteBuffer buffer = getBuffer(1);
        buffer.put((byte) b);
        flipAndWrite(buffer);
    }

    @Override
    public void write(byte[] b) throws IOException
    {
        ByteBuffer buffer = getBuffer(b.length);
        buffer.put(b);
        flipAndWrite(buffer);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException
    {
        ByteBuffer buffer = getBuffer(len);
        buffer.put(b, off, len);
        flipAndWrite(buffer);
    }

    private void flipAndWrite(ByteBuffer buffer) throws IOException
    {
        buffer.flip();
        _channel.write(buffer);
    }

    private ByteBuffer getBuffer(int size)
    {
        // Align to 64 bytes
        size = 64 * ((size + 63) / 64);

        if (size > 512)
        {
            return ByteBuffer.allocate(size);
        }
        if (_buffer == null || size > _buffer.capacity())
        {
            _buffer = ByteBuffer.allocate(size);
        }
        else
        {
            _buffer.clear();
        }
        return _buffer;
    }
}
