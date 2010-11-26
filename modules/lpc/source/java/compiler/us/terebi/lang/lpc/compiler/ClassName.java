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

package us.terebi.lang.lpc.compiler;

/**
 * 
 */
public final class ClassName
{
    public final String packageName;
    public final String className;

    public ClassName(String pkg, String name)
    {
        if (pkg == null)
        {
            throw new NullPointerException("Package is null");
        }
        if (name == null)
        {
            throw new NullPointerException("Name is null");
        }
        this.packageName = pkg;
        this.className = name;
    }

    public String fileName()
    {
        String path = packageName.replace('.', '/');
        return path + '/' + className + ".class";
    }

    public String toString()
    {
        return packageName + "." + className;
    }

    public int hashCode()
    {
        return (packageName.hashCode() << 4) ^ className.hashCode();
    }

    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (obj instanceof ClassName)
        {
            final ClassName other = (ClassName) obj;
            return (className.equals(other.className) && packageName.equals(other.packageName));
        }
        return false;
    }

}
