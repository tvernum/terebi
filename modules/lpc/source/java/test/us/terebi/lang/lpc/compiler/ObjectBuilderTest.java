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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.jci.compilers.JavaCompilerFactory;
import org.junit.Test;

import us.terebi.lang.lpc.compiler.java.JavaCompiler;
import us.terebi.lang.lpc.compiler.java.context.CompiledObjectDefinition;
import us.terebi.lang.lpc.compiler.java.context.CompilerObjectManager;
import us.terebi.lang.lpc.compiler.java.context.FunctionMap;
import us.terebi.lang.lpc.compiler.java.context.ObjectManager;
import us.terebi.lang.lpc.io.ByteArrayResource;
import us.terebi.lang.lpc.io.Resource;
import us.terebi.lang.lpc.io.ResourceFinder;
import us.terebi.lang.lpc.parser.LpcParser;
import us.terebi.lang.lpc.runtime.MethodDefinition;
import us.terebi.lang.lpc.runtime.jvm.StandardEfuns;
import us.terebi.lang.lpc.runtime.jvm.context.BasicScopeLookup;
import us.terebi.lang.lpc.runtime.jvm.context.ScopeLookup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * 
 */
public class ObjectBuilderTest
{
    @Test
    public void tryCompilingSimpleLpcObject() throws Exception
    {
        ObjectManager manager = new CompilerObjectManager();
        FunctionMap efuns = StandardEfuns.getSignatures();

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

        JavaCompiler javaCompiler = new JavaCompiler(efuns, manager);
        org.apache.commons.jci.compilers.JavaCompiler eclipseCompiler = new JavaCompilerFactory().createCompiler("eclipse");
        ScopeLookup scope = new BasicScopeLookup(manager);
        ObjectBuilder builder = new ObjectBuilder(finder, manager, scope, new LpcParser(), javaCompiler, eclipseCompiler, workingDir);
        CompiledObjectDefinition object = builder.compile("/area/foo/bar.c");
        assertNotNull(object);
        assertNotNull(object.getImplementationClass());
        Map<String, ? extends MethodDefinition> methods = object.getMethods();
        assertEquals(1, methods.size());
        assertNotNull(methods.get("getNumber"));
        
    }
}
