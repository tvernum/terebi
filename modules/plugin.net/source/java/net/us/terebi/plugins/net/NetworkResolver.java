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
import java.net.InetAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import us.terebi.util.IdGenerator;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public class NetworkResolver
{
    private static final NetworkResolver INSTANCE = new NetworkResolver();
    private final Executor _executor;
    private final IdGenerator _id;

    final Logger LOG = Logger.getLogger(NetworkResolver.class);

    public static NetworkResolver getResolver()
    {
        return INSTANCE;
    }

    public interface Callback
    {
        public void addressResolved(String ip, String hostName, Token token);
        public void addressNotResolved(String address, Token token);
    }

    public class Token
    {
        public final int id;
        public final Callback callback;

        /*package*/Token(int tokenId, Callback cb)
        {
            this.id = tokenId;
            this.callback = cb;
        }
    }

    public NetworkResolver()
    {
        this(Executors.newCachedThreadPool());
    }

    public NetworkResolver(Executor executor)
    {
        _executor = executor;
        _id = new IdGenerator();
    }

    public Token resolve(final String ipOrHost, final Callback callback)
    {
        final int id = _id.next();
        final Token token = new Token(id, callback);
        Runnable command = new Runnable()
        {
            public void run()
            {
                try
                {
                    LOG.info("Attempting to resolve address " + ipOrHost);
                    InetAddress address = InetAddress.getByName(ipOrHost);
                    String ip = address.getHostAddress();
                    String name = address.getHostName();
                    LOG.info("Resolved " + ipOrHost + " as " + ip + " / " + name);
                    callback.addressResolved(ip, name, token);
                }
                catch (IOException e)
                {
                    LOG.info("Not resolved " + ipOrHost);
                    callback.addressNotResolved(ipOrHost, token);
                }
            }
        };
        _executor.execute(command);
        return token;
    }
}
