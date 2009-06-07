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
import java.io.IOException;

import org.apache.commons.jci.compilers.JavaCompilerFactory;

import us.terebi.lang.lpc.compiler.java.JavaCompiler;
import us.terebi.lang.lpc.compiler.java.context.CompilerObjectManager;
import us.terebi.lang.lpc.compiler.java.context.FunctionMap;
import us.terebi.lang.lpc.compiler.java.context.ObjectManager;
import us.terebi.lang.lpc.io.FileFinder;
import us.terebi.lang.lpc.io.ResourceFinder;
import us.terebi.lang.lpc.parser.LpcParser;
import us.terebi.lang.lpc.runtime.jvm.StandardEfuns;
import us.terebi.lang.lpc.runtime.jvm.context.BasicScopeLookup;
import us.terebi.lang.lpc.runtime.jvm.context.ScopeLookup;

/**
 * 
 */
public class ObjectBuilderFactory
{
    private FunctionMap _efuns;
    private ObjectManager _manager;
    private LpcParser _parser;
    private File _workingDir;

    public ObjectBuilderFactory() throws IOException
    {
        _efuns = StandardEfuns.getSignatures();
        _manager = new CompilerObjectManager();
        _parser = new LpcParser();

        _workingDir = File.createTempFile("lpc", "test");
        _workingDir.delete();
        _workingDir.mkdir();
        _workingDir.deleteOnExit();
    }

    public void setEfuns(FunctionMap efuns)
    {
        _efuns = efuns;
    }

    public void setManager(ObjectManager manager)
    {
        _manager = manager;
    }

    public void setParser(LpcParser parser)
    {
        _parser = parser;
    }

    public void setWorkingDir(File workingDir)
    {
        _workingDir = workingDir;
    }

    public ObjectBuilder createBuilder(final File rootDirectory)
    {
        ResourceFinder finder = new FileFinder(rootDirectory);
        return createBuilder(finder);
    }

    public ObjectBuilder createBuilder(ResourceFinder finder)
    {
        Compiler javaCompiler = new JavaCompiler(_efuns, _manager);
        org.apache.commons.jci.compilers.JavaCompiler eclipseCompiler = new JavaCompilerFactory().createCompiler("eclipse");
        ScopeLookup scope = new BasicScopeLookup(_manager);
        return new ObjectBuilder(finder, _manager, scope, _parser, javaCompiler, eclipseCompiler, _workingDir);
    }
}
