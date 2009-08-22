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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import us.terebi.lang.lpc.compiler.java.context.CompiledObjectDefinition;
import us.terebi.lang.lpc.compiler.java.context.CompiledObjectInstance;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.ClassDefinition;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.LpcType.Kind;
import us.terebi.lang.lpc.runtime.MemberDefinition.Modifier;
import us.terebi.lang.lpc.runtime.jvm.context.Efuns;
import us.terebi.lang.lpc.runtime.jvm.context.SystemContext;
import us.terebi.lang.lpc.runtime.jvm.context.RuntimeContext;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;
import us.terebi.lang.lpc.runtime.jvm.support.MiscSupport;
import us.terebi.lang.lpc.runtime.jvm.support.ValueSupport;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.ArrayValue;
import us.terebi.lang.lpc.runtime.jvm.value.ClassReference;
import us.terebi.lang.lpc.runtime.jvm.value.FloatValue;
import us.terebi.lang.lpc.runtime.jvm.value.MappingValue;
import us.terebi.lang.lpc.runtime.jvm.value.NilValue;
import us.terebi.lang.lpc.runtime.jvm.value.StringValue;
import us.terebi.lang.lpc.runtime.jvm.value.VoidValue;
import us.terebi.lang.lpc.runtime.util.BoundMethod;
import us.terebi.lang.lpc.runtime.util.type.DynamicClassDefinition;

/**
 * 
 */
public class LpcObject
{
    private CompiledObjectDefinition _definition;
    private CompiledObjectInstance _instance;

    public void setDefinition(CompiledObjectDefinition definition)
    {
        _definition = definition;
    }

    public CompiledObjectDefinition getObjectDefinition()
    {
        return _definition;
    }

    public void setInstance(CompiledObjectInstance instance)
    {
        _instance = instance;
    }

    public CompiledObjectInstance getObjectInstance()
    {
        return _instance;
    }

    protected LpcValue makeValue(String value)
    {
        return new StringValue(value);
    }

    protected LpcValue makeValue(long value)
    {
        return ValueSupport.intValue(value);
    }

    protected LpcValue makeValue(double value)
    {
        return new FloatValue(value);
    }

    protected LpcValue makeValue()
    {
        return VoidValue.INSTANCE;
    }

    protected Callable efun(String name)
    {
        SystemContext context = RuntimeContext.obtain().system();
        Efuns functions = context.efuns();
        return functions.getImplementation(name);
    }

    protected Callable simul_efun(String name)
    {
        SystemContext context = RuntimeContext.obtain().system();
        ObjectInstance sefun = context.objectManager().getSimulatedEfunObject();
        return new BoundMethod(name, sefun);
    }

    protected static LpcType withType(Kind kind, int depth)
    {
        return Types.getType(kind, null, depth);
    }

    protected static LpcType withType(ClassDefinition cls, int depth)
    {
        return Types.getType(Kind.CLASS, cls, depth);
    }

    protected static DynamicClassDefinition createClass(String name, Set< ? extends Modifier> modifiers)
    {
        return new DynamicClassDefinition(name, modifiers);
    }

    protected static Set<Modifier> withModifiers(Modifier... modifiers)
    {
        return new HashSet<Modifier>(Arrays.asList(modifiers));
    }

    public MappingValue makeMapping(LpcValue... elements)
    {
        if ((elements.length % 2) != 0)
        {
            throw new IllegalArgumentException("makeMapping requires an even number of value elements");
        }
        Map<LpcValue, LpcValue> mapping = new HashMap<LpcValue, LpcValue>(elements.length / 2);
        for (int i = 0; i < elements.length; i += 2)
        {
            mapping.put(elements[i], elements[i + 1]);
        }
        return new MappingValue(mapping);
    }

    public ArrayValue makeArray(LpcValue... elements)
    {
        if (elements.length == 0)
        {
            return new ArrayValue(Types.MIXED_ARRAY, new ArrayList<LpcValue>());
        }

        List<LpcValue> list = new ArrayList<LpcValue>(Arrays.asList(elements));
        Set<LpcType> types = new HashSet<LpcType>();
        for (LpcValue lpcValue : elements)
        {
            types.add(lpcValue.getActualType());
        }

        if (types.size() == 1)
        {
            return new ArrayValue(Types.arrayOf(types.iterator().next()), list);
        }

        LpcType[] typeArray = types.toArray(new LpcType[types.size()]);
        return new ArrayValue(Types.arrayOf(MiscSupport.commonType(typeArray)), list);
    }

    public NilValue nil()
    {
        return NilValue.INSTANCE;
    }

    protected LpcValue call(Callable callable, Collection< ? extends LpcValue>... args)
    {
        List<LpcValue> arguments = new ArrayList<LpcValue>(args.length + 10);
        for (Collection< ? extends LpcValue> arg : args)
        {
            arguments.addAll(arg);
        }
        return callable.execute(arguments);
    }

    protected LpcValue classReference(Class< ? extends LpcClass> cls)
    {
        return new ClassReference(classDefinition(cls));
    }

    protected ClassDefinition classDefinition(Class< ? extends LpcClass> cls)
    {
        // @TODO Cache these...
        try
        {
            LpcClass newInstance = cls.newInstance();
            return newInstance.getClassDefinition();
        }
        catch (Exception e)
        {
            throw new LpcRuntimeException("Internal Error - Cannot instantiate " + cls);
        }
    }
}
