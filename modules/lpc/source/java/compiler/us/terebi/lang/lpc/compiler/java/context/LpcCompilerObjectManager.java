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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import us.terebi.lang.lpc.compiler.CompilerAware;
import us.terebi.lang.lpc.compiler.CompilerObjectManager;
import us.terebi.lang.lpc.compiler.ObjectCompiler;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectDefinition;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.context.ObjectLifecycleListener;
import us.terebi.lang.lpc.runtime.jvm.context.RuntimeContext;
import us.terebi.lang.lpc.runtime.jvm.context.SystemContext;
import us.terebi.lang.lpc.runtime.jvm.exception.InternalError;
import us.terebi.lang.lpc.runtime.jvm.object.VirtualObjectDefinition;
import us.terebi.lang.lpc.runtime.jvm.value.StringValue;
import us.terebi.lang.lpc.runtime.util.Apply;
import us.terebi.util.Predicate;
import us.terebi.util.collection.PredicateIterator;
import us.terebi.util.listener.ListenerManager;

/**
 * 
 */
public class LpcCompilerObjectManager implements CompilerObjectManager, CompilerAware
{
    private final Logger LOG = Logger.getLogger(LpcCompilerObjectManager.class);

    private static final Apply VIRTUAL_COMPILE = new Apply("compile_object");

    private final Map<String, CompiledObjectDefinition> _definitions;
    private final Map<ObjectId, CompiledObjectInstance> _objects;
    private final ListenerManager<ObjectLifecycleListener> _listeners;
    private final Set<String> _compiling;

    private ObjectCompiler _compiler;
    private long _id;
    private String _master;
    private String _sefun;

    public LpcCompilerObjectManager()
    {
        _definitions = new HashMap<String, CompiledObjectDefinition>();
        _objects = new HashMap<ObjectId, CompiledObjectInstance>();
        _listeners = new ListenerManager<ObjectLifecycleListener>(ObjectLifecycleListener.class);
        _compiling = new HashSet<String>();
        _compiler = null;
        _id = 0;
        _master = null;
        _sefun = null;
    }

    public void setCompiler(ObjectCompiler compiler)
    {
        _compiler = compiler;
    }

    public CompiledObjectDefinition findObject(String name, boolean load)
    {
        CompiledObjectDefinition definition = _definitions.get(ObjectId.normalise(name));
        if (definition == null && load && _compiler != null)
        {
            definition = compile(name);
        }
        return definition;
    }

    private CompiledObjectDefinition compile(String name)
    {
        if (_compiling.contains(name))
        {
            throw new InternalError("Compile cycle detected");
        }

        _compiling.add(name);
        try
        {
            CompiledObjectDefinition definition = _compiler.compile(filename(name));
            if (definition != null)
            {
                return register(definition, null);
            }
            LpcValue virtual = VIRTUAL_COMPILE.invoke(getMasterObject(), new StringValue(name));
            if (!virtual.asBoolean())
            {
                throw new ObjectNotFoundException(name);
            }
            ObjectInstance object = virtual.asObject();
            assert object instanceof CompiledObjectInstance;
            definition = new VirtualObjectDefinition(this, name, (CompiledObjectInstance) object);
            return register(definition, object);
        }
        finally
        {
            _compiling.remove(name);
        }
    }

    private CompiledObjectDefinition register(CompiledObjectDefinition definition, ObjectInstance prototype)
    {
        registerObject(definition);
        if (prototype != null)
        {
            _listeners.dispatch().vitualObjectCompiled(getContext(), this, definition, prototype);
        }
        else
        {
            _listeners.dispatch().objectCompiled(getContext(), this, definition);
        }
        forceLoad(definition);
        return definition;
    }

    private String filename(String name)
    {
        return ObjectId.normalise(name) + ".c";
    }

    public void registerObject(CompiledObjectDefinition object)
    {
        if (object == null)
        {
            return;
        }
        LOG.info("Loaded " + object);
        String name = ObjectId.normalise(object.getName());
        _definitions.put(name, object);
    }

    private CompiledObjectInstance forceLoad(CompiledObjectDefinition object)
    {
        CompiledObjectInstance master = object.getMasterInstance();
        return master;
    }

    public void registerObject(CompiledObjectInstance object)
    {
        _objects.put(new ObjectId(object), object);
        _listeners.dispatch().objectCreated(getContext(), this, object);
    }

    public long allocateObjectIdentifier()
    {
        synchronized (this)
        {
            _id++;
            return _id;
        }
    }

    public CompiledObjectInstance findObject(ObjectId id)
    {
        CompiledObjectDefinition definition = _definitions.get(id.getFile());
        if (definition == null)
        {
            return null;
        }
        return _objects.get(id);
    }

    public int objectCount()
    {
        return _objects.size();
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

    public ObjectDefinition defineMasterObject(String name)
    {
        if (_master == null)
        {
            _master = name;
        }
        else if (!_master.equals(name))
        {
            throw new IllegalStateException("Master object is already defined as " + _master + " cannot redfined as " + name);
        }
        return findObject(_master, true);
    }

    public ObjectDefinition defineSimulatedEfunObject(String name)
    {
        if (_sefun != null && !_sefun.equals(name))
        {
            throw new IllegalStateException("Simulated Efun object is already defined as " + _sefun + " cannot redfined as " + name);
        }
        CompiledObjectDefinition definition = findObject(name, true);
        _sefun = name;
        return definition;
    }

    public ObjectInstance getMasterObject()
    {
        if (_master == null)
        {
            return null;
        }
        return findObject(_master, true).getMasterInstance();
    }

    public ObjectInstance getSimulatedEfunObject()
    {
        if (_sefun == null)
        {
            return null;
        }
        return findObject(_sefun, true).getMasterInstance();
    }

    public void instanceDestructed(ObjectInstance instance)
    {
        _objects.values().remove(instance);
        _listeners.dispatch().objectDestructed(getContext(), this, instance);
    }

    private SystemContext getContext()
    {
        return RuntimeContext.obtain().system();
    }

    public void addListener(ObjectLifecycleListener listener)
    {
        _listeners.addListener(listener);
    }

    public void removeListener(ObjectLifecycleListener listener)
    {
        _listeners.removeListener(listener);
    }

}
