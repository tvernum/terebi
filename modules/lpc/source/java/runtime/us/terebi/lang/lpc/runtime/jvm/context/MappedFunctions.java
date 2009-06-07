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

package us.terebi.lang.lpc.runtime.jvm.context;

import java.util.HashMap;
import java.util.Map;

import us.terebi.lang.lpc.runtime.Callable;

/**
 * 
 */
public class MappedFunctions implements Functions
{
    private final Map<String, Callable> _efuns;

    public MappedFunctions()
    {
        _efuns = new HashMap<String, Callable>();
    }

    public MappedFunctions(Map<String, ? extends Callable> efuns)
    {
        this();
        _efuns.putAll(efuns);
    }

    public void defineEfun(String name, Callable efun)
    {
        _efuns.put(name, efun);
    }

    public Callable efun(String name)
    {
        return _efuns.get(name);
    }

}
