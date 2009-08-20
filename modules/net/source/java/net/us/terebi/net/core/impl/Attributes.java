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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import us.terebi.net.core.AttributeSet;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public class Attributes implements AttributeSet
{
    private final Map<String, Object> _attributes;
    private final PropertyListener<Object> _listener;

    public Attributes(PropertyListener<Object> listener)
    {
        _listener = listener;
        _attributes = new HashMap<String, Object>();
    }

    public Object getAttribute(String attribute)
    {
        return _attributes.get(attribute);
    }

    public Set<String> getAttributeNames()
    {
        return Collections.unmodifiableSet(_attributes.keySet());
    }

    public boolean hasAttribute(String attribute)
    {
        return _attributes.containsKey(attribute);
    }

    public void removeAttribute(String attribute)
    {
        Object old = _attributes.remove(attribute);
        _listener.propertyRemoved(attribute, old);
    }

    public void setAttribute(String attribute, Object value)
    {
        Object old = _attributes.put(attribute, value);
        _listener.propertyChanged(attribute, old, value);
    }

}
