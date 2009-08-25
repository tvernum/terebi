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
import java.util.Map;
import java.util.Set;

/**
 * 
 */
public interface MultiMap<K,V>
{
    public Map<K,? extends Collection<V>> toMap();

    public boolean contains(K key);
    public boolean contains(K key, V value);
    
    public Collection<V> get(K key);
    public void add(K key, V value);
    public void addAll(K key, Collection<? extends V> values);
    public void remove(K key, V value);
    public void removeAll(K key, Collection<? extends V> values);
    public void clear(K key);
    public void set(K key, Collection<? extends V> values);
    
    public Set<K> keys();
}
