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
import java.util.List;

import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.context.CallStack.Origin;
import us.terebi.lang.lpc.runtime.util.CallableProxy;
import us.terebi.lang.lpc.runtime.util.StackCall;

/**
 * 
 */
public class CallableSupport
{
    public static class BoundCallable extends CallableProxy implements Callable
    {
        private final LpcValue[] _boundArguments;

        public BoundCallable(Callable delegate, LpcValue[] boundArguments)
        {
            super(delegate);
            _boundArguments = boundArguments;
        }

        public LpcValue execute(List< ? extends LpcValue> arguments)
        {
            LpcValue[] allArguments = newArray(arguments.size());
            Iterator< ? extends LpcValue> iterator = arguments.iterator();
            for (int i = _boundArguments.length; iterator.hasNext(); i++)
            {
                allArguments[i] = iterator.next();
            }
            return super.execute(allArguments);
        }

        private LpcValue[] newArray(int size)
        {
            LpcValue[] array = new LpcValue[_boundArguments.length + size];
            System.arraycopy(_boundArguments, 0, array, 0, _boundArguments.length);
            return array;
        }

        public LpcValue execute(LpcValue... arguments)
        {
            LpcValue[] allArguments = newArray(arguments.length);
            System.arraycopy(arguments, 0, allArguments, _boundArguments.length, arguments.length);
            return super.execute(allArguments);
        }
    }

    public static Callable asCallable(LpcValue value)
    {
        // @TODO - This isn't very efficient...
        return new StackCall(value.asCallable(), Origin.POINTER);
    }

    public static Callable bindArguments(Callable callable, LpcValue[] arguments)
    {
        if (arguments.length == 0)
        {
            return callable;
        }
        else
        {
            return new BoundCallable(callable, arguments);
        }
    }

}
