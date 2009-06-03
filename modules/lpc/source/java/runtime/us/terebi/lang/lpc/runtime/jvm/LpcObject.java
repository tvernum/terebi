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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import us.terebi.lang.lpc.compiler.java.context.CompiledObjectDefinition;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.ClassDefinition;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.LpcType.Kind;
import us.terebi.lang.lpc.runtime.MemberDefinition.Modifier;
import us.terebi.lang.lpc.runtime.jvm.context.Functions;
import us.terebi.lang.lpc.runtime.jvm.context.RuntimeContext;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;
import us.terebi.lang.lpc.runtime.jvm.type.MappingValue;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.ArrayValue;
import us.terebi.lang.lpc.runtime.jvm.value.FloatValue;
import us.terebi.lang.lpc.runtime.jvm.value.IntValue;
import us.terebi.lang.lpc.runtime.jvm.value.NilValue;
import us.terebi.lang.lpc.runtime.jvm.value.StringValue;
import us.terebi.lang.lpc.runtime.jvm.value.VoidValue;

/**
 * 
 */
public class LpcObject
{
    private CompiledObjectDefinition _definition;

    public void setDefinition(CompiledObjectDefinition definition)
    {
        _definition = definition;
    }

    protected LpcValue makeValue(String value)
    {
        return new StringValue(value);
    }

    protected LpcValue makeValue(long value)
    {
        return new IntValue(value);
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
        Functions functions = RuntimeContext.get().functions();
        return functions.efun(name);
    }

    protected <T> InheritedObject<T> loadInherited(Class< ? extends T> type)
    {
        try
        {
            return new InheritedObject<T>(type);
        }
        catch (Exception e)
        {
            throw new LpcRuntimeException("Cannot load inherited type " + type, e);
        }
    }

    protected LpcType withType(Kind kind, int depth)
    {
        return Types.getType(kind, null, depth);
    }

    protected LpcType withType(ClassDefinition cls, int depth)
    {
        return Types.getType(Kind.CLASS, cls, depth);
    }

    protected Set<Modifier> withModifiers(Modifier... modifiers)
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

        // @TODO, if all elements of "types" are arrays, then we can be more specific about the type of the new array
        return new ArrayValue(Types.MIXED_ARRAY, list);
    }

    public NilValue nil()
    {
        return NilValue.INSTANCE;
    }

    protected LpcValue call(Callable callable, Iterable< ? extends LpcValue>... args)
    {
        // @TODO
        return nil();
    }
}
