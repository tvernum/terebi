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

package us.terebi.lang.lpc.runtime.util.type;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import us.terebi.lang.lpc.runtime.ClassDefinition;
import us.terebi.lang.lpc.runtime.ClassInstance;
import us.terebi.lang.lpc.runtime.FieldDefinition;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;
import us.terebi.util.Factory;

import static us.terebi.util.StringUtil.isBlank;

/**
 * 
 */
public class DynamicClassDefinition implements ClassDefinition
{
    private final String _name;
    private final Map<String, FieldDefinition> _fields;
    private Factory< ? extends ClassInstance> _factory;

    public DynamicClassDefinition(String name)
    {
        if (isBlank(name))
        {
            throw new IllegalArgumentException("Cannot create a class without a name");
        }
        _name = name;
        _fields = new HashMap<String, FieldDefinition>();
    }

    public void setFactory(Factory< ? extends ClassInstance> factory)
    {
        _factory = factory;
    }

    public ClassInstance newInstance(ObjectInstance owner)
    {
        if (_factory == null)
        {
            throw new LpcRuntimeException("Internal Error - No factory specified for class definition " + getName());
        }
        return _factory.create(owner);
    }

    public Map<String, ? extends FieldDefinition> getFields()
    {
        return Collections.unmodifiableMap(_fields);
    }

    public void addField(FieldDefinition field)
    {
        _fields.put(field.getName(), field);
    }

    public String getName()
    {
        return _name;
    }

    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof DynamicClassDefinition)
        {
            DynamicClassDefinition other = (DynamicClassDefinition) obj;
            return equals(this._name, other._name) && fieldEquals(this._fields, other._fields, other);
        }
        if (obj instanceof ClassDefinition)
        {
            ClassDefinition other = (ClassDefinition) obj;
            return equals(this._name, other.getName()) && fieldEquals(this._fields, other.getFields(), other);
        }
        return false;
    }

    private boolean fieldEquals(Map<String, ? extends FieldDefinition> self, Map<String, ? extends FieldDefinition> other, ClassDefinition otherClass)
    {
        if (self == other)
        {
            return true;
        }
        if (self.size() != other.size())
        {
            return false;
        }
        if (!self.keySet().equals(other.keySet()))
        {
            return false;
        }
        /**
         * Need to do it this way to avoid a recursive loop if the class has a field that references itself
         */
        for (String key : self.keySet())
        {
            if (!fieldEquals(self.get(key), other.get(key), otherClass))
            {
                return false;
            }
        }
        return true;
    }

    private boolean fieldEquals(FieldDefinition self, FieldDefinition other, ClassDefinition otherClass)
    {
        if (!equals(self.getName(), other.getName()))
        {
            return false;
        }
        if (!equals(self.getModifiers(), other.getModifiers()))
        {
            return false;
        }
        LpcType t1 = self.getType();
        LpcType t2 = other.getType();
        if (t1 == t2)
        {
            return true;
        }
        // This is the magic that stops the infinite recursion
        if (t1 == this && t2 == otherClass)
        {
            return true;
        }
        return t1.equals(t2);
    }

    private boolean equals(Object self, Object other)
    {
        if (self == other)
        {
            return true;
        }
        if (self == null)
        {
            return false;
        }
        return self.equals(other);
    }

    public int hashCode()
    {
        return _name.hashCode() ^ _fields.size();
    }

    public String toString()
    {
        return "class " + _name;
    }

}
