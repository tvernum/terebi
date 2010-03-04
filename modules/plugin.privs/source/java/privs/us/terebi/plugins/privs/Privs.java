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

package us.terebi.plugins.privs;

import us.terebi.lang.lpc.runtime.AttributeMap;
import us.terebi.lang.lpc.runtime.ObjectDefinition;
import us.terebi.lang.lpc.runtime.ObjectInstance;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public class Privs
{
    private static final String ATTRIBUTE = Privs.class.getName();

    public static void set(ObjectDefinition definition, String privs)
    {
        set(definition.getAttributes(), privs);
    }

    public static void set(ObjectInstance instance, String privs)
    {
        set(instance.getAttributes(), privs);
    }

    private static void set(AttributeMap attributes, String privs)
    {
        attributes.set(ATTRIBUTE, privs);
    }

    public static String get(ObjectInstance instance)
    {
        Object privs = instance.getAttributes().get(ATTRIBUTE);
        if (privs == null)
        {
            privs = instance.getDefinition().getAttributes().get(ATTRIBUTE);
        }
        return (String) privs;
    }

}
