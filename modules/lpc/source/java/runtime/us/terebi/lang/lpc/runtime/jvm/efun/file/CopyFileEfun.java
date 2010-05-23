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
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import us.terebi.lang.lpc.io.Resource;
import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.LpcConstants;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.IntValue;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;

/**
 * 
 */
public class CopyFileEfun extends FileEfun implements FunctionSignature, Callable
{
    // int cp(string src, string dst);
    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        ArrayList<ArgumentDefinition> list = new ArrayList<ArgumentDefinition>();
        list.add(new ArgumentSpec("src", Types.STRING));
        list.add(new ArgumentSpec("dst", Types.STRING));
        return list;
    }

    public boolean acceptsLessArguments()
    {
        return false;
    }

    public LpcType getReturnType()
    {
        return Types.INT;
    }

    public IntValue execute(List< ? extends LpcValue> arguments)
    {
        checkArguments(arguments);
        String src = arguments.get(0).asString();
        String dst = arguments.get(1).asString();

        InputStream input = null;
        OutputStream output = null;
        try
        {
            Resource source = getResource(src);
            try
            {
                input = source.read();
            }
            catch (Exception e)
            {
                return LpcConstants.INT.MINUS_ONE;
            }

            Resource destination = getResource(dst);
            try
            {
                output = destination.write();
            }
            catch (Exception e)
            {
                return LpcConstants.INT.MINUS_TWO;
            }

            IOUtils.copy(input, output);
            return LpcConstants.INT.ONE;
        }
        catch (IOException e)
        {
            return LpcConstants.INT.MINUS_THREE;
        }
        finally
        {
            IOUtils.closeQuietly(input);
            IOUtils.closeQuietly(output);
        }
    }

}
