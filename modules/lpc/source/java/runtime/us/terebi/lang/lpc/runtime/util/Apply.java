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

package us.terebi.lang.lpc.runtime.util;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.MethodDefinition;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.context.CallStack;
import us.terebi.lang.lpc.runtime.jvm.context.CallStack.Origin;
import us.terebi.lang.lpc.runtime.jvm.support.CallableSupport;
import us.terebi.lang.lpc.runtime.jvm.value.NilValue;

/**
 * 
 */
public class Apply
{
    private final Logger LOG = Logger.getLogger(Apply.class);

    private final String _name;

    public Apply(String name)
    {
        _name = name;
    }

    public LpcValue invoke(ObjectInstance instance, LpcValue... arguments)
    {
        return invoke(instance, Arrays.asList(arguments));
    }

    public LpcValue invoke(final ObjectInstance instance, final List< ? extends LpcValue> arguments)
    {
        final MethodDefinition method = getMethod(instance);
        if (method == null)
        {
            LOG.debug("No method " + _name + " found in " + instance);
            return NilValue.INSTANCE;
        }

        return InContext.execute(Origin.APPLY, instance, new InContext.Exec<LpcValue>()
        {
            @SuppressWarnings("synthetic-access")
            public LpcValue execute()
            {
                LpcValue value = method.execute(instance, arguments);
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Method " + method + " in " + instance + " ->" + value);
                }
                return value;
            }
        });
    }

    public Callable bind(ObjectInstance instance)
    {
        MethodDefinition method = getMethod(instance);
        if (method == null)
        {
            return new NilMethod(instance, _name);
        }
        BoundMethod bind = new BoundMethod(method, instance);
        return new StackCall(bind, CallStack.Origin.APPLY);
    }

    private MethodDefinition getMethod(ObjectInstance instance)
    {
        return CallableSupport.findMethod(_name, instance.getDefinition(), instance);
    }

    public String toString()
    {
        return "apply " + _name;
    }

    public boolean existsIn(ObjectInstance instance)
    {
        return getMethod(instance) != null;
    }
    
    public String getName()
    {
        return _name;
    }

}
