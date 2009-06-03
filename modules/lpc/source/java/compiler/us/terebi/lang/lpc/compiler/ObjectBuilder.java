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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.jci.compilers.CompilationResult;
import org.apache.commons.jci.compilers.JavaCompiler;
import org.apache.commons.jci.compilers.JavaCompilerSettings;
import org.apache.commons.jci.readers.FileResourceReader;
import org.apache.commons.jci.stores.FileResourceStore;
import org.apache.commons.jci.stores.ResourceStore;
import org.apache.commons.jci.stores.ResourceStoreClassLoader;

import us.terebi.lang.lpc.compiler.java.context.CompiledObjectDefinition;
import us.terebi.lang.lpc.compiler.java.context.ObjectManager;
import us.terebi.lang.lpc.io.Resource;
import us.terebi.lang.lpc.io.ResourceFinder;
import us.terebi.lang.lpc.parser.LineMapping;
import us.terebi.lang.lpc.parser.LpcParser;
import us.terebi.lang.lpc.parser.ParserState;
import us.terebi.lang.lpc.parser.ast.ASTObjectDefinition;
import us.terebi.lang.lpc.parser.ast.SimpleNode;
import us.terebi.lang.lpc.parser.jj.Token;
import us.terebi.lang.lpc.preprocessor.LexerException;
import us.terebi.lang.lpc.runtime.jvm.LpcObject;
import us.terebi.lang.lpc.runtime.jvm.context.ScopeLookup;
import us.terebi.lang.lpc.runtime.jvm.object.CompiledObject;
import us.terebi.util.ToString;

/**
 * 
 */
public class ObjectBuilder implements ObjectCompiler
{
    private final ResourceFinder _sourceFinder;
    private final Compiler _compiler;
    private final File _workingDirectory;
    private final JavaCompiler _javaCompiler;
    private final LpcParser _parser;
    private final FileResourceReader _reader;
    private final FileResourceStore _store;
    private final ClassLoader _classLoader;
    private final ObjectManager _manager;
    private final ScopeLookup _scope;
    private boolean _compileOnly;

    public ObjectBuilder(ResourceFinder sourceFinder, ObjectManager manager, ScopeLookup scope, LpcParser parser,
            Compiler compiler, JavaCompiler javaCompiler, File workingDirectory)
    {
        _sourceFinder = sourceFinder;
        _manager = manager;
        _scope = scope;
        _parser = parser;
        _compiler = compiler;
        _javaCompiler = javaCompiler;
        _workingDirectory = workingDirectory;
        _reader = new FileResourceReader(_workingDirectory);
        _store = new FileResourceStore(_workingDirectory);
        _classLoader = new ResourceStoreClassLoader(getClass().getClassLoader(), new ResourceStore[] { _store });
    }

    public CompiledObjectDefinition compile(String objectSource)
    {
        ASTObjectDefinition ast = null;
        try
        {
            Resource resource = _sourceFinder.getResource(objectSource);
            ast = parse(resource);
        }
        catch (LexerException e)
        {
            throw new CompileException((Token) null, e.getMessage());
        }
        catch (IOException e)
        {
            throw new CompileException(ast, e.getMessage());
        }
        LineMapping lineMapping = ParserState.getState().getLineMapping();
        try
        {
            Source source = getSource(objectSource, ast);
            Output output = getOutput(objectSource);
            compile(source, output);
            if (_compileOnly)
            {
                return null;
            }
            compileToByteCode(ast, output);
            Class< ? extends LpcObject> cls = loadClass(ast, output);
            return new CompiledObject(_manager, _scope, objectSource, cls);
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
    }

    @SuppressWarnings("unchecked")
    private Class< ? extends LpcObject> loadClass(SimpleNode ast, Output output)
    {
        String fqn = output.getPackageName() + "." + output.getClassName();
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
            throw new CompileException(ast, "Internal Error - Failed to load generated class " + fqn);
        }
    }

    private void compileToByteCode(SimpleNode ast, Output output)
    {
        JavaCompilerSettings settings = _javaCompiler.createDefaultSettings();
        settings.setSourceVersion("1.5");
        settings.setTargetVersion("1.5");
        String fileName = output.getFile().getAbsolutePath();
        String root = _workingDirectory.getAbsolutePath();
        if (fileName.startsWith(root))
        {
            fileName = fileName.substring(root.length());
            if (fileName.charAt(0) == '/')
            {
                fileName = fileName.substring(1);
            }
        }
        CompilationResult result = _javaCompiler.compile(new String[] { fileName }, _reader, _store, _classLoader, settings);
        if (result.getErrors() != null && result.getErrors().length != 0)
        {
            throw new CompileException(ast, "Internal Error" + ToString.toString(result.getErrors()));
        }
    }

    private void compile(Source source, Output output) throws IOException
    {
        _compiler.compile(source, output);
    }

    private Source getSource(String objectSource, ASTObjectDefinition ast)
    {
        return new Source(objectSource, ast);
    }

    private Output getOutput(String objectSource)
    {
        String path = FilenameUtils.getFullPath(objectSource);
        String pkg = "lpc." + path.replace('/', '.');
        pkg = pkg.replace("..", ".");
        if (pkg.endsWith("."))
        {
            pkg = pkg.substring(0, pkg.length() - 1);
        }

        String cls = FilenameUtils.getBaseName(objectSource);
        cls.replaceAll("[^A-Za-z0-9]", "_");

        String fileName = "lpc/" + path + '/' + cls + ".java";
        File javaFile = new File(_workingDirectory, fileName);
        javaFile.getParentFile().mkdirs();
        Output output = new Output(pkg, cls, javaFile);
        return output;
    }

    private ASTObjectDefinition parse(Resource resource) throws IOException, LexerException
    {
        _parser.setSourceFinder(_sourceFinder);
        return _parser.parse(resource);
    }

    public void setCompileOnly(boolean compileOnly)
    {
        _compileOnly = compileOnly;
    }
}
