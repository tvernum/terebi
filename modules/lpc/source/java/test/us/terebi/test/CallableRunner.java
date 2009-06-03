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

package us.terebi.test;

import java.util.concurrent.Callable;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

import us.terebi.util.exec.RunnableCallable;

/**
 * 
 */
public class CallableRunner extends Runner
{
    private final Callable< ? > _callable;
    private final Description _description;

    public CallableRunner(String name, Runnable runnable)
    {
        this(runnable.getClass(), name, new RunnableCallable(runnable));
    }

    public CallableRunner(String name, Callable< ? > callable)
    {
        this(callable.getClass(), name, callable);
    }

    private CallableRunner(Class< ? > cls, String name, Callable< ? > callable)
    {
        _callable = callable;
        _description = Description.createTestDescription(cls, name);
    }

    public Description getDescription()
    {
        return _description;
    }

    public void run(RunNotifier notifier)
    {
        notifier.fireTestStarted(_description);
        try
        {
            _callable.call();
        }
        catch (Throwable th)
        {
            notifier.fireTestFailure(new Failure(_description, th));
        }
        notifier.fireTestFinished(_description);
    }

}
