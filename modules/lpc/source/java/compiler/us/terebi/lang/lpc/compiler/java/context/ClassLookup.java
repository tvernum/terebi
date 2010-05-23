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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import us.terebi.lang.lpc.compiler.CompileException;
import us.terebi.lang.lpc.parser.ast.ASTIdentifier;
import us.terebi.lang.lpc.parser.util.ASTUtil;
import us.terebi.lang.lpc.runtime.ClassDefinition;
import us.terebi.lang.lpc.runtime.ObjectDefinition;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;
import us.terebi.util.ToString;

/**
 * 
 */
public class ClassLookup
{
    private final Map<String, ClassDefinition> _local;
    private final Map<String, ObjectDefinition> _inherited;

    public ClassLookup()
    {
        _local = new HashMap<String, ClassDefinition>();
        _inherited = new HashMap<String, ObjectDefinition>();
    }

    public ClassLookup(ObjectInstance object)
    {
        this();
        if (object != null)
        {
            addInherits(object.getDefinition().getInheritedObjects());
            defineClasses(object.getDefinition().getDefinedClasses().values());
        }
    }

    public ClassDefinition findClass(ASTIdentifier classNode)
    {
        String className = ASTUtil.getImage(classNode);
        try
        {
            return findClass(className);
        }
        catch (LpcRuntimeException e)
        {
            throw new CompileException(classNode, e.getMessage());
        }
    }

    public ClassDefinition findClass(String className)
    {
        ClassDefinition definition = _local.get(className);
        if (definition != null)
        {
            return definition;
        }

        Set<ClassDefinition> match = new HashSet<ClassDefinition>();
        findMatchingClasses(className, _inherited, match);

        if (match.isEmpty())
        {
            throw new LookupException("The class " + className + " has not been defined in the current scope");
        }
        if (match.size() > 1)
        {
            throw new LookupException("Multiple classes with the name "
                    + className
                    + " have been defined in the current scope - "
                    + ToString.toString(match));
        }
        return match.iterator().next();
    }

    private void findMatchingClasses(String className, Map<String, ? extends ObjectDefinition> inherited, Set<ClassDefinition> match)
    {
        ClassDefinition definition;
        for (ObjectDefinition parent : inherited.values())
        {
            definition = parent.getDefinedClasses().get(className);
            if (definition != null)
            {
                match.add(definition);
            }
            findMatchingClasses(className, parent.getInheritedObjects(), match);
        }
    }

    public void addInherit(String name, ObjectDefinition parent)
    {
        _inherited.put(name, parent);
    }

    public void defineClass(ClassDefinition classDefinition)
    {
        _local.put(classDefinition.getName(), classDefinition);
    }

    public void addInherits(Map<String, ? extends ObjectDefinition> parents)
    {
        _inherited.putAll(parents);
    }

    public void defineClasses(Iterable< ? extends ClassDefinition> classes)
    {
        for (ClassDefinition classDefinition : classes)
        {
            defineClass(classDefinition);
        }
    }

    public String toString()
    {
        return getClass().getSimpleName() + "{" + _local.keySet() + ";" + _inherited.keySet() + "}";
    }

}
