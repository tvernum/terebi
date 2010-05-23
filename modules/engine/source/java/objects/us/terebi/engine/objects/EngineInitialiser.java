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

package us.terebi.engine.objects;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;

import us.terebi.engine.config.Config;
import us.terebi.engine.config.ConfigNames;
import us.terebi.lang.lpc.compiler.CompilerObjectManager;
import us.terebi.lang.lpc.compiler.ObjectBuilder;
import us.terebi.lang.lpc.compiler.ObjectBuilderFactory;
import us.terebi.lang.lpc.compiler.bytecode.context.DebugOptions;
import us.terebi.lang.lpc.io.FileFinder;
import us.terebi.lang.lpc.io.Resource;
import us.terebi.lang.lpc.io.ResourceFinder;
import us.terebi.lang.lpc.parser.LpcParser;
import us.terebi.lang.lpc.runtime.ObjectDefinition;
import us.terebi.lang.lpc.runtime.jvm.context.Efuns;
import us.terebi.lang.lpc.runtime.jvm.context.ObjectMap;
import us.terebi.lang.lpc.runtime.jvm.context.RuntimeContext;
import us.terebi.lang.lpc.runtime.jvm.context.SystemContext;
import us.terebi.lang.lpc.runtime.jvm.support.ExecutionTimeCheck;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public class EngineInitialiser
{
    private final Config _config;
    private final MudlibSetup _mudlib;
    private final BehaviourOptions _behaviour;
    private final CompileOptions _compileOptions;
    private CoreObjects _coreObjects;
    private SystemContext _context;
    private CompilerObjectManager _objectManager;

    public EngineInitialiser(Config config, SystemContext context)
    {
        _config = config;
        _context = context;
        _mudlib = loadMudlibConfig();
        _behaviour = loadBehaviourOptions();
        _compileOptions = loadCompileOptions();
        _coreObjects = null;
    }

    private CoreObjects loadCoreObjects() throws IOException
    {
        if (_objectManager == null)
        {
            loadObjectManager();
        }

        ExecutionTimeCheck check = new ExecutionTimeCheck(_config.getLong(ConfigNames.MAX_EVAL_TIME_INIT, 30000));
        try
        {
            synchronized (_context.lock())
            {
                check.begin();
                ObjectDefinition sefunDefinition = _objectManager.defineSimulatedEfunObject(getLocalPath(_mudlib.sefun()));
                ObjectDefinition masterDefinition = _objectManager.defineMasterObject(getLocalPath(_mudlib.master()));
                return new CoreObjects(_objectManager, masterDefinition.getMasterInstance(), sefunDefinition.getMasterInstance());
            }
        }
        finally
        {
            check.end();
        }
    }

    public void loadObjectManager() throws IOException
    {
        LpcParser parser = new LpcParser();
        FileFinder fileFinder = new FileFinder(_mudlib.root());
        parser.setSourceFinder(fileFinder);
        for (Resource dir : _compileOptions.includeDirectories())
        {
            parser.addSystemIncludeDirectory(dir);
        }
        for (Resource auto : _compileOptions.autoIncludeFiles())
        {
            parser.addAutoIncludeFile(auto);
        }
        for (Entry<String, String> entry : _compileOptions.getPreprocessorDefinitions().entrySet())
        {
            parser.addDefine(entry.getKey(), entry.getValue());
        }

        Efuns efuns = _context.efuns();

        ObjectBuilderFactory factory = new ObjectBuilderFactory(efuns);
        factory.setWorkingDir(_compileOptions.compilerOutputDirectory());
        factory.setParser(parser);
        factory.setDebugOptions(new DebugOptions(_compileOptions.getDebugPatterns()));
        ObjectBuilder builder = factory.createBuilder(fileFinder);

        CompilerObjectManager objectManager = builder.getObjectManager();
        configureSystemContext(objectManager, fileFinder);
        _objectManager = objectManager;
    }

    private void configureSystemContext(CompilerObjectManager objectManager, ResourceFinder resourceFinder)
    {
        ObjectMap attachments = _context.attachments();
        _context = new SystemContext(_context.efuns(), objectManager, resourceFinder);
        _context.attachments().putAll(attachments);

        attachToContext(BehaviourOptions.class, _behaviour);
        attachToContext(CompileOptions.class, _compileOptions);
        attachToContext(Config.class, _config);
        attachToContext(MudlibSetup.class, _mudlib);

        RuntimeContext.activate(_context);
    }

    private <T> void attachToContext(Class<T> type, T value)
    {
        if (value != null)
        {
            _context.attachments().put(type, value);
        }
    }

    private String getLocalPath(File file) throws IOException
    {
        String path = file.getCanonicalPath();
        String root = _mudlib.root().getCanonicalPath();
        if (path.startsWith(root))
        {
            path = path.substring(root.length());
            if (path.startsWith("/"))
            {
                path = path.substring(1);
            }
        }
        return path;
    }

    private CompileOptions loadCompileOptions()
    {
        CompileOptions options = new CompileOptions(_config, _mudlib);
        options.defineTrue("__TEREBI__");
        options.defineString("__ARCH__", getArchitecture());
        options.defineString("__SAVE_EXTENSION__", _behaviour.getSaveBehaviour().extension);
        attachToContext(CompileOptions.class, options);
        return options;
    }

    private String getArchitecture()
    {
        String version = System.getProperty("java.specification.version");
        if (version == null)
        {
            version = System.getProperty("java.version");
        }
        String name = System.getProperty("os.name", "Unknown Platform");
        return "Java " + version + " (" + name + ")";
    }

    private BehaviourOptions loadBehaviourOptions()
    {
        BehaviourOptions behaviour = new BehaviourOptions(_config);
        attachToContext(BehaviourOptions.class, behaviour);
        return behaviour;
    }

    private MudlibSetup loadMudlibConfig()
    {
        MudlibSetup mudlib = new MudlibSetup(_config);
        attachToContext(MudlibSetup.class, mudlib);
        return mudlib;
    }

    public Config getConfig()
    {
        return _config;
    }

    public MudlibSetup getMudlibSetup()
    {
        return _mudlib;
    }

    public CompileOptions getCompileOptions()
    {
        return _compileOptions;
    }

    public CoreObjects getCoreObjects() throws IOException
    {
        if (_coreObjects == null)
        {
            _coreObjects = loadCoreObjects();
        }
        return _coreObjects;
    }

    public SystemContext getSystemContext()
    {
        return _context;
    }

    public void load() throws IOException
    {
        if (_objectManager == null)
        {
            loadObjectManager();
        }
    }
}
