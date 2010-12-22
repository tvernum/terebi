/* ------------------------------------------------------------------------
 * Copyright 2010 Tim Vernum
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

package us.terebi.plugins.action.handler;

import java.lang.ref.WeakReference;

import org.apache.log4j.Logger;

import us.terebi.engine.server.InputHandler;
import us.terebi.engine.server.InputHandlerSet;
import us.terebi.engine.server.ObjectShell;
import us.terebi.lang.lpc.runtime.AttributeMap;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.context.RuntimeContext;
import us.terebi.lang.lpc.runtime.jvm.context.CallStack.Origin;
import us.terebi.lang.lpc.runtime.jvm.exception.InternalError;
import us.terebi.lang.lpc.runtime.jvm.support.MiscSupport;
import us.terebi.lang.lpc.runtime.jvm.value.StringValue;
import us.terebi.lang.lpc.runtime.util.StackCall;
import us.terebi.net.core.Connection;
import us.terebi.plugins.interactive.efun.WriteEfun;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public class ActionHandler implements InputHandler
{
    private static final Logger LOG = Logger.getLogger(ActionHandler.class);

    private static final String ATTR_ACTIONS = ObjectShell.SWITCHABLE_ATTRIBUTE_PREFIX + "actions";
    private static final String ATTR_VERB = "action.verb";

    public enum VerbType
    {
        REGULAR, ABBREV, PREFIX,
    }

    public static class Action
    {
        public final Callable handler;
        public final String verb;
        public final VerbType type;
        public final WeakReference<ObjectInstance> provider;

        public Action(Callable handler_, String verb_, VerbType type_, ObjectInstance provider_)
        {
            this.handler = handler_;
            this.verb = verb_;
            this.type = type_;
            this.provider = new WeakReference<ObjectInstance>(provider_);
        }

        public String toString()
        {
            return getClass().getSimpleName() + ':' + verb + '(' + type + "):" + handler;
        }
    }

    private static String _defaultFailMessage = "What?\n";

    public static void setDefaultFailMessage(String defaultFailMessage)
    {
        ActionHandler._defaultFailMessage = defaultFailMessage;
    }

    public static void addAction(ObjectInstance player, Action action)
    {
        ActionSet actions = getActions(player);
        actions.add(action);
    }

    private static ActionSet getActions(ObjectInstance player)
    {
        InputHandlerSet handlers = ObjectShell.getInputHandlers(player);
        if (!handlers.contains(ActionHandler.class))
        {
            handlers.append(new ActionHandler());
        }

        Object attr = player.getAttributes().get(ATTR_ACTIONS);
        if (attr == null)
        {
            ActionSet actions = new ActionSet();
            player.getAttributes().set(ATTR_ACTIONS, actions);
            LOG.info("Created action set " + actions + "for " + player);
            return actions;
        }
        else if (attr instanceof ActionSet)
        {
            return (ActionSet) attr;
        }
        else
        {
            throw new InternalError("Player attribute '"
                    + ATTR_ACTIONS
                    + "' is not a "
                    + ActionSet.class.getSimpleName()
                    + " - is "
                    + attr.getClass());
        }
    }

    public static String getVerb()
    {
        AttributeMap attributes;
        synchronized (RuntimeContext.lock())
        {
            attributes = RuntimeContext.obtain().attributes();
            return (String) attributes.get(ATTR_VERB);
        }
    }

    public String inputReceived(ObjectInstance user, Connection connection, String input)
    {
        AttributeMap attributes;
        synchronized (RuntimeContext.lock())
        {
            attributes = RuntimeContext.obtain().attributes();
        }

        input = input.trim();
        int space = input.indexOf(' ');
        String verb = (space < 0) ? input : input.substring(0, space);
        String rest = (space < 0) ? "" : input.substring(space + 1);

        ActionSet actionSet = getActions(user);
        Iterable<Action> actions = actionSet.find(verb);
        for (Action action : actions)
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Attempting input '" + verb + "' '" + rest + "' against " + action);
            }
            if (action.type == VerbType.PREFIX)
            {
                attributes.set(ATTR_VERB, verb.substring(action.verb.length()));
            }
            else
            {
                attributes.set(ATTR_VERB, verb);
            }

            LpcValue result = new StackCall(action.handler, Origin.DRIVER).execute(new StringValue(rest));
            attributes.remove(ATTR_VERB);
            if (result.asBoolean())
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Input '" + input + "' handled by " + action);
                }
                return null;
            }
        }

        LpcValue fail = (LpcValue) attributes.get("action.failure");
        if (fail == null)
        {
            WriteEfun.write(user, _defaultFailMessage);
            return null;
        }

        if (MiscSupport.isFunction(fail))
        {
            fail = new StackCall(fail.asCallable(), Origin.DRIVER).execute();
        }
        if (MiscSupport.isString(fail))
        {
            WriteEfun.write(user, fail.asString());
        }
        return null;
    }
}
