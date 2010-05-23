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

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.LpcConstants;
import us.terebi.lang.lpc.runtime.jvm.efun.Efun;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcSecurityException;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.IntValue;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;
import us.terebi.plugins.net.SocketManager;

/**
 */
public class SocketCloseEfun extends AbstractSocketEfun implements Efun
{
    // int socket_status( int sd )

    public List< ? extends ArgumentDefinition> defineArguments()
    {
        return Collections.singletonList(new ArgumentSpec("socket", Types.INT));
    }

    public boolean acceptsLessArguments()
    {
        return false;
    }

    public LpcType getReturnType()
    {
        return Types.INT;
    }

    public IntValue execute(List< ? extends LpcValue> arguments)
    {
        checkArguments(arguments);
        long socket = arguments.get(0).asLong();
        SocketManager manager = getSocketManager();
        try
        {
            manager.close(socket);
            return LpcConstants.INT.ZERO;
        }
        catch (NoSuchElementException e)
        {
            return SocketErrors.BAD_SOCKET_HANDLE;
        }
        catch (LpcSecurityException e)
        {
            return SocketErrors.NOT_ALLOWED;
        }
    }
}
