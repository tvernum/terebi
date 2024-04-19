/* ------------------------------------------------------------------------
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

package us.terebi.plugins.crypt.core;

import us.terebi.plugins.crypt.internal.Crypt;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public class UnixCrypt implements CryptMethod
{
    private Crypt _crypt;

    public UnixCrypt()
    {
        _crypt = new Crypt();
    }
    
    public synchronized String crypt(String password, String salt)
    {
        return _crypt.crypt(password, salt);
    }

}
