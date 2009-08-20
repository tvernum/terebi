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

import us.terebi.net.core.AttributeChange;
import us.terebi.net.core.Connection;
import us.terebi.net.core.Shell;
import us.terebi.net.core.impl.AttributeModification;
import us.terebi.net.core.impl.PropertyListener;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public class ShellAttributeNotifier implements PropertyListener<Object>
{
    private final Logger LOG = Logger.getLogger(ShellAttributeNotifier.class);

    private final Connection _connection;

    public ShellAttributeNotifier(Connection connection)
    {
        _connection = connection;
    }

    public void propertyChanged(String property, Object oldValue, Object newValue)
    {
        Shell shell = _connection.boundTo();
        if (shell == null)
        {
            return;
        }
        AttributeChange change = new AttributeModification(property, oldValue, newValue);
        try
        {
            shell.attributeChanged(change, _connection);
        }
        catch (IOException e)
        {
            LOG.warn("Shell " + shell + "(" + _connection + ") threw exception on property change " + change, e);
        }
    }

    public void propertyRemoved(String property, Object oldValue)
    {
        propertyChanged(property, oldValue, null);
    }

}
