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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import us.terebi.lang.lpc.compiler.java.context.ClassFinder;
import us.terebi.lang.lpc.compiler.java.context.CompiledInstance;
import us.terebi.lang.lpc.runtime.ClassDefinition;
import us.terebi.lang.lpc.runtime.FieldDefinition;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.UserTypeDefinition;
import us.terebi.lang.lpc.runtime.UserTypeInstance;
import us.terebi.lang.lpc.runtime.jvm.LpcMember;
import us.terebi.lang.lpc.runtime.jvm.LpcMemberType;
import us.terebi.lang.lpc.runtime.jvm.LpcReference;
import us.terebi.lang.lpc.runtime.jvm.exception.InternalError;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.util.reflect.ObjectIntrospector;

import static us.terebi.util.StringUtil.isBlank;

/**
 * 
 */
public class CompiledField implements FieldDefinition
{
    private final UserTypeDefinition _declaringObject;
    private final Field _field;
    private final String _name;
    private final Set<Modifier> _modifiers;
    private final LpcType _type;

    public CompiledField(UserTypeDefinition declaringObject, Field field)
    {
        try
        {
            // @TODO - Optimise for when field is "final" and "LpcReference"
            _declaringObject = declaringObject;
            _field = field;

            LpcMember member = getMemberAnnotation();
            _name = member.name();
            _modifiers = toSet(member.modifiers());

            LpcMemberType type = getTypeAnnotation();
            ClassDefinition cls = getClassDefintion(type);
            _type = Types.getType(type.kind(), cls, type.depth());
        }
        catch (Exception e)
        {
            throw new InternalError("For field " + getSimpleName(field) + ": " + e.getMessage(), e);
        }
    }

    private static String getSimpleName(Field field)
    {
        return field.getDeclaringClass().getName() + "." + field.getName();
    }

    private ClassDefinition getClassDefintion(LpcMemberType type)
    {
        String className = type.className();
        if (isBlank(className))
        {
            return null;
        }
        else
        {
            return new ClassFinder(_declaringObject).find(className);
        }
    }

    private LpcMemberType getTypeAnnotation()
    {
        LpcMemberType type = _field.getAnnotation(LpcMemberType.class);
        if (type == null)
        {
            throw new InternalError("Field " + _field + " is not annotated with " + LpcMemberType.class.getName());
        }
        return type;
    }

    private Set<Modifier> toSet(Modifier[] modifiers)
    {
        return new HashSet<Modifier>(Arrays.asList(modifiers));
    }

    private LpcMember getMemberAnnotation()
    {
        LpcMember member = _field.getAnnotation(LpcMember.class);
        if (member == null)
        {
            throw new InternalError("Field " + _field + " is not annotated with " + LpcMember.class.getName());
        }
        return member;
    }

    public LpcType getType()
    {
        return _type;
    }

    public LpcReference getReference(UserTypeInstance instance)
    {
        Object value = getFieldValue(instance);
        if (value instanceof LpcReference)
        {
            return (LpcReference) value;
        }
        return new FieldReference(instance, this);
    }

    public LpcValue getValue(UserTypeInstance instance)
    {
        Object value = getFieldValue(instance);
        if (value instanceof LpcValue)
        {
            return (LpcValue) value;
        }
        if (value instanceof LpcReference)
        {
            return ((LpcReference) value).get();
        }
        throw new InternalError("Field " + value + " is not a valid LpcValue type");
    }

    private Object getFieldValue(UserTypeInstance instance)
    {
        UserTypeInstance implementing = new ObjectIntrospector(instance).getParent(_declaringObject);
        if (implementing == null)
        {
            throw new LpcRuntimeException("Internal Error - Type " + instance + " does not implement " + _declaringObject + " for field " + this);
        }
        if (implementing instanceof CompiledInstance)
        {
            CompiledInstance ci = (CompiledInstance) implementing;
            try
            {
                return _field.get(ci.getImplementingObject());
            }
            catch (LpcRuntimeException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                throw new LpcRuntimeException("Internal Error - " + e.getMessage(), e);
            }
        }
        throw new LpcRuntimeException("Internal Error - Cannot get value from instance of type " + instance.getClass());
    }

    public void initialise(UserTypeInstance instance)
    {
        // @TODO ?
    }

    public void setValue(UserTypeInstance instance, LpcValue value)
    {
        UserTypeInstance implementing = new ObjectIntrospector(instance).getParent(_declaringObject);
        if (implementing == null)
        {
            throw new LpcRuntimeException("Internal Error - Type " + instance + " does not implement " + _declaringObject + " for field " + this);
        }
        if (implementing instanceof CompiledInstance)
        {
            CompiledInstance ci = (CompiledInstance) implementing;
            try
            {
                if (_field.getType().isAssignableFrom(LpcValue.class))
                {
                    _field.set(ci.getImplementingObject(), value);
                }
                else if (LpcReference.class.isAssignableFrom(_field.getType()))
                {
                    LpcReference ref = (LpcReference) _field.get(ci.getImplementingObject());
                    ref.set(value);
                }
                else
                {
                    throw new InternalError("Field " + _field + " cannot be assigned from " + LpcValue.class.getSimpleName());
                }
            }
            catch (LpcRuntimeException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                throw new LpcRuntimeException("Internal Error - " + e.getMessage(), e);
            }
        }
        else
        {
            throw new LpcRuntimeException("Internal Error - Cannot get value from instance of type " + instance.getClass());
        }
    }

    public UserTypeDefinition getDeclaringType()
    {
        return _declaringObject;
    }

    public Set<Modifier> getModifiers()
    {
        return _modifiers;
    }

    public String getName()
    {
        return _name;
    }

    public Kind getKind()
    {
        return Kind.FIELD;
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
        if (obj instanceof CompiledField)
        {
            CompiledField other = (CompiledField) obj;
            if (this._declaringObject.equals(other._declaringObject) && this._field.equals(other._field))
            {
                return true;
            }
        }
        return false;
    }

    public int hashCode()
    {
        return _declaringObject.hashCode() ^ _field.hashCode();
    }

    public String toString()
    {
        return getClass().getSimpleName() + "{" + getSimpleName(_field) + "}";
    }
}
