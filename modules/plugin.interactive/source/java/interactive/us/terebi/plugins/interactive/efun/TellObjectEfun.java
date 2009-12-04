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

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import us.terebi.engine.server.ObjectShell;
import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.efun.AbstractEfun;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.VoidValue;
import us.terebi.lang.lpc.runtime.util.Apply;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;
import us.terebi.net.core.Connection;

/**
 * @version $Revision$
 */
public class TellObjectEfun extends AbstractEfun
{
    private final boolean _directToInteractive;

    public TellObjectEfun(boolean directToInteractive)
    {
        _directToInteractive = directToInteractive;
    }

    public TellObjectEfun writeDirectToInteractives()
    {
        return new TellObjectEfun(true);
    }

    public TellObjectEfun useApplyForInteractives()
    {
        return new TellObjectEfun(false);
    }

    protected void tell(ObjectInstance obj, LpcValue message)
    {
        if (_directToInteractive && ObjectShell.isConnectionObject(obj))
        {
            Connection connection = ObjectShell.getConnection(obj);
            if (connection == null)
            {
                return;
            }
            PrintWriter writer = connection.getWriter();
            writer.write(message.asString());
            writer.flush();
        }
        else
        {
            new Apply("catch_tell").invoke(obj, message);
        }
    }

    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        return Arrays.asList( //
                new ArgumentSpec("obj", Types.OBJECT), //
                new ArgumentSpec("message", Types.STRING) //
        );
    }

    public LpcType getReturnType()
    {
        return Types.VOID;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        checkArguments(arguments);

        ObjectInstance obj = arguments.get(0).asObject();
        LpcValue message = arguments.get(1);

        tell(obj, message);
        return VoidValue.INSTANCE;
    }
}
