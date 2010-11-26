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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isObject;

import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.LpcConstants;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.ArrayValue;
import us.terebi.lang.lpc.runtime.jvm.value.FloatValue;
import us.terebi.lang.lpc.runtime.jvm.value.IntValue;
import us.terebi.lang.lpc.runtime.jvm.value.MappingValue;
import us.terebi.lang.lpc.runtime.jvm.value.StringValue;

import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isArray;
import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isInteger;
import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isMapping;
import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isNothing;
import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isNumber;
import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isString;
import static us.terebi.lang.lpc.runtime.jvm.support.ValueSupport.intValue;

/**
 * 
 */
public class MathSupport
{
    public static LpcValue negate(LpcValue value)
    {
        LpcType type = value.getActualType();
        if (type.getArrayDepth() != 0)
        {
            throw new LpcRuntimeException("Cannot negate an array (" + value + ")");
        }
        switch (type.getKind())
        {
            case FLOAT:
                return new FloatValue(-value.asDouble());
            case INT:
                {
                    long l = value.asLong();
                    if (l == 0)
                    {
                        return value;
                    }
                    if (l == 1)
                    {
                        return LpcConstants.INT.MINUS_ONE;
                    }
                    if (l == -1)
                    {
                        return LpcConstants.INT.ONE;
                    }
                    return new IntValue(-l);
                }
            case NIL:
                return value;
            default:
                throw new LpcRuntimeException("Cannot negate type " + type + "(" + value + ")");
        }
    }

    public static LpcValue add(LpcValue left, LpcValue right)
    {
        if (left == null || right == null)
        {
            throw new NullPointerException("Internal Error - Attempt to add null value (" + left + "+" + right + ")");
        }
        if (isString(left))
        {
            return new StringValue(left.asString() + right.asString());
        }
        if (isArray(left) && isArray(right))
        {
            return addArrays(left, right);
        }
        if (MiscSupport.isInteger(left) && MiscSupport.isInteger(right))
        {
            return add(left.asLong(), right.asLong());
        }
        if (isNumber(left) && isNumber(right))
        {
            return addDouble(left.asDouble(), right.asDouble());
        }
        if (isMapping(left) && isMapping(right))
        {
            return addMapping(left.asMap(), right.asMap());
        }
        if (isNumber(left) && isString(right))
        {
            return new StringValue(left.asString() + right.asString());
        }
        if (isObject(left) && isString(right))
        {
            return new StringValue(left.asString() + right.asString());
        }
        if (isNothing(left) && isString(right))
        {
            return new StringValue(left.asString() + right.asString());
        }
        if (isNothing(left) && isNumber(right))
        {
            return right;
        }
        throw new UnsupportedOperationException("add - Not implemented for " + left.getActualType() + " + " + right.getActualType());
    }

    private static LpcValue addMapping(Map<LpcValue, LpcValue> left, Map<LpcValue, LpcValue> right)
    {
        Map<LpcValue, LpcValue> sum = new HashMap<LpcValue, LpcValue>(left.size() + right.size());
        sum.putAll(left);
        sum.putAll(right);
        return new MappingValue(sum);
    }

    private static LpcValue addDouble(double left, double right)
    {
        return new FloatValue(left + right);
    }

    private static LpcValue addArrays(LpcValue left, LpcValue right)
    {
        List<LpcValue> leftList = left.asList();
        List<LpcValue> rightList = right.asList();
        List<LpcValue> add = new ArrayList<LpcValue>(leftList.size() + rightList.size());
        add.addAll(leftList);
        add.addAll(rightList);
        LpcType type = MiscSupport.commonType(left.getActualType(), right.getActualType());
        return new ArrayValue(type, add);
    }

    public static LpcValue add(long left, LpcValue right)
    {
        if (Types.INT.equals(right.getActualType()))
        {
            return intValue(left + right.asLong());
        }
        if (Types.FLOAT.equals(right.getActualType()))
        {
            return new FloatValue(left + right.asDouble());
        }
        return add(intValue(left), right);
    }

    public static LpcValue add(LpcValue left, long right)
    {
        if (Types.INT.equals(left.getActualType()))
        {
            return intValue(left.asLong() + right);
        }
        if (Types.FLOAT.equals(left.getActualType()))
        {
            return new FloatValue(left.asDouble() + right);
        }
        return add(left, intValue(right));
    }

    public static LpcValue add(long left, long right)
    {
        return intValue(left + right);
    }

    public static LpcValue multiply(LpcValue left, LpcValue right)
    {
        if (MiscSupport.isInteger(left) && MiscSupport.isInteger(right))
        {
            return intValue(left.asLong() * right.asLong());
        }
        if (isNumber(left) && isNumber(right))
        {
            return new FloatValue(left.asDouble() * right.asDouble());
        }
        throw new UnsupportedOperationException("multiply(" + left.getActualType() + "," + right.getActualType() + ") - Not implemented");
    }

    public static LpcValue divide(LpcValue left, LpcValue right)
    {
        if (MiscSupport.isInteger(left) && MiscSupport.isInteger(right))
        {
            final long divisor = right.asLong();
            if (divisor == 0)
            {
                throw new LpcRuntimeException("Division by zero");
            }
            return intValue(left.asLong() / divisor);
        }
        if (isNumber(left) && isNumber(right))
        {
            return new FloatValue(left.asDouble() / right.asDouble());
        }
        throw new UnsupportedOperationException("divide(" + left.getActualType() + "," + right.getActualType() + ") - Not implemented");
    }

    public static LpcValue subtract(LpcValue left, LpcValue right)
    {
        if (isInteger(left) && isInteger(right))
        {
            return intValue(left.asLong() - right.asLong());
        }
        if (isNumber(left) && isNumber(right))
        {
            return new FloatValue(left.asDouble() - right.asDouble());
        }
        if (isArray(left) && isArray(right))
        {
            List<LpcValue> result = new ArrayList<LpcValue>(left.asList());
            result.removeAll(right.asList());
            return new ArrayValue(left.getActualType(), result);
        }
        throw new UnsupportedOperationException("subtract(" + left.getActualType() + "," + right.getActualType() + ") - Not implemented");
    }

    public static LpcValue modulus(LpcValue left, LpcValue right)
    {
        if (MiscSupport.isInteger(left) && MiscSupport.isInteger(right))
        {
            return intValue(left.asLong() % right.asLong());
        }
        if (isNumber(left) && isNumber(right))
        {
            return new FloatValue(left.asDouble() % right.asDouble());
        }
        throw new UnsupportedOperationException("modulus(" + left.getActualType() + "," + right.getActualType() + ") - Not implemented");
    }
}
