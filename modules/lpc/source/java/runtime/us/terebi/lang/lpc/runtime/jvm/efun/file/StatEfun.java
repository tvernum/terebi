/* ------------------------------------------------------------------------
 * Copyright 2010 Tim Vernum
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
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import us.terebi.lang.lpc.io.Resource;
import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectDefinition;
import us.terebi.lang.lpc.runtime.jvm.LpcConstants;
import us.terebi.lang.lpc.runtime.jvm.context.ObjectManager;
import us.terebi.lang.lpc.runtime.jvm.context.RuntimeContext;
import us.terebi.lang.lpc.runtime.jvm.context.ThreadContext;
import us.terebi.lang.lpc.runtime.jvm.support.ValueSupport;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;

/**
 * 
 */
public class StatEfun extends FileEfun
{
    private final Logger LOG = Logger.getLogger(StatEfun.class);

    //    mixed stat(string str);
    //    mixed stat(string str, int x);
    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        ArrayList<ArgumentDefinition> list = new ArrayList<ArgumentDefinition>();
        list.add(new ArgumentSpec("file_or_dir", Types.STRING));
        list.add(new ArgumentSpec("flag", Types.INT));
        return list;
    }

    public boolean acceptsLessArguments()
    {
        return true;
    }

    public LpcType getReturnType()
    {
        return Types.MIXED;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        checkArguments(arguments);
        String file = getArgument(arguments, 0).asString();
        long flag = getArgument(arguments, 1).asLong();
        try
        {
            Resource resource = getResource(file);
            if (flag == -1 || resource.isDirectory())
            {
                List<LpcValue> info = GetDirectoryInfoEfun.getDirectoryInfo(this, file, flag);
                return ValueSupport.arrayValue(info);
            }
            else
            {
                return statFile(resource);
            }
        }
        catch (IOException e)
        {
            LOG.warn(e);
            return LpcConstants.ARRAY.EMPTY;
        }
    }

    private LpcValue statFile(Resource resource)
    {
        //  ({ file_size, last_time_file_touched, time_object_loaded })

        ThreadContext context = RuntimeContext.obtain();
        ObjectManager manager = context.system().objectManager();
        ObjectDefinition definition = manager.findObject(resource.getPath(), false);

        long loadTime = (definition == null ? -1 : definition.getMasterInstance().getCreationTime());

        LpcValue[] array = new LpcValue[] //
        { ValueSupport.intValue(resource.getSizeInBytes()), //
                ValueSupport.intValue(resource.lastModified()), //
                ValueSupport.intValue(loadTime) };

        return ValueSupport.arrayValue(array);
    }
}
