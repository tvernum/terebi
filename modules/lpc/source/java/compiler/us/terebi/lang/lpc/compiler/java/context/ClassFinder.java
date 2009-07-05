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

package us.terebi.lang.lpc.compiler.java.context;

import java.util.Map;

import us.terebi.lang.lpc.runtime.ClassDefinition;
import us.terebi.lang.lpc.runtime.MemberDefinition;
import us.terebi.lang.lpc.runtime.ObjectDefinition;
import us.terebi.lang.lpc.runtime.UserTypeDefinition;

/**
 * 
 */
public class ClassFinder
{
    private final ClassLookup _lookup;

    public ClassFinder(UserTypeDefinition declaringObject)
    {
        _lookup = new ClassLookup();
        populate(declaringObject);
    }

    private void populate(UserTypeDefinition declaringObject)
    {
        if (declaringObject instanceof ObjectDefinition)
        {
            populate((ObjectDefinition) declaringObject);
        }
        if (declaringObject instanceof MemberDefinition)
        {
            populate((MemberDefinition) declaringObject);
        }
    }

    private void populate(MemberDefinition declaringObject)
    {
        populate(declaringObject.getDeclaringType());
    }

    private void populate(ObjectDefinition declaringObject)
    {
        for (ClassDefinition classDefinition : declaringObject.getDefinedClasses().values())
        {
            _lookup.defineClass(classDefinition);
        }

        Map<String, ? extends ObjectDefinition> inherited = declaringObject.getInheritedObjects();
        for (String name : inherited.keySet())
        {
            _lookup.addInherit(name, inherited.get(name));
        }
    }

    public ClassDefinition find(String className)
    {
        return _lookup.findClass(className);
    }

}
