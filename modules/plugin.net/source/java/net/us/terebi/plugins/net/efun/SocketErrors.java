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

import us.terebi.lang.lpc.runtime.jvm.value.IntValue;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public interface SocketErrors
{
    public static final IntValue BAD_SOCKET_HANDLE = new IntValue(-6);
    public static final IntValue NOT_ALLOWED = new IntValue(-7);
    public static final IntValue BAD_MODE = new IntValue(-12);

}
