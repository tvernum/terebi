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

package us.terebi.lang.lpc.runtime.jvm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.AbstractValue;
import us.terebi.lang.lpc.runtime.jvm.value.NilValue;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;
import us.terebi.lang.lpc.runtime.util.Signature;

/**
 * 
 */
public abstract class LpcFunction extends AbstractValue implements LpcValue, Callable
{
    private final FunctionSignature _signature;
    private final boolean _strict;

    public LpcFunction(int argumentCount)
    {
        this(argumentCount, false);
    }

    public LpcFunction(ArgumentDefinition... arguments)
    {
        this(new Signature(false, Types.MIXED, Arrays.asList(arguments)));
    }

    public LpcFunction(FunctionSignature signature)
    {
        _signature = signature;
        _strict = true;
    }

    public LpcFunction(int argumentCount, boolean strictCount)
    {
        List<ArgumentDefinition> args = new ArrayList<ArgumentDefinition>();
        for (int i = 1; i <= argumentCount; i++)
        {
            args.add(new ArgumentSpec("$" + i, Types.MIXED));
        }
        _signature = new Signature(true, Types.MIXED, args);
        _strict = strictCount;
    }

    protected CharSequence getDescription()
    {
        return "function";
    }

    public LpcType getActualType()
    {
        return Types.FUNCTION;
    }

    public Callable asCallable()
    {
        return this;
    }

    public LpcValue execute(LpcValue... arguments)
    {
        return execute(Arrays.asList(arguments));
    }

    public LpcValue getArg(List< ? extends LpcValue> args, int index)
    {
        if (index > args.size())
        {
            if (_strict)
            {
                throw new LpcRuntimeException("Insufficient number of arguments provided to function literal - argument "
                        + index
                        + " was expected");
            }else {
                return NilValue.INSTANCE;
            }
        }
        return args.get(index - 1);
    }

    public Kind getKind()
    {
        return Kind.FUNCTION;
    }

    public FunctionSignature getSignature()
    {
        return _signature;
    }

    protected int valueHashCode()
    {
        return getClass().hashCode();
    }

    protected boolean valueEquals(LpcValue other)
    {
        return this.getClass() == other.getClass();
    }

}
