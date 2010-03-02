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

package us.terebi.lang.lpc.runtime.jvm.object;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import us.terebi.lang.lpc.compiler.java.context.CompiledObjectDefinition;
import us.terebi.lang.lpc.compiler.java.context.CompiledObjectInstance;
import us.terebi.lang.lpc.runtime.AttributeMap;
import us.terebi.lang.lpc.runtime.FieldDefinition;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.LpcObject;
import us.terebi.lang.lpc.runtime.util.Attributes;
import us.terebi.lang.lpc.runtime.util.Destructable;

/**
 * 
 */
public class CompiledObject<T extends LpcObject> extends AbstractCompiledObjectInstance implements CompiledObjectInstance
{
    private final Destructable<T> _object;
    private final Map<String, ? extends ObjectInstance> _parents;
    private final AttributeMap _attributes;
    private boolean _destructed;

    public CompiledObject(CompiledObjectDefinition definition, long id, T object, Map<String, ? extends ObjectInstance> parents)
    {
        super(definition, id);
        _object = new Destructable<T>(object);
        _parents = Collections.unmodifiableMap(parents);
        _attributes = new Attributes();
        _destructed = false;
    }

    public void destruct()
    {
        if (_destructed)
        {
            return;
        }
        _destructed = true;

        for (Object object : _attributes.asMap().values())
        {
            if (object instanceof DestructListener)
            {
                ((DestructListener) object).instanceDestructed(this);
            }
        }
        getDefinition().instanceDestructed(this);
        _attributes.asMap().clear();
        for (ObjectInstance parent : _parents.values())
        {
            parent.destruct();
        }
        _object.destroy();
    }

    public boolean isDestructed()
    {
        return _destructed;
    }

    public T getImplementingObject()
    {
        return _object.get();
    }

    public AttributeMap getAttributes()
    {
        return _attributes;
    }

    public Map<String, ? extends ObjectInstance> getInheritedObjects()
    {
        return _parents;
    }

    public Map<FieldDefinition, LpcValue> getFieldValues()
    {
        Map<FieldDefinition, LpcValue> values = new HashMap<FieldDefinition, LpcValue>();
        Collection< ? extends FieldDefinition> definitions = getDefinition().getFields().values();
        for (FieldDefinition definition : definitions)
        {
            values.put(definition, definition.getValue(this));
        }
        return values;
    }

    public boolean isVirtual()
    {
        return false;
    }

}
