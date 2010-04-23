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

package us.terebi.engine;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import us.terebi.engine.config.Config;
import us.terebi.engine.config.ConfigNames;
import us.terebi.engine.objects.CoreObjects;
import us.terebi.engine.objects.EngineInitialiser;
import us.terebi.engine.plugin.PluginController;
import us.terebi.engine.server.ConnectionFactory;
import us.terebi.engine.server.TerebiServer;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.LpcConstants;
import us.terebi.lang.lpc.runtime.jvm.StandardEfuns;
import us.terebi.lang.lpc.runtime.jvm.context.Efuns;
import us.terebi.lang.lpc.runtime.jvm.context.RuntimeContext;
import us.terebi.lang.lpc.runtime.jvm.context.SystemContext;
import us.terebi.lang.lpc.runtime.jvm.context.ThreadContext;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;
import us.terebi.lang.lpc.runtime.jvm.support.ExecutionTimeCheck;
import us.terebi.lang.lpc.runtime.util.Apply;
import us.terebi.net.core.NetException;

import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isNil;
import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isStringArray;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public class Engine
{
    private final Logger LOG = Logger.getLogger(Engine.class);

    private static final String PRELOAD_APPLY = "preload";
    private static final String EPILOG_APPLY = "epilog";

    private final Config _config;

    public Engine(Config config)
    {
        _config = config;
    }

    public void run() throws IOException, NetException, InterruptedException
    {
        ExecutionTimeCheck.setDefaultEvalTime(_config.getLong(ConfigNames.MAX_EVAL_TIME_MILLIS, ExecutionTimeCheck.DEFAULT_EVAL_TIME_MILLIS));

        PluginController plugins = new PluginController(_config);
        EngineInitialiser init = new EngineInitialiser(_config, newContext());
        
        plugins.load(init.getSystemContext());
        init.load();

        plugins.init(init.getSystemContext());
        CoreObjects coreObjects = init.getCoreObjects();

        plugins.epilog(init.getSystemContext());
        ObjectInstance master = coreObjects.master();
        preload(master);
        
        runServer(master, init.getSystemContext(), plugins);
    }

    private SystemContext newContext()
    {
        return new SystemContext(getEfuns(), null, null);
    }

    private Efuns getEfuns()
    {
        ThreadContext threadContext = RuntimeContext.peek();
        if (threadContext == null)
        {
            return StandardEfuns.getImplementation();
        }
        else
        {
            return threadContext.system().efuns();
        }
    }

    private void preload(ObjectInstance master)
    {
        Apply epilogApply = new Apply(EPILOG_APPLY);
        LpcValue epilogValue = epilogApply.invoke(master, LpcConstants.INT.ZERO);
        if (isNil(epilogValue))
        {
            return;
        }
        if (!isStringArray(epilogValue))
        {
            LOG.error(epilogApply.toString() + " returned " + epilogValue + " but a string array was expected");
            return;
        }
        List<LpcValue> list = epilogValue.asList();
        Callable preload = new Apply(PRELOAD_APPLY).bind(master);
        for (LpcValue filename : list)
        {
            try
            {
                preload.execute(filename);
            }
            catch (LpcRuntimeException e)
            {
                LOG.warn("In " + preload + "(" + filename + ")", e);
            }
        }
    }

    private void runServer(ObjectInstance master, SystemContext context, PluginController plugins) throws IOException,
            NetException, InterruptedException
    {
        plugins.start(context);
        Apply connect = new Apply("connect");
        TerebiServer server = new TerebiServer(new ConnectionFactory(connect.bind(master)), _config, context);
        LOG.info("Attaching " + server + " to " + context.attachments());
        context.attachments().put(TerebiServer.class, server);
        server.start();
        plugins.run(context);
        try
        {
            Object token = Shutdown.token();
            synchronized (token)
            {
                token.wait();
            }
        }
        finally
        {
            server.stop();
        }
    }

}
