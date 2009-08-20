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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.FilenameUtils;

/**
 * 
 */
public class ByteArrayResource implements Resource
{
    private final byte[] _bytes;
    private final String _name;

    public ByteArrayResource(String name, byte[] bytes)
    {
        _name = name;
        _bytes = bytes;
    }

    public Resource getChild(String name)
    {
        return new NoSuchResource(this, name);
    }

    public String getName()
    {
        return FilenameUtils.getName(_name);
    }

    public Resource getParent()
    {
        return new NoSuchResource(getParentName());
    }

    public InputStream openInput()
    {
        return new ByteArrayInputStream(_bytes);
    }

    public OutputStream openOutput() throws IOException
    {
        throw new IOException(getClass().getSimpleName() + " is read-only");
    }
    
    public boolean exists()
    {
        return true;
    }

    public boolean isFile()
    {
        return true;
    }

    public String toString()
    {
        return getClass().getSimpleName() + "(" + _name + ", size=" + _bytes.length + ")";
    }

    public String getParentName()
    {
        return FilenameUtils.getPath(_name);
    }

    public String getPath()
    {
        return _name;
    }

    public long getSize()
    {
        return _bytes.length;
    }
}
