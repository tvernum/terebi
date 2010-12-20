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

package us.terebi.engine.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import us.terebi.engine.server.TerebiServer.ConnectionObjectFactory;
import us.terebi.lang.lpc.runtime.AttributeMap;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.context.RuntimeContext;
import us.terebi.lang.lpc.runtime.jvm.context.SystemContext;
import us.terebi.lang.lpc.runtime.jvm.exception.InternalError;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;
import us.terebi.lang.lpc.runtime.util.Apply;
import us.terebi.net.core.AttributeChange;
import us.terebi.net.core.Connection;
import us.terebi.net.core.FeatureChange;
import us.terebi.net.core.InputInfo;
import us.terebi.net.core.Shell;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public class ObjectShell implements Shell
{
    public static final String SWITCHABLE_ATTRIBUTE_PREFIX = "user.";

    private final static Logger LOG = Logger.getLogger(ObjectShell.class);

    private static final String[] EMPTY_INPUT = new String[] { "" };

    private static final Apply LOGON_HANDLER = new Apply("logon");

    private static final String OBJECT_CONNECTION_ATTRIBUTE = SWITCHABLE_ATTRIBUTE_PREFIX + "net.connection";
    private static final String OBJECT_SWITCH_TO = SWITCHABLE_ATTRIBUTE_PREFIX + "net.connection.switch";
    private static final String OBJECT_IDLE_ATTRIBUTE = SWITCHABLE_ATTRIBUTE_PREFIX + "net.idle";
    private static final String OBJECT_INPUT_HANDLER_ATTRIBUTE = SWITCHABLE_ATTRIBUTE_PREFIX + "net.handler.input";
    private static final String OBJECT_DESTRUCT_LISTENER = SWITCHABLE_ATTRIBUTE_PREFIX + "net.connection.listener";

    private static final String PREFIX = ObjectShell.class.getName();
    private static final String CONNECTION_OBJECT_ATTRIBUTE = PREFIX + ".net.object";

    private static final Object DESTRUCT_LISTENER = new ObjectInstance.DestructListener()
    {
        public void instanceDestructed(ObjectInstance instance)
        {
            try
            {
                Connection connection = getConnection(instance);
                if (connection != null)
                {
                    connection.close();
                }
            }
            catch (IOException e)
            {
                // Ignore
            }
        }
    };

    private final ConnectionObjectFactory _factory;
    private final SystemContext _context;

    public ObjectShell(ConnectionObjectFactory factory, SystemContext context)
    {
        _factory = factory;
        _context = context;
    }

    public void connectionCreated(Connection connection) throws IOException
    {
        RuntimeContext.activate(_context);
        ObjectInstance instance = _factory.getConnectionObject(connection);
        LOG.info("New object " + instance + " for connection " + connection);

        if (instance == null)
        {
            // @TODO
            connection.getWriter().println("Mud is unavailable. Please contact the administrators for more information");
            connection.close();
            return;
        }
        instance.getAttributes().set(OBJECT_CONNECTION_ATTRIBUTE, connection);
        instance.getAttributes().set(OBJECT_DESTRUCT_LISTENER, DESTRUCT_LISTENER);
        instance.getAttributes().set(OBJECT_INPUT_HANDLER_ATTRIBUTE, new InputHandlerSet(ProcessInputHandler.INSTANCE));
        connection.getAttributes().setAttribute(CONNECTION_OBJECT_ATTRIBUTE, instance);

        LOGON_HANDLER.invoke(instance);
    }

    public void attributeChanged(AttributeChange attribute, Connection connection)
    {
        // no-op (for now)
    }

    public static ObjectInstance getConnectionObject(Connection connection)
    {
        return (ObjectInstance) connection.getAttributes().getAttribute(CONNECTION_OBJECT_ATTRIBUTE);
    }

    public static Connection getConnection(ObjectInstance instance)
    {
        return (Connection) instance.getAttributes().get(OBJECT_CONNECTION_ATTRIBUTE);
    }

    public static boolean isConnectionObject(ObjectInstance instance)
    {
        return getConnection(instance) != null;
    }

    public static boolean switchConnectionObject(Connection connection, ObjectInstance toObject)
    {
        if (isConnectionObject(toObject))
        {
            LOG.info("Object " + toObject.getCanonicalName() + " is already linked to a connection");
            return false;
        }

        ObjectInstance oldObject = getConnectionObject(connection);
        AttributeMap fromAttributes = oldObject.getAttributes();
        AttributeMap toAttributes = toObject.getAttributes();

        Iterable<String> names = fromAttributes.names();
        for (String name : names)
        {
            if (name.startsWith(SWITCHABLE_ATTRIBUTE_PREFIX))
            {
                LOG.info("Copying attribute " + name + " from " + oldObject + " to " + toObject);
                toAttributes.set(name, fromAttributes.get(name));
            }
            else
            {
                LOG.debug("Not copying attribute " + name + " from " + oldObject);
            }
        }

        fromAttributes.remove(OBJECT_CONNECTION_ATTRIBUTE);
        fromAttributes.set(OBJECT_SWITCH_TO, toObject);
        connection.getAttributes().setAttribute(CONNECTION_OBJECT_ATTRIBUTE, toObject);
        LOG.info("Switch connection " + connection + " from " + oldObject + " to " + toObject);
        return true;
    }

    public static ObjectInstance getSwitchedObject(ObjectInstance instance)
    {
        return (ObjectInstance) instance.getAttributes().get(OBJECT_SWITCH_TO);
    }

    public void connectionClosed(boolean clientInitiated, Connection connection)
    {
        ObjectInstance instance = getConnectionObject(connection);
        instance.destruct();
    }

    public void connectionIdle(long ms, Connection connection)
    {
        ObjectInstance instance = getConnectionObject(connection);
        instance.getAttributes().set(OBJECT_IDLE_ATTRIBUTE, ms);
    }

    public void featureChanged(FeatureChange feature, Connection connection)
    {
        // no-op        
    }

    public void inputReceived(byte input, InputInfo info, Connection connection)
    {
        this.inputReceived(new String(new byte[] { input }), info, connection);
    }

    public void inputReceived(ByteBuffer input, InputInfo info, Connection connection)
    {
        byte[] bytes = new byte[input.remaining()];
        for (int i = 0; i < bytes.length; i++)
        {
            bytes[i] = input.get();
        }
        this.inputReceived(new String(bytes), info, connection);
    }

    public void inputReceived(String input, InputInfo info, Connection connection)
    {
        String[] lines = input.split("[\r\n]");
        if (lines.length == 0)
        {
            lines = EMPTY_INPUT;
        }
        for (String line : lines)
        {
            ObjectInstance instance = getConnectionObject(connection);
            RuntimeContext.activate(_context);
            handleInput(instance, connection, line);
        }
    }

    private void handleInput(ObjectInstance user, Connection connection, String line)
    {
        if (line == null)
        {
            return;
        }
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Handle input for " + user + " [" + user.getDefinition() + "]");
        }

        InputHandlerSet handlers = getInputHandlers(user);
        // clone the handlers to avoid concurrent modification exceptions..
        for (InputHandler handler : new ArrayList<InputHandler>(handlers.handlers()))
        {
            try
            {
                String result = handler.inputReceived(user, connection, line);
                LOG.debug("Passed input through " + handler + " and received " + result);
                if (result == null)
                {
                    return;
                }
                line = result;
            }
            catch (LpcRuntimeException e)
            {
                LOG.error("Unhandled exception ", e);
                PrintWriter writer = connection.getWriter();
                writer.write("Oh no! Something went wrong ...");
                writer.flush();
            }
        }
    }

    public static InputHandlerSet getInputHandlers(ObjectInstance user)
    {
        if (!isConnectionObject(user))
        {
            ObjectInstance switchedObject = getSwitchedObject(user);
            if (switchedObject == null)
            {
                throw new InternalError("Attempt to get input handlers for non-connection " + user);
            }
            else
            {
                user = switchedObject;
            }
        }
        Object attribute = user.getAttributes().get(OBJECT_INPUT_HANDLER_ATTRIBUTE);
        if (attribute == null)
        {
            throw new InternalError("User attribute " + OBJECT_INPUT_HANDLER_ATTRIBUTE + " is missing from " + user);
        }

        if (!(attribute instanceof InputHandlerSet))
        {
            throw new InternalError("User attribute "
                    + OBJECT_INPUT_HANDLER_ATTRIBUTE
                    + " should be "
                    + InputHandler.class.getSimpleName()
                    + " but is "
                    + attribute.getClass());
        }

        InputHandlerSet handlers = (InputHandlerSet) attribute;
        return handlers;
    }

}
