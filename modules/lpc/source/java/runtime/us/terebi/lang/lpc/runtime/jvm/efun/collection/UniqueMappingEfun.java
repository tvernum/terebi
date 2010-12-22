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

import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.efun.AbstractEfun;
import us.terebi.lang.lpc.runtime.jvm.support.MiscSupport;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.ArrayValue;
import us.terebi.lang.lpc.runtime.jvm.value.MappingValue;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;

import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isFunction;

/**
 * 
 */
public class UniqueMappingEfun extends AbstractEfun implements FunctionSignature, Callable
{
    //    mapping unique_mapping(array arr, string func, object obj, mixed ... extra_args);
    //    mapping unique_mapping(array arr, function func, mixed ... extra_args);

    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        ArrayList<ArgumentDefinition> list = new ArrayList<ArgumentDefinition>();
        list.add(new ArgumentSpec("array", Types.MIXED_ARRAY));
        list.add(new ArgumentSpec("func", Types.MIXED));
        list.add(new ArgumentSpec("args", Types.MIXED_ARRAY, true));
        return list;
    }

    public boolean acceptsLessArguments()
    {
        return false;
    }

    public LpcType getReturnType()
    {
        return Types.MAPPING;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        checkArguments(arguments, 2);
        LpcValue array = arguments.get(0);
        LpcValue func = arguments.get(1);
        List< ? extends LpcValue> extraArguments = arguments.get(2).asList();

        Callable callable;
        if (isFunction(func))
        {
            callable = func.asCallable();
        }
        else if (MiscSupport.isString(func))
        {
            callable = super.getFunctionReference(func, extraArguments.get(0));
            extraArguments = extraArguments.subList(1, extraArguments.size());
        }
        else
        {
            throw new UnsupportedOperationException(getName() + "(array, " + func.getActualType() + ",..) - Not implemented");
        }
        
        return new MappingValue(unique(array, callable, extraArguments));

    }

    private Map<LpcValue, LpcValue> unique(LpcValue array, Callable function, List< ? extends LpcValue> extraArguments)
    {
        List<LpcValue> callArguments = new ArrayList<LpcValue>(extraArguments.size());
        callArguments.add(null);
        callArguments.addAll(extraArguments);
        
        Map<LpcValue, LpcValue> buckets = new HashMap<LpcValue, LpcValue>();
        for (LpcValue value : array.asList())
        {
            callArguments.set(0, value);
            LpcValue key = function.execute(callArguments);

            LpcValue bucket = buckets.get(key);
            if (bucket == null)
            {
                bucket = new ArrayValue(array.getActualType(), new ArrayList<LpcValue>());
                buckets.put(key, bucket);
            }

            bucket.asList().add(value);
        }
        return buckets;
    }
}
