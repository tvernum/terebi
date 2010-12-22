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
import java.util.ListIterator;

import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.LpcConstants;
import us.terebi.lang.lpc.runtime.jvm.efun.AbstractEfun;
import us.terebi.lang.lpc.runtime.jvm.support.ValueSupport;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;

import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isArray;
import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isInt;
import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isString;
import static us.terebi.lang.lpc.runtime.jvm.support.ValueSupport.intValue;

/**
 * 
 */
public class MemberArrayEfun extends AbstractEfun implements FunctionSignature, Callable
{
    //    int member_array(mixed item, mixed arr);
    //    int member_array(mixed item, mixed arr, int start);

    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        ArrayList<ArgumentDefinition> list = new ArrayList<ArgumentDefinition>();
        list.add(new ArgumentSpec("item", Types.MIXED));
        list.add(new ArgumentSpec("array", Types.MIXED));
        list.add(new ArgumentSpec("start", Types.INT));
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
        LpcValue item = arguments.get(0);
        LpcValue array = arguments.get(1);
        LpcValue arg3 = arguments.get(2);
        long start = isInt(arg3) ? arg3.asLong() : 0;

        if (item == null)
        {
            throw new NullPointerException("Internal Error - Cannot search for null member");
        }

        if (isString(array))
        {
            if (isInt(item))
            {
                return ValueSupport.intValue(searchString(array.asString(), item.asLong(), start));
            }
        }
        else if (isArray(array))
        {
            return intValue(searchList(array.asList(), item, start));
        }
        return LpcConstants.INT.MINUS_ONE;
    }

    private long searchString(String str, long charValue, long start)
    {
        if (charValue > Character.MAX_VALUE)
        {
            return -1;
        }
        if (start > str.length())
        {
            return -1;
        }
        for (int i = (int) start; i < str.length(); i++)
        {
            if (str.charAt(i) == charValue)
            {
                return i;
            }
        }
        return -1;
    }

    private long searchList(List<LpcValue> list, LpcValue item, long start)
    {
        if (start > list.size())
        {
            return -1;
        }

        ListIterator<LpcValue> iterator = list.listIterator((int) start);
        while (iterator.hasNext())
        {
            if (item.equals(iterator.next()))
            {
                return iterator.previousIndex();
            }
        }
        return -1;
    }
}
