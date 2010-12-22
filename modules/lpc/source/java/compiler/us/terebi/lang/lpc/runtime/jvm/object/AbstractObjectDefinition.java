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

package us.terebi.lang.lpc.runtime.jvm.object;

import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import us.terebi.lang.lpc.compiler.CompilerObjectManager;
import us.terebi.lang.lpc.compiler.java.context.CompiledObjectDefinition;
import us.terebi.lang.lpc.compiler.java.context.CompiledObjectInstance;
import us.terebi.lang.lpc.runtime.AttributeMap;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.util.Apply;
import us.terebi.lang.lpc.runtime.util.Attributes;

/**
 * 
 */
public abstract class AbstractObjectDefinition implements CompiledObjectDefinition
{
    private final Logger LOG = Logger.getLogger(AbstractObjectDefinition.class);
    private static final Apply CREATE = new Apply("create");

    private final CompilerObjectManager _manager;
    private final String _name;
    private final AttributeMap _attributes;

    private CompiledObjectInstance _master;
    private boolean _loadingMaster;

    public AbstractObjectDefinition(CompilerObjectManager manager, String name)
    {
        _manager = manager;
        _name = name;
        _attributes = new Attributes();
        _master = null;
        _loadingMaster = false;
    }

    public String getName()
    {
        return _name;
    }

    protected void setMaster(CompiledObjectInstance master)
    {
        _master = master;
    }

    public CompiledObjectInstance getMasterInstance()
    {
        if (_loadingMaster)
        {
            LOG.warn("The master object for " + this + " is currently being loaded - it cannot be accessed");
            return null;
        }
        if (_master == null)
        {
            try
            {
                _loadingMaster = true;
                _master = newInstance(0, InstanceType.MASTER, null, Collections.<LpcValue> emptyList());
            }
            finally
            {
                _loadingMaster = false;
            }
        }
        return _master;
    }

    protected enum InstanceType
    {
        MASTER, INSTANCE, INHERIT, PROTOTYPE;
    }

    protected abstract CompiledObjectInstance newInstance(long id, InstanceType type, CompiledObjectInstance forInstance,
            List< ? extends LpcValue> createArguments);

    public void instanceDestructed(ObjectInstance instance)
    {
        if (instance == _master)
        {
            _master = null;
        }
        _manager.instanceDestructed(instance);
    }

    public String getBaseName()
    {
        return FilenameUtils.getBaseName(getName());
    }

    public CompiledObjectInstance newInstance(List< ? extends LpcValue> arguments)
    {
        return newInstance(objectManager().allocateObjectIdentifier(), InstanceType.INSTANCE, null, arguments);
    }

    protected CompilerObjectManager objectManager()
    {
        return _manager;
    }

    public CompiledObjectInstance getInheritableInstance(ObjectInstance forInstance)
    {
        if (forInstance instanceof CompiledObjectInstance)
        {
            CompiledObjectInstance coi = ((CompiledObjectInstance) forInstance);
            return newInstance(0, InstanceType.INHERIT, coi, Collections.<LpcValue> emptyList());
        }
        else
        {
            throw new IllegalArgumentException("Cannot create instance for non-compiled object " + forInstance);
        }
    }

    public CompiledObjectInstance getPrototypeInstance(ObjectInstance forInstance)
    {
        if (forInstance == null || forInstance instanceof CompiledObjectInstance)
        {
            return newInstance(0, InstanceType.PROTOTYPE, (CompiledObjectInstance) forInstance, Collections.<LpcValue> emptyList());
        }
        throw new IllegalArgumentException("Cannot create instance for non-compiled object " + forInstance);
    }

    protected LpcValue create(CompiledObjectInstance instance, List< ? extends LpcValue> createArguments)
    {
        return CREATE.invoke(instance, createArguments);
    }

    protected void register(CompiledObjectInstance instance)
    {
        objectManager().registerObject(instance);
    }

    public AttributeMap getAttributes()
    {
        return _attributes;
    }
}
