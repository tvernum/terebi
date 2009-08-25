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

package us.terebi.util.collection;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 
 */
public class MultiHashMap<K, V> implements MultiMap<K, V>
{
    private Map<K, Set<V>> _map;

    public MultiHashMap()
    {
        _map = new HashMap<K, Set<V>>();
    }

    public Map<K, ? extends Collection<V>> toMap()
    {
        return _map;
    }

    public void add(K key, V value)
    {
        getCollection(key).add(value);
    }

    private Set<V> getCollection(K key)
    {
        if (_map.containsKey(key))
        {
            return _map.get(key);
        }
        Set<V> set = new HashSet<V>();
        _map.put(key, set);
        return set;
    }

    public void addAll(K key, Collection< ? extends V> values)
    {
        getCollection(key).addAll(values);
    }

    public void clear(K key)
    {
        if (_map.containsKey(key))
        {
            _map.get(key).clear();
        }
    }

    public Collection<V> get(K key)
    {
        if (_map.containsKey(key))
        {
            return _map.get(key);
        }
        return null;
    }

    public Set<K> keys()
    {
        return _map.keySet();
    }

    public void remove(K key, V value)
    {
        if (_map.containsKey(key))
        {
            _map.get(key).remove(value);
        }
    }

    public void removeAll(K key, Collection< ? extends V> values)
    {
        if (_map.containsKey(key))
        {
            _map.get(key).removeAll(values);
        }
    }

    public void set(K key, Collection< ? extends V> values)
    {
        _map.put(key, new HashSet<V>(values));
    }

    public boolean contains(K key)
    {
        return _map.containsKey(key);
    }

    public boolean contains(K key, V value)
    {
        return _map.containsKey(key) && _map.get(key).contains(value);
    }

}
