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
public class SortArrayEfun extends AbstractEfun implements FunctionSignature, Callable
{
    //  array sort_array( array arr, string fun, object ob, ... );
    //  array sort_array( array arr, function f, ... );
    //  array sort_array( array arr, int direction );    
    //  
    public List< ? extends ArgumentDefinition> getArguments()
    {
        ArrayList<ArgumentDefinition> list = new ArrayList<ArgumentDefinition>();
        list.add(new ArgumentSpec("array", Types.MIXED_ARRAY));
        list.add(new ArgumentSpec("func", Types.MIXED));
        list.add(new ArgumentSpec("args", Types.MIXED, false, true));
        return list;
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
