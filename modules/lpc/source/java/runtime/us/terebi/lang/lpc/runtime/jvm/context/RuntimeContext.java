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

package us.terebi.lang.lpc.runtime.jvm.context;

import org.apache.log4j.Logger;

import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;

/**
 * 
 */
public class RuntimeContext
{
    private static final Logger LOG = Logger.getLogger(RuntimeContext.class);

    private static final ThreadLocal<ThreadContext> CONTEXT = new ThreadLocal<ThreadContext>();

    public static ThreadContext activate(SystemContext context)
    {
        LOG.debug("Activating: " + context);
        ThreadContext tc = new ThreadContext(context);
        activate(tc);
        return tc;
    }

    private static void activate(ThreadContext context)
    {
        ThreadContext existing = CONTEXT.get();
        if (context == existing)
        {
            return;
        }
        if (existing != null)
        {
            CallStack callStack = existing.callStack();
            if (callStack.size() > 0)
            {
                LOG.warn("Replacing existing context with non-empty call stack " + callStack);
            }
        }
        CONTEXT.set(context);
        context.begin();
    }

    public static void clear()
    {
        CONTEXT.set(null);
    }

    public static Object lock()
    {
        return get(true).system().lock();
    }

    public static ThreadContext obtain()
    {
        ThreadContext context = get(true);
        checkOwner(context.system().lock());
        return context;
    }

    public static ThreadContext peek()
    {
        return get(false);
    }

    private static void checkOwner(Object token)
    {
        try
        {
            // We send a notify to the token, which checks that the current thread owns the monitor for that token
            // We don't care if anyone is waiting on the monitor (in fact, we're assuming that no one is), it's just that this is the only
            // way to ensure that the synchronization has been done correctly.
            token.notify();
        }
        catch (IllegalMonitorStateException e)
        {
            throw new LpcRuntimeException("Attempt to obtain current "
                    + SystemContext.class.getSimpleName()
                    + " without synchronizing on "
                    + RuntimeContext.class.getSimpleName()
                    + ".lock()");
        }
    }

    public static ThreadContext get(boolean required)
    {
        ThreadContext context = CONTEXT.get();
        if (context == null)
        {
            if (required)
            {
                throw new IllegalStateException("No active context on thread " + Thread.currentThread().getName());
            }
            return null;
        }
        return context;
    }

    public static boolean hasActiveContext()
    {
        return CONTEXT.get() != null;
    }
    
    
}
