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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 
 */
public class NoSuchResource implements Resource
{
    private static final Resource[] NO_CHILDREN = new Resource[0];

    private final Resource _parent;
    private final String _name;

    public NoSuchResource(Resource parent, String name)
    {
        _parent = parent;
        _name = name;
    }

    public NoSuchResource(String name)
    {
        _parent = null;
        _name = name;
    }

    public boolean exists()
    {
        return false;
    }

    public Resource getChild(String name)
    {
        return new NoSuchResource(this, name);
    }

    public Resource[] getChildren()
    {
        return NO_CHILDREN;
    }

    public String getName()
    {
        return _name;
    }

    public Resource getParent()
    {
        return _parent;
    }

    public String getPath()
    {
        return (_parent == null ? _name : _parent.getPath() + "/" + _name);
    }

    public String getParentName()
    {
        return (_parent == null ? "" : _parent.getPath());
    }

    public boolean isFile()
    {
        return false;
    }

    public boolean isDirectory()
    {
        return false;
    }

    public InputStream read() throws IOException
    {
        throw doesNotExist();
    }

    public OutputStream write() throws IOException
    {
        throw doesNotExist();
    }

    public OutputStream append() throws IOException
    {
        throw doesNotExist();
    }

    public String toString()
    {
        return getClass().getSimpleName() + "(" + getPath() + ")";
    }

    public long getSizeInBytes()
    {
        return 0;
    }

    public void delete() throws IOException
    {
        throw doesNotExist();
    }

    public void mkdir() throws IOException
    {
        throw doesNotExist();
    }

    private FileNotFoundException doesNotExist()
    {
        return new FileNotFoundException(getPath());
    }

    public boolean newerThan(long mod)
    {
        return false;
    }

    public long lastModified()
    {
        return -1;
    }

    public void rename(Resource to) throws IOException
    {
        throw doesNotExist();
    }

}
