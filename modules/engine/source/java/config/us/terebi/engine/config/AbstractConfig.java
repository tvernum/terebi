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
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import us.terebi.lang.lpc.io.FileResource;
import us.terebi.lang.lpc.io.Resource;
import us.terebi.util.StringUtil;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public abstract class AbstractConfig implements Config
{
    private static final long[] EMPTY_LONG_ARRAY = new long[0];
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private static final File[] EMPTY_FILE_PATH = new File[0];
    private static final Resource[] EMPTY_RESOURCE_PATH = new Resource[0];

    private final Set<String> _trueValues;
    private final Set<String> _falseValues;

    public AbstractConfig()
    {
        _trueValues = new HashSet<String>();
        _trueValues.add("true");
        _trueValues.add("yes");
        _trueValues.add("on");
        _falseValues = new HashSet<String>();
        _falseValues.add("false");
        _falseValues.add("no");
        _falseValues.add("off");
    }

    protected abstract String getConfigValue(String key);

    protected abstract String getConfigLocation();

    protected void configNotFound(String key)
    {
        throw new ConfigException("Configuration value for " + key + " was not supplied in " + getConfigLocation());
    }

    protected void badConfigType(String key, String value, Class< ? > type)
    {
        throw new ConfigException("Configuration value '"
                + value
                + "'( for "
                + key
                + " in"
                + getConfigLocation()
                + ") is not in a valid "
                + type.getSimpleName()
                + " format");
    }

    protected void badFileType(String key, File file, FileType[] allowedTypes)
    {
        throw new ConfigException("File '"
                + getPath(file)
                + "' for configuration "
                + key
                + " in "
                + getConfigLocation()
                + ") must be one of "
                + StringUtil.join(",", allowedTypes));
    }

    public boolean hasKey(String key)
    {
        return getConfigValue(key) != null;
    }

    private String getPath(File file)
    {
        try
        {
            return file.getCanonicalPath();
        }
        catch (IOException e)
        {
            return file.getAbsolutePath();
        }
    }

    public String getString(String key)
    {
        String value = getConfigValue(key);
        if (value == null)
        {
            configNotFound(key);
            return null;
        }
        return value;
    }

    public String getString(String key, String defaultValue)
    {
        String value = getConfigValue(key);
        if (value == null)
        {
            return defaultValue;
        }
        return value;

    }

    public String[] getStrings(String key)
    {
        String value = getConfigValue(key);
        if (value == null || "".equals(value))
        {
            return EMPTY_STRING_ARRAY;
        }
        String[] array = value.split("\\s*,\\s*");
        return array;
    }

    public boolean getBoolean(String key)
    {
        return getBoolean(key, null);
    }

    public boolean getBoolean(String key, boolean defaultValue)
    {
        return getBoolean(key, Boolean.valueOf(defaultValue));
    }

    private boolean getBoolean(String key, Boolean defaultValue)
    {
        String value = getConfigValue(key);
        if (value == null)
        {
            if (defaultValue == null)
            {
                configNotFound(key);
                return false;
            }
            else
            {
                return defaultValue.booleanValue();
            }
        }
        if (_trueValues.contains(value.toLowerCase()))
        {
            return true;
        }
        if (_falseValues.contains(value.toLowerCase()))
        {
            return false;
        }
        badConfigType(key, value, Boolean.TYPE);
        return false;
    }

    public long[] getLongs(String key)
    {
        String value = getConfigValue(key);
        if (value == null || "".equals(value))
        {
            return EMPTY_LONG_ARRAY;
        }

        String[] elements = value.split("\\s*,\\s*");
        long[] array = new long[elements.length];
        for (int i = 0; i < elements.length; i++)
        {
            array[i] = parseLong(key, elements[i]);
        }

        return array;
    }

    public long getLong(String key)
    {
        return getLong(key, null);
    }

    public long getLong(String key, long defaultValue)
    {
        return getLong(key, Long.valueOf(defaultValue));
    }

    private long getLong(String key, Long defaultValue)
    {
        String value = getConfigValue(key);
        if (value == null)
        {
            if (defaultValue == null)
            {
                configNotFound(key);
                return 0;
            }
            else
            {
                return defaultValue.longValue();
            }
        }
        return parseLong(key, value);
    }

    private long parseLong(String key, String value)
    {
        try
        {
            if (value.startsWith("0x"))
            {
                return Long.parseLong(value, 16);
            }
            if (value.startsWith("0o"))
            {
                return Long.parseLong(value, 8);
            }
            if (value.startsWith("0b"))
            {
                return Long.parseLong(value, 2);
            }
            return Long.parseLong(value, 10);
        }
        catch (NumberFormatException e)
        {
            badConfigType(key, value, Long.TYPE);
            return 0;
        }
    }

    public File getFile(String key, FileType... allowedTypes)
    {
        return getFile(key, null, allowedTypes);
    }

    public File getFile(String key, File relativeTo, FileType... allowedTypes)
    {
        String value = getConfigValue(key);
        if (value == null)
        {
            return null;
        }
        if ("".equals(value))
        {
            return null;
        }
        return resolveFile(key, value, relativeTo, allowedTypes);
    }

    public Resource getResource(String key, FileType... allowedTypes)
    {
        return getResource(key, null, allowedTypes);
    }

    public Resource getResource(String key, File relativeTo, FileType... allowedTypes)
    {
        String value = getConfigValue(key);
        if (value == null)
        {
            return null;
        }
        if ("".equals(value))
        {
            return null;
        }
        return resolveResource(key, value, relativeTo, allowedTypes);
    }

    public File[] getFilePath(String key, FileType... allowedTypes)
    {
        return getFilePath(key, null, allowedTypes);
    }

    public File[] getFilePath(String key, File relativeTo, FileType... allowedTypes)
    {
        String value = getConfigValue(key);
        if (value == null)
        {
            return EMPTY_FILE_PATH;
        }
        if ("".equals(value))
        {
            return EMPTY_FILE_PATH;
        }
        String[] parts = value.split(":");
        File[] path = new File[parts.length];
        for (int i = 0; i < path.length; i++)
        {
            path[i] = resolveFile(key, parts[i], relativeTo, allowedTypes);
        }
        return path;
    }

    public Resource[] getResourcePath(String key, FileType... allowedTypes)
    {
        return getResourcePath(key, null, allowedTypes);
    }

    public Resource[] getResourcePath(String key, File relativeTo, FileType... allowedTypes)
    {
        String value = getConfigValue(key);
        if (value == null)
        {
            return EMPTY_RESOURCE_PATH;
        }
        if ("".equals(value))
        {
            return EMPTY_RESOURCE_PATH;
        }
        String[] parts = value.split(":");
        Resource[] path = new Resource[parts.length];
        for (int i = 0; i < path.length; i++)
        {
            path[i] = resolveResource(key, parts[i], relativeTo, allowedTypes);
        }
        return path;
    }

    protected Resource resolveResource(String key, String name, File relativeTo, FileType... allowedArray)
    {
        File file = resolveFile(key, name, relativeTo, allowedArray);
        return new FileResource(file, name);
    }

    protected File resolveFile(String key, String name, File relativeTo, FileType... allowedArray)
    {
        File file;
        if (relativeTo == null)
        {
            file = new File(name);
        }
        else
        {
            file = new File(relativeTo, name);
        }

        if (isAllowed(file, new HashSet<FileType>(Arrays.asList(allowedArray))))
        {
            return normalise(file);
        }
        else
        {
            badFileType(key, file, allowedArray);
            return null;
        }
    }

    private File normalise(File file)
    {
        try
        {
            return file.getCanonicalFile();
        }
        catch (IOException e)
        {
            return file;
        }
    }

    private boolean isAllowed(File file, Set<FileType> allowed)
    {
        if (allowed.contains(FileType.ANY_NAME))
        {
            return true;
        }
        if (file.exists() && allowed.contains(FileType.EXISTING_ANY))
        {
            return true;
        }
        if (file.isFile() && allowed.contains(FileType.EXISTING_FILE))
        {
            return true;
        }
        if (file.isDirectory() && allowed.contains(FileType.EXISTING_DIRECTORY))
        {
            return true;
        }
        if (file.isFile() && file.getName().endsWith(".c") && allowed.contains(FileType.EXISTING_LPC))
        {
            return true;
        }
        return false;
    }

    public String toString()
    {
        return getClass().getSimpleName() + "{" + getConfigLocation() + "}";
    }
}
