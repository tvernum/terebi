/* ------------------------------------------------------------------------
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

package us.terebi.plugins.parser.efun;

import java.util.Arrays;
import java.util.List;

import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.efun.AbstractEfun;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.VoidValue;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public class ParseMyRulesEfun extends AbstractEfun implements FunctionSignature
{
    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        return Arrays.asList( //
                new ArgumentSpec("arg1", Types.STRING), //
                new ArgumentSpec("arg2", Types.STRING), //
                new ArgumentSpec("arg3", Types.STRING) //
        );
    }

    public LpcType getReturnType()
    {
        return Types.VOID;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        // @TODO Auto-generated method stub
        return VoidValue.INSTANCE;
    }

}
