/* ------------------------------------------------------------------------
 * Copyright 2010 Tim Vernum
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

package us.terebi.lang.lpc.runtime.util.cache;

import java.util.Map;
import java.util.WeakHashMap;

import us.terebi.lang.lpc.runtime.LpcValue;

/**
 * 
 */
public abstract class ValueCache<T>
{
    private Map<T, LpcValue> _cache;

    public ValueCache()
    {
        _cache = new WeakHashMap<T, LpcValue>();
    }

    public LpcValue get(T val)
    {
        LpcValue value = _cache.get(val);
        if (value == null)
        {
            value = this.create(val);
            if (shouldCache(val, value))
            {
                _cache.put(val, value);
            }
        }
        return value;
    }

    @SuppressWarnings("unused")
    protected boolean shouldCache(T val, LpcValue value)
    {
        return true;
    }

    protected abstract LpcValue create(T val);
}
