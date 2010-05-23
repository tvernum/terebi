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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.apache.log4j.Logger;

import us.terebi.util.io.IOUtil;

class PluginSpec
{
    private final Logger LOG = Logger.getLogger(PluginSpec.class);

    private static final String NAME_KEY = "plugin.name";
    private static final String MANAGER_KEY = "plugin.manager";

    private final File _file;
    private final URL _url;
    private final Properties _properties;
    private final String _name;
    private final String _managerName;

    public PluginSpec(File file, Properties properties) throws MalformedURLException
    {
        _file = file;
        _url = _file.toURL();
        _properties = properties;
        _name = _properties.getProperty(NAME_KEY, file.getName());
        _managerName = _properties.getProperty(MANAGER_KEY);
        if (_managerName == null)
        {
            LOG.error("Plugin "
                    + _name
                    + " ("
                    + IOUtil.canonicalPath(file)
                    + ") has no '"
                    + MANAGER_KEY
                    + "' specified. Plugin will not be loaded");
        }
    }

    public boolean isValid()
    {
        return _managerName != null;
    }

    public URL getUrl()
    {
        return _url;
    }

    public String getName()
    {
        return _name;
    }

    public String getPluginManager()
    {
        return _managerName;
    }

    public String toString()
    {
        return _name + " (" + IOUtil.canonicalPath(_file) + ")";
    }
    
    public Properties getProperties()
    {
        return _properties;
    }
}
