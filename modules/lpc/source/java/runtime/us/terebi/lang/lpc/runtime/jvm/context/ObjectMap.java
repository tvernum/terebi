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

package us.terebi.lang.lpc.runtime.jvm.context;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 */
public class ObjectMap
{
    @SuppressWarnings("unchecked")
    private Map<Class, Object> _map;

    @SuppressWarnings("unchecked")
    public ObjectMap()
    {
        _map = new HashMap<Class, Object>();
    }

    public <T> T get(Class< ? extends T> type)
    {
        if (_map.containsKey(type))
        {
            return type.cast(_map.get(type));
        }
        return null;
    }

    public <T> void put(Class< ? extends T> type, T value)
    {
        _map.put(type, value);
    }

    public void putAll(ObjectMap map)
    {
        _map.putAll(map._map);
    }

    public String toString()
    {
        return getClass().getSimpleName() + ":" + System.identityHashCode(this) + ":" + _map;
    }
}
