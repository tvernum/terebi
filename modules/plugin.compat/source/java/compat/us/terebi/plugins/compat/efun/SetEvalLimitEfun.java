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

package us.terebi.plugins.compat.efun;

import java.util.Collections;
import java.util.List;

import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.efun.AbstractEfun;
import us.terebi.lang.lpc.runtime.jvm.support.ExecutionTimeCheck;
import us.terebi.lang.lpc.runtime.jvm.support.ValueSupport;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;

/**
 * 
 */
public class SetEvalLimitEfun extends AbstractEfun implements FunctionSignature, Callable
{
    public static final int SCALING = 1000;

    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        return Collections.singletonList(new ArgumentSpec("n", Types.INT));
    }

    public LpcType getReturnType()
    {
        return Types.INT;
    }

    public boolean acceptsLessArguments()
    {
        return false;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        checkArguments(arguments);
        long n = arguments.get(0).asLong();
        long v = setEvalLimit(n);
        return ValueSupport.intValue(v);
    }

    private long setEvalLimit(long n)
    {
        ExecutionTimeCheck check = ExecutionTimeCheck.get();
        if (n > 0)
        {
            check.setMaximumTime(n / SCALING);
            return n;
        }
        else if (n < 0)
        {
            long remaining = check.getRemainingTime();
            return (remaining * SCALING);
        }
        else
        {
            check.reset();
            long maxTimeMs = check.getMaximumTime();
            return (maxTimeMs * SCALING);
        }
    }
}
