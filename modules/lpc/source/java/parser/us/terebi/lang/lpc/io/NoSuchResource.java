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

/**
 * 
 */
public class NoSuchResource implements Resource
{
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

    public boolean isFile()
    {
        return false;
    }

    public InputStream open() throws IOException
    {
        throw new FileNotFoundException(getPath());
    }

}
