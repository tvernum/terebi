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
import java.util.List;

import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.ArgumentSemantics;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.efun.AbstractEfun;
import us.terebi.lang.lpc.runtime.jvm.support.MiscSupport;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.ArrayValue;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;

import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isFunction;

/**
 * 
 */
public class MapArrayEfun extends AbstractEfun implements FunctionSignature, Callable
{
    //    array map_array( array arr, string fun, object ob, mixed extra, ... );
    //    array map_array( array arr, function f, mixed extra, ... );;

    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        ArrayList<ArgumentDefinition> list = new ArrayList<ArgumentDefinition>();
        list.add(new ArgumentSpec("arr", Types.MIXED_ARRAY));
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
        List<LpcValue> list = array.asList();

        LpcValue func = arguments.get(1);

        List< ? extends LpcValue> args = arguments.get(2).asList();

        Callable callable;
        if (isFunction(func))
        {
            callable = func.asCallable();
        }
        else if (MiscSupport.isString(func))
        {
            callable = this.getFunctionReference(func.asString(), getArgument(args, 0).asObject());
            args = args.subList(1, args.size());
        }
        else
        {
            throw new UnsupportedOperationException(getName() + "(array," + func.getActualType() + ", ..) - Not implemented");
        }
        List<LpcValue> result = map_array(list, callable, args);
        return new ArrayValue(Types.arrayOf(MiscSupport.commonType(result)), result);
    }

    private List<LpcValue> map_array(List<LpcValue> list, Callable function, List< ? extends LpcValue> additionalArguments)
    {
        List<LpcValue> result = new ArrayList<LpcValue>(list.size());

        List<LpcValue> arguments = new ArrayList<LpcValue>(1 + additionalArguments.size());
        arguments.add(null);
        arguments.addAll(additionalArguments);

        for (LpcValue value : list)
        {
            arguments.set(0, value);
            value = function.execute(arguments);
            result.add(value);
        }
        return result;
    }

}
