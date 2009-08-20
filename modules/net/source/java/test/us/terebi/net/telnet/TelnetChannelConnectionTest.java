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

import java.nio.ByteBuffer;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public class TelnetChannelConnectionTest
{

    @Test
    public void testProcessTelnetBufferWithNoIAC()
    {
        TelnetChannelConnection connection = new TelnetChannelConnection(null, null);
        byte[] bytes = new byte[] { 'b', 'u', 'f', 'f', 'e', 'r' };
        ByteBuffer userBuffer = processBytes(connection, bytes);
        assertEquals(bytes.length, userBuffer.remaining());
        for (byte b : bytes)
        {
            assertEquals(b, userBuffer.get());
        }
        assertFalse(userBuffer.hasRemaining());
    }

    @Test
    public void testReusingTelnetBuffer()
    {
        TelnetChannelConnection connection = new TelnetChannelConnection(null, null);
        for (int i = 1; i <= 50; i++)
        {
            byte[] bytes = new byte[] { 'b', 'u', 'f', 'f', 'e', 'r', '#', (byte) ('0' + (i / 10)), (byte) ('0' + (i % 10)) };
            ByteBuffer userBuffer = processBytes(connection, bytes);
            assertEquals(bytes.length, userBuffer.remaining());
            for (byte b : bytes)
            {
                assertEquals(b, userBuffer.get());
            }
            assertFalse(userBuffer.hasRemaining());
        }
    }

    @Test
    public void testProcessTelnetBufferWithEmbeddedNOP()
    {
        TelnetChannelConnection connection = new TelnetChannelConnection(null, null);
        byte[] bytes = new byte[] { 'b', 'u', 'f', 'f', TelnetCodes.IAC, TelnetCodes.NOP, 'e', 'r' };
        ByteBuffer userBuffer = processBytes(connection, bytes);
        assertEquals(bytes.length - 2, userBuffer.remaining());
        for (byte b : bytes)
        {
            if (b >= 0)
            {
                assertEquals(b, userBuffer.get());
            }
        }
        assertFalse(userBuffer.hasRemaining());
    }

    @Test
    public void testProcessTelnetBufferWithTrailingIAC()
    {
        TelnetChannelConnection connection = new TelnetChannelConnection(null, null);
        byte[] bytes = new byte[] { 'b', 'u', 'f', 'f', 'e', 'r', TelnetCodes.IAC };
        ByteBuffer userBuffer = processBytes(connection, bytes);
        assertEquals(bytes.length - 1, userBuffer.remaining());
        for (int i = 0; i < bytes.length - 1; i++)
        {
            assertEquals(bytes[i], userBuffer.get());
        }
        assertFalse(userBuffer.hasRemaining());

        ByteBuffer internalBuffer = connection.getInternalBufferForTesting();
        assertEquals(1, internalBuffer.position());
        assertEquals(TelnetCodes.IAC, internalBuffer.get(0));
    }

    private ByteBuffer processBytes(TelnetChannelConnection connection, byte[] bytes)
    {
        ByteBuffer internalBuffer = connection.getInternalBufferForTesting();
        internalBuffer.put(bytes);
        internalBuffer.flip();
        ByteBuffer userBuffer = connection.processTelnetBuffer();
        userBuffer.flip();
        return userBuffer;
    }

}
