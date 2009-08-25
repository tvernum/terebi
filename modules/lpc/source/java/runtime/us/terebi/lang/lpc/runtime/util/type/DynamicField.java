/* ------------------------------------------------------------------------
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
import java.util.Set;

import us.terebi.lang.lpc.runtime.FieldDefinition;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.UserTypeDefinition;
import us.terebi.lang.lpc.runtime.UserTypeInstance;
import us.terebi.lang.lpc.runtime.jvm.LpcReference;
import us.terebi.lang.lpc.runtime.jvm.object.FieldReference;
import us.terebi.lang.lpc.runtime.jvm.value.NilValue;

/**
 * 
 */
public class DynamicField implements FieldDefinition
{
    private final UserTypeDefinition _declaring;
    private final Set< ? extends Modifier> _modifiers;
    private final String _name;
    private final LpcType _type;
    private final LpcValue _initial;

    public DynamicField(UserTypeDefinition declaring, Set< ? extends Modifier> modifiers, String name, LpcType type,
            LpcValue initial)
    {
        _declaring = declaring;
        _modifiers = modifiers;
        _name = name;
        _type = type;
        _initial = initial;
    }

    public DynamicField(DynamicClassDefinition declaring, String name, LpcType type)
    {
        this(declaring, Collections.singleton(Modifier.PUBLIC), name, type, NilValue.INSTANCE);
    }

    public LpcType getType()
    {
        return _type;
    }

    public LpcReference getReference(UserTypeInstance instance)
    {
        return new FieldReference(instance, this);
    }

    public LpcValue getValue(UserTypeInstance instance)
    {
        return getDynamic(instance).getFieldValue(this);
    }

    private DynamicUserType getDynamic(UserTypeInstance instance)
    {
        if (instance instanceof DynamicUserType)
        {
            return (DynamicUserType) instance;
        }
        else
        {
            throw new IllegalArgumentException("Not a " + DynamicUserType.class.getSimpleName() + " " + instance);
        }
    }

    public void initialise(UserTypeInstance instance)
    {
        getDynamic(instance).setFieldValue(this, _initial);
    }

    public void setValue(UserTypeInstance instance, LpcValue value)
    {
        getDynamic(instance).setFieldValue(this, value);
    }

    public UserTypeDefinition getDeclaringType()
    {
        return _declaring;
    }

    public Kind getKind()
    {
        return Kind.FIELD;
    }

    public Set< ? extends Modifier> getModifiers()
    {
        return _modifiers;
    }

    public String getName()
    {
        return _name;
    }

}
