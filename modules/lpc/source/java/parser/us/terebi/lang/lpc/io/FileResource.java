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

package us.terebi.lang.lpc.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * 
 */
public class FileResource implements Resource
{
    private final File _file;
    private final String _path;

    public FileResource(File file)
    {
        this(file, file.getPath());
    }

    public FileResource(File file, String path)
    {
        _file = file;
        _path = path;
    }

    public Resource getChild(String name)
    {
        return new FileResource(new File(_file, name));
    }

    public String getName()
    {
        return _file.getName();
    }

    public Resource getParent()
    {
        return new FileResource(_file.getParentFile());
    }

    public String getPath()
    {
        return _path;
    }

    public InputStream open() throws FileNotFoundException
    {
        return new FileInputStream(_file);
    }

    public boolean exists()
    {
        return _file.exists();
    }

    public boolean isFile()
    {
        return _file.isFile();
    }

    public String toString()
    {
        return getClass().getSimpleName()
                + "("
                + _file
                + ", type="
                + (_file.isFile() ? 'f' : _file.isDirectory() ? 'd' : _file.exists() ? "s" : '-')
                + ")";
    }

}
