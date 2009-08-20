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

import org.apache.log4j.Logger;

import us.terebi.net.core.Connection;
import us.terebi.net.core.Shell;
import us.terebi.net.core.impl.FeatureModification;
import us.terebi.net.core.impl.PropertyListener;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public class ShellFeatureNotifier implements PropertyListener<Boolean>
{
    private final Logger LOG = Logger.getLogger(ShellFeatureNotifier.class);

    private final Connection _connection;

    public ShellFeatureNotifier(Connection connection)
    {
        _connection = connection;
    }

    public void propertyChanged(String property, Boolean oldValue, Boolean newValue)
    {
        Shell shell = _connection.boundTo();
        if (shell == null)
        {
            return;
        }
        FeatureModification change = new FeatureModification(property, oldValue, newValue);
        try
        {
            shell.featureChanged(change, _connection);
        }
        catch (IOException e)
        {
            LOG.warn("Shell " + shell + "(" + _connection + ") threw exception on property change " + change, e);
        }
    }

    public void propertyRemoved(String property, Boolean oldValue)
    {
        propertyChanged(property, oldValue, null);
    }

}
