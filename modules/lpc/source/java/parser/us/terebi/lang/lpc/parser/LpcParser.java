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

import us.terebi.lang.lpc.parser.ast.ASTFile;
import us.terebi.lang.lpc.parser.jj.ParseException;
import us.terebi.lang.lpc.parser.jj.Parser;

import us.terebi.lang.lpc.preprocessor.ChrootFileSystem;
import us.terebi.lang.lpc.preprocessor.CppReader;
import us.terebi.lang.lpc.preprocessor.Feature;
import us.terebi.lang.lpc.preprocessor.FileLexerSource;
import us.terebi.lang.lpc.preprocessor.LexerException;
import us.terebi.lang.lpc.preprocessor.Preprocessor;
import us.terebi.lang.lpc.preprocessor.PreprocessorListener;
import us.terebi.lang.lpc.preprocessor.Source;
import org.apache.commons.io.IOUtils;

/*
 * @TODO
 *    Add predefined macros (constants), e.g. __SAVE_EXTENSION__ (Partially done)
 *    Auto-include file
 */
public class LpcParser
{
    public class Listener extends PreprocessorListener
    {
        public void handleWarning(Source source, int line, int column, String msg) throws LexerException
        {
            System.out.println("Preprocessor warning: " + source.toString() + ":" + line + ":" + column + " - " + msg);
        }
    }

    private boolean _debug;
    private Preprocessor _preprocessor;
    private File _root;

    public LpcParser()
    {
        _preprocessor = new Preprocessor();
        _preprocessor.addFeature(Feature.LINEMARKERS);
    }

    public void setDebug(boolean debug)
    {
        _debug = debug;
    }

    public void addSystemIncludeDirectory(String dir)
    {
        _preprocessor.getSystemIncludePath().add(dir);
    }

    public void addUserIncludeDirectory(String dir)
    {
        _preprocessor.getQuoteIncludePath().add(dir);
    }

    public void addAutoIncludeFile(String filename) throws IOException
    {
        FileLexerSource source = getSource(filename);
        _preprocessor.addInput(source);
    }

    public void setFileSystemRoot(File root)
    {
        _root = root;
    }

    public ASTFile parse(String filename) throws IOException, LexerException
    {
        LineMapping mapping = new LineMapping(filename);
        new ParserState(this, mapping);

        String content = preprocess(filename);

        Parser parser = new Parser(new StringReader(content));
        parser.setDebug(_debug);
        try
        {
            return parser.File();
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

                System.err.println(err);
            }
            pe.printStackTrace(System.err);
            return null;
        }
    }

    public String preprocess(String filename) throws IOException, LexerException
    {
        if (_root != null)
        {
            _preprocessor.setFileSystem(new ChrootFileSystem(_root));
        }

        String dirname = dirname(filename);

        _preprocessor.addMacro("__SAVE_EXTENSION__", "\".o\"");
        _preprocessor.addMacro("__DIR__", "\"" + dirname + "\"");
        _preprocessor.addInput(getSource(filename));

        _preprocessor.setListener(new Listener());
        CppReader reader = new CppReader(_preprocessor);
        String content = IOUtils.toString(reader);
        return content;
    }

    private FileLexerSource getSource(String filename) throws IOException
    {
        File file = getFileInRoot(filename);
        return new FileLexerSource(file, filename);
    }

    private File getFileInRoot(String filename)
    {
        File file;
        if (_root != null)
        {
            file = new File(_root, filename);
        }
        else
        {
            file = new File(filename);
        }
        return file;
    }

    private String dirname(String filename)
    {
        int idx = filename.lastIndexOf('/');
        if (idx == -1)
        {
            return "/";
        }
        String dirname = filename.substring(0, idx + 1);
        if (dirname.charAt(0) != '/')
        {
            dirname = "/" + dirname;
        }
        return dirname;
    }

}
