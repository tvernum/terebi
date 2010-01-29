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

package us.terebi.plugins.crypt.efun;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.efun.AbstractEfun;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.StringValue;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;
import us.terebi.plugins.crypt.core.CryptMethod;

/**
 * 
 */
public class CryptEfun extends AbstractEfun implements FunctionSignature, Callable
{
    private static final String ASCII = "./#<>@,_=+~{}|0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private final CryptMethod _method;
    private final Random _random;

    public CryptEfun(CryptMethod method)
    {
        _method = method;
        _random = new Random();
    }

    //    string crypt( string str, string seed );
    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        ArrayList<ArgumentDefinition> list = new ArrayList<ArgumentDefinition>();
        list.add(new ArgumentSpec("str", Types.STRING));
        list.add(new ArgumentSpec("seed", Types.STRING));
        return list;
    }

    public boolean acceptsLessArguments()
    {
        return false;
    }

    public LpcType getReturnType()
    {
        return Types.STRING;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        checkArguments(arguments);
        String password = arguments.get(0).asString();

        LpcValue saltArg = arguments.get(1);
        String salt = saltArg.asBoolean() ? saltArg.asString() : randomSalt();

        String result = _method.crypt(password, salt);
        return new StringValue(result);
    }

    private String randomSalt()
    {
        char[] chars = new char[2];
        chars[0] = ASCII.charAt(_random.nextInt(ASCII.length()));
        chars[1] = ASCII.charAt(_random.nextInt(ASCII.length()));
        return new String(chars);
    }

}
