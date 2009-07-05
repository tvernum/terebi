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

/**
 * 
 */
public class CompiledObject<T extends LpcObject> implements CompiledObjectInstance
{
    private final CompiledObjectDefinition _definition;
    private final long _id;
    private final T _object;
    private final Map<String, ? extends ObjectInstance> _parents;
    private final AttributeMap _attributes;

    public CompiledObject(CompiledObjectDefinition definition, long id, T object,
            Map<String, ? extends ObjectInstance> parents)
    {
        _definition = definition;
        _id = id;
        _object = object;
        _parents = Collections.unmodifiableMap(parents);
        _attributes = new Attributes();
    }

    public CompiledObjectDefinition getDefinition()
    {
        return _definition;
    }

    public T getImplementingObject()
    {
        return _object;
    }

    public AttributeMap getAttributes()
    {
        return _attributes;
    }

    public long getId()
    {
        return _id;
    }

    public Map<String, ? extends ObjectInstance> getInheritedObjects()
    {
        return _parents;
    }

    public String getCanonicalName()
    {
        if (_id == 0)
        {
            return _definition.getName();
        }
        else
        {
            return _definition.getName() + "#" + _id;
        }
    }

    public Map<FieldDefinition, LpcValue> getFieldValues()
    {
        Map<FieldDefinition, LpcValue> values = new HashMap<FieldDefinition, LpcValue>();
        Collection< ? extends FieldDefinition> definitions = _definition.getFields().values();
        for (FieldDefinition definition : definitions)
        {
            values.put(definition, definition.getValue(this));
        }
        return values;
    }

}
