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

package us.terebi.lang.lpc.compiler;

import us.terebi.lang.lpc.compiler.java.context.ObjectId;
import us.terebi.lang.lpc.io.Resource;

/**
 * 
 */
public class ClassNameMapper
{
    private static final String PACKAGE_PREFIX = "lpc.";

    public static ClassName getImplementingClass(ObjectId object)
    {
        String file = object.getFile();
        int slash = file.lastIndexOf('/');
        return getImplementingClass(file.substring(0, slash), file.substring(slash + 1));
    }

    public static ClassName getImplementingClass(Resource source)
    {
        String path = source.getParentName();
        String name = source.getName();
        return getImplementingClass(path, name);
    }

    private static ClassName getImplementingClass(String path, String file)
    {
        String pkg = PACKAGE_PREFIX + path.replace('/', '.');
        pkg = pkg.replace("..", ".");
        if (pkg.endsWith("."))
        {
            pkg = pkg.substring(0, pkg.length() - 1);
        }

        String cls = dropExtension(file);
        cls = cls.replaceAll("[^A-Za-z0-9]", "_");

        return new ClassName(pkg, cls);
    }

    static String dropExtension(String path)
    {
        int slash = path.lastIndexOf('/');
        int dot = path.lastIndexOf('.');
        if (dot > slash)
        {
            path = path.substring(0, dot);
        }
        return path;
    }

    public static String getLpcName(final String implementingClass)
    {
        if (!implementingClass.startsWith(PACKAGE_PREFIX))
        {
            return null;
        }
        String lpc = implementingClass;
        int dollar = lpc.indexOf('$');
        if (dollar != -1)
        {
            lpc = lpc.substring(0, dollar);
        }
        lpc = lpc.substring(PACKAGE_PREFIX.length());
        lpc = "/" + lpc.replace('.', '/') + ".c";
        return lpc;
    }

}
