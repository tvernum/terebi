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

package us.terebi.util.lang;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import org.apache.log4j.Logger;

/**
 * 
 */
public class LoggingClassLoader extends ClassLoader
{
    private final Logger LOG = Logger.getLogger(LoggingClassLoader.class);

    private final ClassLoader _delegate;

    public LoggingClassLoader(ClassLoader delegate)
    {
        _delegate = delegate;
    }

    public void clearAssertionStatus()
    {
        _delegate.clearAssertionStatus();
    }

    public URL getResource(String name)
    {
        URL resource = null;
        try
        {
            resource = _delegate.getResource(name);
        }
        finally
        {
            LOG.info("Resource " + name + " = " + resource);
        }
        return resource;
    }

    public InputStream getResourceAsStream(String name)
    {
        InputStream stream = null;
        try
        {
            stream = _delegate.getResourceAsStream(name);
        }
        finally
        {
            LOG.info("Resource " + name + " = " + stream);
        }
        return stream;
    }

    public Enumeration<URL> getResources(String name) throws IOException
    {
        Enumeration<URL> resources = null;
        try
        {
            resources = _delegate.getResources(name);
        }
        finally
        {
            LOG.info("Resources " + name + " = " + resources);
        }
        return resources;
    }

    public Class< ? > loadClass(String name) throws ClassNotFoundException
    {
        Class< ? > cls = null;
        try
        {
            cls = _delegate.loadClass(name);
        }
        finally
        {
            LOG.info("Loading " + name + " = " + cls);
        }
        return cls;
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
