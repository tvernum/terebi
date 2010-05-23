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

import us.terebi.lang.lpc.compiler.bytecode.ByteCodeCompiler;
import us.terebi.lang.lpc.compiler.bytecode.context.DebugOptions;
import us.terebi.lang.lpc.compiler.java.context.BasicScopeLookup;
import us.terebi.lang.lpc.compiler.java.context.LpcCompilerObjectManager;
import us.terebi.lang.lpc.compiler.java.context.ScopeLookup;
import us.terebi.lang.lpc.io.FileFinder;
import us.terebi.lang.lpc.io.ResourceFinder;
import us.terebi.lang.lpc.parser.LpcParser;
import us.terebi.lang.lpc.runtime.jvm.context.Efuns;

import static java.util.Arrays.asList;

/**
 * 
 */
public class ObjectBuilderFactory
{
    private Efuns _efuns;
    private CompilerObjectManager _manager;
    private LpcParser _parser;
    private File _workingDir;
    private DebugOptions _debug;
    private boolean _insertTimeCheck;

    public ObjectBuilderFactory(Efuns efuns) throws IOException
    {
        _efuns = efuns;
        _parser = new LpcParser();

        _workingDir = File.createTempFile("lpc", "test");
        _workingDir.delete();
        _workingDir.mkdir();
        _workingDir.deleteOnExit();
        _insertTimeCheck = true;
    }

    public void setManager(CompilerObjectManager manager)
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
        if (_manager == null)
        {
            _manager = new LpcCompilerObjectManager();
        }
        Compiler javaCompiler = new ByteCodeCompiler(_manager, _efuns, _debug, _insertTimeCheck);
        ScopeLookup scope = new BasicScopeLookup(_manager);
        ObjectBuilder builder = new ObjectBuilder(finder, _manager, scope, _parser, javaCompiler, _workingDir);
        for (Object object : asList(_manager, _parser, _efuns, finder))
        {
            setCompiler(object, builder);
        }
        return builder;
    }

    private void setCompiler(Object object, ObjectCompiler compiler)
    {
        if (object instanceof CompilerAware)
        {
            CompilerAware ca = (CompilerAware) object;
            ca.setCompiler(compiler);
        }
    }

    public void setDebugOptions(DebugOptions debugOptions)
    {
        _debug = debugOptions;
    }
    
    public void setInsertTimeCheck(boolean insertTimeCheck)
    {
        _insertTimeCheck = insertTimeCheck;
    }
}
