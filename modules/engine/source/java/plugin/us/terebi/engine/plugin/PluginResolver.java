/* ------------------------------------------------------------------------
 * The Terebi (LPC) Game Engine
 * Copyright 2009 Tim Vernum
 * ------------------------------------------------------------------------
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ------------------------------------------------------------------------
 */

package us.terebi.engine.plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.log4j.Logger;

import us.terebi.engine.config.Config;
import us.terebi.lang.lpc.runtime.jvm.context.SystemContext;
import us.terebi.util.io.IOUtil;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
class PluginResolver
{
    private static final String PROPERTIES_LOCATION = "META-INF/terebi.properties";

    private final Logger LOG = Logger.getLogger(PluginResolver.class);

    private final File _file;
    private final PluginSpec _config;

    private boolean _failed;
    private Plugin _plugin;

    public PluginResolver(File file)
    {
        _file = file;
        _config = resolveConfig(file, loadPluginProperties(file));
        _failed = false;
    }

    public PluginSpec getConfig()
    {
        return _config;
    }

    private PluginSpec resolveConfig(File file, Properties properties)
    {
        if (properties == null)
        {
            return null;
        }
        try
        {
            return new PluginSpec(_file, properties);
        }
        catch (MalformedURLException e)
        {
            LOG.error("File " + IOUtil.canonicalPath(file) + "could not be converted to a URL. Plugin will not be loaded");
            return null;
        }
    }

    public boolean isValid()
    {
        return _config != null && _config.isValid();
    }

    private Properties loadPluginProperties(File file)
    {
        InputStream input = null;
        try
        {
            input = getPluginProperties(file);
            return readProperties(input);
        }
        catch (IOException e)
        {
            LOG.error("Plugin " + IOUtil.canonicalPath(file) + " cannot be read. Plugin will not be loaded", e);
            return null;
        }
        finally
        {
            IOUtil.close(input);
        }
    }

    private InputStream getPluginProperties(File file) throws IOException
    {
        JarFile jar = new JarFile(file);
        ZipEntry entry = jar.getEntry(PROPERTIES_LOCATION);
        if (entry == null)
        {
            LOG.error("Plugin " + IOUtil.canonicalPath(file) + " does not contain " + PROPERTIES_LOCATION + ". Plugin will not be loaded");
            return null;
        }
        return jar.getInputStream(entry);
    }

    private Properties readProperties(InputStream input) throws IOException
    {
        if (input == null)
        {
            return null;
        }
        Properties properties = new Properties();
        properties.load(input);
        return properties;
    }

    public void load(ClassLoader classLoader, Config config, SystemContext context)
    {
        create(classLoader);
        if (_failed)
        {
            return;
        }
        _plugin.load(config, context, _config.getProperties());
    }

    private void create(ClassLoader classLoader)
    {
        if (_failed)
        {
            return;
        }
        if (_plugin != null)
        {
            return;
        }
        try
        {
            Class< ? > cls = classLoader.loadClass(_config.getPluginManager());
            if (!Plugin.class.isAssignableFrom(cls))
            {
                LOG.error("Specified plugin manager '" + _config.getPluginManager() + "' for " + _config + " does not implement " + Plugin.class);
                _failed = true;
                return;
            }
            Object instance = cls.newInstance();
            _plugin = (Plugin) instance;
        }
        catch (Exception e)
        {
            LOG.error("Specified plugin manager '"
                    + _config.getPluginManager()
                    + "' for "
                    + _config
                    + " cannot be loaded - "
                    + e.getClass().getSimpleName(), e);
            _failed = true;
        }
    }

    public void init(SystemContext context)
    {
        if (_failed)
        {
            return;
        }
        _plugin.init(context);
    }

    public void start(SystemContext context)
    {
        if (_failed)
        {
            return;
        }
        _plugin.start(context);
    }

    public void run(SystemContext context)
    {
        if (_failed)
        {
            return;
        }
        _plugin.run(context);
    }

    public void epilog(SystemContext context)
    {
        if (_failed)
        {
            return;
        }
        _plugin.epilog(context);
    }
}
