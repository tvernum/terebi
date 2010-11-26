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

package us.terebi.lang.lpc.runtime.util.reflect;

import java.util.Collection;
import java.util.LinkedHashSet;

import us.terebi.lang.lpc.runtime.FieldDefinition;
import us.terebi.lang.lpc.runtime.ObjectDefinition;

/**
 * 
 */
public class TypeIntrospector
{
    private final ObjectDefinition _definition;

    public TypeIntrospector(ObjectDefinition definition)
    {
        _definition = definition;
    }

    public Collection< ? extends FieldDefinition> getFields(boolean deep)
    {
        if (!deep)
        {
            return _definition.getFields().values();
        }
        Collection<FieldDefinition> fields = new LinkedHashSet<FieldDefinition>();
        collectFields(_definition, fields);
        return fields;
    }

    private void collectFields(ObjectDefinition definition, Collection<FieldDefinition> fields)
    {
        fields.addAll(definition.getFields().values());
        for (ObjectDefinition parent : definition.getInheritedObjects().values())
        {
            collectFields(parent, fields);
        }
    }

    public FieldDefinition getField(String name)
    {
        return getField(name, _definition);
    }

    private FieldDefinition getField(String name, ObjectDefinition definition)
    {
        FieldDefinition field = definition.getFields().get(name);
        if (field != null)
        {
            return field;
        }
        for (ObjectDefinition parent : definition.getInheritedObjects().values())
        {
            field = getField(name, parent);
            if (field != null)
            {
                return field;
            }
        }
        return null;
    }
}
