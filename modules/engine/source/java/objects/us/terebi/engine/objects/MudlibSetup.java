/* ------------------------------------------------------------------------
 * Copyright 2009 Tim Vernum
 * ------------------------------------------------------------------------
 * Licensed under the GNU Affero General Public License (AGPL) 
 * ------------------------------------------------------------------------
 */

package us.terebi.engine.objects;

import java.io.File;

import us.terebi.engine.config.Config;
import us.terebi.engine.config.ConfigNames;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public class MudlibSetup
{
    private final File _root;
    private final File _master;
    private final File _sefun;

    public MudlibSetup(Config config)
    {
        _root = config.getFile(ConfigNames.MUDLIB_ROOT, Config.FileType.EXISTING_DIRECTORY);
        _master = config.getFile(ConfigNames.MASTER_OBJECT, _root, Config.FileType.EXISTING_LPC);
        _sefun = config.getFile(ConfigNames.SEFUN_OBJECT, _root, Config.FileType.EXISTING_LPC);
    }

    public MudlibSetup(File root, File master, File sefun)
    {
        _root = root;
        _master = master;
        _sefun = sefun;
    }

    public File root()
    {
        return _root;
    }

    public File sefun()
    {
        return _sefun;
    }

    public File master()
    {
        return _master;
    }
}
