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

package us.terebi.util.lang;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

/**
 * 
 */
public abstract class DelegatingClassLoader extends ClassLoader
{
    private final ClassLoader _delegate;

    public DelegatingClassLoader(ClassLoader delegate)
    {
        _delegate = delegate;
    }

    public void clearAssertionStatus()
    {
        _delegate.clearAssertionStatus();
    }

    public URL getResource(String name)
    {
        return _delegate.getResource(name);
    }

    public InputStream getResourceAsStream(String name)
    {
        return _delegate.getResourceAsStream(name);
    }

    public Enumeration<URL> getResources(String name) throws IOException
    {
        return _delegate.getResources(name);
    }

    public Class< ? > loadClass(String name) throws ClassNotFoundException
    {
        return _delegate.loadClass(name);
    }

    public void setClassAssertionStatus(String className, boolean enabled)
    {
        _delegate.setClassAssertionStatus(className, enabled);
    }

    public void setDefaultAssertionStatus(boolean enabled)
    {
        _delegate.setDefaultAssertionStatus(enabled);
    }

    public void setPackageAssertionStatus(String packageName, boolean enabled)
    {
        _delegate.setPackageAssertionStatus(packageName, enabled);
    }

    public String toString()
    {
        return getClass().getSimpleName() + ":" + _delegate.toString();
    }

}
