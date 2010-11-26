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
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import org.apache.log4j.Logger;

import us.terebi.lang.lpc.compiler.classloader.AutoCompilingClassLoader;
import us.terebi.lang.lpc.compiler.java.context.CompiledObjectDefinition;
import us.terebi.lang.lpc.compiler.java.context.ScopeLookup;
import us.terebi.lang.lpc.io.Resource;
import us.terebi.lang.lpc.io.ResourceFinder;
import us.terebi.lang.lpc.parser.LineMapping;
import us.terebi.lang.lpc.parser.LpcParser;
import us.terebi.lang.lpc.parser.ParserException;
import us.terebi.lang.lpc.parser.ParserState;
import us.terebi.lang.lpc.parser.ast.ASTObjectDefinition;
import us.terebi.lang.lpc.parser.ast.SimpleNode;
import us.terebi.lang.lpc.parser.jj.Token;
import us.terebi.lang.lpc.runtime.jvm.LpcObject;
import us.terebi.lang.lpc.runtime.jvm.exception.InternalError;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;
import us.terebi.lang.lpc.runtime.jvm.object.CompiledDefinition;
import us.terebi.util.log.LogContext;

/**
 * 
 */
public class ObjectBuilder implements ObjectCompiler
{
    private final Logger LOG = Logger.getLogger(ObjectBuilder.class);

    private final ResourceFinder _sourceFinder;
    private final Compiler _compiler;
    private final LpcParser _parser;
    private final ClassLoader _classLoader;
    private final CompilerObjectManager _manager;
    private final ScopeLookup _scope;
    private final ClassStore _store;

    private PrintStream _printStats;

    public ObjectBuilder(ResourceFinder sourceFinder, CompilerObjectManager manager, ScopeLookup scope, LpcParser parser, Compiler compiler,
            File workingDirectory)
    {
        _sourceFinder = sourceFinder;
        _manager = manager;
        _scope = scope;
        _parser = parser;
        _compiler = compiler;
        _classLoader = getClassLoader(workingDirectory);
        _store = new FileStore(workingDirectory);
        _printStats = null;
    }

    private ClassLoader getClassLoader(File workingDirectory)
    {
        try
        {
            return new AutoCompilingClassLoader(new URL[] { workingDirectory.toURL() }, getClass().getClassLoader(), this);
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException("!! Cannot convert File " + workingDirectory + " into URL !!", e);
        }
    }

    public void setPrintStats(PrintStream printStats)
    {
        _printStats = printStats;
    }

    public CompiledObjectDefinition compile(String objectSource)
    {
        Resource resource = getResource(objectSource);
        if (!resource.exists())
        {
            LOG.debug("Resource (" + resource + ") for " + objectSource + " does not exist");
            return null;
        }
        return compile(resource);
    }

    public CompiledObjectDefinition compile(Resource resource)
    {
        LogContext lc = new LogContext(resource.getPath());
        try
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Compiling " + resource);
            }
            long startAt = System.currentTimeMillis();
            CompiledObjectDefinition definition = doCompilation(resource, true);
            long endAt = System.currentTimeMillis();
            if (_printStats != null)
            {
                printStat("Compiled " + resource + " to " + definition.getImplementationClass() + " (" + (endAt - startAt) + "ms)");
            }
            return definition;
        }
        finally
        {
            lc.end();
        }
    }

    private CompiledObjectDefinition doCompilation(Resource resource, boolean initialise)
    {
        ClassName name = ClassNameMapper.getImplementingClass(resource);
        long mod = _store.getLastModified(name);
        ASTObjectDefinition ast1 = null;
        if (resource.newerThan(mod))
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Compiling " + resource + " to bytecode (" + name + ")");
            }
            ast1 = compile(resource, name);
        }
        else
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Bytecode (" + name + ") for " + resource + " is up to date");
            }
        }
        ASTObjectDefinition ast = ast1;
        if (initialise)
        {
            Class< ? extends LpcObject> cls = loadClass(ast, name);
            CompiledDefinition<LpcObject> definition = loadObjectDefinition(resource, cls);

            return definition;
        }
        else
        {
            return null;
        }
    }

    public void precompile(String objectSource) throws CompileException
    {
        Resource resource = getResource(objectSource);
        doCompilation(resource, false);
    }

    private ASTObjectDefinition compile(Resource resource, ClassName name)
    {
        ASTObjectDefinition ast = getAST(resource);
        Source source = getSource(resource, ast);
        LineMapping lineMapping = ParserState.getState().getLineMapping();
        try
        {
            compile(source, name, lineMapping);
        }
        catch (IOException e)
        {
            throw new CompileException(ast, e.getMessage());
        }
        catch (CompileException e)
        {
            Token token = e.getToken();
            if (token == null)
            {
                throw e;
            }
            int rawLine = token.beginLine;
            String file = lineMapping.getFile(rawLine);
            int mappedLine = lineMapping.getLine(rawLine);
            throw new CompileException(file, mappedLine, e);
        }
        catch (LpcRuntimeException e)
        {
            throw new InternalError("While compiling " + resource + " - " + e.getMessage(), e);
        }
        return ast;
    }

    private CompiledDefinition<LpcObject> loadObjectDefinition(Resource resource, Class< ? extends LpcObject> cls)
    {
        long start = System.currentTimeMillis();
        String name = ClassNameMapper.dropExtension(resource.getPath());
        CompiledDefinition<LpcObject> definition = new CompiledDefinition<LpcObject>(_manager, _scope, name, cls);
        long end = System.currentTimeMillis();
        if (_printStats != null)
        {
            printStat("Object definition is " + definition + " (" + (end - start) + "ms)");
        }
        return definition;
    }

    private ASTObjectDefinition getAST(Resource resource)
    {
        long start = System.currentTimeMillis();
        ASTObjectDefinition ast = null;
        try
        {
            if (_printStats != null)
            {
                printStat("Compiling " + resource);
            }
            ast = parse(resource);
            long end = System.currentTimeMillis();
            if (_printStats != null)
            {
                printStat(resource.toString() + " is " + ast.toString() + " with " + ast.jjtGetNumChildren() + " children (" + (end - start) + "ms)");
            }
        }
        catch (ParserException e)
        {
            throw new CompileException(e);
        }
        catch (IOException e)
        {
            if (ast == null)
            {
                throw new CompileResourceException(resource, e);
            }
            else
            {
                throw new CompileException(ast, e.getMessage());
            }
        }
        return ast;
    }

    private Resource getResource(String objectSource)
    {
        try
        {
            return _sourceFinder.getResource(objectSource);
        }
        catch (IOException e)
        {
            throw new CompileException("Cannot access source " + objectSource, e);
        }
    }

    private void printStat(String message)
    {
        _printStats.println(new Date() + " - " + message);
    }

    @SuppressWarnings("unchecked")
    private Class< ? extends LpcObject> loadClass(SimpleNode ast, ClassName output)
    {
        long start = System.currentTimeMillis();
        String fqn = output.packageName + "." + output.className;
        try
        {
            Class< ? > cls = _classLoader.loadClass(fqn);
            if (LpcObject.class.isAssignableFrom(cls))
            {
                return (Class< ? extends LpcObject>) cls;
            }
            else
            {
                throw new CompileException(ast, "Internal Error - Generated class " + fqn + " is not an LpcObject");
            }
        }
        catch (ClassNotFoundException e)
        {
            throw new CompileException(ast, "Internal Error - Failed to load generated class " + fqn, e);
        }
        catch (ClassFormatError e)
        {
            throw new CompileException(ast, "Internal Error - Failed to load generated class " + fqn, e);
        }
        finally
        {
            long end = System.currentTimeMillis();
            if (_printStats != null)
            {
                printStat("Loading of class " + fqn + " (" + (end - start) + "ms)");
            }
        }
    }

    private void compile(Source source, ClassName name, LineMapping lineMapping) throws IOException
    {
        long start = System.currentTimeMillis();
        _compiler.compile(source, name, _store, lineMapping);
        long end = System.currentTimeMillis();

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Compiled " + source + " to " + name);
        }
        if (_printStats != null)
        {
            printStat("Produced output " + name + " (" + (end - start) + "ms)");
        }
    }

    private Source getSource(Resource resource, ASTObjectDefinition ast)
    {
        return new Source(resource.getPath(), ast);
    }

    private ASTObjectDefinition parse(Resource resource) throws IOException, ParserException
    {
        _parser.setSourceFinder(_sourceFinder);
        return _parser.parse(resource);
    }

    public CompilerObjectManager getObjectManager()
    {
        return _manager;
    }

    public ResourceFinder getSourceFinder()
    {
        return _sourceFinder;
    }
}
