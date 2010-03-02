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

import java.util.Arrays;
import java.util.List;

import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isFunction;
import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isString;

import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.ArgumentSemantics;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.MethodDefinition;
import us.terebi.lang.lpc.runtime.ObjectDefinition;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.LpcConstants;
import us.terebi.lang.lpc.runtime.jvm.LpcReference;
import us.terebi.lang.lpc.runtime.jvm.context.RuntimeContext;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;
import us.terebi.lang.lpc.runtime.jvm.support.MiscSupport;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.IntValue;
import us.terebi.lang.lpc.runtime.jvm.value.NilValue;
import us.terebi.lang.lpc.runtime.jvm.value.ObjectValue;
import us.terebi.lang.lpc.runtime.util.BoundMethod;
import us.terebi.lang.lpc.runtime.util.FunctionUtil;
import us.terebi.lang.lpc.runtime.util.NilCallable;
import us.terebi.util.Range;
import us.terebi.util.StringUtil;

/**
 * 
 */
public abstract class AbstractEfun implements Efun, FunctionSignature, Callable
{
    private List< ? extends ArgumentDefinition> _arguments;

    public List< ? extends ArgumentDefinition> getArguments()
    {
        synchronized (this)
        {
            if (_arguments == null)
            {
                _arguments = defineArguments();
            }
        }
        return _arguments;
    }

    protected abstract List< ? extends ArgumentDefinition> defineArguments();

    public LpcValue execute(LpcValue... arguments)
    {
        LpcValue result = execute(Arrays.asList(arguments));
        if (result == null)
        {
            throw new NullPointerException("Internal Error - efun " + getName() + " returned NULL");
        }
        return result;
    }

    public Kind getKind()
    {
        return Kind.EFUN;
    }

    public FunctionSignature getSignature()
    {
        return this;
    }

    public ObjectInstance getOwner()
    {
        return null;
    }

    public boolean acceptsLessArguments()
    {
        return false;
    }

    public boolean hasUnstructuredArguments()
    {
        return false;
    }

    protected void checkArguments(List< ? extends LpcValue> argumentValues)
    {
        Range<Integer> argumentRange = FunctionUtil.getAllowedNumberOfArgument(this);
        if (!argumentRange.inRange(argumentValues.size()))
        {
            throw new LpcRuntimeException(getName() + " requires " + argumentRange + " argument(s) but " + argumentValues.size() + " were provided");
        }
        List< ? extends ArgumentDefinition> argumentDefinitions = getSignature().getArguments();
        for (int i = 0; i < argumentDefinitions.size() && i < argumentValues.size(); i++)
        {
            ArgumentDefinition def = argumentDefinitions.get(i);
            LpcValue val = argumentValues.get(i);
            if (def.isVarArgs())
            {
                checkType(i + 1, Types.MIXED_ARRAY, val.getActualType());
                for (LpcValue element : val.asList())
                {
                    checkArgument(def, element, i + 1);
                }
            }
            else
            {
                checkArgument(def, val, i + 1);
            }
        }
    }

    private void checkArgument(ArgumentDefinition def, LpcValue val, int index)
    {
        LpcType valType = (val instanceof LpcReference) ? ((LpcReference) val).getType() : val.getActualType();
        checkType(index, def.getType(), valType);
        checkSemantics(index, def.getSemantics(), val);
    }

    protected void checkSemantics(int index, ArgumentSemantics semantics, LpcValue value)
    {
        if (semantics == ArgumentSemantics.IMPLICIT_REFERENCE)
        {
            if (!(value instanceof LpcReference))
            {
                throw new LpcRuntimeException("Internal Error - expected argument " + index + " to " + getName() + " to be a reference value");
            }
        }
    }

    private void checkType(int index, LpcType expectedType, LpcType actualType)
    {
        if (expectedType.equals(actualType))
        {
            return;
        }
        if (expectedType.getKind() == LpcType.Kind.MIXED && expectedType.getArrayDepth() <= actualType.getArrayDepth())
        {
            return;
        }
        if (acceptsLessArguments() && actualType.getKind() == LpcType.Kind.NIL)
        {
            return;
        }
        badArgumentType(index, actualType, expectedType);
    }

    protected LpcValue badArgumentType(int index, LpcType actualType, LpcType... expectedTypes)
    {
        throw new LpcRuntimeException("Bad argument "
                + index
                + " to "
                + getName()
                + " expected "
                + StringUtil.join("|", expectedTypes)
                + " got "
                + actualType);
    }

    protected CharSequence getName()
    {
        StringBuilder builder = new StringBuilder();
        String name = getClass().getSimpleName();
        if (name.endsWith("Efun"))
        {
            name = name.substring(0, name.length() - 4);
        }
        for (int i = 0; i < name.length(); i++)
        {
            char ch = name.charAt(i);
            if (Character.isUpperCase(ch))
            {
                if (i > 0)
                {
                    builder.append('_');
                }
                builder.append(Character.toLowerCase(ch));
            }
            else
            {
                builder.append(ch);
            }
        }
        return builder;

    }

    protected void checkArguments(List< ? extends LpcValue> arguments, int minCount)
    {
        if (arguments.size() < minCount)
        {
            throw new LpcRuntimeException(getName()
                    + " requires at least "
                    + minCount
                    + " argument(s) but only "
                    + arguments.size()
                    + " were provided");
        }
        this.checkArguments(arguments);
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(getName());
        builder.append('(');
        for (ArgumentDefinition arg : getArguments())
        {
            builder.append(arg);
            builder.append(' ');
        }
        builder.append(')');
        return builder.toString();
    }

    protected Callable getFunction(LpcValue value, int index)
    {
        if (isFunction(value))
        {
            return value.asCallable();
        }
        if (isString(value))
        {
            ObjectInstance thisObject = RuntimeContext.obtain().callStack().peekFrame(0).instance;
            MethodDefinition method = thisObject.getDefinition().getMethods().get(value.asString());
            if (method == null)
            {
                return new NilCallable(thisObject, Callable.Kind.METHOD);
            }
            else
            {
                return new BoundMethod(method, thisObject);
            }
        }
        this.badArgumentType(index, value.getActualType(), Types.FUNCTION, Types.STRING);
        return null;
    }

    protected IntValue getValue(boolean bool)
    {
        return bool ? LpcConstants.INT.TRUE : LpcConstants.INT.FALSE;
    }

    protected LpcValue getValue(ObjectInstance object)
    {
        if (object == null)
        {
            return NilValue.INSTANCE;
        }
        return new ObjectValue(object);
    }

    protected LpcValue getValue(ObjectDefinition object)
    {
        if (object == null)
        {
            return NilValue.INSTANCE;
        }
        return new ObjectValue(object.getMasterInstance());
    }

    protected Callable getFunctionReference(LpcValue func)
    {
        if (isFunction(func))
        {
            return func.asCallable();
        }
        else if (MiscSupport.isString(func))
        {
            return new BoundMethod(func.asString(), ThisObjectEfun.this_object());
        }
        else
        {
            throw new UnsupportedOperationException("Bad argument to " + getName() + " - " + func + " must be a string or a function");
        }
    }
}
