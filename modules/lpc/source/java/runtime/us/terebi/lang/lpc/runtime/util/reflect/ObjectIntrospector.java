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

import java.util.LinkedHashMap;
import java.util.Map;

import us.terebi.lang.lpc.runtime.FieldDefinition;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.UserTypeDefinition;
import us.terebi.lang.lpc.runtime.UserTypeInstance;

/**
 * 
 */
public class ObjectIntrospector
{
    private final UserTypeInstance _instance;
    private final ObjectInstance _object;

    public ObjectIntrospector(UserTypeInstance instance)
    {
        _instance = instance;
        if (_instance instanceof ObjectInstance)
        {
            _object = (ObjectInstance) instance;
        }
        else
        {
            _object = null;
        }
    }

    public Map<FieldDefinition, LpcValue> getFieldValues(boolean deep)
    {
        if (!deep || _object == null)
        {
            return _instance.getFieldValues();
        }

        Map<FieldDefinition, LpcValue> fields = new LinkedHashMap<FieldDefinition, LpcValue>();
        collectFields(_object, fields);
        return fields;
    }

    public UserTypeInstance getParent(UserTypeDefinition definition)
    {
        if (_instance.getDefinition().equals(definition))
        {
            return _instance;
        }
        if (_object == null)
        {
            return null;
        }
        return findParent(_object, definition);
    }

    private void collectFields(ObjectInstance instance, Map<FieldDefinition, LpcValue> fields)
    {
        fields.putAll(instance.getFieldValues());
        for (ObjectInstance parent : instance.getInheritedObjects().values())
        {
            collectFields(parent, fields);
        }
    }

    private ObjectInstance findParent(ObjectInstance instance, UserTypeDefinition definition)
    {
        if (instance.getDefinition().equals(definition))
        {
            return instance;
        }
        for (ObjectInstance parent : instance.getInheritedObjects().values())
        {
            instance = findParent(parent, definition);
            if (instance != null)
            {
                return instance;
            }
        }
        return null;
    }

    public LpcValue getField(String name)
    {
        if (_object == null)
        {
            return _instance.getFieldValues().get(name);
        }
        return getField(name, _object);
    }

    private LpcValue getField(String name, ObjectInstance object)
    {
        Map<FieldDefinition, LpcValue> values = object.getFieldValues();
        for (FieldDefinition field : values.keySet())
        {
            if (field.getName().equals(name))
            {
                return values.get(field);
            }
        }
        for (ObjectInstance parent : object.getInheritedObjects().values())
        {
            LpcValue value = getField(name, parent);
            if (value != null)
            {
                return value;
            }
        }
        return null;
    }

    public boolean setField(String name, LpcValue value)
    {
        if (_object == null)
        {
            Map<String, ? extends FieldDefinition> fields = _instance.getDefinition().getFields();
            FieldDefinition field = fields.get(name);
            field.setValue(_instance, value);
            return true;
        }
        return setField(name, value, _object);
    }

    private boolean setField(String name, LpcValue value, ObjectInstance object)
    {
        Map<String, ? extends FieldDefinition> fields = _instance.getDefinition().getFields();
        FieldDefinition field = fields.get(name);
        if (field != null)
        {
            field.setValue(object, value);
            return true;
        }
        
        for (ObjectInstance parent : object.getInheritedObjects().values())
        {
            if (setField(name, value, parent))
            {
                return true;
            }
        }
        return false;
    }

}
