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

package us.terebi.plugins.action.efun;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.efun.AbstractEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.Efun;
import us.terebi.lang.lpc.runtime.jvm.efun.ThisObjectEfun;
import us.terebi.lang.lpc.runtime.jvm.support.MiscSupport;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.VoidValue;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;
import us.terebi.plugins.action.handler.ActionHandler;
import us.terebi.plugins.action.handler.ActionHandler.Action;
import us.terebi.plugins.action.handler.ActionHandler.VerbType;
import us.terebi.plugins.interactive.efun.ThisPlayerEfun;

/**
 * 
 */
public class AddActionEfun extends AbstractEfun implements Efun
{
    private final Logger LOG = Logger.getLogger(AddActionEfun.class);

    //    void add_action( string | function fun, string | string array cmd );
    //    void add_action( string | function fun, string | string array cmd, int flag );

    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        return Arrays.asList( //
                new ArgumentSpec("fun", Types.MIXED), //
                new ArgumentSpec("verb", Types.MIXED), //
                new ArgumentSpec("flag", Types.INT) //
        );
    }

    public boolean acceptsLessArguments()
    {
        return true;
    }

    public LpcType getReturnType()
    {
        return Types.VOID;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        LpcValue fun = getArgument(arguments, 0);
        LpcValue verb = getArgument(arguments, 1);
        LpcValue flag = getArgument(arguments, 2);

        Callable handler = getFunctionReference(fun);
        VerbType type;
        switch (asInt(flag))
        {
            case 1:
                type = VerbType.ABBREV;
                break;
            case 2:
                type = VerbType.PREFIX;
                break;
            case 0:
            default:
                type = VerbType.REGULAR;
                break;
        }

        if (MiscSupport.isArray(verb))
        {
            List<LpcValue> verbs = verb.asList();
            for (LpcValue verbElement : verbs)
            {
                add_action(handler, verbElement.asString(), type);
            }
        }
        else
        {
            add_action(handler, verb.asString(), type);
        }
        return VoidValue.INSTANCE;
    }

    private void add_action(Callable func, String verb, VerbType type)
    {
        ObjectInstance player = ThisPlayerEfun.this_player();
        if (player == null)
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("No active player to add action '" + verb + "' to");
            }
            return;
        }
        ObjectInstance object = ThisObjectEfun.this_object();
        Action action = new ActionHandler.Action(func, verb, type, object);
        ActionHandler.addAction(player, action);
    }
}
