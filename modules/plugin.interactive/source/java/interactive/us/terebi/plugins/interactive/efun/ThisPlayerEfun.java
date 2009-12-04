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

package us.terebi.plugins.interactive.efun;

import java.util.Collections;
import java.util.List;

import us.terebi.engine.server.ObjectShell;
import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.AttributeMap;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.context.CallStack;
import us.terebi.lang.lpc.runtime.jvm.context.RuntimeContext;
import us.terebi.lang.lpc.runtime.jvm.context.ThreadContext;
import us.terebi.lang.lpc.runtime.jvm.context.CallStack.MajorFrame;
import us.terebi.lang.lpc.runtime.jvm.efun.AbstractEfun;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.NilValue;
import us.terebi.lang.lpc.runtime.jvm.value.ObjectValue;

/**
 * @version $Revision$
 */
public class ThisPlayerEfun extends AbstractEfun
{
    private static final String CONTEXT_ATTRIBUTE = "this_player";

    public List< ? extends ArgumentDefinition> defineArguments()
    {
        return Collections.emptyList();
    }

    public LpcType getReturnType()
    {
        return Types.OBJECT;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        ObjectInstance instance = this_player();
        if (instance == null)
        {
            return NilValue.INSTANCE;
        }
        else
        {
            return new ObjectValue(instance);
        }
    }

    public static ObjectInstance this_player()
    {
        ThreadContext context = RuntimeContext.obtain();
        AttributeMap attributes = context.attributes();
        Object tp = attributes.get(CONTEXT_ATTRIBUTE);
        if (tp != null)
        {
            return (ObjectInstance) tp;
        }
        CallStack stack = context.callStack();
        MajorFrame frame = stack.topFrame();
        if (isUser(frame.instance))
        {
            attributes.set(CONTEXT_ATTRIBUTE, frame.instance);
            return frame.instance;
        }
        return null;
    }

    private static boolean isUser(ObjectInstance instance)
    {
        return ObjectShell.isConnectionObject(instance);
    }

}
