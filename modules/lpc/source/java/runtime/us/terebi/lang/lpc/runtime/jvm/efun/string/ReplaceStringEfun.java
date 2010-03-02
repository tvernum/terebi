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
import us.terebi.lang.lpc.runtime.jvm.value.StringValue;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;

import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isNil;

/**
 * 
 */
public class ReplaceStringEfun extends AbstractEfun implements FunctionSignature, Callable
{
    //    string replace_string( string str, string pattern, string replace, [int first], [int last] );
    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        ArrayList<ArgumentDefinition> list = new ArrayList<ArgumentDefinition>();
        list.add(new ArgumentSpec("str", Types.STRING));
        list.add(new ArgumentSpec("pattern", Types.STRING));
        list.add(new ArgumentSpec("replace", Types.STRING));
        list.add(new ArgumentSpec("first", Types.INT));
        list.add(new ArgumentSpec("last", Types.INT));
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
        checkArguments(arguments, 3);
        String str = arguments.get(0).asString();
        String pattern = arguments.get(1).asString();
        String replace = arguments.get(2).asString();

        LpcValue first = arguments.get(3);
        LpcValue last = arguments.get(4);

        return new StringValue(execute(str, pattern, replace, first, last));
    }

    private CharSequence execute(String str, String pattern, String replace, LpcValue first, LpcValue last)
    {
        if (isNil(first))
        {
            return replace(str, pattern, replace, 0, 0);
        }
        if (isNil(last))
        {
            return replace(str, pattern, replace, 0, first.asLong());
        }
        return replace(str, pattern, replace, first.asLong(), last.asLong());
    }

    private CharSequence replace(String str, String pattern, String replace, long first, long last)
    {
        if (first > last)
        {
            return str;
        }

        if (last == 0)
        {
            last = Long.MAX_VALUE;
        }

        if (first <= 1 && last > str.length())
        {
            return str.replace(pattern, replace);
        }

        int patternLength = pattern.length();

        StringBuilder builder = new StringBuilder();
        int count = 0;
        int pos = 0;
        while (count < last)
        {
            int index = str.indexOf(pattern, pos);
            if (index == -1)
            {
                break;
            }
            count++;
            builder.append(str.subSequence(pos, index));
            if (count < first)
            {
                builder.append(pattern);
            }
            else
            {
                builder.append(replace);
            }
            pos = index + patternLength;
        }
        builder.append(str.substring(pos));

        return builder;
    }
}
