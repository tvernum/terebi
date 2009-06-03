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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.Callable;

import org.junit.internal.runners.CompositeRunner;
import org.junit.internal.runners.InitializationError;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;


/**
 * 
 */
public class TestSuiteRunner extends Runner
{
    private final CompositeRunner _runner;

    public TestSuiteRunner(Class< ? > cls) throws InitializationError
    {
        _runner = new CompositeRunner(cls.getName());

        Method[] methods = cls.getMethods();
        for (Method method : methods)
        {
            TestSuite annotation = method.getAnnotation(TestSuite.class);
            if (annotation == null)
            {
                continue;
            }
            if (!Modifier.isStatic(method.getModifiers()))
            {
                throw new InitializationError("Method " + method + " should be static to be a " + annotation);
            }
            processMethod(method);
            return;
        }
        throw new InitializationError("No " + TestSuite.class + " method in " + cls);
    }

    private void processMethod(Method method) throws InitializationError
    {
        Object suite = null;
        try
        {
            suite = method.invoke(null);
        }
        catch (Exception e)
        {
            throw new InitializationError(e);
        }
        Class< ? extends Object> suiteClass = suite.getClass();
        if (Iterable.class.isInstance(suite))
        {
            initialise((Iterable< ? >) suite);
        }
        else if (suiteClass.isArray() && !suiteClass.getComponentType().isPrimitive())
        {
            initialise((Object[]) suite);
        }
        else
        {
            throw new InitializationError("Object " + suite + " is not a collection of tests");
        }
    }

    private void initialise(Iterable< ? > suite) throws InitializationError
    {
        for (Object object : suite)
        {
            addTest(object);
        }
    }

    private void initialise(Object[] suite) throws InitializationError
    {
        for (Object object : suite)
        {
            addTest(object);
        }
    }

    private void addTest(Object object) throws InitializationError
    {
        if (Callable.class.isInstance(object))
        {
            _runner.add(new CallableRunner(object.toString(), (Callable< ? >) object));
        }
        else if (Runnable.class.isInstance(object))
        {
            _runner.add(new CallableRunner(object.toString(), (Runnable) object));
        }
        else
        {
            throw new InitializationError("Test object " + object + " does not implement " + Callable.class);
        }
    }

    public Description getDescription()
    {
        return _runner.getDescription();
    }

    public void run(RunNotifier notifier)
    {
        _runner.run(notifier);
    }

}
