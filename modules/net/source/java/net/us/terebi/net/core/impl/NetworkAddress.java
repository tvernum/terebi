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

package us.terebi.net.core.impl;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import us.terebi.net.core.ConnectionAddress;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public class NetworkAddress implements ConnectionAddress
{
    private final String _address;
    private final int _port;
    private String _host;

    public NetworkAddress(String address, String host, int port)
    {
        _address = address;
        _host = host;
        _port = port;
    }

    public NetworkAddress(InetSocketAddress address)
    {
        this(address.getAddress().getHostAddress(), address.getHostName(), address.getPort());
    }

    public NetworkAddress(InetAddress address)
    {
        this(address.getHostAddress(), address.getHostName(), -1);
    }

    public void setHost(String host)
    {
        _host = host;
    }

    public String getAddress()
    {
        return _address;
    }

    public String getHost()
    {
        return _host;
    }

    public int getPort()
    {
        return _port;
    }

}
