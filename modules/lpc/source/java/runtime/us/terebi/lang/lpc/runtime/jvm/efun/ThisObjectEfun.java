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

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.context.CallStack;
import us.terebi.lang.lpc.runtime.jvm.context.RuntimeContext;
import us.terebi.lang.lpc.runtime.jvm.context.ThreadContext;
import us.terebi.lang.lpc.runtime.jvm.type.Types;

/**
 * 
 */
public class ThisObjectEfun extends AbstractEfun implements FunctionSignature, Callable
{
    private final static Logger LOG = Logger.getLogger(ThisObjectEfun.class);

    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        return Collections.emptyList();
    }

    public LpcType getReturnType()
    {
        return Types.OBJECT;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        // @TODO - This can be optimised drastically, if we assume there is no sefun overriding this_object(), and make "this_object()" a local call in LpcObject
        ObjectInstance instance = this_object();
        return instance.asValue();
    }

    public static ObjectInstance this_object()
    {
        ThreadContext context = RuntimeContext.obtain();
        return this_object(context);
    }

    public static ObjectInstance this_object(ThreadContext context)
    {
        CallStack stack = context.callStack();
        ObjectInstance instance = stack.peekFrame(0).instance();
        if (LOG.isDebugEnabled())
        {
            LOG.debug("this_object() : Stack = " + stack + " ; this_object = " + instance);
        }
        return instance;
    }
}
