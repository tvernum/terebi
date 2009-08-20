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

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public interface Component
{
    public enum State
    {
        NEW, INITIALISING, INITIALISED, STARTING, RUNNING, SUSPENDING, SUSPENDED, RESUMING, STOPPING, DESTROYED;
    }
    
    public void attachedToParent(Component parent);
    public Component getParent();
    
    public void init() throws NetException;
    public void begin() throws NetException;
    public void suspend() throws NetException;
    public void resume() throws NetException;
    public void destroy() throws NetException;
    
    public State getState();
}
