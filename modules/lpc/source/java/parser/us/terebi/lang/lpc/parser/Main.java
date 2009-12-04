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

import us.terebi.lang.lpc.io.FileFinder;
import us.terebi.lang.lpc.io.Resource;
import us.terebi.lang.lpc.io.ResourceFinder;
import us.terebi.lang.lpc.parser.ast.ASTObjectDefinition;
import us.terebi.lang.lpc.parser.util.ASTUtil;

/**
 */
public class Main
{
    public static void main(String[] args) throws Exception
    {
        if (new Main().processArguments(args) == 0)
        {
            System.err.println("Usage: <java> " + Main.class.getName() + " [options] <files>");
        }
    }

    private LpcParser _parser = new LpcParser();
    private boolean _preprocessOnly;
    private ResourceFinder _sourceFinder;

    private int processArguments(String[] args) throws Exception
    {
        int count = 0;
        _sourceFinder = new FileFinder(new File("/"));
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

    private void processOption(String option) throws IOException
    {
        if (option.equals("d"))
        {
            _parser.setDebug(true);
            return;
        }

        char optionChar = option.charAt(0);
        if (optionChar == 'I' || optionChar == 'i' || optionChar == 'r' || optionChar == 'a')
        {
            if (option.length() == 1)
            {
                System.err.println("Option '" + option + "' requires an arugment.");
                return;
            }

            String file = option.substring(1);
            switch (optionChar)
            {
                case 'I':
                    _parser.addSystemIncludeDirectory(file);
                    break;

                case 'i':
                    _parser.addUserIncludeDirectory(file);
                    break;

                case 'r':
                    _sourceFinder = new FileFinder(new File(file));
                    _parser.setSourceFinder(_sourceFinder);
                    break;

                case 'a':
                    _parser.addAutoIncludeFile(file);
                    break;
            }
            return;
        }
        if (option.equals("E"))
        {
            _preprocessOnly = true;
            return;
        }

        System.err.println("Option '" + option + "' not understood.");
    }

    public void processFile(String filename) throws Exception
    {
        Resource resource = _sourceFinder.getResource(filename);
        if (_preprocessOnly)
        {
            System.out.println(_parser.preprocess(resource));
        }
        else
        {
            ASTObjectDefinition file = _parser.parse(resource);
            if (file != null)
            {
                ASTUtil.printTree(file);
            }
        }
    }

}
