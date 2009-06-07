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

import org.hamcrest.core.IsSame;

import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isString;

import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.LpcConstants;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;
import us.terebi.lang.lpc.runtime.jvm.value.FloatValue;
import us.terebi.lang.lpc.runtime.jvm.value.IntValue;
import us.terebi.lang.lpc.runtime.jvm.value.StringValue;

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
        if (isString(left))
        {
            return new StringValue(left.asString() + right.asString());
        }
        throw new UnsupportedOperationException("add - Not implemented");
    }

    public static LpcValue add(int left, LpcValue right)
    {
        // @TODO Auto-generated method stub
        return null;
    }

    public static LpcValue add(LpcValue left, int right)
    {
        // @TODO Auto-generated method stub
        return null;
    }

    public static LpcValue add(int left, int right)
    {
        // @TODO Auto-generated method stub
        return null;
    }

    public static LpcValue multiply(LpcValue left, LpcValue right)
    {
        // @TODO Auto-generated method stub
        return null;
    }

    public static LpcValue divide(LpcValue lpcValue, LpcValue _lpc_v590)
    {
        // @TODO Auto-generated method stub
        return null;
    }

    public static LpcValue subtract(LpcValue lpcValue, LpcValue _lpc_v766)
    {
        // @TODO Auto-generated method stub
        return null;
    }

    public static LpcValue modulus(LpcValue lpcValue, LpcValue _lpc_v967)
    {
        // @TODO Auto-generated method stub
        return null;
    }
}
