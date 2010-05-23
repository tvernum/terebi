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

package us.terebi.lang.lpc.compiler;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import org.junit.Test;

import us.terebi.lang.lpc.compiler.bytecode.ByteCodeCompiler;
import us.terebi.lang.lpc.compiler.bytecode.context.DebugOptions;
import us.terebi.lang.lpc.compiler.java.context.BasicScopeLookup;
import us.terebi.lang.lpc.compiler.java.context.CompiledObjectDefinition;
import us.terebi.lang.lpc.compiler.java.context.CompiledObjectInstance;
import us.terebi.lang.lpc.compiler.java.context.LpcCompilerObjectManager;
import us.terebi.lang.lpc.compiler.java.context.ScopeLookup;
import us.terebi.lang.lpc.io.ByteArrayResource;
import us.terebi.lang.lpc.io.Resource;
import us.terebi.lang.lpc.io.ResourceFinder;
import us.terebi.lang.lpc.parser.LpcParser;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.MethodDefinition;
import us.terebi.lang.lpc.runtime.jvm.StandardEfuns;
import us.terebi.lang.lpc.runtime.jvm.context.Efuns;
import us.terebi.lang.lpc.runtime.jvm.context.RuntimeContext;
import us.terebi.lang.lpc.runtime.jvm.context.SystemContext;
import us.terebi.lang.lpc.runtime.jvm.support.ExecutionTimeCheck;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * 
 */
public class ObjectBuilderTest
{
    @Test
    public void tryCompilingSimpleLpcObject() throws Exception
    {
        CompilerObjectManager manager = new LpcCompilerObjectManager();
        Efuns efuns = StandardEfuns.getImplementation();

        ResourceFinder finder = new ResourceFinder()
        {
            public Resource getResource(String path)
            {
                String code = "public int getNumber() { return 7; }";
                return new ByteArrayResource(path, code.getBytes());
            }
        };
        File workingDir = File.createTempFile("lpc", "test");
        workingDir.delete();
        workingDir.mkdir();

        ByteCodeCompiler compiler = new ByteCodeCompiler(manager, efuns, new DebugOptions(null), true);
        ScopeLookup scope = new BasicScopeLookup(manager);
        ObjectBuilder builder = new ObjectBuilder(finder, manager, scope, new LpcParser(), compiler, workingDir);
        CompiledObjectDefinition object = builder.compile("/area/foo/bar.c");
        assertNotNull(object);
        assertNotNull(object.getImplementationClass());
        Map<String, ? extends MethodDefinition> methods = object.getMethods();
        assertEquals(1, methods.size());
        MethodDefinition method = methods.get("getNumber");
        assertNotNull(method);

        RuntimeContext.activate(new SystemContext(efuns, manager, finder));
        synchronized (RuntimeContext.lock())
        {
            new ExecutionTimeCheck(1500).begin();
            CompiledObjectInstance instance = object.newInstance(Collections.<LpcValue> emptyList());
            assertNotNull(instance);
            assertIsInstance(object.getImplementationClass(), instance.getImplementingObject());

            LpcValue result = method.execute(instance, Collections.<LpcValue> emptyList());
            assertNotNull(result);
            assertEquals(7, result.asLong());
        }
    }

    private void assertIsInstance(Class< ? > expected, Object object)
    {
        if (expected.isInstance(object))
        {
            return;
        }
        Class< ? extends Object> objectClass = object.getClass();
        ClassLoader expectedLoader = expected.getClassLoader();
        ClassLoader objectLoader = objectClass.getClassLoader();
        if (expectedLoader == objectLoader)
        {
            fail("Object " + object + " (" + objectClass + ") is not an instance of " + expected);
        }
        else
        {
            fail("Object " + object + " (" + objectClass + "::" + objectLoader + ") is not an instance of " + expected + "::" + expectedLoader);
        }
    }
}
