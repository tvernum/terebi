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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.efun.AbstractEfun;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.ArrayValue;
import us.terebi.lang.lpc.runtime.jvm.value.VoidValue;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;

import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isFunction;

/**
 * 
 */
public class UniqueArrayEfun extends AbstractEfun implements FunctionSignature, Callable
{
    //    array unique_array(object array obarr, string separator);
    //    array unique_array(object array obarr, string separator, mixed skip);
    //
    //    array unique_array(array arr, function f);
    //    array unique_array(array arr, function f, mixed skip);

    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        ArrayList<ArgumentDefinition> list = new ArrayList<ArgumentDefinition>();
        list.add(new ArgumentSpec("array", Types.MIXED_ARRAY));
        list.add(new ArgumentSpec("func", Types.MIXED));
        list.add(new ArgumentSpec("skip", Types.MIXED));
        return list;
    }

    public boolean acceptsLessArguments()
    {
        return true;
    }

    public LpcType getReturnType()
    {
        return Types.arrayOf(Types.MIXED_ARRAY);
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        checkArguments(arguments, 2);
        LpcValue array = arguments.get(0);
        LpcValue func = arguments.get(1);
        LpcValue skip = VoidValue.INSTANCE;
        if (hasArgument(arguments, 2))
        {
            skip = arguments.get(2);
        }

        if (isFunction(func))
        {
            return new ArrayValue(Types.arrayOf(array.getActualType()), unique(array, func.asCallable(), skip));
        }
        throw new UnsupportedOperationException(getName() + "(array, " + func.getActualType() + ",..) - Not implemented");

    }

    private Collection< ? extends LpcValue> unique(LpcValue array, Callable function, LpcValue skip)
    {
        Map<LpcValue, ArrayValue> buckets = new HashMap<LpcValue, ArrayValue>();
        for (LpcValue value : array.asList())
        {
            LpcValue token = function.execute(value);
            if (skip.equals(token))
            {
                continue;
            }

            ArrayValue bucket = buckets.get(token);
            if (bucket == null)
            {
                bucket = new ArrayValue(array.getActualType(), new ArrayList<LpcValue>());
                buckets.put(token, bucket);
            }
            
            bucket.asList().add(value);
        }
        return buckets.values();
    }
}
