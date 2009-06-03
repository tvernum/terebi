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
import java.util.Set;

import us.terebi.lang.lpc.runtime.FieldDefinition;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.UserTypeDefinition;
import us.terebi.lang.lpc.runtime.UserTypeInstance;

/**
 * 
 */
public class CompiledField implements FieldDefinition
{

    public CompiledField(CompiledObject compiledObject, Field field)
    {
        // @TODO Auto-generated constructor stub
    }

    public LpcType getType()
    {
        // @TODO Auto-generated method stub
        return null;
    }

    public LpcValue getValue(UserTypeInstance instance)
    {
        // @TODO Auto-generated method stub
        return null;
    }

    public void initialise(UserTypeInstance instance)
    {
        // @TODO Auto-generated method stub

    }

    public void setValue(UserTypeInstance instance, LpcValue value)
    {
        // @TODO Auto-generated method stub

    }

    public UserTypeDefinition getDeclaringType()
    {
        // @TODO Auto-generated method stub
        return null;
    }

    public Set<Modifier> getModifiers()
    {
        // @TODO Auto-generated method stub
        return null;
    }

    public String getName()
    {
        // @TODO Auto-generated method stub
        return null;
    }

}
