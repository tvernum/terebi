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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.efun.Efun;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.ArrayBuilder;
import us.terebi.lang.lpc.runtime.jvm.value.ArrayValue;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;
import us.terebi.plugins.net.SocketManager;
import us.terebi.plugins.net.SocketManager.SocketInfo;

/**
 */
public class SocketStatusEfun extends AbstractSocketEfun implements Efun
{
    // mixed array socket_status( int sd )
    // mixed array array socket_status( void )

    public List< ? extends ArgumentDefinition> defineArguments()
    {
        return Collections.singletonList(new ArgumentSpec("socket", Types.INT));
    }

    public boolean acceptsLessArguments()
    {
        return true;
    }

    public LpcType getReturnType()
    {
        return Types.MIXED_ARRAY;
    }

    public ArrayValue execute(List< ? extends LpcValue> arguments)
    {
        if (hasArgument(arguments, 0))
        {
            long socket = arguments.get(0).asLong();
            return getSocketStatus(socket);
        }
        else
        {
            return getSocketStatus();
        }
    }

    private ArrayValue getSocketStatus()
    {
        SocketManager manager = getSocketManager();
        Collection<SocketInfo> sockets = manager.getSockets();
        ArrayBuilder array = new ArrayBuilder(Types.MIXED_ARRAY_ARRAY, sockets.size());
        for (SocketInfo socket : sockets)
        {
            array.add(getSocketStatus(socket));
        }
        return array.toArray();
    }

    private ArrayValue getSocketStatus(long socket)
    {
        SocketManager manager = getSocketManager();
        SocketInfo info = manager.getSocketInfo(socket);
        return getSocketStatus(info);
    }

    private ArrayValue getSocketStatus(SocketInfo info)
    {
        ArrayBuilder array = new ArrayBuilder(Types.MIXED_ARRAY, 6);
        array.add(info.handle);
        array.add(info.status.name());
        array.add(info.mode.name());
        array.add(info.localAddress());
        array.add(info.remoteAddress(true));
        array.add(info.owner);
        return array.toArray();
    }
}
