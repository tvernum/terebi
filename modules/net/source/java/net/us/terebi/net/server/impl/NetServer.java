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

import java.util.Set;

import us.terebi.net.core.Component;
import us.terebi.net.core.impl.AbstractComponent;
import us.terebi.net.server.ConnectionListener;
import us.terebi.net.server.Server;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public class NetServer extends AbstractComponent<ConnectionListener> implements Server
{
    public void addListener(ConnectionListener listener)
    {
        super.addChild(listener);
    }
    
    public Set<ConnectionListener> getListeners()
    {
        return super.getChildren();
    }

    public void attachedToParent(Component parent)
    {
        // Ignore
    }
}
