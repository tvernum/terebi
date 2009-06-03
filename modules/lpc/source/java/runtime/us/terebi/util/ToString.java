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

import java.util.Collection;

/**
 * 
 */
public class ToString
{

    public static CharSequence toString(Collection< ? > collection)
    {
        StringBuilder builder = new StringBuilder();

        builder.append("(");
        builder.append(collection.getClass().getSimpleName());
        builder.append("{ ");

        if (collection.isEmpty())
        {
            builder.append("})");
            return builder;
        }

        for (Object object : collection)
        {
            builder.append(object);
            builder.append(" , ");
        }
        builder.replace(builder.length() - 2, builder.length(), "})");
        return builder;
    }

    public static CharSequence toString(Object[] array)
    {
        StringBuilder builder = new StringBuilder();

        builder.append("(");
        builder.append("{ ");

        if (array.length == 0)
        {
            builder.append("})");
            return builder;
        }

        for (Object object : array)
        {
            builder.append(object);
            builder.append(" , ");
        }
        builder.replace(builder.length() - 2, builder.length(), "})");
        return builder;
    }

}
