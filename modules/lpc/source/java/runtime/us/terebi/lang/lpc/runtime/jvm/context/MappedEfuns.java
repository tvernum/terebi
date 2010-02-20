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
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.FunctionSignature;

/**
 * 
 */
public class MappedEfuns implements Efuns
{
    private final Logger LOG = Logger.getLogger(MappedEfuns.class);

    private static class Efun
    {
        public final FunctionSignature signature;
        public final Callable implementation;

        public Efun(FunctionSignature sig, Callable impl)
        {
            this.signature = sig;
            this.implementation = impl;
        }
    }

    private final Map<String, Efun> _efuns;

    public MappedEfuns()
    {
        _efuns = new HashMap<String, Efun>();
    }

    public FunctionSignature getSignature(String name)
    {
        Efun efun = _efuns.get(name);
        if (efun == null)
        {
            return null;
        }
        return efun.signature;
    }

    public Callable getImplementation(String name)
    {
        Efun efun = _efuns.get(name);
        if (efun == null)
        {
            return null;
        }
        return efun.implementation;
    }

    public void define(String efun, FunctionSignature signature, Callable implementation)
    {
        LOG.debug("Defining efun " + efun + " - " + signature + " : " + implementation.getClass().getSimpleName());
        _efuns.put(efun, new Efun(signature, implementation));
    }

    public <E extends FunctionSignature & Callable> void define(String name, E efun)
    {
        define(name, efun, efun);
    }

    public void delete(String efun)
    {
        _efuns.remove(efun);
    }

    public Map<String, ? extends Callable> getImplementations()
    {
        Map<String, Callable> map = new HashMap<String, Callable>(_efuns.size());
        for (Entry<String, Efun> entry : _efuns.entrySet())
        {
            map.put(entry.getKey(), entry.getValue().implementation);
        }
        return map;
    }

    public Map<String, ? extends FunctionSignature> getSignatures()
    {
        Map<String, FunctionSignature> map = new HashMap<String, FunctionSignature>(_efuns.size());
        for (Entry<String, Efun> entry : _efuns.entrySet())
        {
            map.put(entry.getKey(), entry.getValue().signature);
        }
        return map;
    }

}
