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

package us.terebi.net.core;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public interface Shell
{
    public void inputReceived(byte input, InputInfo info, Connection connection) throws IOException;
    public void inputReceived(ByteBuffer input, InputInfo info, Connection connection) throws IOException;
    public void inputReceived(String input, InputInfo info, Connection connection) throws IOException;
    
    public void featureChanged(FeatureChange feature, Connection connection) throws IOException;
    public void attributeChanged(AttributeChange attribute, Connection connection) throws IOException;

    public void connectionCreated(Connection connection) throws IOException;
    public void connectionIdle(long ms, Connection connection) throws IOException;
    public void connectionClosed(boolean clientInitiated, Connection connection) throws IOException;
}
