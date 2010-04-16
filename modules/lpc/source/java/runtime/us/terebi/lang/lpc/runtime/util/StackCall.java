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

import java.util.List;

import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.context.CallStack.Origin;
import us.terebi.lang.lpc.runtime.jvm.exception.InternalError;

public class StackCall extends CallableProxy implements Callable
{
    private final Origin _origin;

    public StackCall(Callable function, Origin origin)
    {
        super(function);
        _origin = origin;
    }

    public ObjectInstance getOwner()
    {
        final ObjectInstance owner = super.getOwner();
        if (owner == null)
        {
            throw new InternalError("Function " + super.getDelegate() + " has no owner");
        }
        return owner;
    }

    public LpcValue execute(final List< ? extends LpcValue> arguments)
    {
        return InContext.execute(_origin, getOwner(), new InContext.Exec<LpcValue>()
        {
            @SuppressWarnings("synthetic-access")
            public LpcValue execute()
            {
                return StackCall.super.execute(arguments);
            }
        });
    }

    public LpcValue execute(final LpcValue... arguments)
    {
        return InContext.execute(_origin, getOwner(), new InContext.Exec<LpcValue>()
        {
            @SuppressWarnings("synthetic-access")
            public LpcValue execute()
            {
                return StackCall.super.execute(arguments);
            }
        });
    }
}
