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

package us.terebi.lang.lpc.runtime.jvm;

import us.terebi.lang.lpc.compiler.java.context.ClassFinder;
import us.terebi.lang.lpc.compiler.java.context.CompiledObjectDefinition;
import us.terebi.lang.lpc.compiler.java.context.CompiledObjectInstance;
import us.terebi.lang.lpc.compiler.java.context.CompiledImplementation;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.ClassDefinition;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.MethodDefinition;
import us.terebi.lang.lpc.runtime.ObjectDefinition;
import us.terebi.lang.lpc.runtime.jvm.exception.InternalError;
import us.terebi.lang.lpc.runtime.jvm.support.CallableSupport;
import us.terebi.lang.lpc.runtime.jvm.value.ClassReference;

/**
 * 
 */
public class LpcObject extends LpcRuntimeSupport implements CompiledImplementation
{
    private CompiledObjectDefinition _definition;
    private CompiledObjectInstance _instance;

    public LpcObject()
    {
        this((CompiledObjectDefinition) null, (CompiledObjectInstance) null);
    }

    public LpcObject(CompiledObjectDefinition definition)
    {
        this(definition, (CompiledObjectInstance) null);
    }

    public LpcObject(CompiledObjectDefinition definition, CompiledObjectInstance instance)
    {
        _definition = definition;
        _instance = instance;
    }

    public void init()
    {
        // no-op
    }

    public void setDefinition(CompiledObjectDefinition definition)
    {
        if (_definition == null)
        {
            _definition = definition;
        }
        else if (definition != _definition)
        {
            throw new IllegalStateException("Attempt to redefine ObjectDefinition for " + this);
        }
    }

    public CompiledObjectDefinition getObjectDefinition()
    {
        return _definition;
    }

    public CompiledObjectDefinition getTypeDefinition()
    {
        return getObjectDefinition();
    }

    public void setInstance(CompiledObjectInstance instance)
    {
        if (_instance == null)
        {
            _instance = instance;
        }
        else if (instance != _instance)
        {
            throw new IllegalStateException("Attempt to redefine ObjectInstance for " + this);
        }
    }

    public CompiledObjectInstance getObjectInstance()
    {
        return _instance;
    }

    public CompiledObjectInstance getInstance()
    {
        return getObjectInstance();
    }

    protected LpcType withType(Class< ? extends LpcClass> cls, int depth)
    {
        return withType(classDefinition(cls), depth);
    }

    protected LpcValue classReference(Class< ? extends LpcClass> cls)
    {
        return new ClassReference(classDefinition(cls), getInstance());
    }

    protected ClassDefinition classDefinition(Class< ? extends LpcClass> cls)
    {
        // @TODO Cache these...
        return LpcClass.getClassDefinition(cls);
    }

    protected ClassDefinition findClassDefinition(String lpcClassName)
    {
        // @TODO - this shouldn't use something in the compiler package
        return new ClassFinder(_definition).find(lpcClassName);
    }

    public Callable method(String name)
    {
        CompiledObjectDefinition object = getTypeDefinition();
        Callable callable = findMethod(name, object);
        if (callable != null)
        {
            return callable;
        }
        throw new InternalError("No such method " + name + " in " + object);
    }

    private Callable findMethod(String name, ObjectDefinition object)
    {
        CompiledObjectInstance instance = getInstance();
        MethodDefinition method = CallableSupport.findMethod(name, object, instance);
        if (method == null)
        {
            return null;
        }
        return method.getFunction(instance);
    }

}
