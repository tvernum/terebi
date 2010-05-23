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

package us.terebi.lang.lpc.parser;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import us.terebi.lang.lpc.io.FileFinder;
import us.terebi.lang.lpc.io.Resource;
import us.terebi.lang.lpc.io.ResourceFinder;
import us.terebi.lang.lpc.io.ResourceLexerSource;
import us.terebi.lang.lpc.io.SourceFinderFileSystem;
import us.terebi.lang.lpc.io.VirtualResource;
import us.terebi.lang.lpc.parser.ast.ASTObjectDefinition;
import us.terebi.lang.lpc.parser.jj.ParseException;
import us.terebi.lang.lpc.parser.jj.Parser;
import us.terebi.lang.lpc.preprocessor.CppReader;
import us.terebi.lang.lpc.preprocessor.Feature;
import us.terebi.lang.lpc.preprocessor.LexerException;
import us.terebi.lang.lpc.preprocessor.Preprocessor;
import us.terebi.lang.lpc.preprocessor.PreprocessorListener;
import us.terebi.lang.lpc.preprocessor.Source;
import us.terebi.lang.lpc.preprocessor.VirtualFile;

/*
 * @TODO
 *    Add predefined macros (constants), e.g. __SAVE_EXTENSION__ (Partially done)
 *    Auto-include file
 */
public class LpcParser
{
    public class Listener extends PreprocessorListener
    {
        public void handleWarning(Source source, int line, int column, String msg)
        {
            System.out.println("Preprocessor warning: " + source.toString() + ":" + line + ":" + column + " - " + msg);
        }
    }

    private boolean _debug;
    private ResourceFinder _sourceFinder;
    private final List<VirtualFile> _systemIncludePath;
    private final List<VirtualFile> _quoteIncludePath;
    private final List<Resource> _autoInclude;
    private final Map<String, String> _macros;

    public LpcParser()
    {
        _sourceFinder = new FileFinder(new File("/"));
        _systemIncludePath = new ArrayList<VirtualFile>();
        _quoteIncludePath = new ArrayList<VirtualFile>();
        _autoInclude = new ArrayList<Resource>();
        _macros = new HashMap<String, String>();
    }

    public void setDebug(boolean debug)
    {
        _debug = debug;
    }

    public void addSystemIncludeDirectory(String dir) throws IOException
    {
        VirtualFile vFile = getVirtualFile(dir);
        addSystemIncludeDirectory(vFile);
    }

    public void addSystemIncludeDirectory(Resource resource)
    {
        VirtualFile vFile = getVirtualFile(resource);
        addSystemIncludeDirectory(vFile);
    }

    private void addSystemIncludeDirectory(VirtualFile vFile)
    {
        _systemIncludePath.add(vFile);
    }

    private VirtualFile getVirtualFile(String dir) throws IOException
    {
        Resource resource = getResource(dir);
        VirtualFile vFile = getVirtualFile(resource);
        return vFile;
    }

    public void addUserIncludeDirectory(String dir) throws IOException
    {
        VirtualFile vFile = getVirtualFile(dir);
        _quoteIncludePath.add(vFile);
    }

    public void addAutoIncludeFile(String filename) throws IOException
    {
        Resource resource = getResource(filename);
        addAutoIncludeFile(resource);
    }

    public void addAutoIncludeFile(Resource resource)
    {
        _autoInclude.add(resource);
    }

    public void setFileSystemRoot(File root)
    {
        _sourceFinder = new FileFinder(root);
    }

    public void setSourceFinder(ResourceFinder sourceFinder)
    {
        _sourceFinder = sourceFinder;
    }

    public String preprocess(Resource resource) throws IOException, LexerException
    {
        Preprocessor preprocessor = new Preprocessor();
        preprocessor.addFeature(Feature.LINEMARKERS);
        preprocessor.addFeature(Feature.PRAGMAS);

        preprocessor.getSystemIncludePath().addAll(_systemIncludePath);
        preprocessor.getQuoteIncludePath().addAll(_quoteIncludePath);

        if (_sourceFinder != null)
        {
            preprocessor.setFileSystem(new SourceFinderFileSystem(_sourceFinder));
        }

        for (Map.Entry<String, String> entry : _macros.entrySet())
        {
            preprocessor.addMacro(entry.getKey(), entry.getValue());
        }

        String dirname = resource.getParentName();
        preprocessor.addMacro("__DIR__", "\"" + dirname + "\"");

        for (Resource auto : _autoInclude)
        {
            preprocessor.addInput(new ResourceLexerSource(auto));
        }
        preprocessor.addInput(getSource(resource));

        preprocessor.setListener(new Listener());
        CppReader reader = new CppReader(preprocessor);
        String content = IOUtils.toString(reader);
        return content;
    }

    private Resource getResource(String filename) throws IOException
    {
        return _sourceFinder.getResource(filename);
    }

    private ResourceLexerSource getSource(Resource resource) throws IOException
    {
        return new ResourceLexerSource(resource);
    }

    public ASTObjectDefinition parse(Resource resource) throws IOException, ParserException
    {
        LineMapping mapping = new LineMapping(resource.getPath());
        new ParserState(this, mapping);

        String content;
        try
        {
            content = preprocess(resource);
        }
        catch (LexerException e)
        {
            throw new ParserException("Error in preprocessor for resource " + resource + " - " + e.getMessage(), e);
        }

        Parser parser = new Parser(new StringReader(content));
        parser.setDebug(_debug);
        try
        {
            ASTObjectDefinition ast = parser.ObjectDefinition();
            new PragmaResolver().resolve(ast);
            return ast;
        }
        catch (ParseException pe)
        {
            if (pe.currentToken != null)
            {
                StringBuilder err = new StringBuilder("Syntax error at ");
                int inputLine = pe.currentToken.beginLine;
                String file = mapping.getFile(inputLine);
                if (file != null)
                {
                    err.append(file).append(':');
                }
                else
                {
                    err.append("line ");
                }
                int line = mapping.getLine(inputLine);
                err.append(line);
                throw new ParserException(file, line, pe);
            }
            throw new ParserException("Cannot parse resource " + resource, pe);
        }

    }

    public void addDefine(String name, String value)
    {
        _macros.put(name, value);
    }

    private VirtualFile getVirtualFile(Resource resource)
    {
        return new VirtualResource(resource);
    }

}
