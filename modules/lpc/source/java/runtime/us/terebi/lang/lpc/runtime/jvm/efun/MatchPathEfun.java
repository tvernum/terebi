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
import java.util.Map;

import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.NilValue;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;

/**
 * 
 */
public class MatchPathEfun extends AbstractEfun implements FunctionSignature, Callable
{
    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        ArrayList<ArgumentDefinition> list = new ArrayList<ArgumentDefinition>();
        list.add(new ArgumentSpec("map", Types.MAPPING));
        list.add(new ArgumentSpec("str", Types.STRING));
        return list;
    }

    public LpcType getReturnType()
    {
        return Types.MIXED;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        checkArguments(arguments);
        Map<LpcValue, LpcValue> map = arguments.get(0).asMap();
        String target = arguments.get(1).asString();

        int matchLength = -1;
        LpcValue matchKey = null;
        for (LpcValue k : map.keySet())
        {
            String key = k.asString();

            if (matchLength > key.length())
            {
                continue;
            }
            if (isMatch(target, key))
            {
                matchLength = key.length();
                matchKey = k;
            }
        }

        if (matchKey != null)
        {
            return map.get(matchKey);
        }
        
        return NilValue.INSTANCE;
    }

    private boolean isMatch(String target, String key)
    {
        return (key.endsWith("/") && target.startsWith(key)) || key.equals(target);
    }

}
