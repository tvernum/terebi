/* ------------------------------------------------------------------------
 * Copyright 2009 Tim Vernum
 * ------------------------------------------------------------------------
 * Licensed under the GNU Affero General Public License (AGPL) 
 * ------------------------------------------------------------------------
 */

package us.terebi.engine.objects;

import java.io.File;

import us.terebi.engine.config.Config;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public class MudlibSetup
{
    private static final String CONFIG_SEFUN_OBJECT = "mudlib.sefun";
    private static final String CONFIG_MASTER_OBJECT = "mudlib.master";
    private static final String CONFIG_MUDLIB_ROOT = "mudlib.root";

    private final File _root;
    private final File _master;
    private final File _sefun;

    
    public MudlibSetup(Config config) {
        _root = config.getFile(CONFIG_MUDLIB_ROOT, Config.FileType.EXISTING_DIRECTORY);
        _master = config.getFile(CONFIG_MASTER_OBJECT, _root, Config.FileType.EXISTING_LPC);
        _sefun = config.getFile(CONFIG_SEFUN_OBJECT, _root, Config.FileType.EXISTING_LPC);
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
