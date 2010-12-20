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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import us.terebi.engine.server.InputHandler;
import us.terebi.engine.server.ObjectShell;
import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.LpcConstants;
import us.terebi.lang.lpc.runtime.jvm.context.CallStack.Origin;
import us.terebi.lang.lpc.runtime.jvm.efun.AbstractEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.Efun;
import us.terebi.lang.lpc.runtime.jvm.support.MiscSupport;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.NilValue;
import us.terebi.lang.lpc.runtime.jvm.value.StringValue;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;
import us.terebi.lang.lpc.runtime.util.InContext;
import us.terebi.net.core.Connection;
import us.terebi.net.core.FeatureSet;
import us.terebi.net.core.NetworkFeatures;

/**
 * @version $Revision$
 */
public class InputToEfun extends AbstractEfun implements Efun
{
    public static class Handler implements InputHandler
    {
        private final Callable _func;
        private final boolean _allowEscape;
        private final List< ? extends LpcValue> _extraArgs;

        public Handler(Callable func, boolean allowEscape, List< ? extends LpcValue> extraArgs)
        {
            _func = func;
            _allowEscape = allowEscape;
            _extraArgs = extraArgs;
        }

        public String inputReceived(ObjectInstance user, Connection connection, String line)
        {
            FeatureSet features = connection.getFeatures();
            if (!features.isEnabled(NetworkFeatures.CLIENT_ECHO))
            {
                connection.getWriter().println();
                features.enableFeature(NetworkFeatures.CLIENT_ECHO);
            }

            if (_allowEscape && line.startsWith("!"))
            {
                line = line.substring(1);
                return line;
            }

            LpcValue string = new StringValue(line);
            int count = _func.getSignature().getArguments().size();
            final List<LpcValue> args = new ArrayList<LpcValue>(count);
            args.add(string);
            args.addAll(_extraArgs);
            while (args.size() < count)
            {
                args.add(LpcConstants.NIL);
            }

            ObjectShell.getInputHandlers(user).remove(this);

            final Callable func = _func;
            InContext.<Object> execute(Origin.EFUN, user, new InContext.Exec<Object>()
            {
                public Object execute()
                {
                    return func.execute(args);
                }
            });

            return null;
        }
        
        public String toString()
        {
            return "input_to:" + _func;
        }
    }

    private final Logger LOG = Logger.getLogger(InputToEfun.class);

    public List< ? extends ArgumentDefinition> defineArguments()
    {
        return Arrays.asList( //
                new ArgumentSpec("func", Types.MIXED), //
                new ArgumentSpec("flag", Types.INT), //
                new ArgumentSpec("args", Types.MIXED_ARRAY, true));
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
        checkArguments(arguments, 1);
        ObjectInstance user = ThisPlayerEfun.this_player();
        Callable func = getFunction(arguments.get(0), 1);
        long flag = getFlag(arguments);
        List< ? extends LpcValue> extraArgs = getExtraArguments(arguments);
        inputTo(user, func, flag, extraArgs);
        return NilValue.INSTANCE;
    }

    private long getFlag(List< ? extends LpcValue> arguments)
    {
        long flag = 0;
        if (arguments.size() > 1)
        {
            flag = arguments.get(1).asLong();
        }
        return flag;
    }

    private List< ? extends LpcValue> getExtraArguments(List< ? extends LpcValue> arguments)
    {
        if (arguments.size() > 2)
        {
            LpcValue arg = arguments.get(2);
            if (MiscSupport.isArray(arg))
            {
                return arg.asList();
            }
        }
        return Collections.emptyList();
    }

    private void inputTo(ObjectInstance user, Callable func, long flag, List< ? extends LpcValue> extraArgs)
    {
        if (user == null)
        {
            LOG.info("Attempt to use 'input_to' when there is no active user");
        }

        if (ObjectShell.getInputHandlers(user).contains(Handler.class))
        {
            LOG.info("Attempt to use 'input_to' when there is already an active input handler");
        }

        if (isSet(flag, 0x1))
        {
            ObjectShell.getConnection(user).getFeatures().disableFeature(NetworkFeatures.CLIENT_ECHO);
        }
        Handler handler = new Handler(func, isSet(flag, 0x2), extraArgs);
        ObjectShell.getInputHandlers(user).prepend(handler);
    }

    private boolean isSet(long flag, int i)
    {
        return (flag & i) == i;
    }
}
