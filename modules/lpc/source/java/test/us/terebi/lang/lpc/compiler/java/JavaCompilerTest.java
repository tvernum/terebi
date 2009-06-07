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

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.runner.RunWith;

import us.terebi.lang.lpc.compiler.ObjectOutput;
import us.terebi.lang.lpc.compiler.Source;
import us.terebi.lang.lpc.compiler.java.context.CompilerObjectManager;
import us.terebi.lang.lpc.compiler.java.context.FunctionMap;
import us.terebi.lang.lpc.compiler.java.context.ObjectManager;
import us.terebi.lang.lpc.compiler.java.test.ObjectTestObject;
import us.terebi.lang.lpc.compiler.java.test.SwordTestObject;
import us.terebi.lang.lpc.parser.ast.ASTObjectDefinition;
import us.terebi.lang.lpc.parser.jj.ParseException;
import us.terebi.lang.lpc.parser.jj.Parser;
import us.terebi.lang.lpc.runtime.jvm.StandardEfuns;
import us.terebi.lang.lpc.runtime.jvm.context.BasicScopeLookup;
import us.terebi.lang.lpc.runtime.jvm.object.CompiledObject;
import us.terebi.test.TestSuite;
import us.terebi.test.TestSuiteRunner;

/**
 * 
 */
@RunWith(TestSuiteRunner.class)
public class JavaCompilerTest implements Callable<Object>
{
    private final File _lpcFile;

    private final class TestOutput implements ObjectOutput
    {
        private final OutputStream _stream;
        private final String _class;

        TestOutput(String cls, OutputStream stream)
        {
            _class = cls;
            _stream = stream;
        }

        public OutputStream open()
        {
            return _stream;
        }

        public String getPackageName()
        {
            return "us.terebi.lang.lpc.test";
        }

        public String getClassName()
        {
            return _class;
        }
    }

    public JavaCompilerTest(File lpcFile)
    {
        _lpcFile = lpcFile;
    }

    public String toString()
    {
        return _lpcFile.getName();
    }
    
    public Object call() throws Exception
    {
        testOutputOfCompiler(_lpcFile);
        return null;
    }

    private void testOutputOfCompiler(File lpc) throws Exception
    {
        String base = lpc.getName().substring(0, lpc.getName().indexOf('.'));

        ASTObjectDefinition ast = parseFile(lpc);
        String result = compileToJava(new Source(lpc.getPath(), ast), base);
        String expected = readExpectedOutput(lpc.getParentFile(), base);

        assertMatching(expected, result);
    }

    private void assertMatching(String expected, String rawResult)
    {
        String result = compressWhiteSpace(rawResult);
        expected = compressWhiteSpace(expected);

        for (int i = 0; i < expected.length(); i++)
        {
            if (result.length() <= i)
            {
                Assert.fail("Expected string to end with '" + expected.substring(i) + "' but '" + result + "' does not");
            }
            char e = expected.charAt(i);
            char r = result.charAt(i);
            if (e == r)
            {
                continue;
            }
            if (e == '#' && Character.isDigit(r))
            {
                continue;
            }
            if (e == '%' && Character.isLetter(r))
            {
                continue;
            }
            System.out.println(rawResult);
            Assert.fail("Strings differ at character #"
                    + i
                    + " expected "
                    + e
                    + " ("
                    + substring(expected, i)
                    + ") but was "
                    + r
                    + "("
                    + substring(result, i)
                    + ")");
        }
    }

    private CharSequence substring(String string, int around)
    {
        int start = around - 10;
        int end = around + 10;
        String prefix = "..";
        String suffix = "..";
        if (start <= 0)
        {
            start = 0;
            prefix = "";
        }
        if (end >= string.length())
        {
            end = string.length();
            suffix = "";
        }
        return prefix + string.substring(start, end) + suffix;
    }

    private String compressWhiteSpace(String string)
    {
        return string.replace('\n', ' ').replaceAll("\\s+", " ");
    }

    private String readExpectedOutput(File directory, String base) throws FileNotFoundException, IOException
    {
        File java = new File(directory, base + ".java");
        FileInputStream input = new FileInputStream(java);
        String string = IOUtils.toString(input);
        return string;
    }

    private String compileToJava(Source source, String className) throws IOException
    {
        FunctionMap efuns = StandardEfuns.getSignatures();
        ObjectManager manager = new CompilerObjectManager();
        JavaCompiler compiler = new JavaCompiler(efuns, manager);
        manager.registerObject(new CompiledObject(manager, new BasicScopeLookup(manager), "/std/lib/object",
                ObjectTestObject.class));
        manager.registerObject(new CompiledObject(manager, new BasicScopeLookup(manager), "/std/lib/weapons/sword",
                SwordTestObject.class));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        compiler.compile(source, new TestOutput(className, output));
        return output.toString();
    }

    private ASTObjectDefinition parseFile(File lpc) throws FileNotFoundException, ParseException
    {
        FileInputStream input = new FileInputStream(lpc);
        try
        {
            Parser parser = new Parser(input);
            ASTObjectDefinition ast = parser.ObjectDefinition();
            return ast;
        }
        finally
        {
            close(input);
        }
    }

    private void close(Closeable closeable)
    {
        try
        {
            closeable.close();
        }
        catch (IOException e)
        {
            // Ignore
        }
    }

    @TestSuite
    public static List<JavaCompilerTest> suite()
    {
        File testDir = new File("source/lpc/test/us/terebi/lang/lpc/compiler/java/");
        File[] files = testDir.listFiles();
        List<JavaCompilerTest> list = new ArrayList<JavaCompilerTest>(files.length);

        for (File file : files)
        {
            if (file.getName().endsWith(".c"))
            {
                list.add(new JavaCompilerTest(file));
            }
        }
        Assert.assertTrue("No files found to test", list.size() > 0);
        return list;
    }
}
