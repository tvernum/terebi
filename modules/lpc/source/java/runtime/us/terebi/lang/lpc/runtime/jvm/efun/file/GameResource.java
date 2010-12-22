/* ------------------------------------------------------------------------
 * Copyright 2010 Tim Vernum
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

package us.terebi.lang.lpc.runtime.jvm.efun.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import us.terebi.lang.lpc.io.Resource;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.context.RuntimeContext;
import us.terebi.lang.lpc.runtime.jvm.context.SystemContext;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcSecurityException;
import us.terebi.lang.lpc.runtime.jvm.value.StringValue;
import us.terebi.lang.lpc.runtime.util.Apply;

/**
 * 
 */
class GameResource implements Resource
{
    private static final Apply CHECK_WRITE_ACCESS = new Apply("valid_write");
    private static final Apply CHECK_READ_ACCESS = new Apply("valid_read");

    private final Resource _resource;
    private final StringValue _efun;
    private final ObjectInstance _object;

    public GameResource(Resource resource, ObjectInstance object, StringValue efun)
    {
        _resource = resource;
        _object = object;
        _efun = efun;
    }

    private void checkWriteAccess()
    {
        checkAccess(CHECK_WRITE_ACCESS, "write", _object, _efun, true);
    }

    private void checkReadAccess()
    {
        checkAccess(CHECK_READ_ACCESS, "read", _object, _efun, true);
    }

    private boolean checkAccess(Apply apply, String type, ObjectInstance object, StringValue name, boolean throwException)
    {
        SystemContext system = RuntimeContext.obtain().system();
        ObjectInstance master = system.objectManager().getMasterObject();
        if (master == null)
        {
            // Only possible while we're still loading the master object;
            return true;
        }
        LpcValue access = apply.invoke(master, new StringValue(_resource.getPath()), object.asValue(), name);
        boolean hasAccess = access.asLong() != 0;
        if (!hasAccess && throwException)
        {
            throw new LpcSecurityException("Efun "
                    + name.asString()
                    + " : no "
                    + type
                    + " access to "
                    + _resource.getPath()
                    + " from "
                    + object.getCanonicalName()
                    + " [ "
                    + master
                    + "->"
                    + apply.getName()
                    + " = "
                    + access
                    + " ]");
        }
        return hasAccess;
    }

    public boolean exists()
    {
        if (!checkAccess(CHECK_READ_ACCESS, "read", _object, _efun, false))
        {
            return false;
        }
        return _resource.exists();
    }

    public String getName()
    {
        return _resource.getName();
    }

    public String getParentName()
    {
        return _resource.getParentName();
    }

    public String getPath()
    {
        return _resource.getPath();
    }

    public long getSizeInBytes()
    {
        checkReadAccess();
        return _resource.getSizeInBytes();
    }

    public boolean isDirectory()
    {
        checkReadAccess();
        return _resource.isDirectory();
    }

    public boolean isFile()
    {
        checkReadAccess();
        return _resource.isFile();
    }

    public Resource getChild(String name)
    {
        return makeResource(_resource.getChild(name));
    }

    private GameResource makeResource(final Resource delegate)
    {
        return new GameResource(delegate, _object, _efun);
    }

    public Resource[] getChildren()
    {
        Resource[] delegate = _resource.getChildren();
        Resource[] wrapped = new Resource[delegate.length];
        for (int i = 0; i < wrapped.length; i++)
        {
            wrapped[i] = makeResource(delegate[i]);
        }
        return wrapped;
    }

    public Resource getParent()
    {
        return makeResource(_resource.getParent());
    }

    public InputStream read() throws IOException
    {
        checkReadAccess();
        return _resource.read();
    }

    public OutputStream write() throws IOException
    {
        checkWriteAccess();
        return _resource.write();
    }

    public OutputStream append() throws IOException
    {
        checkWriteAccess();
        return _resource.append();
    }

    public void delete() throws IOException
    {
        checkWriteAccess();
        _resource.delete();
    }

    public void mkdir() throws IOException
    {
        checkWriteAccess();
        _resource.mkdir();
    }

    public boolean newerThan(long mod)
    {
        checkReadAccess();
        return _resource.newerThan(mod);
    }

    public long lastModified()
    {
        checkReadAccess();
        return _resource.lastModified();
    }

    public void rename(Resource to) throws IOException
    {
        checkReadAccess();
        GameResource destination = null;
        if (to instanceof GameResource)
        {
            destination = (GameResource) to;
            to = destination._resource;
        }
        else
        {
            destination = makeResource(to);
        }
        destination.checkWriteAccess();
        _resource.rename(to);
    }

    public String toString()
    {
        return getClass().getSimpleName() + "{" + _object + ":-" + _efun + ' ' + _resource + "}";
    }

}
