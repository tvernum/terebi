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

package us.terebi.net.shell;

import java.io.PrintStream;
import java.nio.ByteBuffer;

import us.terebi.net.core.AttributeChange;
import us.terebi.net.core.Connection;
import us.terebi.net.core.FeatureChange;
import us.terebi.net.core.InputInfo;
import us.terebi.net.core.Shell;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public class PrintShell implements Shell
{
    private final PrintStream _stream;

    public PrintShell(PrintStream stream)
    {
        _stream = stream;
    }

    public void attributeChanged(AttributeChange attribute, Connection connection)
    {
        _stream.println("["
                + connection
                + "] {Attribute: "
                + attribute.name()
                + " changed from "
                + attribute.oldValue()
                + " to "
                + attribute.newValue()
                + "}");
    }

    public void connectionClosed(boolean clientInitiated, Connection connection)
    {
        _stream.println("[" + connection + "] {Connection closed : " + clientInitiated + "}");
    }

    public void connectionIdle(long ms, Connection connection)
    {
        _stream.println("[" + connection + "] {Connection idle : " + ms + "ms}");
    }

    public void featureChanged(FeatureChange feature, Connection connection)
    {
        _stream.println("["
                + connection
                + "] {Feature: "
                + feature.name()
                + " changed from "
                + feature.oldValue()
                + " to "
                + feature.newValue()
                + "}");
    }

    public void inputReceived(byte input, InputInfo info, Connection connection)
    {
        _stream.println("[" + connection + "] * " + Integer.toHexString(input));
    }

    public void inputReceived(ByteBuffer input, InputInfo info, Connection connection)
    {
        _stream.print("[" + connection + "] * ");
        while (input.hasRemaining())
        {
            String s = Integer.toHexString(input.get());
            if (s.length() == 1)
            {
                _stream.print('0');
            }
            _stream.print(s);
            _stream.print(' ');
        }
        _stream.println();
    }

    public void inputReceived(String input, InputInfo info, Connection connection)
    {
        _stream.println("[" + connection + "] $ " + input);
    }

    public void connectionCreated(Connection connection)
    {
        _stream.println("[" + connection + "] {Connection created}");
    }

}
