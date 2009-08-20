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

package us.terebi.lang.lpc.runtime.jvm.efun;

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
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.ArrayValue;
import us.terebi.lang.lpc.runtime.jvm.value.MappingValue;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;

import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isArray;
import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isFunction;
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
        list.add(new ArgumentSpec("args", Types.MIXED, true, ArgumentSemantics.BY_VALUE));
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

        if (isFunction(func))
        {
            Callable callable = func.asCallable();
            List< ? extends LpcValue> args = arguments.get(2).asList();
            List< ? extends LpcValue> result = filter(list, callable, args);
            return new ArrayValue(array.getActualType(), result);
        }

        // @TODO
        throw new UnsupportedOperationException(getName() + "(array," + func.getActualType() + ", ..) - Not implemented");
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

        if (isFunction(func))
        {
            Callable callable = func.asCallable();
            List< ? extends LpcValue> args = arguments.get(2).asList();
            Map<LpcValue, LpcValue> result = filter(map, callable, args);
            return new MappingValue(result);

        }
        // @TODO
        throw new UnsupportedOperationException(getName() + "(mapping," + func.getActualType() + ", ..) - Not implemented");
    }

    private Map<LpcValue, LpcValue> filter(Map<LpcValue, LpcValue> map, Callable function,
            List< ? extends LpcValue> additionalArguments)
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
