/* ------------------------------------------------------------------------
 * The Terebi (LPC) Game Engine
 * Copyright 2009 Tim Vernum
 * ------------------------------------------------------------------------
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ------------------------------------------------------------------------
 */

package us.terebi.engine.plugin;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import us.terebi.engine.config.Config;
import us.terebi.engine.config.Config.FileType;
import us.terebi.lang.lpc.runtime.jvm.context.RuntimeContext;
import us.terebi.lang.lpc.runtime.jvm.context.SystemContext;
import us.terebi.util.io.IOUtil;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public class PluginController
{
    private static final String CONFIG_PLUGIN_DIR = "plugins.directory";

    final Logger LOG = Logger.getLogger(PluginController.class);

    private final Config _config;
    private final File _pluginDirectory;
    private URLClassLoader _classLoader;

    private List<PluginResolver> _plugins;

    public PluginController(Config config)
    {
        _config = config;
        _pluginDirectory = _config.getFile(CONFIG_PLUGIN_DIR, FileType.EXISTING_DIRECTORY);
        if (_pluginDirectory == null)
        {
            LOG.warn("No plugin directory specified (" + CONFIG_PLUGIN_DIR + " in " + _config + ")");
        }
        _classLoader = null;
    }

    private void loadPlugins()
    {
        List<String> pluginInclude = Arrays.asList(_config.getStrings("plugins.include"));
        List<String> pluginExclude = Arrays.asList(_config.getStrings("plugins.exclude"));

        LOG.info("Loading plugins: "
                + IOUtil.canonicalPath(_pluginDirectory)
                + " ; IN="
                + pluginInclude
                + " ; EX="
                + pluginExclude);

        File[] pluginFiles = getPluginFiles(pluginInclude, pluginExclude);
        _plugins = new ArrayList<PluginResolver>(pluginFiles.length);
        for (File file : pluginFiles)
        {
            LOG.info("Loading plugin " + file);
            PluginResolver plugin = new PluginResolver(file);
            if (plugin.isValid())
            {
                _plugins.add(plugin);
            }
            else
            {
                LOG.info("Dropping invalid plugin " + IOUtil.canonicalPath(file));
            }
        }
    }

    private URL[] getPluginUrls()
    {
        URL[] urls = new URL[_plugins.size()];
        int i = 0;
        for (PluginResolver plugin : _plugins)
        {
            urls[i] = plugin.getConfig().getUrl();
            i++;
        }
        return urls;
    }

    private File[] getPluginFiles(final List<String> include, final List<String> exclude)
    {
        FilenameFilter filter = new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                if (exclude.contains(name))
                {
                    LOG.info("Plugin " + name + " is excluded");
                    return false;
                }
                if (include.isEmpty() || include.contains(name))
                {
                    LOG.info("Plugin " + name + " is included");
                    return true;
                }
                LOG.info("Plugin " + name + " is not included");
                return false;
            }
        };
        File[] pluginFiles = _pluginDirectory.listFiles(filter);
        return pluginFiles;
    }


    /**
     * Called very early in the initialisation process, before the master object or simul-efun objects are loaded.
     * The provided {@link SystemContext} will have efuns, and some attachments (such as {@link us.terebi.engine.objects.CompileOptions},
     * but will not have an object manager or master/simul-efun objects.
     * This is the appropriate place to configure new efuns (so they can be used in the master object) and new preprocessor directives
     */
    public void load(SystemContext context)
    {
        if (_pluginDirectory == null)
        {
            _plugins = Collections.emptyList();
            return;
        }

        loadPlugins();
        _classLoader = new URLClassLoader(getPluginUrls(), getClass().getClassLoader());
        for (PluginResolver plugin : _plugins)
        {
            plugin.load(_classLoader, _config, context);
        }
    }

    /**
     * Called during the initialisation process, after the object manager is loaded, but before the master object and simul-efun objects are loaded
     */
    public void init(SystemContext context)
    {
        for (PluginResolver plugin : _plugins)
        {
            plugin.init(context);
        }
    }

    /**
     * Called during the initialisation process, after the master object and simul-efun objects are loaded, but before epilog is called in the  master object
     */
    public void epilog(SystemContext context)
    {
        for (PluginResolver plugin : _plugins)
        {
            plugin.epilog(context);
        }
    }

    /**
     * Called after the initialisation process, before external connections are opened.
     * {@link RuntimeContext} will be set at this time
     */
    public void start(SystemContext context)
    {
        for (PluginResolver plugin : _plugins)
        {
            plugin.start(context);
        }
    }

    /**
     * Called at the end of the start-up process, after external connections are opened.
     * {@link RuntimeContext} will be set at this time
     */
    public void run(SystemContext context)
    {
        for (PluginResolver plugin : _plugins)
        {
            plugin.run(context);
        }
    }


}
