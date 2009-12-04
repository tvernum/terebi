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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import us.terebi.lang.lpc.runtime.ClassDefinition;
import us.terebi.lang.lpc.runtime.ClassInstance;
import us.terebi.lang.lpc.runtime.FieldDefinition;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.UserTypeDefinition;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;
import us.terebi.util.Factory;

/**
 * 
 */
public class DynamicClassDefinition implements ClassDefinition
{
    private final String _name;
    private final Set< ? extends Modifier> _modifiers;
    private final Map<String, FieldDefinition> _fields;
    private UserTypeDefinition _declaringType;
    private Factory< ? extends ClassInstance> _factory;

    public DynamicClassDefinition(String name, Set< ? extends Modifier> modifiers)
    {
        _name = name;
        _modifiers = modifiers;
        _fields = new HashMap<String, FieldDefinition>();
    }

    public DynamicClassDefinition(String name, Set<Modifier> modifiers, UserTypeDefinition declaring)
    {
        this(name, modifiers);
        _declaringType = declaring;
    }

    public DynamicClassDefinition(String name, Modifier[] modifiers, UserTypeDefinition declaring)
    {
        this(name, new HashSet<Modifier>(Arrays.asList(modifiers)), declaring);
    }

    public void setDeclaringType(UserTypeDefinition declaringType)
    {
        _declaringType = declaringType;
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

    public UserTypeDefinition getDeclaringType()
    {
        return _declaringType;
    }

    public Set< ? extends Modifier> getModifiers()
    {
        return _modifiers;
    }

    public Kind getKind()
    {
        return Kind.CLASS;
    }

    public boolean equals(Object obj)
    {
        // @TODO Auto-generated method stub
        return super.equals(obj);
    }

    public int hashCode()
    {
        // @TODO Auto-generated method stub
        return super.hashCode();
    }

    public String toString()
    {
        if (_declaringType == null)
        {
            return _name;
        }
        return _declaringType.getName() + "::" + _name;
    }

}
