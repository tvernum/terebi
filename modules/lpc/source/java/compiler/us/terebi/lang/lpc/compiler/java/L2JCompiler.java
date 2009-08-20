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

package us.terebi.lang.lpc.compiler.java;

import java.io.File;
import java.io.IOException;

import us.terebi.lang.lpc.compiler.ObjectBuilder;
import us.terebi.lang.lpc.compiler.ObjectBuilderFactory;
import us.terebi.lang.lpc.compiler.java.context.CompiledObjectDefinition;
import us.terebi.lang.lpc.io.FileFinder;
import us.terebi.lang.lpc.io.Resource;
import us.terebi.lang.lpc.io.ResourceFinder;
import us.terebi.lang.lpc.parser.LpcParser;
import us.terebi.lang.lpc.preprocessor.LexerException;
import us.terebi.lang.lpc.runtime.ObjectDefinition;
import us.terebi.lang.lpc.runtime.jvm.StandardEfuns;

/**
 * 
 */
public class L2JCompiler
{
    public static void main(String[] args) throws Exception
    {
        L2JCompiler compiler = new L2JCompiler();
        if (compiler.processArguments(args) == 0)
        {
            System.err.println("Usage: <java> " + compiler.getClass().getName() + " [options] <files>");
        }
    }

    private LpcParser _parser = new LpcParser();
    private ResourceFinder _resourceFinder;
    private ObjectBuilder _builder;
    private boolean _compileOnly = false;

    private int processArguments(String[] args) throws Exception
    {
        int count = 0;
        for (int i = 0; i < args.length; i++)
        {
            if (args[i].length() > 1 && args[i].charAt(0) == '-')
            {
                processOption(args[i].substring(1));
            }
            else
            {
                count++;
                processFile(args[i]);
            }
        }
        return count;
    }

    private void processOption(String option) throws IOException, LexerException
    {
        if (option.equals("d"))
        {
            _parser.setDebug(true);
            return;
        }

        if (option.equals("c"))
        {
            _compileOnly = true;
            return;
        }

        char optionChar = option.charAt(0);
        if (optionChar == 'I' || optionChar == 'i' || optionChar == 'r' || optionChar == 'a' || optionChar == 'D')
        {
            if (option.length() == 1)
            {
                System.err.println("Option '" + option + "' requires an arugment.");
                return;
            }

            String arg = option.substring(1);
            switch (optionChar)
            {
                case 'I':
                    _parser.addSystemIncludeDirectory(arg);
                    break;

                case 'i':
                    _parser.addUserIncludeDirectory(arg);
                    break;

                case 'r':
                    _resourceFinder = new FileFinder(new File(arg));
                    _parser.setSourceFinder(_resourceFinder);
                    break;

                case 'a':
                    _parser.addAutoIncludeFile(arg);
                    break;

                case 'D':
                    {
                        int index = arg.indexOf('=');
                        if (index == -1)
                        {
                            _parser.addDefine(arg, "");
                        }
                        else
                        {
                            _parser.addDefine(arg.substring(0, index), arg.substring(index + 1));
                        }
                    }
            }
            return;
        }

        System.err.println("Option '" + option + "' not understood.");
    }

    public void processFile(String filename) throws Exception
    {
        Resource resource = _resourceFinder.getResource(filename);
        if (!resource.exists())
        {
            System.err.println("Cannot find file " + filename);
            return;
        }
        if (_builder == null)
        {
            ObjectBuilderFactory factory = new ObjectBuilderFactory(StandardEfuns.getImplementation());
            factory.setParser(_parser);
            _builder = factory.createBuilder(_resourceFinder);
            _builder.setCompileOnly(_compileOnly);
            _builder.setPrintStats(System.out);
        }

        CompiledObjectDefinition object = _builder.compile(filename);
        System.out.println("Filename " + filename + " = " + describe(object));
    }

    private CharSequence describe(CompiledObjectDefinition object)
    {
        if (object == null)
        {
            return "<null>";
        }

        StringBuilder builder = new StringBuilder();
        builder.append(object.getName());
        builder.append(" : ");
        builder.append(object.getImplementationClass());
        builder.append(" : inherits ");
        for (ObjectDefinition parent : object.getInheritedObjects().values())
        {
            builder.append(parent.getName());
            builder.append(",");
        }
        builder.append(" : ");
        builder.append(object.getMethods().size());
        builder.append(" method(s) : ");
        builder.append(object.getFields().size());
        builder.append(" field(s) : ");
        return builder;
    }
}
