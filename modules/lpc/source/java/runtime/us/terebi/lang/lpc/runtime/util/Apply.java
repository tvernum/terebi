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
import java.util.Map;

import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.MethodDefinition;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.context.CallStack;
import us.terebi.lang.lpc.runtime.jvm.context.CallStack.Origin;
import us.terebi.lang.lpc.runtime.jvm.value.NilValue;

/**
 * 
 */
public class Apply
{
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
            return NilValue.INSTANCE;
        }

        return InContext.execute(Origin.APPLY, instance, new InContext.Exec<LpcValue>()
        {
            public LpcValue execute()
            {
                return method.execute(instance, arguments);
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
        Map<String, ? extends MethodDefinition> methods = instance.getDefinition().getMethods();
        MethodDefinition method = methods.get(_name);
        return method;
    }

    public String toString()
    {
        return "apply " + _name;
    }

}
