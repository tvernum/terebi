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

import java.util.ArrayList;
import java.util.List;

import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.MethodDefinition;
import us.terebi.lang.lpc.runtime.ObjectDefinition;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.context.CallStack;
import us.terebi.lang.lpc.runtime.jvm.context.ObjectManager;
import us.terebi.lang.lpc.runtime.jvm.context.RuntimeContext;
import us.terebi.lang.lpc.runtime.jvm.exception.InternalError;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;
import us.terebi.lang.lpc.runtime.jvm.support.CallableSupport;
import us.terebi.lang.lpc.runtime.jvm.support.MiscSupport;
import us.terebi.lang.lpc.runtime.jvm.support.ValueSupport;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.NilValue;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;

/**
 * 
 */
public class CallOtherEfun extends AbstractEfun implements FunctionSignature, Callable
{
    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        ArrayList<ArgumentDefinition> list = new ArrayList<ArgumentDefinition>();
        list.add(new ArgumentSpec("object", Types.MIXED));
        list.add(new ArgumentSpec("method", Types.STRING));
        list.add(new ArgumentSpec("arguments", Types.MIXED_ARRAY, true));
        return list;
    }

    public LpcType getReturnType()
    {
        return Types.MIXED;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        checkArguments(arguments);
        return callOther(arguments);
    }

    private LpcValue callOther(List< ? extends LpcValue> arguments)
    {
        LpcValue arg1 = arguments.get(0);
        LpcValue arg2 = arguments.get(1);
        List< ? extends LpcValue> args = arguments.get(2).asList();

        return callOther(arg1, arg2, args);
    }

    private LpcValue callOther(LpcValue arg1, LpcValue arg2, List< ? extends LpcValue> args)
    {
        ObjectInstance target;

        if (MiscSupport.isArray(arg1))
        {
            List<LpcValue> elements = arg1.asList();
            List<LpcValue> result = new ArrayList<LpcValue>(elements.size());
            for (LpcValue element : elements)
            {
                result.add(callOther(element, arg2, args));
            }
            return ValueSupport.arrayValue(result);
        }

        if (MiscSupport.isString(arg1))
        {
            ObjectManager objectManager = RuntimeContext.obtain().system().objectManager();
            String objectName = arg1.asString();
            ObjectDefinition objectDefinition = objectManager.findObject(objectName, true);
            if (objectDefinition == null)
            {
                throw new LpcRuntimeException("No such object " + objectName);
            }
            target = objectDefinition.getMasterInstance();
            if (target == null)
            {
                throw new InternalError("Cannot get master instance for object " + objectDefinition);
            }
        }
        else if (MiscSupport.isObject(arg1))
        {
            target = arg1.asObject();
            if (target == null)
            {
                throw new LpcRuntimeException("Object " + arg1 + " is nil");
            }
        }
        else
        {
            throw new LpcRuntimeException("Attempt to call function " + arg2 + " in non-object " + arg1);
        }

        String name = arg2.asString();
        return callOther(target, name, args);
    }

    public static LpcValue callOther(ObjectInstance target, String name, List< ? extends LpcValue> args)
    {
        MethodDefinition method = CallableSupport.findMethod(name, target.getDefinition(), target);
        if (method == null)
        {
            return NilValue.INSTANCE;
        }

        CallStack stack = RuntimeContext.obtain().callStack();
        stack.pushFrame(CallStack.Origin.CALL_OTHER, target);
        try
        {
            return method.execute(target, args);
        }
        finally
        {
            stack.popFrame();
        }
    }

}
