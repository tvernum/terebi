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

package us.terebi.engine.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import us.terebi.util.io.IOUtil;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public class PropertiesConfig extends AbstractConfig
{
    private final File _location;
    private final Properties _properties;

    public PropertiesConfig(File location) throws IOException
    {
        _location = location;
        _properties = new Properties();
        readProperties();
    }

    private void readProperties() throws IOException
    {
        FileInputStream input = new FileInputStream(_location);
        try
        {
            _properties.load(input);
        }
        finally
        {
            IOUtil.close(input);
        }
    }

    protected String getConfigLocation()
    {
        try
        {
            return _location.getCanonicalPath();
        }
        catch (IOException e)
        {
            return _location.getAbsolutePath();
        }
    }

    protected String getConfigValue(String key)
    {
        return _properties.getProperty(key);
    }

    protected File resolveFile(String key, String name, File relativeTo, FileType... allowedArray)
    {
        if (relativeTo == null && isRelativePath(name))
        {
            relativeTo = _location.getParentFile();
        }
        return super.resolveFile(key, name, relativeTo, allowedArray);
    }

    private boolean isRelativePath(String name)
    {
        return name.startsWith("./") || name.startsWith("../");
    }
}
