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

import org.apache.log4j.Logger;

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
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;

/**
 * @version $Revision$
 */
public class ThisPlayerEfun extends AbstractEfun
{
    private static final Logger LOG = Logger.getLogger(ThisPlayerEfun.class);

    private static final String CONTEXT_ATTRIBUTE = "this_player";

    public List< ? extends ArgumentDefinition> defineArguments()
    {
        return Collections.singletonList(new ArgumentSpec("flag", Types.INT));
    }

    public boolean acceptsLessArguments()
    {
        return true;
    }

    public LpcType getReturnType()
    {
        return Types.OBJECT;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        // @TODO - Use flag
        ObjectInstance instance = this_player();
        return super.getValue(instance);
    }

    public static ObjectInstance this_player()
    {
        ThreadContext context = RuntimeContext.obtain();
        AttributeMap attributes = context.attributes();
        Object tp = attributes.get(CONTEXT_ATTRIBUTE);
        if (tp != null)
        {
            ObjectInstance instance = (ObjectInstance) tp;
            return getUser(instance);
        }
        CallStack stack = context.callStack();
        MajorFrame frame = stack.topFrame();
        ObjectInstance user = getUser(frame.instance());
        if (user != null)
        {
            attributes.set(CONTEXT_ATTRIBUTE, frame.instance());
            return user;
        }
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Top of stack is not a user: " + frame);
            LOG.debug("Full stack is " + stack);
        }
        return null;
    }

    private static ObjectInstance getUser(ObjectInstance instance)
    {
        if (ObjectShell.isConnectionObject(instance))
        {
            return instance;
        }
        ObjectInstance switchedObject = ObjectShell.getSwitchedObject(instance);
        return switchedObject;
    }

}
