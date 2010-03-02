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

package us.terebi.lang.lpc.compiler.java.context;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import us.terebi.lang.lpc.runtime.FunctionSignature;

/**
 * 
 */
public class FunctionMap<T> implements Map<String, FunctionSignature>
{
    private final Map<String, FunctionSignature> _map;
    private final Map<String, T> _secondary;

    public FunctionMap()
    {
        _map = new HashMap<String, FunctionSignature>();
        _secondary = new HashMap<String, T>();
    }

    public FunctionMap(Map<String, ? extends FunctionSignature> signatures)
    {
        this();
        putAll(signatures);
    }

    public void clear()
    {
        _map.clear();
    }

    public boolean containsKey(Object key)
    {
        return _map.containsKey(key);
    }

    public boolean containsValue(Object value)
    {
        return _map.containsValue(value);
    }

    public Set<Entry<String, FunctionSignature>> entrySet()
    {
        return _map.entrySet();
    }

    public boolean equals(Object o)
    {
        return _map.equals(o);
    }

    public FunctionSignature get(Object key)
    {
        return _map.get(key);
    }

    public int hashCode()
    {
        return _map.hashCode();
    }

    public boolean isEmpty()
    {
        return _map.isEmpty();
    }

    public Set<String> keySet()
    {
        return _map.keySet();
    }

    public FunctionSignature put(String key, FunctionSignature value)
    {
        return _map.put(key, value);
    }

    public void putAll(FunctionMap<T> t)
    {
        _map.putAll(t._map);
        _secondary.putAll(t._secondary);
    }

    public void putAll(Map< ? extends String, ? extends FunctionSignature> t)
    {
        _map.putAll(t);
    }

    public FunctionSignature remove(Object key)
    {
        return _map.remove(key);
    }

    public int size()
    {
        return _map.size();
    }

    public Collection<FunctionSignature> values()
    {
        return _map.values();
    }

    public void put(String name, FunctionSignature signature, T secondary)
    {
        put(name, signature);
        _secondary.put(name, secondary);
    }

    public T getSecondary(String name)
    {
        return _secondary.get(name);
    }

}
