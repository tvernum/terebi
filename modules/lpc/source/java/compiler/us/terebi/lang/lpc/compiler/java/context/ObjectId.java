/* ------------------------------------------------------------------------
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

package us.terebi.lang.lpc.compiler.java.context;

/**
 * 
 */
public class ObjectId
{
    private final String _file;
    private final long _id;

    public ObjectId(String file, long id)
    {
        _file = normalise(file);
        _id = id;
    }

    public ObjectId(String name) throws NumberFormatException
    {
        int hash = name.indexOf('#');
        if (hash == -1)
        {
            _file = normalise(name);
            _id = 0;
        }
        else
        {
            _file = normalise(name.substring(0, hash));
            _id = Long.parseLong(name.substring(hash + 1));
        }
    }

    public ObjectId(CompiledObjectInstance object)
    {
        this(object.getDefinition().getName(), object.getId());
    }

    public static String normalise(String name)
    {
        if (name.length() == 0)
        {
            return name;
        }
        if (name.endsWith(".c"))
        {
            name = name.substring(0, name.length() - 2);
        }
        name = name.replace("//", "/");
        if (name.charAt(0) != '/')
        {
            name = "/" + name;
        }
        return name;
    }

    public String getFile()
    {
        return _file;
    }

    public long getId()
    {
        return _id;
    }

    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_file == null) ? 0 : _file.hashCode());
        result = prime * result + (int) (_id ^ (_id >>> 32));
        return result;
    }

    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        ObjectId other = (ObjectId) obj;
        return equals(other);
    }

    public boolean equals(ObjectId other)
    {
        if (_file.equals(other._file) && _id == other._id)
        {
            return true;
        }
        return false;
    }

}
