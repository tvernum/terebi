/* ------------------------------------------------------------------------
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

package us.terebi.plugins.crypt.internal;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import org.junit.Assert;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public class CryptTest
{
    private static final String CRYPT_TEXT = "crypt.text";

    @Test
    public void testSingleThread() throws Exception
    {
        List<String[]> items = readTestCases();

        Crypt crypt = new Crypt();
        runTests(items, crypt);

        System.out.println(String.valueOf(items.size()) + " tests executed");
    }

    @Test
    public void testMultipleThreads() throws Exception
    {
        final List<String[]> items = readTestCases();


        int threadCount = 4;
        final Error[] errors = new Error[threadCount];
        final int[] count = new int[threadCount];
        final Thread[] threads = new Thread[threadCount];

        final long until = System.currentTimeMillis() + 500;

        for (int i = 0; i < threadCount; i++)
        {
            final int index = i;
            threads[i] = new Thread("Crypt-" + (i+1))
            {
                public void run()
                {
                    Crypt crypt = new Crypt();
                    try
                    {
                        do
                        {
                            runTests(items, crypt);
                            count[index] += items.size();
                        } while (System.currentTimeMillis() < until);
                    }
                    catch (Error e)
                    {
                        errors[index] = e;
                        return;
                    }
                }
            };
            threads[i].start();
        }

        for (int i = 0; i < threadCount; i++)
        {
            threads[i].join();
            if (errors[i] != null)
            {
                throw errors[i];
            }
            System.out.println("Thread #" + (i + 1) + " executed " + count[i] + " tests");
        }

    }

    void runTests(List<String[]> items, Crypt crypt)
    {
        for (String[] parts : items)
        {
            String salt = parts[1];
            String password = parts[0];
            String expected = parts[2];

            String result = crypt.crypt(password, salt);
            assertEquals("Crypt(" + password + "," + salt + ")", expected, result);
        }
    }

    private List<String[]> readTestCases() throws IOException
    {
        InputStream stream = getClass().getResourceAsStream(CRYPT_TEXT);
        List<String[]> items = new ArrayList<String[]>();

        if (stream == null)
        {
            Assert.fail("Cannot find resource file " + CRYPT_TEXT);
        }
        try
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            for (;;)
            {
                String line = reader.readLine();
                if (line == null)
                {
                    break;
                }
                line = line.trim();
                if (line.length() == 0)
                {
                    continue;
                }
                String[] parts = line.split("\\s");
                if (parts.length != 3)
                {
                    Assert.fail("Line '" + line + "' does not contain 3 parts");
                }
                items.add(parts);
            }
        }
        finally
        {
            close(stream);
        }

        Assert.assertTrue("Insufficient test cases found", items.size() > 50);

        return items;
    }

    private void assertEquals(String message, String expected, String result)
    {
        if (expected.equals(result))
        {
            return;
        }
        if (result == null)
        {
            Assert.fail(message + " ; Expected '" + expected + "' ; Result is null");
        }
        if (result.length() > expected.length())
        {
            StringBuilder buf = new StringBuilder(message).append(" ; Expected '").append(expected).append("' ; Result is ").append(result);
            buf.append(" [");
            for (int i = expected.length(); i < result.length(); i++)
            {
                buf.append(Integer.toHexString(result.charAt(i)));
                buf.append(' ');
            }
            buf.setCharAt(buf.length() - 1, ']');
            Assert.fail(buf.toString());
        }
        else
        {
            Assert.assertEquals(message, expected, result);
        }
    }

    private void close(Closeable input)
    {
        try
        {
            input.close();
        }
        catch (IOException e)
        {
            // Ignore
        }
    }
}
