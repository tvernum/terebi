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

import us.terebi.net.core.FeatureChange;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public class FeatureModification implements FeatureChange
{
    private final String _name;
    private final Boolean _oldValue;
    private final Boolean _newValue;

    public FeatureModification(String name, Boolean oldValue, Boolean newValue)
    {
        _name = name;
        _oldValue = oldValue;
        _newValue = newValue;
    }

    public boolean disabled()
    {
        return Boolean.TRUE.equals(_oldValue) && !Boolean.TRUE.equals(_newValue);
    }

    public boolean enabled()
    {
        return !Boolean.TRUE.equals(_oldValue) && Boolean.TRUE.equals(_newValue);
    }

    public String name()
    {
        return _name;
    }

    public Boolean newValue()
    {
        return _newValue;
    }

    public Boolean oldValue()
    {
        return _oldValue;
    }
    
    @Override
    public String toString()
    {
        return getClass().getSimpleName() + "{" + _name + ":" + _oldValue + "=>" + _newValue + "}";
    }
}
