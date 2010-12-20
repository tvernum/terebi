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

package us.terebi.lang.lpc.runtime.jvm.support;

import java.util.ArrayList;
import java.util.List;

import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isInteger;

import us.terebi.lang.lpc.compiler.util.MathLength;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.ArrayValue;
import us.terebi.lang.lpc.runtime.jvm.value.NilValue;

import static us.terebi.lang.lpc.runtime.jvm.support.ValueSupport.intValue;

/**
 * 
 */
public class BinarySupport
{
    public static LpcValue leftShift(LpcValue left, LpcValue right)
    {
        return intValue(left.asLong() << right.asLong());
    }

    public static LpcValue rightShift(LpcValue left, LpcValue right)
    {
        return intValue(left.asLong() >> right.asLong());
    }

    public static LpcValue binaryOr(LpcValue... values)
    {
        if (values.length == 0)
        {
            return NilValue.INSTANCE;
        }
        if (MiscSupport.isInteger(values[0]))
        {
            long v = 0;
            for (LpcValue value : values)
            {
                v |= value.asLong();
            }
            return intValue(v);
        }
        else if (MiscSupport.isArray(values[0]))
        {
            return union(values);
        }
        else
        {
            throw new LpcRuntimeException("Incompatible type (" + values[0].getActualType() + ") to operator '|'");
        }
    }

    private static LpcValue union(LpcValue... values)
    {
        List<LpcValue> union = new ArrayList<LpcValue>();
        LpcType[] types = new LpcType[values.length];
        int i = 0;

        for (LpcValue value : values)
        {
            types[i++] = value.getActualType();
            List<LpcValue> list = value.asList();
            for (LpcValue element : list)
            {
                if (!union.contains(element))
                {
                    union.add(element);
                }
            }
        }
        return new ArrayValue(MiscSupport.commonType(types), union);
    }

    public static LpcValue binaryAnd(LpcValue... values)
    {
        if (values.length == 0)
        {
            return NilValue.INSTANCE;
        }
        if (isInteger(values[0]))
        {
            long v = 0xFFFFFFFFFFFFFFFFL;
            for (LpcValue value : values)
            {
                v &= value.asLong();
            }
            return intValue(v);
        }
        else if (MiscSupport.isArray(values[0]))
        {
            return intersection(values);
        }
        else
        {
            throw new LpcRuntimeException("Incompatible type (" + values[0].getActualType() + ") to operator '&'");
        }

    }

    private static LpcValue intersection(LpcValue[] values)
    {
        List<LpcValue> intersection = new ArrayList<LpcValue>(values[0].asList());
        for (LpcValue value : values)
        {
            List<LpcValue> list = value.asList();
            intersection.retainAll(list);
        }
        return new ArrayValue(Types.arrayOf(MiscSupport.commonType(intersection)), intersection);
    }

    public static LpcValue xor(MathLength math, LpcValue... values)
    {
        long v = 0;
        boolean first = true;
        for (LpcValue value : values)
        {
            if (first)
            {
                v = value.asLong();
                first = false;
            }
            else
            {
                v ^= value.asLong();
                if (math == MathLength.MATH_32_BIT)
                {
                    v &= 0xFFFFFFFFL;
                }
            }
        }
        return intValue(v);
    }

    public static LpcValue not(LpcValue value, MathLength math)
    {
        long v = value.asLong();
        v = ~v;
        if (math == MathLength.MATH_32_BIT)
        {
            v &= 0xFFFFFFFFL;
        }
        return intValue(v);
    }

}
