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

package us.terebi.lang.lpc.compiler.java;

import java.util.Collections;
import java.util.Set;

import us.terebi.lang.lpc.runtime.ClassDefinition;
import us.terebi.lang.lpc.runtime.FieldDefinition;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.UserTypeDefinition;
import us.terebi.lang.lpc.runtime.UserTypeInstance;
import us.terebi.lang.lpc.runtime.jvm.LpcReference;

public class CompileTimeField implements FieldDefinition
{
    private final ClassDefinition _declaring;
    private final String _name;
    private final LpcType _fieldType;

    public CompileTimeField(ClassDefinition declaring, String name, LpcType type)
    {
        _declaring = declaring;
        _name = name;
        _fieldType = type;
    }

    public LpcType getType()
    {
        return _fieldType;
    }

    public LpcReference getReference(UserTypeInstance instance)
    {
        throw new UnsupportedOperationException("getReference - Not implemented");
    }

    public LpcValue getValue(UserTypeInstance instance)
    {
        throw new UnsupportedOperationException("getValue - Not implemented");
    }

    public void initialise(UserTypeInstance instance)
    {
        throw new UnsupportedOperationException("initialise - Not implemented");
    }

    public void setValue(UserTypeInstance instance, LpcValue value)
    {
        throw new UnsupportedOperationException("setValue - Not implemented");
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
        return Collections.<Modifier> emptySet();
    }

    public String getName()
    {
        return _name;
    }
}
