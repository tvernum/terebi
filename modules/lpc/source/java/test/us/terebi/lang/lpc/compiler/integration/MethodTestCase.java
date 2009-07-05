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
import us.terebi.lang.lpc.runtime.jvm.context.CallStack.MajorFrame;
import us.terebi.lang.lpc.runtime.jvm.context.CallStack.Origin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * 
 */
public class MethodTestCase implements Callable<Object>
{
    private static final Pattern STRING_REGEX = Pattern.compile("str\\d*_(.*)");
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
        ObjectInstance instance = method.getDeclaringType().newInstance();
        List< ? extends LpcValue> arguments = Collections.emptyList();
        LpcValue result = execute(method, instance, arguments);

        String name = method.getName();

        Matcher matcher = STRING_REGEX.matcher(name);
        if (matcher.matches())
        {
            String expected = matcher.group(1);
            assertEquals(expected, result.asString());
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
                assertEquals(expectedValue.longValue(), result.asLong());
            }
            return;
        }

        fail("Method " + name + " does not match the regex : " + NUMERIC_REGEX);
    }

    private LpcValue execute(CompiledMethodDefinition method, ObjectInstance instance, List< ? extends LpcValue> arguments)
    {
        RuntimeContext context = new RuntimeContext(StandardEfuns.getImplementation(), _builder.getObjectManager());
        RuntimeContext.set(context);
        
        CallStack stack = context.callStack();
        stack.pushFrame(Origin.APPLY, instance);

        LpcValue result = method.execute(instance, arguments);

        MajorFrame frame = stack.peekFrame(0);
        assertEquals(instance, frame.instance);
        assertEquals(Origin.APPLY, frame.origin);

        stack.popFrame();

        assertEquals(0, stack.size());

        return result;
    }

}
