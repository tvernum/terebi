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
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import us.terebi.lang.lpc.compiler.java.context.CompiledObjectDefinition;
import us.terebi.lang.lpc.compiler.java.context.CompiledObjectInstance;
import us.terebi.lang.lpc.compiler.java.context.ObjectManager;
import us.terebi.lang.lpc.runtime.ClassDefinition;
import us.terebi.lang.lpc.runtime.CompiledMethodDefinition;
import us.terebi.lang.lpc.runtime.FieldDefinition;
import us.terebi.lang.lpc.runtime.ObjectDefinition;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.LpcField;
import us.terebi.lang.lpc.runtime.jvm.LpcInherited;
import us.terebi.lang.lpc.runtime.jvm.LpcMethod;
import us.terebi.lang.lpc.runtime.jvm.LpcObject;
import us.terebi.lang.lpc.runtime.jvm.context.ScopeLookup;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;

/**
 * 
 */
public class CompiledObject implements CompiledObjectDefinition
{
    private final ScopeLookup _lookup;
    private final Class< ? extends LpcObject> _implementation;
    private final Map<String, CompiledObjectDefinition> _inherited;
    private final Map<String, CompiledField> _fields;
    private final Map<String, CompiledMethod> _methods;
    private final String _name;

    public CompiledObject(ObjectManager manager, ScopeLookup lookup, String name, Class< ? extends LpcObject> implementation)
    {
        _lookup = lookup;
        _name = name;
        _implementation = implementation;
        _inherited = new HashMap<String, CompiledObjectDefinition>();
        _fields = new HashMap<String, CompiledField>();
        _methods = new HashMap<String, CompiledMethod>();
        introspect(manager);
    }

    private void introspect(ObjectManager manager)
    {
        Method[] methods = _implementation.getDeclaredMethods();
        for (Method method : methods)
        {
            LpcMethod annotation = method.getAnnotation(LpcMethod.class);
            if (annotation != null)
            {
                _methods.put(annotation.name(), new CompiledMethod(this, method, _lookup));
            }
        }

        Field[] fields = _implementation.getDeclaredFields();
        for (Field field : fields)
        {
            if (LpcField.class.isAssignableFrom(field.getType()))
            {
                _fields.put(field.getName(), new CompiledField(this, field));
            }
            else if (field.getAnnotation(LpcInherited.class) != null)
            {
                LpcInherited inherited = field.getAnnotation(LpcInherited.class);
                assert (inherited != null);
                _inherited.put(inherited.name(), findInherited(manager, inherited));
            }
        }
    }

    private CompiledObjectDefinition findInherited(ObjectManager manager, LpcInherited inherited)
    {
        String lpc = inherited.lpc();
        CompiledObjectDefinition object = manager.findObject(lpc);
        if (object == null)
        {
            throw new LpcRuntimeException("Internal error - Object manager "
                    + manager
                    + " has no object for  "
                    + lpc
                    + " but "
                    + _implementation
                    + " expects it to be implemented by "
                    + inherited.implementation());
        }
        if (inherited.implementation().equals(object.getImplementationClass()))
        {
            return object;
        }
        throw new LpcRuntimeException("Internal error - Object manager "
                + manager
                + " thinks "
                + lpc
                + " is implemented by "
                + object
                + " but "
                + _implementation
                + " expected "
                + inherited.implementation());
    }

    public String getImplementationClass()
    {
        return _implementation.getName();
    }

    public Map<String, ClassDefinition> getDefinedClasses()
    {
        // @TODO Support for classes
        return Collections.emptyMap();
    }

    public ObjectInstance getInheritableInstance()
    {
        // @TODO Auto-generated method stub
        return null;
    }

    public Map<String, ? extends ObjectDefinition> getInheritedObjects()
    {
        return Collections.unmodifiableMap(_inherited);
    }

    public ObjectInstance getMasterInstance()
    {
        // @TODO Auto-generated method stub
        return null;
    }

    public Map<String, ? extends CompiledMethodDefinition> getMethods()
    {
        return Collections.unmodifiableMap(_methods);
    }

    public CompiledObjectInstance newInstance()
    {
        // @TODO Auto-generated method stub
        return null;
    }

    public Map<String, ? extends FieldDefinition> getFields()
    {
        return Collections.unmodifiableMap(_fields);
    }

    public String getName()
    {
        return _name;
    }

}
