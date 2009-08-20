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

import us.terebi.net.core.Component;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public class NoChildren implements Component
{
    private NoChildren()
    {
        // This exists purely to mark an Abstract component as not having any children
    }

    public void attachedToParent(Component parent)
    {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " should not be used");
    }

    public void begin()
    {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " should not be used");
    }

    public void destroy()
    {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " should not be used");
    }

    public Component getParent()
    {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " should not be used");
    }

    public State getState()
    {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " should not be used");
    }

    public void init()
    {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " should not be used");
    }

    public void resume()
    {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " should not be used");
    }

    public void suspend()
    {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " should not be used");
    }

}
