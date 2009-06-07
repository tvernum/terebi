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

package us.terebi.lang.lpc.runtime.jvm.efun;

import java.util.Arrays;
import java.util.List;

import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.ArgumentSemantics;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.LpcConstants;
import us.terebi.lang.lpc.runtime.jvm.LpcReference;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;
import us.terebi.lang.lpc.runtime.jvm.value.IntValue;
import us.terebi.lang.lpc.runtime.util.FunctionUtil;
import us.terebi.util.Range;

/**
 * 
 */
public abstract class AbstractEfun implements Efun, FunctionSignature, Callable
{
    public LpcValue execute(LpcValue... arguments)
    {
        return execute(Arrays.asList(arguments));
    }

    public Kind getKind()
    {
        return Kind.EFUN;
    }

    public FunctionSignature getSignature()
    {
        return this;
    }

    public boolean isVarArgs()
    {
        return false;
    }

    protected void checkArguments(List< ? extends LpcValue> argumentValues)
    {
        Range<Integer> argumentRange = FunctionUtil.getAllowedNumberOfArgument(this);
        if (!argumentRange.inRange(argumentValues.size()))
        {
            throw new LpcRuntimeException(getName()
                    + " requires "
                    + argumentRange
                    + " argument(s) but "
                    + argumentValues.size()
                    + " were provided");
        }
        List< ? extends ArgumentDefinition> argumentDefinitions = getSignature().getArguments();
        for (int i = 0; i < argumentDefinitions.size() && i < argumentValues.size(); i++)
        {
            ArgumentDefinition def = argumentDefinitions.get(i);
            LpcValue val = argumentValues.get(i);
            LpcType valType = (val instanceof LpcReference) ? ((LpcReference) val).getType() : val.getActualType();
            checkType(i, def.getType(), valType);
            checkSemantics(i, def.getSemantics(), val);
        }
    }

    protected void checkSemantics(int index, ArgumentSemantics semantics, LpcValue value)
    {
        if (semantics == ArgumentSemantics.IMPLICIT_REFERENCE)
        {
            if (!(value instanceof LpcReference))
            {
                throw new LpcRuntimeException("Internal Error - expected argument "
                        + index
                        + " to "
                        + getName()
                        + " to be a reference value");
            }
        }
    }

    private void checkType(int index, LpcType expectedType, LpcType actualType)
    {
        if (expectedType.equals(actualType))
        {
            return;
        }
        if (expectedType.getKind() == LpcType.Kind.MIXED && expectedType.getArrayDepth() <= actualType.getArrayDepth())
        {
            return;
        }
        throw new LpcRuntimeException("Bad argument "
                + index
                + " to "
                + getName()
                + " expected "
                + expectedType
                + " got "
                + actualType);
    }

    protected CharSequence getName()
    {
        StringBuilder builder = new StringBuilder();
        String name = getClass().getName();
        for (int i = 0; i < name.length(); i++)
        {
            char ch = name.charAt(i);
            if (Character.isUpperCase(ch))
            {
                if (i > 0)
                {
                    builder.append('_');
                }
                builder.append(Character.toLowerCase(ch));
            }
            else
            {
                builder.append(ch);
            }
        }
        return builder;

    }

    protected IntValue getValue(boolean bool)
    {
        return bool ? LpcConstants.INT.TRUE : LpcConstants.INT.FALSE;
    }
}
