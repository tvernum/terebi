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

package us.terebi.lang.lpc.runtime.jvm.naming;

import java.util.Set;

import us.terebi.lang.lpc.runtime.jvm.LpcObject;
import us.terebi.util.reflect.MethodIntrospector;

/**
 * 
 */
public class StandardMethods
{
    private static final Set<String> NAMES = new MethodIntrospector(LpcObject.class).getAllDeclaredMethodNames(true);

    public static boolean isStandardMethod(String name)
    {
        return NAMES.contains(name);
    }
}
