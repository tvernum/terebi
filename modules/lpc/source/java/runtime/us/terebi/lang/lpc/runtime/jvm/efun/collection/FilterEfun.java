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

package us.terebi.lang.lpc.runtime.jvm.efun.collection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.ArgumentSemantics;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.efun.AbstractEfun;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;
import us.terebi.lang.lpc.runtime.jvm.support.MiscSupport;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.ArrayValue;
import us.terebi.lang.lpc.runtime.jvm.value.MappingValue;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;

import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isArray;
import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isMapping;

/**
 * 
 */
public class FilterEfun extends AbstractEfun implements FunctionSignature, Callable
{
    //    mixed filter(mixed x, string fun, object ob, mixed extra, ...);
    //    mixed filter(mixed x, function f, mixed extra, ...);

    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        ArrayList<ArgumentDefinition> list = new ArrayList<ArgumentDefinition>();
        list.add(new ArgumentSpec("collection", Types.MIXED));
        list.add(new ArgumentSpec("func", Types.MIXED));
        list.add(new ArgumentSpec("args", Types.MIXED_ARRAY, true, ArgumentSemantics.BY_VALUE));
        return list;
    }

    public LpcType getReturnType()
    {
        return Types.MIXED;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        checkArguments(arguments);
        LpcValue collection = arguments.get(0);
        if (isMapping(collection))
        {
            return filter_mapping(arguments);
        }
        if (isArray(collection))
        {
            return filter_array(arguments);
        }
        throw new UnsupportedOperationException(getName() + "(" + collection.getActualType() + ", ..) - Not implemented");
    }

    private LpcValue filter_array(List< ? extends LpcValue> arguments)
    {
        LpcValue array = arguments.get(0);
        List<LpcValue> list = array.asList();

        LpcValue func = arguments.get(1);

        List< ? extends LpcValue> args = arguments.get(2).asList();
        LpcValue object = null;
        if (MiscSupport.isString(func) && !args.isEmpty())
        {
            object = args.get(0);
            args = args.subList(1, args.size());
        }

        Callable callable = getFunctionReference(func, object);
        List< ? extends LpcValue> result = filter(list, callable, args);
        return new ArrayValue(array.getActualType(), result);
    }

    private List< ? extends LpcValue> filter(List<LpcValue> list, Callable function, List< ? extends LpcValue> additionalArguments)
    {
        List<LpcValue> result = new ArrayList<LpcValue>();

        List<LpcValue> arguments = new ArrayList<LpcValue>(1 + additionalArguments.size());
        arguments.add(null);
        arguments.addAll(additionalArguments);

        for (LpcValue value : list)
        {
            arguments.set(0, value);

            LpcValue predicate = function.execute(arguments);
            if (predicate.asBoolean())
            {
                result.add(value);
            }
        }
        return result;
    }

    private LpcValue filter_mapping(List< ? extends LpcValue> arguments)
    {
        LpcValue mapping = arguments.get(0);
        Map<LpcValue, LpcValue> map = mapping.asMap();

        LpcValue func = arguments.get(1);
        List< ? extends LpcValue> args = arguments.get(2).asList();

        LpcValue object = null;
        if (MiscSupport.isString(func) && !args.isEmpty())
        {
            object = args.get(0);
            args = args.subList(1, args.size());
        }

        Callable callable = getFunctionReference(func, object);
        Map<LpcValue, LpcValue> result = filter(map, callable, args);
        return new MappingValue(result);
    }

    private Map<LpcValue, LpcValue> filter(Map<LpcValue, LpcValue> map, Callable function, List< ? extends LpcValue> additionalArguments)
    {
        HashMap<LpcValue, LpcValue> result = new HashMap<LpcValue, LpcValue>(map.size());

        List<LpcValue> arguments = new ArrayList<LpcValue>(2 + additionalArguments.size());
        arguments.add(null);
        arguments.add(null);
        arguments.addAll(additionalArguments);

        for (Entry<LpcValue, LpcValue> entry : map.entrySet())
        {
            LpcValue key = entry.getKey();
            LpcValue value = entry.getValue();

            arguments.set(0, key);
            arguments.set(1, value);

            LpcValue predicate = function.execute(arguments);
            if (predicate.asBoolean())
            {
                result.put(key, value);
            }
        }
        return result;
    }

}
