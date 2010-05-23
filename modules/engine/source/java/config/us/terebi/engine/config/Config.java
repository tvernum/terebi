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

import us.terebi.lang.lpc.io.Resource;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public interface Config
{
    public enum FileType
    {
        ANY_NAME, EXISTING_ANY, EXISTING_DIRECTORY, EXISTING_FILE, EXISTING_LPC
    }

    public boolean hasKey(String key);
    
    public boolean getBoolean(String key);
    public boolean getBoolean(String key, boolean defaultValue);

    public long getLong(String key);
    public long[] getLongs(String key);
    public long getLong(String key, long defaultValue);

    public String getString(String key);
    public String[] getStrings(String string);
    public String getString(String key, String defaultValue);
    
    public File getFile(String key, FileType... allowedTypes);
    public File getFile(String key, File relativeTo, FileType... allowedTypes);

    public Resource getResource(String key, FileType... allowedTypes);
    public Resource getResource(String key, File relativeTo, FileType... allowedTypes);
    
    public File[] getFilePath(String key, FileType... allowedTypes);
    public File[] getFilePath(String key, File relativeTo, FileType... allowedTypes);

    public Resource[] getResourcePath(String key, FileType... allowedTypes);
    public Resource[] getResourcePath(String key, File relativeTo, FileType... allowedTypes);
}
