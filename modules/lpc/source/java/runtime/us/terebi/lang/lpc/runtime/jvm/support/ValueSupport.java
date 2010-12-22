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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.LpcConstants;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.ArrayValue;
import us.terebi.lang.lpc.runtime.jvm.value.FloatValue;
import us.terebi.lang.lpc.runtime.jvm.value.IntValue;
import us.terebi.lang.lpc.runtime.util.cache.StringCache;
import us.terebi.lang.lpc.runtime.util.cache.ValueCache;

/**
 * 
 */
public class ValueSupport
{
    private static final ValueCache<String> STRING_CACHE = new StringCache();
    
    public static IntValue intValue(long value)
    {
        if (value < 10 && value > -10)
        {
            switch ((int) value)
            {
                case 0:
                    return LpcConstants.INT.ZERO;
                case 1:
                    return LpcConstants.INT.ONE;
                case 2:
                    return LpcConstants.INT.TWO;
                case -1:
                    return LpcConstants.INT.MINUS_ONE;
                case -2:
                    return LpcConstants.INT.MINUS_TWO;
                case -3:
                    return LpcConstants.INT.MINUS_THREE;
            }
        }
        return new IntValue(value);
    }

    public static LpcValue floatValue(double value)
    {
        return new FloatValue(value);
    }

    public static ArrayValue arrayValue(LpcValue[] elements)
    {
        return arrayValue(Arrays.asList(elements));
    }

    public static ArrayValue arrayValue(Collection<LpcValue> elements)
    {
        if (elements.isEmpty())
        {
            return LpcConstants.ARRAY.EMPTY;
        }

        List<LpcValue> list = new ArrayList<LpcValue>(elements);
        Set<LpcType> types = new HashSet<LpcType>();
        for (LpcValue lpcValue : elements)
        {
            if (lpcValue == null)
            {
                throw new IllegalArgumentException("Cannot put null values into an array (" + elements + ")");
            }
            types.add(lpcValue.getActualType());
        }

        if (types.size() == 1)
        {
            return new ArrayValue(Types.arrayOf(types.iterator().next()), list);
        }

        LpcType[] typeArray = types.toArray(new LpcType[types.size()]);
        return new ArrayValue(Types.arrayOf(MiscSupport.commonType(typeArray)), list);
    }

    public static LpcValue stringValue(String s)
    {
        if (s == null)
        {
            return LpcConstants.STRING.NIL;
        }
        if (s.length() == 0)
        {
            return LpcConstants.STRING.BLANK;
        }
        return STRING_CACHE.get(s);
    }
}
