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
import java.io.OutputStream;
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
import us.terebi.lang.lpc.runtime.jvm.LpcConstants;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;

/**
 * 
 */
public class WriteFileEfun extends FileEfun implements FunctionSignature, Callable
{
    private final Logger LOG = Logger.getLogger(WriteFileEfun.class);

    // int write_file( string file, string str, int flag );
    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        ArrayList<ArgumentDefinition> list = new ArrayList<ArgumentDefinition>();
        list.add(new ArgumentSpec("file", Types.STRING));
        list.add(new ArgumentSpec("content", Types.STRING));
        list.add(new ArgumentSpec("flag", Types.INT));
        return list;
    }

    public boolean acceptsLessArguments()
    {
        return true;
    }

    public LpcType getReturnType()
    {
        return Types.INT;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        checkArguments(arguments, 2);
        String file = arguments.get(0).asString();
        String content = arguments.get(1).asString();
        boolean overwrite = (arguments.size() > 2 ? arguments.get(2).asBoolean() : false);
        OutputStream stream = null;
        try
        {
            Resource resource = getResource(file);
            if (LOG.isDebugEnabled())
            {
                LOG.debug((overwrite ? "Write" : "Append") + ' ' + content.length() + " chars to " + resource);
            }
            if (overwrite)
            {
                stream = resource.write();
            }
            else
            {
                stream = resource.append();
            }
            write(stream, content);
            return LpcConstants.INT.TRUE;
        }
        catch (IOException e)
        {
            LOG.info("Cannot write to file " + file, e);
            return LpcConstants.INT.FALSE;
        }
        finally
        {
            IOUtils.closeQuietly(stream);
        }
    }

    private void write(OutputStream stream, String content) throws IOException
    {
        IOUtils.write(content, stream);
    }

}
