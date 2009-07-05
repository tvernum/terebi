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
import java.util.Iterator;
import java.util.Map;

import us.terebi.lang.lpc.compiler.CompilerObjectManager;
import us.terebi.lang.lpc.compiler.ObjectCompiler;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.util.Predicate;
import us.terebi.util.collection.PredicateIterator;

/**
 * 
 */
public class LpcCompilerObjectManager implements CompilerObjectManager
{
    private final Map<String, CompiledObjectDefinition> _definitions;
    private final Map<ObjectId, CompiledObjectInstance> _objects;
    private ObjectCompiler _compiler;
    private long _id;

    public LpcCompilerObjectManager()
    {
        _definitions = new HashMap<String, CompiledObjectDefinition>();
        _objects = new HashMap<ObjectId, CompiledObjectInstance>();
        _compiler = null;
        _id = 0;
    }

    public void setCompiler(ObjectCompiler compiler)
    {
        _compiler = compiler;
    }

    public CompiledObjectDefinition findObject(String name)
    {
        CompiledObjectDefinition definition = _definitions.get(normalise(name));
        if (definition == null && _compiler != null)
        {
            definition = _compiler.compile(name);
            registerObject(definition);
        }
        return definition;
    }

    public void registerObject(CompiledObjectDefinition object)
    {
        _definitions.put(normalise(object.getName()), object);
    }

    public void registerObject(CompiledObjectInstance object)
    {
        _objects.put(new ObjectId(object), object);
    }

    private String normalise(String name)
    {
        if (name.endsWith(".c"))
        {
            name = name.substring(0, name.length() - 2);
        }
        name = name.replace("//", "/");
        if (name.charAt(0) != '/')
        {
            name = "/" + name;
        }
        return name;
    }

    public long allocateObjectIdentifier()
    {
        synchronized (this)
        {
            _id++;
            return _id;
        }
    }

    public ObjectInstance findObject(String name, int id)
    {
        return findObject(new ObjectId(findObject(name), id));
    }

    private ObjectInstance findObject(ObjectId objectId)
    {
        return _objects.get(objectId);
    }

    public Iterable< ? extends ObjectInstance> objects()
    {
        return _objects.values();
    }

    public Iterable< ? extends ObjectInstance> objects(final String name)
    {
        final Predicate<CompiledObjectInstance> predicate = new Predicate<CompiledObjectInstance>()
        {
            public boolean test(CompiledObjectInstance element)
            {
                return name.equals(element.getDefinition().getName());
            }
        };
        final Collection<CompiledObjectInstance> values = _objects.values();

        return new Iterable<CompiledObjectInstance>()
        {
            public Iterator<CompiledObjectInstance> iterator()
            {
                return new PredicateIterator<CompiledObjectInstance>(predicate, values.iterator());
            }
        };
    }

    public String toString()
    {
        return getClass().getSimpleName() + "(" + _compiler + ")";
    }
}