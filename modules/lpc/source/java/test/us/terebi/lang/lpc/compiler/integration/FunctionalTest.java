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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.runner.RunWith;

import us.terebi.lang.lpc.compiler.ObjectBuilder;
import us.terebi.lang.lpc.compiler.ObjectBuilderFactory;
import us.terebi.lang.lpc.compiler.java.context.CompiledObjectDefinition;
import us.terebi.lang.lpc.runtime.CompiledMethodDefinition;
import us.terebi.lang.lpc.runtime.MemberDefinition.Modifier;
import us.terebi.lang.lpc.runtime.jvm.StandardEfuns;
import us.terebi.test.TestSuite;
import us.terebi.test.TestSuiteRunner;

@RunWith(TestSuiteRunner.class)
public class FunctionalTest
{
    @TestSuite
    public static List< ? extends Callable<Object>> functionalTests() throws IOException
    {
        List<Callable<Object>> list = new ArrayList<Callable<Object>>();
        ObjectBuilder builder = createBuilder(new File("source/lpc/test/us/terebi/lang/lpc/compiler/functional/"));

        list.addAll(getTests("binary.c", builder));
        list.addAll(getTests("math.c", builder));
        list.addAll(getTests("loop.c", builder));
        list.addAll(getTests("string.c", builder));
        list.addAll(getTests("function.c", builder));
        list.addAll(getTests("varargs.c", builder));
        list.addAll(getTests("mapping.c", builder));
        list.addAll(getTests("array.c", builder));
        list.addAll(getTests("sprintf.c", builder));

        return list;
    }

    private static List< ? extends Callable<Object>> getTests(final String file, ObjectBuilder builder)
    {
        try
        {
            CompiledObjectDefinition definition = builder.compile(file);
            Collection< ? extends CompiledMethodDefinition> methods = definition.getMethods().values();
            List<MethodTestCase> tests = new ArrayList<MethodTestCase>(methods.size());
            for (CompiledMethodDefinition method : methods)
            {
                if (method.getModifiers().contains(Modifier.PUBLIC))
                {
                    tests.add(new MethodTestCase(method, builder));
                }
            }
            return tests;
        }
        catch (final RuntimeException e)
        {
            Callable<Object> failure = new Callable<Object>()
            {
                public Object call() throws Exception
                {
                    throw e;
                }
                public String toString()
                {
                    return file;
                }
            };
            return Collections.<Callable<Object>> singletonList(failure);
        }
    }

    private static ObjectBuilder createBuilder(File directory) throws IOException
    {
        ObjectBuilderFactory factory = new ObjectBuilderFactory(StandardEfuns.getImplementation());
        ObjectBuilder builder = factory.createBuilder(directory);
        builder.setPrintStats(System.out);
        return builder;
    }

}
