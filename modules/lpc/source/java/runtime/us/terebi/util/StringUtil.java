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
        if (values.length == 0)
        {
            return "";
        }
        if (values.length == 1)
        {
            return values[0].toString();
        }

        StringBuilder builder = new StringBuilder();
        builder.append(values[0]);
        for (int i = 1; i < values.length; i++)
        {
            builder.append(delim);
            builder.append(values[i]);
        }
        return builder;
    }
}
