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

package us.terebi.lang.lpc.runtime.jvm.efun.string;

import java.util.ArrayList;
import java.util.List;

import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.efun.AbstractEfun;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.NilValue;
import us.terebi.lang.lpc.runtime.jvm.value.StringValue;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;

import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isFunction;
import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isNil;
import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isString;

/**
 * 
 */
public class ImplodeEfun extends AbstractEfun implements FunctionSignature, Callable
{
    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        // @TODO, this really has 3 signatures, rather than being "MIXED" and "VARARGS"
        ArrayList<ArgumentDefinition> list = new ArrayList<ArgumentDefinition>();
        list.add(new ArgumentSpec("array", Types.MIXED_ARRAY));
        list.add(new ArgumentSpec("delim", Types.MIXED));
        list.add(new ArgumentSpec("start", Types.MIXED));
        return list;
    }

    public boolean acceptsLessArguments()
    {
        return true;
    }

    public LpcType getReturnType()
    {
        return Types.MIXED;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        checkArguments(arguments, 2);
        List<LpcValue> list = arguments.get(0).asList();
        LpcValue delim = arguments.get(1);
        LpcValue start = null;
        if (arguments.size() > 2)
        {
            start = arguments.get(2);
        }
        return implode(list, delim, start);
    }

    private LpcValue implode(List<LpcValue> list, LpcValue delim, LpcValue start)
    {
        if (isString(delim))
        {
            if (!isNil(start))
            {
                throw new LpcRuntimeException("implode(array,string) does not take a 3rd argument (" + start.debugInfo() + ")");
            }
            return new StringValue(implodeStrings(list, delim.asString()));
        }
        if (isFunction(delim))
        {
            return reduce(list, delim.asCallable(), start);
        }
        return badArgumentType(2, delim.getActualType(), Types.STRING, Types.FUNCTION);
    }

    public static CharSequence implodeStrings(List<LpcValue> array, String delim)
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (LpcValue element : array)
        {
            if (isString(element))
            {
                if (first)
                {
                    first = false;
                }
                else
                {
                    result.append(delim);
                }
                result.append(element.asString());
            }
        }
        return result;
    }

    private LpcValue reduce(List<LpcValue> list, Callable function, LpcValue current)
    {
        if (list.isEmpty())
        {
            return NilValue.INSTANCE;
        }
        for (LpcValue value : list)
        {
            if(isNil(current))
            {
                current = value;
            }
            else
            {
                current = function.execute(current, value);
            }

        }
        return current;
    }

}
