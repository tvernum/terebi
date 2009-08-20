/* ------------------------------------------------------------------------
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

package us.terebi.lang.lpc.runtime.util;

import java.util.List;

import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.context.CallStack;
import us.terebi.lang.lpc.runtime.jvm.context.RuntimeContext;
import us.terebi.lang.lpc.runtime.jvm.context.CallStack.Origin;

public class StackCall extends CallableProxy implements Callable
{
    private final Origin _origin;

    public StackCall(Callable function, Origin origin)
    {
        super(function);
        _origin = origin;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        synchronized (RuntimeContext.lock())
        {
            CallStack stack = RuntimeContext.obtain().callStack();
            stack.pushFrame(_origin, getOwner());
            try
            {
                return super.execute(arguments);
            }
            finally
            {
                stack.popFrame();
            }
        }
    }

    public LpcValue execute(LpcValue... arguments)
    {
        synchronized (RuntimeContext.lock())
        {
            CallStack stack = RuntimeContext.obtain().callStack();
            stack.pushFrame(_origin, getOwner());
            try
            {
                return super.execute(arguments);
            }
            finally
            {
                stack.popFrame();
            }
        }
    }
}
