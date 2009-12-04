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

import us.terebi.engine.server.ObjectShell;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.net.core.Connection;

/**
 * @version $Revision$
 */
public class WriteEfun extends us.terebi.lang.lpc.runtime.jvm.efun.WriteEfun
{
    protected void write(String message)
    {
        ObjectInstance player = ThisPlayerEfun.this_player();
        write(player, message);
    }

    public static boolean write(ObjectInstance player, String message)
    {
        if (player == null)
        {
            return false;
        }
        Connection connection = ObjectShell.getConnection(player);
        if (connection == null)
        {
            return false;
        }
        PrintWriter writer = connection.getWriter();
        writer.write(message);
        writer.flush();
        return true;
    }
}
