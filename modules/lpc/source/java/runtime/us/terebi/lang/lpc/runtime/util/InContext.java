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

import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.context.CallStack;
import us.terebi.lang.lpc.runtime.jvm.context.RuntimeContext;
import us.terebi.lang.lpc.runtime.jvm.context.CallStack.Origin;
import us.terebi.lang.lpc.runtime.jvm.support.ExecutionTimeCheck;

/**
 * 
 */
public class InContext
{
    public interface Exec<T>
    {
        public T execute();
    }

    public static <T> T execute(Origin origin, ObjectInstance owner, Exec<T> exec)
    {
        ExecutionTimeCheck check = ExecutionTimeCheck.get();
        synchronized (RuntimeContext.lock())
        {
            CallStack stack = RuntimeContext.obtain().callStack();
            stack.pushFrame(origin, owner);
            try
            {
                check.begin();
                return exec.execute();
            }
            finally
            {
                stack.popFrame();
                check.end();
            }
        }
    }
}
