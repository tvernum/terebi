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

package us.terebi.lang.lpc.compiler.integration;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import us.terebi.lang.lpc.compiler.ObjectBuilder;
import us.terebi.lang.lpc.runtime.CompiledMethodDefinition;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.StandardEfuns;
import us.terebi.lang.lpc.runtime.jvm.context.CallStack;
import us.terebi.lang.lpc.runtime.jvm.context.RuntimeContext;
import us.terebi.lang.lpc.runtime.jvm.context.SystemContext;
import us.terebi.lang.lpc.runtime.jvm.context.ThreadContext;
import us.terebi.lang.lpc.runtime.jvm.context.CallStack.MajorFrame;
import us.terebi.lang.lpc.runtime.jvm.context.CallStack.Origin;
import us.terebi.lang.lpc.runtime.jvm.support.ExecutionTimeCheck;
import us.terebi.lang.lpc.runtime.jvm.support.MiscSupport;
import us.terebi.lang.lpc.runtime.jvm.type.Types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * 
 */
public class MethodTestCase implements Callable<Object>
{
    private static final Pattern STRING_REGEX = Pattern.compile("str\\d*_(.*)");
    private static final Pattern EQUAL_REGEX = Pattern.compile("eq(\\d+.*)?");
    private static final Pattern COMPARE_REGEX = Pattern.compile("cmp\\d*_([\\d_]*)");
    private static final Pattern NUMERIC_REGEX = Pattern.compile("\\D+(\\d[\\d_]*)");

    private final CompiledMethodDefinition _method;
    private final ObjectBuilder _builder;

    public MethodTestCase(CompiledMethodDefinition method, ObjectBuilder builder)
    {
        _method = method;
        _builder = builder;
    }

    public String toString()
    {
        return _method.getDeclaringType().getName() + " :: " + _method.getName();
    }

    public Object call() throws Exception
    {
        testMethod(_method);
        return null;
    }

    private void testMethod(CompiledMethodDefinition method) throws Exception
    {
        ObjectInstance instance;

        SystemContext system = getContext();
        synchronized (system.lock())
        {
            RuntimeContext.activate(system);
            new ExecutionTimeCheck(1500).begin();
            instance = method.getDeclaringType().newInstance(Collections.<LpcValue> emptyList());
        }

        List< ? extends LpcValue> arguments = Collections.emptyList();
        String name = method.getName();

        LpcValue result = execute(method, instance, arguments);

        Matcher matcher = STRING_REGEX.matcher(name);
        if (matcher.matches())
        {
            String expected = matcher.group(1);
            assertEquals(expected, result.asString());
            return;
        }

        matcher = EQUAL_REGEX.matcher(name);
        if (matcher.matches())
        {
            List<LpcValue> list = result.asList();
            assertEquals(list.get(0), list.get(1));
            return;
        }

        matcher = COMPARE_REGEX.matcher(name);
        if (matcher.matches())
        {
            String[] elements = matcher.group(1).split("_");
            assertEquals(0, elements.length % 2);
            List<LpcValue> list = result.asList();
            for (int i = 0; i < elements.length; i += 2)
            {
                int idx1 = Integer.parseInt(elements[i]);
                int idx2 = Integer.parseInt(elements[i + 1]);
                assertEquals(list.get(idx1), list.get(idx2));
            }
            return;
        }

        matcher = NUMERIC_REGEX.matcher(name);
        if (matcher.matches())
        {
            String number = matcher.group(1);
            if (number.contains("_"))
            {
                Double expectedValue = Double.parseDouble(number.replace('_', '.'));
                assertEquals(expectedValue.doubleValue(), result.asDouble(), 0.0);
            }
            else
            {
                Integer expectedValue = Integer.parseInt(number);
                if (!MiscSupport.isZero(result))
                {
                    assertEquals(result.debugInfo() + " is not an int", Types.INT, result.getActualType());
                }
                assertEquals(expectedValue.longValue(), result.asLong());
            }
            return;
        }

        fail("Method " + name + " does not match the regex : " + NUMERIC_REGEX);
    }

    private LpcValue execute(CompiledMethodDefinition method, ObjectInstance instance, List< ? extends LpcValue> arguments)
    {
        SystemContext system = getContext();
        synchronized (system.lock())
        {
            ThreadContext thread = RuntimeContext.activate(system);

            CallStack stack = thread.callStack();
            stack.pushFrame(Origin.APPLY, instance);

            LpcValue result;
            try
            {
                result = method.execute(instance, arguments);

                MajorFrame frame = stack.peekFrame(0);
                assertEquals(instance, frame.instance());
                assertEquals(Origin.APPLY, frame.origin());
            }
            finally
            {
                stack.popFrame();
            }
            assertEquals(0, stack.size());

            return result;
        }
    }

    private SystemContext getContext()
    {
        return new SystemContext(StandardEfuns.getImplementation(), _builder.getObjectManager(), _builder.getSourceFinder());
    }

}
