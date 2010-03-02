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

import java.util.List;
import java.util.Map;

import us.terebi.lang.lpc.compiler.CompilerObjectManager;
import us.terebi.lang.lpc.compiler.java.context.CompiledObjectDefinition;
import us.terebi.lang.lpc.compiler.java.context.CompiledObjectInstance;
import us.terebi.lang.lpc.runtime.ClassDefinition;
import us.terebi.lang.lpc.runtime.CompiledMethodDefinition;
import us.terebi.lang.lpc.runtime.FieldDefinition;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectDefinition;

/**
 * 
 */
public class VirtualObjectDefinition extends AbstractObjectDefinition implements CompiledObjectDefinition
{
    private final CompiledObjectInstance _object;
    private final CompiledObjectDefinition _definition;

    public VirtualObjectDefinition(CompilerObjectManager manager, String name, CompiledObjectInstance object)
    {
        super(manager, name);
        _object = object;
        _definition = _object.getDefinition();
    }

    public Class< ? > getImplementationClass()
    {
        return _definition.getImplementationClass();
    }

    public Map<String, ? extends CompiledMethodDefinition> getMethods()
    {
        return _definition.getMethods();
    }

    public Map<String, ? extends ClassDefinition> getDefinedClasses()
    {
        return _definition.getDefinedClasses();
    }

    public Map<String, ? extends ObjectDefinition> getInheritedObjects()
    {
        return _definition.getInheritedObjects();
    }

    public Map<String, ? extends FieldDefinition> getFields()
    {
        return _definition.getFields();
    }

    protected CompiledObjectInstance newInstance(long id, InstanceType type, Object forThis, List< ? extends LpcValue> createArguments)
    {
        CompiledObjectInstance instance = _definition.getPrototypeInstance();
        for (FieldDefinition fieldDefinition : _definition.getFields().values())
        {
            LpcValue value = fieldDefinition.getValue(_object);
            fieldDefinition.setValue(instance, value);
        }
        VirtualInstance virtual = new VirtualInstance(this, id, instance);
        if (type != InstanceType.INHERIT && type != InstanceType.PROTOTYPE)
        {
            register(virtual);
            create(virtual, createArguments);
        }
        return virtual;
    }

    public String toString()
    {
        return getClass().getSimpleName() + "{" + getName() + ":" + _definition + "}";
    }
}
