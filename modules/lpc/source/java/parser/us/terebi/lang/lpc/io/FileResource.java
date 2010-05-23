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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 
 */
public class FileResource implements Resource
{
    private final File _file;
    private final String _path;

    public FileResource(File file)
    {
        this(file, null);
    }

    public FileResource(File file, String path)
    {
        _file = file;
        if (path != null)
        {
            if (path.length() == 0)
            {
                path = "/";
            }
            else if (path.charAt(0) != '/')
            {
                path = "/" + path;
            }
        }
        _path = path;
    }

    public Resource getChild(String name)
    {
        if (_path == null)
        {
            return new FileResource(new File(_file, name));
        }
        else
        {
            return new FileResource(new File(_file, name), _path + "/" + name);
        }
    }

    public Resource[] getChildren()
    {
        File[] files = _file.listFiles();
        Resource[] resources = new Resource[files.length];
        for (int i = 0; i < resources.length; i++)
        {
            resources[i] = new FileResource(files[i]);
        }
        return resources;
    }

    public String getName()
    {
        return _file.getName();
    }

    public String getParentName()
    {
        if (_path == null)
        {
            return _file.getParent();
        }
        int slash = _path.lastIndexOf('/');
        if (slash == -1)
        {
            return _file.getParent();
        }
        return _path.substring(0, slash);
    }

    public Resource getParent()
    {
        if (_path == null)
        {
            return new FileResource(_file.getParentFile());
        }
        else
        {
            return new FileResource(_file.getParentFile(), getParentName());
        }
    }

    public String getPath()
    {
        if (_path == null)
        {
            return _file.getPath();
        }
        else
        {
            return _path;
        }
    }

    public InputStream read() throws FileNotFoundException
    {
        return new FileInputStream(_file);
    }

    public OutputStream write() throws FileNotFoundException
    {
        return new FileOutputStream(_file, false);
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
                + ","
                + (_path == null ? "" : " path=" + _path)
                + " type="
                + (_file.isFile() ? 'f' : _file.isDirectory() ? 'd' : _file.exists() ? "s" : '-')
                + ")";
    }

    public long getSizeInBytes()
    {
        return _file.length();
    }

    public OutputStream append() throws IOException
    {
        return new FileOutputStream(_file, true);
    }

    public boolean isDirectory()
    {
        return _file.isDirectory();
    }

    public void delete() throws IOException
    {
        if (!exists())
        {
            throw new IOException("File " + getPath() + " does not exist");
        }
        if (!_file.delete())
        {
            throw new IOException("Cannot delete " + getPath());
        }
    }

    public void mkdir() throws IOException
    {
        if (exists())
        {
            throw new IOException("File " + getPath() + " already exists");
        }
        if (!_file.mkdir())
        {
            throw new IOException("Cannot create directory " + getPath());
        }
    }

    public boolean newerThan(long mod)
    {
        if (!exists() || mod <= 0)
        {
            return true;
        }
        return _file.lastModified() >= mod;
    }

    public long lastModified()
    {
        return _file.lastModified();
    }

    public void rename(Resource to)
    {
        if (to instanceof FileResource)
        {
            FileResource destination = (FileResource) to;
            _file.renameTo(destination._file);
        }
        else
        {
            _file.renameTo(new File(to.getPath()));
        }
    }
}
