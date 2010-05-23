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

package us.terebi.plugins.net.efun;

import java.util.Arrays;
import java.util.List;

import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.context.RuntimeContext;
import us.terebi.lang.lpc.runtime.jvm.context.ThreadContext;
import us.terebi.lang.lpc.runtime.jvm.efun.Efun;
import us.terebi.lang.lpc.runtime.jvm.efun.ThisObjectEfun;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcSecurityException;
import us.terebi.lang.lpc.runtime.jvm.support.ValueSupport;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.IntValue;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;
import us.terebi.plugins.net.SocketManager;
import us.terebi.plugins.net.SocketManager.SocketMode;

/**
 */
public class SocketCreateEfun extends AbstractSocketEfun implements Efun
{
    // int socket_create( int mode, string | function read_callback);
    // int socket_create( int mode, string | function read_callback, string | function close_callback );

    public List< ? extends ArgumentDefinition> defineArguments()
    {
        return Arrays.asList( //
                new ArgumentSpec("mode", Types.INT), //
                new ArgumentSpec("read_callback", Types.MIXED), //
                new ArgumentSpec("close_callback", Types.MIXED));
    }

    public boolean acceptsLessArguments()
    {
        return true;
    }

    public LpcType getReturnType()
    {
        return Types.INT;
    }

    public IntValue execute(List< ? extends LpcValue> arguments)
    {
        checkArguments(arguments, 2);
        long mode = getArgument(arguments, 0).asLong();
        Callable read = hasArgument(arguments, 1) ? getFunctionReference(arguments.get(1)) : null;
        Callable close = hasArgument(arguments, 2) ? getFunctionReference(arguments.get(2)) : null;

        if (mode < 0 || mode > SocketMode.values().length)
        {
            return SocketErrors.BAD_MODE;
        }
        
        SocketMode socketMode = SocketMode.values()[(int) mode];

        ThreadContext context = RuntimeContext.obtain();
        SocketManager manager = getSocketManager(context);

        try
        {
            long handle = manager.open(socketMode, read, close, ThisObjectEfun.this_object(context));
            return ValueSupport.intValue(handle);
        }
        catch (LpcSecurityException e)
        {
            return SocketErrors.NOT_ALLOWED;
        }
    }
}
