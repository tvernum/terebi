/* ------------------------------------------------------------------------
 * The Terebi (LPC) Game Engine
 * Copyright 2010 Tim Vernum
 * ------------------------------------------------------------------------
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ------------------------------------------------------------------------
 */

package us.terebi.engine.server;

import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.support.MiscSupport;
import us.terebi.lang.lpc.runtime.jvm.value.StringValue;
import us.terebi.lang.lpc.runtime.util.Apply;
import us.terebi.net.core.Connection;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public class ProcessInputHandler implements InputHandler
{
    public static final ProcessInputHandler INSTANCE = new ProcessInputHandler();

    private static final Apply PROCESS_INPUT = new Apply("process_input");

    public String inputReceived(ObjectInstance user, Connection connection, String line)
    {
        LpcValue result = PROCESS_INPUT.invoke(user, new StringValue(line));
        if (MiscSupport.isNothing(result))
        {
            return line;
        }
        if (MiscSupport.isString(result))
        {
            return result.asString();
        }
        return null;
    }

}
