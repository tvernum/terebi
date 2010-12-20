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
import java.util.Collection;
import java.util.List;

import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectDefinition;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.context.CallStack.Origin;
import us.terebi.lang.lpc.runtime.jvm.exception.InternalError;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.AbstractValue;
import us.terebi.lang.lpc.runtime.jvm.value.ArrayValue;
import us.terebi.lang.lpc.runtime.jvm.value.MappingValue;
import us.terebi.lang.lpc.runtime.jvm.value.NilValue;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;
import us.terebi.lang.lpc.runtime.util.InContext;
import us.terebi.lang.lpc.runtime.util.Signature;
import us.terebi.lang.lpc.runtime.util.InContext.Exec;

/**
 * 
 */
public abstract class LpcFunction extends AbstractValue implements LpcValue, Callable
{
    private final LpcRuntimeSupport _support;
    private final FunctionSignature _signature;
    private final boolean _strict;
    private ObjectInstance _owner;

    public LpcFunction(ObjectInstance owner, int argumentCount)
    {
        this(owner, argumentCount, false);
    }

    public LpcFunction(ObjectInstance owner, ArgumentDefinition... arguments)
    {
        this(owner, new Signature(false, Types.MIXED, Arrays.asList(arguments)));
    }

    public LpcFunction(ObjectInstance owner, FunctionSignature signature)
    {
        _signature = signature;
        _strict = true;
        _owner = owner;
        _support = new LpcRuntimeSupport();
    }

    public LpcFunction(ObjectInstance owner, int argumentCount, boolean strictCount)
    {
        List<ArgumentDefinition> args = new ArrayList<ArgumentDefinition>();
        for (int i = 1; i <= argumentCount; i++)
        {
            args.add(new ArgumentSpec("$" + i, Types.MIXED));
        }
        _signature = new Signature(true, Types.MIXED, args);
        _strict = strictCount;
        _owner = owner;
        _support = new LpcRuntimeSupport();
    }

    public LpcType getActualType()
    {
        return Types.FUNCTION;
    }

    public Callable asCallable()
    {
        return this;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        return execute(toArray(arguments));
    }

    public LpcValue execute(final LpcValue... arguments)
    {
        return InContext.execute(Origin.POINTER, _owner, new Exec<LpcValue>()
        {
            public LpcValue execute()
            {
                return invoke(arguments);
            }
        });
    }

    protected abstract LpcValue invoke(LpcValue[] arguments);

    private LpcValue[] toArray(List< ? extends LpcValue> arguments)
    {
        return arguments.toArray(new LpcValue[arguments.size()]);
    }

    public LpcValue getArg(LpcValue[] args, int index)
    {
        if (index > args.length)
        {
            if (_strict)
            {
                throw new LpcRuntimeException("Insufficient number of arguments provided to function literal - argument " + index + " was expected");
            }
            else
            {
                return NilValue.INSTANCE;
            }
        }
        if (index <= 0)
        {
            throw new InternalError("Attempt to get negative argument " + index + " in " + this);
        }
        return args[index - 1];
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

    public abstract String getLocation();

    protected CharSequence getDescription()
    {
        return "function:" + getLocation();
    }

    public CharSequence debugInfo()
    {
        return "function@" + getOwner() + '[' + getLocation() + ']' + getSignature().toString() + " { ... }";
    }

    public ObjectInstance getOwner()
    {
        return _owner;
    }

    public ObjectDefinition getOwnerDefinition()
    {
        return _owner.getDefinition();
    }

    public void setOwner(ObjectInstance owner)
    {
        _owner = owner;
    }

    public LpcValue call(Callable callable, Collection< ? extends LpcValue>... args)
    {
        return _support.call(callable, args);
    }

    public Callable efun(String name)
    {
        return _support.efun(name);
    }

    public ArrayValue makeArray(LpcValue... elements)
    {
        return _support.makeArray(elements);
    }

    public MappingValue makeMapping(LpcValue... elements)
    {
        return _support.makeMapping(elements);
    }

    public LpcValue makeValue()
    {
        return _support.makeValue();
    }

    public LpcValue makeValue(double value)
    {
        return _support.makeValue(value);
    }

    public LpcValue makeValue(int value)
    {
        return _support.makeValue(value);
    }

    public LpcValue makeValue(long value)
    {
        return _support.makeValue(value);
    }

    public LpcValue makeValue(Number value)
    {
        return _support.makeValue(value);
    }

    public LpcValue makeValue(String value)
    {
        return _support.makeValue(value);
    }

    public NilValue nil()
    {
        return _support.nil();
    }

    public Callable simul_efun(String name)
    {
        return _support.simul_efun(name);
    }

    public CharSequence getName()
    {
        return null;
    }

}
