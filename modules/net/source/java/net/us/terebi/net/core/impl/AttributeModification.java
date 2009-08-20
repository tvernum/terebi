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

import us.terebi.net.core.AttributeChange;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public class AttributeModification implements AttributeChange
{
    private final String _name;
    private final Object _oldValue;
    private final Object _newValue;

    public AttributeModification(String name, Object oldValue, Object newValue)
    {
        _name = name;
        _oldValue = oldValue;
        _newValue = newValue;
    }

    public String name()
    {
        return _name;
    }

    public Object newValue()
    {
        return _newValue;
    }

    public Object oldValue()
    {
        return _oldValue;
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + "{" + _name + ":" + _oldValue + "=>" + _newValue + "}";
    }
}
