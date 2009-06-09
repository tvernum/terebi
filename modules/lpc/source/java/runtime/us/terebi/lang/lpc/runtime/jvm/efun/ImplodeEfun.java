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
import us.terebi.lang.lpc.runtime.jvm.value.StringValue;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;

import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isString;

/**
 * 
 */
public class ImplodeEfun extends AbstractEfun implements FunctionSignature, Callable
{
    public List< ? extends ArgumentDefinition> getArguments()
    {
        // @TODO, this really has 3 signatures, rather than being "MIXED" and "VARARGS"
        ArrayList<ArgumentDefinition> list = new ArrayList<ArgumentDefinition>();
        list.add(new ArgumentSpec("array", Types.MIXED_ARRAY));
        list.add(new ArgumentSpec("delim", Types.MIXED));
        list.add(new ArgumentSpec("start", Types.MIXED));
        return list;
    }

    public boolean isVarArgs()
    {
        return true;
    }

    public LpcType getReturnType()
    {
        return Types.STRING;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        LpcValue delim = arguments.get(1);
        if (arguments.size() >= 2 && isString(delim))
        {
            return new StringValue(implodeStrings(arguments.get(0).asList(), arguments.get(1).asString()));
        }
        throw new UnsupportedOperationException("implode function - Not implemented");
    }

    public static CharSequence implodeStrings(List<LpcValue> array, String delim)
    {
        StringBuilder result = new StringBuilder();
        for (LpcValue element : array)
        {
            if (isString(element))
            {
                if (result.length() > 0)
                {
                    result.append(delim);
                }
                result.append(element.asString());
            }
        }
        return result;
    }

}
