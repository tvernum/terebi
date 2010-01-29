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

import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isNumber;

import us.terebi.lang.lpc.runtime.ByteSequence;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.LpcConstants;

/**
 * 
 */
public class ComparisonSupport
{
    private static LpcValue getBoolean(boolean bool)
    {
        return (bool ? LpcConstants.INT.TRUE : LpcConstants.INT.FALSE);
    }

    public static LpcValue notEqual(LpcValue left, LpcValue right)
    {
        nullCheck(left, right);
        return getBoolean(!left.equals(right));
    }

    public static LpcValue equal(LpcValue left, LpcValue right)
    {
        nullCheck(left, right);
        return getBoolean(left.equals(right));
    }

    public static LpcValue greaterThan(LpcValue left, LpcValue right)
    {
        return getBoolean(compare(left, right) > 0);
    }

    public static LpcValue greaterThanOrEqual(LpcValue left, LpcValue right)
    {
        return getBoolean(compare(left, right) >= 0);
    }

    public static LpcValue lessThan(LpcValue left, LpcValue right)
    {
        return getBoolean(compare(left, right) < 0);
    }

    public static LpcValue lessThanOrEqual(LpcValue left, LpcValue right)
    {
        return getBoolean(compare(left, right) <= 0);
    }

    public static int compare(LpcValue left, LpcValue right)
    {
        nullCheck(left, right);

        if (left.equals(right))
        {
            return 0;
        }

        LpcType leftType = left.getActualType();
        LpcType rightType = right.getActualType();

        int leftArray = leftType.getArrayDepth();
        int rightArray = rightType.getArrayDepth();

        int cmp;
        if (leftArray > 0 && rightArray > 0)
        {
            cmp = cmp(leftArray, rightArray);
            if (cmp != 0)
            {
                return cmp;
            }
            return compare(left.asList(), right.asList());
        }
        cmp = cmp(leftArray, rightArray);
        if (cmp != 0)
        {
            return cmp;
        }

        if (isNumber(leftType) && isNumber(rightType))
        {
            double leftDouble = left.asDouble();
            double rightDouble = right.asDouble();
            if (leftDouble > rightDouble)
            {
                return 1;
            }
            if (leftDouble < rightDouble)
            {
                return -1;
            }
            return 0;
        }
        else if (leftType.getKind() == LpcType.Kind.STRING)
        {
            if (rightType.getKind() == LpcType.Kind.STRING)
            {
                String leftStr = left.asString();
                String rightStr = right.asString();
                return leftStr.compareTo(rightStr);
            }
            else if (rightType.getKind() == LpcType.Kind.BUFFER)
            {
                ByteSequence leftBytes = left.asBuffer();
                ByteSequence rightBytes = right.asBuffer();
                return leftBytes.compareTo(rightBytes);
            }
        }
        else if (leftType.getKind() == LpcType.Kind.BUFFER)
        {
            if (rightType.getKind() == LpcType.Kind.BUFFER || rightType.getKind() == LpcType.Kind.STRING)
            {
                ByteSequence leftBytes = left.asBuffer();
                ByteSequence rightBytes = right.asBuffer();
                return leftBytes.compareTo(rightBytes);
            }
        }
        else if (leftType.getKind() == LpcType.Kind.MAPPING)
        {
            if (rightType.getKind() == LpcType.Kind.MAPPING)
            {
                Map<LpcValue, LpcValue> leftMap = left.asMap();
                Map<LpcValue, LpcValue> rightMap = right.asMap();
                if (leftMap.equals(rightMap))
                {
                    return 0;
                }
                cmp = cmp(leftMap.size(), rightMap.size());
                if (cmp != 0)
                {
                    return cmp;
                }
                TreeSet<LpcValue> keys = new TreeSet<LpcValue>(leftMap.keySet());
                cmp = compare(keys, new TreeSet<LpcValue>(rightMap.keySet()));
                if (cmp != 0)
                {
                    return cmp;
                }
                cmp = compare(new TreeSet<LpcValue>(leftMap.values()), new TreeSet<LpcValue>(rightMap.values()));
                if (cmp != 0)
                {
                    return cmp;
                }
                for (LpcValue key : keys)
                {
                    cmp = compare(leftMap.get(key), rightMap.get(key));
                    if (cmp != 0)
                    {
                        return cmp;
                    }
                }
                return 0;
            }
        }
        cmp = cmp(leftType.getKind().ordinal(), rightType.getKind().ordinal());
        if (cmp != 0)
        {
            return cmp;
        }

        cmp = cmp(left.hashCode(), right.hashCode());
        if (cmp != 0)
        {
            return cmp;
        }

        return cmp(System.identityHashCode(left), System.identityHashCode(right));
    }

    private static void nullCheck(LpcValue... values)
    {
        for (LpcValue value : values)
        {
            if (value == null)
            {
                throw new NullPointerException("Internal Error - null value passed to " + ComparisonSupport.class.getSimpleName());
            }
        }
    }

    private static int compare(Iterable<LpcValue> leftList, Iterable<LpcValue> rightList)
    {
        Iterator<LpcValue> leftItr = leftList.iterator();
        Iterator<LpcValue> rightItr = rightList.iterator();

        while (leftItr.hasNext())
        {
            if (!rightItr.hasNext())
            {
                return 1;
            }
            LpcValue leftElement = leftItr.next();
            LpcValue rightElement = rightItr.next();
            int cmp = compare(leftElement, rightElement);
            if (cmp != 0)
            {
                return cmp;
            }
        }
        if (rightItr.hasNext())
        {
            return -1;
        }
        return 0;
    }

    private static int cmp(int a, int b)
    {
        if (a > b)
        {
            return 1;
        }
        if (a < b)
        {
            return -1;
        }
        return 0;
    }

    public static boolean isInRange(LpcValue min, LpcValue max, LpcValue value)
    {
        return compare(min, value) <= 0 && compare(max, value) >= 0;
    }

}
