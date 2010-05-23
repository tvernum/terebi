/* ------------------------------------------------------------------------
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

import us.terebi.lang.lpc.compiler.java.context.ClassFinder;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.ClassDefinition;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.UserTypeDefinition;
import us.terebi.lang.lpc.runtime.LpcType.Kind;
import us.terebi.lang.lpc.runtime.MemberDefinition.Modifier;
import us.terebi.lang.lpc.runtime.jvm.context.Efuns;
import us.terebi.lang.lpc.runtime.jvm.context.RuntimeContext;
import us.terebi.lang.lpc.runtime.jvm.context.SystemContext;
import us.terebi.lang.lpc.runtime.jvm.context.CallStack.Origin;
import us.terebi.lang.lpc.runtime.jvm.exception.InternalError;
import us.terebi.lang.lpc.runtime.jvm.support.ValueSupport;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.ArrayValue;
import us.terebi.lang.lpc.runtime.jvm.value.FloatValue;
import us.terebi.lang.lpc.runtime.jvm.value.MappingValue;
import us.terebi.lang.lpc.runtime.jvm.value.NilValue;
import us.terebi.lang.lpc.runtime.jvm.value.StringValue;
import us.terebi.lang.lpc.runtime.jvm.value.VoidValue;
import us.terebi.lang.lpc.runtime.util.BoundMethod;
import us.terebi.lang.lpc.runtime.util.StackCall;
import us.terebi.lang.lpc.runtime.util.type.DynamicClassDefinition;

/**
 * 
 */
public class LpcRuntimeSupport
{
    public LpcRuntimeSupport()
    {
        super();
    }

    public LpcValue makeValue(String value)
    {
        return new StringValue(value);
    }

    public LpcValue makeValue(long value)
    {
        return ValueSupport.intValue(value);
    }

    public LpcValue makeValue(int value)
    {
        return ValueSupport.intValue(value);
    }

    public LpcValue makeValue(double value)
    {
        return new FloatValue(value);
    }

    public LpcValue makeValue(Number value)
    {
        if ((value instanceof Float) || (value instanceof Double))
        {
            return makeValue(value.doubleValue());
        }
        else
        {
            return makeValue(value.longValue());
        }
    }

    public LpcValue makeValue()
    {
        return VoidValue.INSTANCE;
    }

    public Callable efun(String name)
    {
        SystemContext context = RuntimeContext.obtain().system();
        Efuns functions = context.efuns();
        Callable efun = functions.getImplementation(name);
        if (efun == null)
        {
            throw new InternalError("Cannot access efun " + name);
        }
        return efun;
    }

    public Callable simul_efun(String name)
    {
        SystemContext context = RuntimeContext.obtain().system();
        ObjectInstance sefun = context.objectManager().getSimulatedEfunObject();
        return new StackCall(new BoundMethod(name, sefun), Origin.SIMUL);
    }

    public static LpcType withType(Kind kind, int depth)
    {
        return Types.getType(kind, null, depth);
    }

    public static LpcType withType(ClassDefinition cls, int depth)
    {
        return Types.getType(Kind.CLASS, cls, depth);
    }

    public static LpcType withType(UserTypeDefinition declaring, Kind kind, String className, int depth)
    {
        ClassDefinition classDefinition = null;
        if (className != null)
        {
            classDefinition = new ClassFinder(declaring).find(className);
        }
        return Types.getType(kind, classDefinition, depth);
    }

    public static DynamicClassDefinition createClass(String name)
    {
        return new DynamicClassDefinition(name);
    }

    public static Set<Modifier> withModifiers(Modifier... modifiers)
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
        return ValueSupport.arrayValue(elements);
    }
    
    public ArrayValue makeArray(List<LpcValue>... elements)
    {
        List<LpcValue> array = new ArrayList<LpcValue>(elements.length * 3);
        for (List<LpcValue> element : elements)
        {
            array.addAll(element);
        }
        return ValueSupport.arrayValue(array);
    }

    public NilValue nil()
    {
        return NilValue.INSTANCE;
    }

    public LpcValue call(Callable callable, Collection< ? extends LpcValue>... args)
    {
        List<LpcValue> arguments = new ArrayList<LpcValue>(args.length + 10);
        for (Collection< ? extends LpcValue> arg : args)
        {
            arguments.addAll(arg);
        }
        return callable.execute(arguments);
    }
}
