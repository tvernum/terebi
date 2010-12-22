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
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.efun.AbstractEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.ThisObjectEfun;
import us.terebi.lang.lpc.runtime.jvm.support.MiscSupport;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.MappingValue;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;

import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isFunction;

/**
 * 
 */
public class MapMappingEfun extends AbstractEfun implements FunctionSignature, Callable
{
    //    mapping map_mapping( mapping map, string fun, object ob, ... );
    //    mapping map_mapping( mapping map, function f, ... );

    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        ArrayList<ArgumentDefinition> list = new ArrayList<ArgumentDefinition>();
        list.add(new ArgumentSpec("map", Types.MAPPING));
        list.add(new ArgumentSpec("func", Types.MIXED));
        list.add(new ArgumentSpec("args", Types.MIXED_ARRAY, true, ArgumentSemantics.BY_VALUE));
        return list;
    }

    public LpcType getReturnType()
    {
        return Types.MAPPING;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        checkArguments(arguments);

        LpcValue mapping = arguments.get(0);
        Map<LpcValue, LpcValue> map = mapping.asMap();

        LpcValue func = arguments.get(1);
        List< ? extends LpcValue> args = arguments.get(2).asList();

        Callable callable;
        if (isFunction(func))
        {
            callable = func.asCallable();
        }
        else if (MiscSupport.isString(func))
        {
            ObjectInstance object;
            if (args.isEmpty())
            {
                object = ThisObjectEfun.this_object();
            }
            else
            {
                object = args.get(0).asObject();
            }
            callable = super.getFunctionReference(func.asString(), object);
            args = args.subList(1, args.size());
        }
        else
        {
            throw new UnsupportedOperationException(getName() + "(mapping," + func.getActualType() + ", ..) - Not implemented");
        }
        Map<LpcValue, LpcValue> result = map_function(map, callable, args);
        return new MappingValue(result);
    }

    private Map<LpcValue, LpcValue> map_function(Map<LpcValue, LpcValue> map, Callable function, List< ? extends LpcValue> additionalArguments)
    {
        HashMap<LpcValue, LpcValue> result = new HashMap<LpcValue, LpcValue>(map.size());

        List<LpcValue> arguments = new ArrayList<LpcValue>(2 + additionalArguments.size());
        arguments.add(null);
        arguments.add(null);
        arguments.addAll(additionalArguments);

        for (Entry<LpcValue, LpcValue> entry : map.entrySet())
        {
            LpcValue key = entry.getKey();
            arguments.set(0, key);
            arguments.set(1, entry.getValue());
            LpcValue value = function.execute(arguments);
            result.put(key, value);
        }
        return result;
    }

}
