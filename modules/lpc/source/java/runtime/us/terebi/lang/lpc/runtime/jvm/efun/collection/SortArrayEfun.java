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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections.comparators.ReverseComparator;

import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.ArgumentSemantics;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.efun.AbstractEfun;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;
import us.terebi.lang.lpc.runtime.jvm.support.CallOtherComparator;
import us.terebi.lang.lpc.runtime.jvm.support.CallableComparator;
import us.terebi.lang.lpc.runtime.jvm.support.LpcValueComparator;
import us.terebi.lang.lpc.runtime.jvm.support.MiscSupport;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.ArrayValue;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;

import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isFunction;
import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isInteger;
import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isString;

/**
 * 
 */
public class SortArrayEfun extends AbstractEfun implements FunctionSignature, Callable
{
    //  array sort_array( array arr, string fun, object ob, ... );
    //  array sort_array( array arr, function f, ... );
    //  array sort_array( array arr, int direction );    
    //  
    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        ArrayList<ArgumentDefinition> list = new ArrayList<ArgumentDefinition>();
        list.add(new ArgumentSpec("array", Types.MIXED_ARRAY));
        list.add(new ArgumentSpec("func", Types.MIXED));
        list.add(new ArgumentSpec("args", Types.MIXED_ARRAY, true, ArgumentSemantics.BY_VALUE));
        return list;
    }

    public LpcType getReturnType()
    {
        return Types.MIXED_ARRAY;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        checkArguments(arguments);
        LpcValue array = arguments.get(0);
        LpcValue arg = arguments.get(1);

        LpcValue otherArgs = arguments.get(2);
        List<LpcValue> argList = MiscSupport.isArray(otherArgs) ? arguments.get(2).asList() : Collections.<LpcValue> emptyList();
        List<LpcValue> sorted = sort(array.asList(), arg, argList);

        return new ArrayValue(array.getActualType(), sorted);
    }

    private List<LpcValue> sort(List<LpcValue> list, LpcValue arg0, List<LpcValue> args)
    {
        if (isInteger(arg0))
        {
            return sort(list, arg0.asLong());
        }
        if (isFunction(arg0))
        {
            return sort(list, arg0.asCallable(), args);
        }
        if (isString(arg0))
        {
            if (args.isEmpty())
            {
                return sort(list, arg0.asString());
            }
            else
            {
                return sort(list, arg0.asString(), args.get(0).asObject(), args.subList(1, args.size()));
            }
        }
        throw new LpcRuntimeException("Argument 1 to " + getName() + " must be one of int|function|string");
    }

    private List<LpcValue> sort(@SuppressWarnings("unused")
    List<LpcValue> list, @SuppressWarnings("unused")
    String func)
    {
        throw new UnsupportedOperationException("sort by name on this_object() - Not implemented");
    }

    private List<LpcValue> sort(List<LpcValue> list, String func, ObjectInstance object, List<LpcValue> args)
    {
        return sortList(list, new CallOtherComparator(object, func, args));
    }

    private List<LpcValue> sort(List<LpcValue> list, Callable callable, List<LpcValue> args)
    {
        return sortList(list, new CallableComparator(callable, args));
    }

    @SuppressWarnings("unchecked")
    private List<LpcValue> sort(List<LpcValue> list, long direction)
    {
        Comparator<LpcValue> comparator = new LpcValueComparator();
        if (direction == -1)
        {
            comparator = new ReverseComparator(comparator);
        }
        return sortList(list, comparator);
    }

    private List<LpcValue> sortList(List<LpcValue> list, Comparator<LpcValue> comparator)
    {
        List<LpcValue> sorted = new ArrayList<LpcValue>(list);
        Collections.sort(sorted, comparator);
        return sorted;
    }
}
