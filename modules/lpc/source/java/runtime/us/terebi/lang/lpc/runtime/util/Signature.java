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

package us.terebi.lang.lpc.runtime.util;

import java.util.List;

import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.LpcType;

/**
 * 
 */
public class Signature implements FunctionSignature
{
    private final LpcType _returnType;
    private final List< ? extends ArgumentDefinition> _arguments;
    private final boolean _varargs;

    public Signature(boolean varargs, LpcType returnType, List< ? extends ArgumentDefinition> arguments)
    {
        _varargs = varargs;
        _returnType = returnType;
        _arguments = arguments;
    }

    public List< ? extends ArgumentDefinition> getArguments()
    {
        return _arguments;
    }

    public LpcType getReturnType()
    {
        return _returnType;
    }

    public boolean isVarArgs()
    {
        return _varargs;
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        if (_varargs)
        {
            builder.append("varargs ");
        }
        builder.append(_returnType);
        builder.append(" ( ");
        for (ArgumentDefinition arg : _arguments)
        {
            builder.append(arg);
            builder.append(' ');
        }
        builder.append(')');
        return builder.toString();
    }

}
