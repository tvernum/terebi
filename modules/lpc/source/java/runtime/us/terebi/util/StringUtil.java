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

package us.terebi.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * 
 */
public class StringUtil
{
    public static String repeat(String string, int count)
    {
        if (count == 0)
        {
            return "";
        }
        if (count == 1)
        {
            return string;
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++)
        {
            builder.append(string);
        }
        return builder.toString();
    }

    public static CharSequence join(String delim, Object[] values)
    {
        return join(delim, Arrays.asList(values));
    }

    public static CharSequence join(String delim, Collection< ? > values)
    {
        if (values.isEmpty())
        {
            return "";
        }

        Iterator<?> iterator = values.iterator();
        if (values.size() == 1)
        {
            return iterator.next().toString();
        }

        StringBuilder builder = new StringBuilder();
        builder.append(iterator.next());
        while (iterator.hasNext())
        {
            builder.append(delim);
            builder.append(iterator.next());
        }
        return builder;
    }

    public static boolean isBlank(String name)
    {
        return name == null || "".equals(name);
    }
}
