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

package us.terebi.lang.lpc.runtime.jvm.efun.callout;

import java.util.Collections;
import java.util.List;

import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.LpcConstants;
import us.terebi.lang.lpc.runtime.jvm.context.CallOutManager;
import us.terebi.lang.lpc.runtime.jvm.context.RuntimeContext;
import us.terebi.lang.lpc.runtime.jvm.context.CallOutManager.Entry;
import us.terebi.lang.lpc.runtime.jvm.efun.AbstractEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.ThisObjectEfun;
import us.terebi.lang.lpc.runtime.jvm.support.MiscSupport;
import us.terebi.lang.lpc.runtime.jvm.support.ValueSupport;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;

/**
 * 
 */
public class FindCallOutEfun extends AbstractEfun implements FunctionSignature, Callable
{
    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        /** String or Integer (handle) **/
        return Collections.singletonList(new ArgumentSpec("function", Types.MIXED));
    }

    public LpcType getReturnType()
    {
        /** Time Left, or -1 **/
        return Types.INT;
    }

    public boolean acceptsLessArguments()
    {
        return false;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        checkArguments(arguments);
        LpcValue argument = getArgument(arguments, 0);

        CallOutManager callOutManager = RuntimeContext.obtain().system().callout();

        if (MiscSupport.isString(argument))
        {
            ObjectInstance owner = ThisObjectEfun.this_object();
            String funcName = argument.asString();
            for (Entry entry : callOutManager.all())
            {
                Callable callable = entry.callable;
                if (isMatchingMethod(owner, funcName, callable))
                {
                    return getTimeLeft(entry);
                }
            }
        }
        else
        {
            long handle = argument.asLong();
            Entry entry = callOutManager.find(handle);
            if (entry != null)
            {
                return getTimeLeft(entry);
            }
        }
        return LpcConstants.INT.MINUS_ONE;
    }

    private boolean isMatchingMethod(ObjectInstance owner, String funcName, Callable callable)
    {
        return callable.getKind() == Callable.Kind.METHOD && callable.getOwner() == owner && funcName.equals(callable.getName());
    }

    private LpcValue getTimeLeft(Entry entry)
    {
        long time = entry.time - System.currentTimeMillis();
        if (time <= 0)
        {
            return LpcConstants.INT.ZERO;
        }
        time /= 1000;
        return ValueSupport.intValue(time);
    }
}
