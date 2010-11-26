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

package us.terebi.lang.lpc.runtime.jvm.efun.file;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import us.terebi.lang.lpc.io.Resource;
import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.NilValue;
import us.terebi.lang.lpc.runtime.jvm.value.StringValue;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;
import us.terebi.util.io.IOUtil;

/**
 * 
 */
public class ReadFileEfun extends FileEfun implements FunctionSignature, Callable
{
    private final Logger LOG = Logger.getLogger(ReadFileEfun.class);

    //    string read_file( string file, int start_line, int number_of_lines );
    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        ArrayList<ArgumentDefinition> list = new ArrayList<ArgumentDefinition>();
        list.add(new ArgumentSpec("file", Types.STRING));
        list.add(new ArgumentSpec("start_line", Types.INT));
        list.add(new ArgumentSpec("number_of_lines", Types.INT));
        return list;
    }

    public boolean acceptsLessArguments()
    {
        return true;
    }

    public LpcType getReturnType()
    {
        return Types.STRING;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        checkArguments(arguments, 1);
        String file = arguments.get(0).asString();
        InputStream stream = null;
        try
        {
            Resource resource = getResource(file);
            stream = resource.read();
            if (!hasArgument(arguments, 1))
            {
                LOG.info("Reading entire contents of file " + resource);
                return readFile(stream);
            }
            long start = arguments.get(1).asLong();
            long end = hasArgument(arguments, 2) ? start + arguments.get(2).asLong() : Long.MAX_VALUE;
            LOG.info("Reading lines " + start + '-' + end + " of file " + resource);
            return readLines(stream, start, end);
        }
        catch (IOException e)
        {
            LOG.info("Cannot read file " + file, e);
            return NilValue.INSTANCE;
        }
        finally
        {
            IOUtils.closeQuietly(stream);
        }
    }

    private LpcValue readLines(InputStream stream, long start, long end)
    {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (String line : IOUtil.lines(stream))
        {
            i++;
            if (i >= start)
            {
                builder.append(line);
            }
            if (i == end)
            {
                break;
            }
        }
        return new StringValue(builder);
    }

    private LpcValue readFile(InputStream stream) throws IOException
    {
        String content = IOUtils.toString(stream);
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Read " + content.length() + " chars from stream " + stream);
        }
        return new StringValue(content);
    }

}
