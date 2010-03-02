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

import java.util.List;

import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.efun.AbstractStringEfun;
import us.terebi.lang.lpc.runtime.jvm.value.StringValue;

/**
 * 
 */
public class CapitalizeEfun extends AbstractStringEfun implements FunctionSignature, Callable
{
    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        checkArguments(arguments);
        LpcValue arg = arguments.get(0);
        String str = arg.asString();
        if (str.length() == 0 || !Character.isLowerCase(str.charAt(0)))
        {
            return arg;
        }
        return new StringValue(Character.toUpperCase(str.charAt(0)) + str.substring(1));
    }
}
