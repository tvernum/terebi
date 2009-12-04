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

import static us.terebi.lang.lpc.runtime.jvm.support.ValueSupport.intValue;

import us.terebi.lang.lpc.compiler.util.MathLength;
import us.terebi.lang.lpc.runtime.LpcValue;

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
        long v = 0;
        for (LpcValue value : values)
        {
            v |= value.asLong();
        }
        return intValue(v);
    }

    public static LpcValue binaryAnd(LpcValue... values)
    {
        long v = 0xFFFFFFFFFFFFFFFFL;
        for (LpcValue value : values)
        {
            v &= value.asLong();
        }
        return intValue(v);
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
