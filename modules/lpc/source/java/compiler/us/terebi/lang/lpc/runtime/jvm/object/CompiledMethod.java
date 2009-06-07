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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import us.terebi.lang.lpc.compiler.java.context.CompiledObjectDefinition;
import us.terebi.lang.lpc.compiler.java.context.CompiledObjectInstance;
import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.ClassDefinition;
import us.terebi.lang.lpc.runtime.CompiledMethodDefinition;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.LpcType.Kind;
import us.terebi.lang.lpc.runtime.jvm.LpcMethod;
import us.terebi.lang.lpc.runtime.jvm.LpcParameter;
import us.terebi.lang.lpc.runtime.jvm.LpcReturn;
import us.terebi.lang.lpc.runtime.jvm.context.ScopeLookup;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;
import us.terebi.lang.lpc.runtime.util.BoundMethod;
import us.terebi.lang.lpc.runtime.util.Signature;
import us.terebi.util.AnnotationUtil;

/**
 * 
 */
public class CompiledMethod implements CompiledMethodDefinition
{
    private final CompiledObjectDefinition _objectDefinition;
    private final String _name;
    private final Method _method;
    private final FunctionSignature _signature;
    private final Set< ? extends Modifier> _modifiers;
    private final ScopeLookup _lookup;

    public CompiledMethod(CompiledObjectDefinition object, Method method, ScopeLookup lookup)
    {
        _objectDefinition = object;
        _method = method;
        _name = resolveName(method);
        _signature = resolveSignature(method);
        _modifiers = resolveModifiers(method);
        _lookup = lookup;
    }

    private FunctionSignature resolveSignature(Method method)
    {
        if (!isLpcValue(method))
        {
            throw new LpcRuntimeException("Method " + method + " should return " + LpcValue.class);
        }
        LpcReturn returnAnnotation = method.getAnnotation(LpcReturn.class);
        if (returnAnnotation == null)
        {
            throw new LpcRuntimeException("Method "
                    + method
                    + " is not annotated with a return value ("
                    + LpcReturn.class.getSimpleName()
                    + ")");
        }
        LpcType returnType = getType(returnAnnotation.kind(), returnAnnotation.className(), returnAnnotation.depth());

        ArgumentDefinition[] arguments = new ArgumentDefinition[method.getParameterTypes().length];
        for (int i = 0; i < arguments.length; i++)
        {
            LpcParameter parameterAnnotation = AnnotationUtil.findAnnotation(LpcParameter.class,
                    method.getParameterAnnotations()[i]);
            LpcType type = getType(parameterAnnotation.kind(), parameterAnnotation.className(), parameterAnnotation.depth());
            arguments[i] = new ArgumentSpec(parameterAnnotation.name(), type, parameterAnnotation.semantics(),
                    parameterAnnotation.varargs());
        }

        LpcMethod methodAnnotation = method.getAnnotation(LpcMethod.class);
        boolean varargs = Arrays.asList(methodAnnotation.modifiers()).contains(Modifier.VARARGS);

        return new Signature(varargs, returnType, Arrays.asList(arguments));
    }

    private boolean isLpcValue(Method method)
    {
        return LpcValue.class.isAssignableFrom(method.getReturnType());
    }

    private Set< ? extends Modifier> resolveModifiers(Method method)
    {
        LpcMethod annotation = method.getAnnotation(LpcMethod.class);
        Modifier[] modifiers = annotation.modifiers();
        return new HashSet<Modifier>(Arrays.asList(modifiers));
    }

    private String resolveName(Method method)
    {
        LpcMethod annotation = method.getAnnotation(LpcMethod.class);
        return annotation.name();
    }

    private LpcType getType(Kind kind, String className, int depth)
    {
        ClassDefinition classDef = findClass(className);
        LpcType t = Types.getType(kind, classDef, depth);
        return t;
    }

    private ClassDefinition findClass(String name)
    {
        if (name == null || name.length() == 0)
        {
            return null;
        }
        return _lookup.classes().findClass(name);
    }

    public LpcValue execute(ObjectInstance instance, List< ? extends LpcValue> arguments)
    {
        if (instance instanceof CompiledObjectInstance)
        {
            CompiledObjectInstance i = (CompiledObjectInstance) instance;
            return executeMethod(i, arguments);
        }
        else
        {
            throw new IllegalArgumentException("Object instance "
                    + instance
                    + " is not a "
                    + CompiledObjectInstance.class.getSimpleName());
        }
    }

    private LpcValue executeMethod(CompiledObjectInstance instance, List< ? extends LpcValue> arguments)
    {
        Object object = instance.getImplementingObject();
        if (arguments.size() != _method.getParameterTypes().length)
        {
            throw new IllegalArgumentException("Wrong argument count to "
                    + this
                    + " (expected "
                    + _method.getParameterTypes().length
                    + ", got "
                    + arguments.size()
                    + ") - [varargs not implemented yet]");
        }
        try
        {
            Object result = _method.invoke(object, arguments.toArray());
            if (result instanceof LpcValue)
            {
                return (LpcValue) result;
            }
            throw new IllegalStateException("Method " + _method + " did not return an LpcValue - returned " + result + " instead");
        }
        catch (IllegalAccessException e)
        {
            throw new LpcRuntimeException("During method " + _method + " - " + e.getMessage(), e);
        }
        catch (InvocationTargetException e)
        {
            throw new LpcRuntimeException("During method " + _method + " - " + e.getMessage(), e);
        }
    }

    public CompiledObjectDefinition getDeclaringType()
    {
        return _objectDefinition;
    }

    public Callable getFunction(ObjectInstance instance)
    {
        return new BoundMethod(this, instance);
    }

    public FunctionSignature getSignature()
    {
        return _signature;
    }

    public Set< ? extends Modifier> getModifiers()
    {
        return _modifiers;
    }

    public String getName()
    {
        return _name;
    }

    public String getInternalName()
    {
        return _method.getName();
    }
}
