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

import java.util.Map;

import us.terebi.lang.lpc.compiler.java.context.CompiledImplementation;
import us.terebi.lang.lpc.compiler.java.context.CompiledObjectInstance;
import us.terebi.lang.lpc.runtime.AttributeMap;
import us.terebi.lang.lpc.runtime.FieldDefinition;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectInstance;

/**
 * 
 */
public class VirtualInstance extends AbstractCompiledObjectInstance implements CompiledObjectInstance
{
    private CompiledObjectInstance _instance;

    public VirtualInstance(VirtualObjectDefinition definition, long id)
    {
        super(definition, id);
    }

    public CompiledObjectInstance getInstance()
    {
        return _instance;
    }

    public void setInstance(CompiledObjectInstance instance)
    {
        if (instance == _instance)
        {
            return;
        }
        if (_instance != null)
        {
            throw new IllegalStateException("Instance already exists (" + instance + ")");
        }
        _instance = instance;
    }

    public void destruct()
    {
        _instance.destruct();
    }

    public AttributeMap getAttributes()
    {
        return _instance.getAttributes();
    }

    public Map<String, ? extends ObjectInstance> getInheritedObjects()
    {
        return _instance.getInheritedObjects();
    }

    public boolean isDestructed()
    {
        return _instance.isDestructed();
    }

    public Map<FieldDefinition, LpcValue> getFieldValues()
    {
        return _instance.getFieldValues();
    }

    public CompiledImplementation getImplementingObject()
    {
        if (_instance == null)
        {
            return null;
        }
        return _instance.getImplementingObject();
    }

    public boolean isVirtual()
    {
        return true;
    }

    public boolean isSet()
    {
        return _instance != null;
    }
}
