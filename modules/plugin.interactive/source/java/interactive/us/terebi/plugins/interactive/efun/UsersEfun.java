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
import java.util.Collections;
import java.util.List;

import us.terebi.engine.server.ObjectShell;
import us.terebi.engine.server.TerebiServer;
import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.LpcConstants;
import us.terebi.lang.lpc.runtime.jvm.context.ObjectMap;
import us.terebi.lang.lpc.runtime.jvm.context.RuntimeContext;
import us.terebi.lang.lpc.runtime.jvm.context.SystemContext;
import us.terebi.lang.lpc.runtime.jvm.efun.AbstractEfun;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.ArrayValue;
import us.terebi.net.core.Connection;
import us.terebi.net.server.ConnectionListener;
import us.terebi.net.server.impl.NetServer;

/**
 * 
 */
public class UsersEfun extends AbstractEfun implements FunctionSignature, Callable
{
    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        return Collections.emptyList();
    }

    public LpcType getReturnType()
    {
        return Types.OBJECT_ARRAY;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        List<LpcValue> users = new ArrayList<LpcValue>();

        final SystemContext systemContext = RuntimeContext.obtain().system();
        final ObjectMap attachments = systemContext.attachments();

        TerebiServer server = attachments.get(TerebiServer.class);
        if (server == null)
        {
            // Can happen if "users()" is called during MUD startup.
           return LpcConstants.ARRAY.EMPTY; 
        }
        
        NetServer net = server.getNetServer();

        for (ConnectionListener listener : net.getListeners())
        {
            for (Connection connection : listener.getConnections())
            {
                ObjectInstance object = ObjectShell.getConnectionObject(connection);
                users.add(object.asValue());
            }
        }
        return new ArrayValue(Types.OBJECT_ARRAY, users);
    }

}
