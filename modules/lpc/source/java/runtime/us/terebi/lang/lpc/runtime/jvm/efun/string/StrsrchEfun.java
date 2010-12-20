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
import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;
import us.terebi.lang.lpc.runtime.jvm.support.MiscSupport;
import us.terebi.lang.lpc.runtime.jvm.support.ValueSupport;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;

/**
 * 
 */
public class StrsrchEfun extends AbstractEfun implements FunctionSignature, Callable
{
    //    int strsrch( string str, string substr | int char, int flag );
    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        ArrayList<ArgumentDefinition> list = new ArrayList<ArgumentDefinition>();
        list.add(new ArgumentSpec("str", Types.STRING));
        list.add(new ArgumentSpec("pattern", Types.MIXED));
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
        String str = arguments.get(0).asString();
        LpcValue pattern = arguments.get(1);
        long flag = 0;
        if (arguments.size() >= 3)
        {
            flag = arguments.get(2).asLong();
        }
        if (MiscSupport.isString(pattern))
        {
            return ValueSupport.intValue(search(str, pattern.asString(), flag));
        }
        if (MiscSupport.isInteger(pattern))
        {
            return ValueSupport.intValue(search(str, pattern.asLong(), flag));
        }
        throw new LpcRuntimeException("Argument 2 to "
                + getName()
                + " must be either a string or a character (int) not "
                + pattern.getActualType());
    }

    private long search(String str, long charValue, long flag)
    {
        if (charValue > Character.MAX_VALUE)
        {
            return -1;
        }
        char ch = (char) charValue;
        if (flag == -1)
        {
            return str.lastIndexOf(ch);
        }
        else
        {
            return str.indexOf(ch);
        }
    }

    private long search(String str, String pattern, long flag)
    {
        if (flag == -1)
        {
            return str.lastIndexOf(pattern);
        }
        else
        {
            return str.indexOf(pattern);
        }
    }
}
