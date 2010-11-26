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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import us.terebi.lang.lpc.compiler.java.context.CompiledClass;
import us.terebi.lang.lpc.compiler.java.context.CompiledClassInstance;
import us.terebi.lang.lpc.compiler.java.context.CompiledImplementation;
import us.terebi.lang.lpc.compiler.java.context.CompiledInstance;
import us.terebi.lang.lpc.compiler.java.context.CompiledObjectInstance;
import us.terebi.lang.lpc.runtime.ClassDefinition;
import us.terebi.lang.lpc.runtime.ObjectDefinition;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.UserTypeDefinition;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;
import us.terebi.lang.lpc.runtime.jvm.object.CompiledField;
import us.terebi.lang.lpc.runtime.util.type.DynamicClassDefinition;
import us.terebi.util.Factory;

/**
 * 
 */
public abstract class LpcClass extends LpcRuntimeSupport implements CompiledImplementation
{
    private final ObjectDefinition _declaring;
    private ClassDefinition _definition;

    public LpcClass(ObjectDefinition declaring)
    {
        _declaring = declaring;
        _definition = null;
    }

    public void init()
    {
        // no-op
    }

    public ClassDefinition getClassDefinition()
    {
        synchronized (this)
        {
            if (_definition == null)
            {
                _definition = loadDefinition();
            }
        }
        return _definition;
    }

    public ObjectDefinition getDeclaringObject()
    {
        return _declaring;
    }
    
    private ClassDefinition loadDefinition()
    {
        Class< ? extends LpcClass> cls = getClass();
        return getClassDefinition(cls);
    }

    public static ClassDefinition getClassDefinition(final Class< ? extends LpcClass> cls)
    {
        // @TODO Cache these by class (weak hash map ?)
        LpcMember annotation = cls.getAnnotation(LpcMember.class);
        if (annotation == null)
        {
            throw new LpcRuntimeException("Class object " + cls + " is not annotated with " + LpcMember.class.getSimpleName());
        }
        final DynamicClassDefinition definition = new DynamicClassDefinition(annotation.name());

        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields)
        {
            if (LpcField.class.isAssignableFrom(field.getType()))
            {
                definition.addField(new CompiledField(definition, field));
            }
        }

        Factory<CompiledClassInstance> factory = new Factory<CompiledClassInstance>()
        {
            public CompiledClassInstance create(Object... arguments)
            {
                ObjectInstance owner = (ObjectInstance) arguments[0];
                CompiledObjectInstance coi = (CompiledObjectInstance) owner;
                try
                {
                    Constructor< ? extends LpcClass> constructor = cls.getConstructor(ObjectDefinition.class);
                    LpcClass lpc = constructor.newInstance(coi.getDefinition());
                    return new CompiledClass(definition, lpc);
                }
                catch (Exception e)
                {
                    throw new LpcRuntimeException("Cannot create class instance", e);
                }
            }
        };
        definition.setFactory(factory);
        return definition;
    }

    public UserTypeDefinition getTypeDefinition()
    {
        return getClassDefinition();
    }

}
