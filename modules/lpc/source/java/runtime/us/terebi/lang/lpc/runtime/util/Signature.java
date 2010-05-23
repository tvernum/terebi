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

import java.util.Iterator;
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

    public boolean hasVarArgsArgument()
    {
        for (ArgumentDefinition arg : _arguments)
        {
            if (arg.isVarArgs())
            {
                return true;
            }
        }
        return false;
    }

    public LpcType getReturnType()
    {
        return _returnType;
    }

    public boolean acceptsLessArguments()
    {
        return _varargs;
    }

    public boolean hasUnstructuredArguments()
    {
        return false;
    }

    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof Signature)
        {
            Signature other = (Signature) obj;
            if (this._returnType.equals(other._returnType) && this._varargs == other._varargs && this._arguments.size() == other._arguments.size())
            {
                Iterator< ? extends ArgumentDefinition> thisArgs = this._arguments.iterator();
                Iterator< ? extends ArgumentDefinition> otherArgs = other._arguments.iterator();
                while (thisArgs.hasNext())
                {
                    ArgumentDefinition thisArg = thisArgs.next();
                    ArgumentDefinition otherArg = otherArgs.next();

                    if (thisArg.isVarArgs() != otherArg.isVarArgs())
                    {
                        return false;
                    }
                    if (thisArg.getSemantics() != otherArg.getSemantics())
                    {
                        return false;
                    }
                    if (!thisArg.getType().equals(otherArg.getType()))
                    {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
        return false;
    }

    public int hashCode()
    {
        return _returnType.hashCode() ^ (_arguments.size() << (_varargs ? 2 : 4));
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
        Iterator< ? extends ArgumentDefinition> iterator = _arguments.iterator();
        while (iterator.hasNext())
        {
            ArgumentDefinition arg = iterator.next();
            builder.append(arg);
            if (iterator.hasNext())
            {
                builder.append(" , ");
            } else {
                builder.append(' ');
            }
        }
        builder.append(')');
        return builder.toString();
    }

}
