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

package us.terebi.lang.lpc.runtime.jvm.efun;

import java.util.ArrayList;
import java.util.List;

import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;

/**
 * 
 */
public class GetDirectoryInfoEfun extends AbstractEfun implements FunctionSignature, Callable
{
    //    mixed array get_dir(string dir);
    //
    //    mixed array get_dir(string dir, int flag);
    //
    //    If `dir' is a filename ('*' and '?' wildcards are supported), an array of 
    //    strings is returned containing all filenames that match the specification. 
    //    If `dir' is a directory name (ending with a slash--ie: "/u/", "/adm/", etc),
    //    all filenames in that directory are returned.  
    //
    //    If called with a second argument equal to -1, get_dir will return an array
    //    of subarrays, where the format of each subarray is:
    //
    //      ({ filename, size_of_file, last_time_file_touched })
    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        ArrayList<ArgumentDefinition> list = new ArrayList<ArgumentDefinition>();
        list.add(new ArgumentSpec("dir", Types.STRING));
        list.add(new ArgumentSpec("flag", Types.INT));
        return list;
    }

    public boolean acceptsLessArguments()
    {
        return true;
    }

    public LpcType getReturnType()
    {
        return Types.MIXED_ARRAY;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        // @TODO Auto-generated method stub
        return null;
    }

}
