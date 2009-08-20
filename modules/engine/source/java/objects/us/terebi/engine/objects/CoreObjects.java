/* ------------------------------------------------------------------------
 * Copyright 2009 Tim Vernum
 * ------------------------------------------------------------------------
 * Licensed under the GNU Affero General Public License (AGPL) 
 * ------------------------------------------------------------------------
 */

package us.terebi.engine.objects;

import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.context.ObjectManager;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public class CoreObjects
{
    private final ObjectInstance _master;
    private final  ObjectInstance _sefun;
    private final ObjectManager _manager;

    public CoreObjects(ObjectManager manager, ObjectInstance master, ObjectInstance sefun)
    {
        _manager = manager;
        _master = master;
        _sefun = sefun;
    }

    public ObjectManager manager()
    {
        return _manager;
    }

    public ObjectInstance master()
    {
        return _master;
    }

    public ObjectInstance sefun()
    {
        return _sefun;
    }

}
