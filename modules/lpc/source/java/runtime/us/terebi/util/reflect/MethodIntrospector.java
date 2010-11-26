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

package us.terebi.util.reflect;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 */
public class MethodIntrospector
{
    private final Class< ? > _type;

    public MethodIntrospector(Class< ? > type)
    {
        _type = type;
    }

    public Set<String> getAllDeclaredMethodNames(boolean includeObject)
    {
        Set<String> names = new HashSet<String>();
        for (Class< ? > type = _type; type != null; type = type.getSuperclass())
        {
            if (type == null)
            {
                break;
            }
            if (type == Object.class && !includeObject)
            {
                break;
            }
            for (Method method : type.getDeclaredMethods())
            {
                names.add(method.getName());
            }
        }
        return names;
    }
}
