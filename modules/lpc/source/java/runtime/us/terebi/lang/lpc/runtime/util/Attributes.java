/* ------------------------------------------------------------------------
 * $Id$
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

package us.terebi.lang.lpc.runtime.util;

import java.util.HashMap;
import java.util.Map;

import us.terebi.lang.lpc.runtime.AttributeMap;

/**
 * 
 */
public class Attributes extends HashMap<String, Object> implements AttributeMap
{
    public Map<String, Object> asMap()
    {
        return this;
    }

    public Object get(String name)
    {
        return super.get(name);
    }

    public Iterable<String> names()
    {
        return keySet();
    }

    public Object remove(String name)
    {
        return super.remove(name);
    }

    public void set(String name, Object value)
    {
        put(name, value);
    }

    public Object put(String key, Object value)
    {
        if (value == null)
        {
            return remove(key);
        }
        else
        {
            return super.put(key, value);
        }
    }

    public void putAll(Map< ? extends String, ? extends Object> m)
    {
        super.putAll(m);
    }

}
