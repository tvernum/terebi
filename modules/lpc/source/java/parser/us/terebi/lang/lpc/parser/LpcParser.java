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

import org.apache.commons.io.IOUtils;

import us.terebi.lang.lpc.io.FileFinder;
import us.terebi.lang.lpc.io.FileResource;
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
    private Preprocessor _preprocessor;
    private ResourceFinder _sourceFinder;

    public LpcParser()
    {
        _preprocessor = new Preprocessor();
        _preprocessor.addFeature(Feature.LINEMARKERS);
        _sourceFinder = new FileFinder(new File("/"));
    }

    public void setDebug(boolean debug)
    {
        _debug = debug;
    }

    public void addSystemIncludeDirectory(String dir) throws IOException
    {
        VirtualFile vFile = getVirtualFile(dir);
        _preprocessor.getSystemIncludePath().add(vFile);
    }

    private VirtualFile getVirtualFile(String dir) throws IOException
    {
        Resource resource = getResource(dir);
        VirtualFile vFile = getVirtualFile(resource);
        return vFile;
    }

    public void addUserIncludeDirectory(String dir) throws IOException
    {
        _preprocessor.getQuoteIncludePath().add(getVirtualFile(dir));
    }

    public void addAutoIncludeFile(String filename) throws IOException
    {
        ResourceLexerSource source = getSource(filename);
        _preprocessor.addInput(source);
    }

    public void setFileSystemRoot(File root)
    {
        _sourceFinder = new FileFinder(root);
    }

    public void setSourceFinder(ResourceFinder sourceFinder)
    {
        _sourceFinder = sourceFinder;
    }

    /**
     * @deprecated Use {@link #parse(Resource)} instead
     */
    public ASTObjectDefinition parse(String filename) throws IOException, ParserException
    {
        Resource resource = getResource(filename);
        return parse(resource);
    }

    public String preprocess(Resource resource) throws IOException, LexerException
    {
        if (_sourceFinder != null)
        {
            _preprocessor.setFileSystem(new SourceFinderFileSystem(_sourceFinder));
        }

        String dirname = resource.getParentName();

        _preprocessor.addMacro("__DIR__", "\"" + dirname + "\"");
        _preprocessor.addInput(getSource(resource));

        _preprocessor.setListener(new Listener());
        CppReader reader = new CppReader(_preprocessor);
        String content = IOUtils.toString(reader);
        return content;
    }

    private ResourceLexerSource getSource(String filename) throws IOException
    {
        return getSource(getResource(filename));
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
            return parser.ObjectDefinition();
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

    public void addDefine(String name, String value) throws LexerException
    {
        _preprocessor.addMacro(name, value);
    }

    public void addAutoIncludeFile(File auto) throws IOException
    {
        _preprocessor.addInput(getSource(new FileResource(auto)));
    }

    public void addSystemIncludeDirectory(File dir)
    {
        FileResource resource = new FileResource(dir);
        _preprocessor.getSystemIncludePath().add(getVirtualFile(resource));
    }

    private VirtualFile getVirtualFile(Resource resource)
    {
        return new VirtualResource(resource);
    }

}
