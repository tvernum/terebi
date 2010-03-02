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

package us.terebi.lang.lpc.runtime.jvm.efun.string;

import java.util.ArrayList;
import java.util.List;

import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.efun.AbstractEfun;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.ArrayValue;
import us.terebi.lang.lpc.runtime.jvm.value.StringValue;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;

/**
 * 
 */
public class ExplodeEfun extends AbstractEfun implements FunctionSignature, Callable
{
    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        ArrayList<ArgumentDefinition> list = new ArrayList<ArgumentDefinition>();
        list.add(new ArgumentSpec("str", Types.STRING));
        list.add(new ArgumentSpec("delim", Types.STRING));
        return list;
    }

    public LpcType getReturnType()
    {
        return Types.STRING_ARRAY;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        //        eval return explode( "///AAA//BB/CCC///DDD/EE/", "/" );
        //        Result = ({ "", "", "AAA", "", "BB", "CCC", "", "", "DDD", "EE" })

        //        eval return explode("/A/B/C/////", "/");
        //        Result = ({ "A", "B", "C", "", "", "", "" })

        //        eval return explode("//A/B//" , "/" );
        //        Result = ({ "", "A", "B", "" })

        checkArguments(arguments);
        String str = arguments.get(0).asString();
        String delim = arguments.get(1).asString();

        List<LpcValue> parts = explode(str, delim);
        
        return new ArrayValue(Types.STRING_ARRAY, parts);
    }

    public static List<LpcValue> explode(String str, String delim)
    {
        int current = 0;

        if (str.startsWith(delim))
        {
            current = delim.length();
        }
        if (str.endsWith(delim))
        {
            str = str.substring(0, str.length() - delim.length());
        }

        List<LpcValue> parts = new ArrayList<LpcValue>();
        while (true)
        {
            int index = str.indexOf(delim, current);
            if (index == -1)
            {
                parts.add(new StringValue(str.substring(current)));
                break;
            }
            else
            {
                String part = str.substring(current, index);
                parts.add(new StringValue(part));
                current = index + delim.length();
            }
        }
        return parts;
    }
}
