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

package us.terebi.plugins.persist.support;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import us.terebi.engine.objects.BehaviourOptions;
import us.terebi.engine.objects.BehaviourOptions.SaveBehaviour;
import us.terebi.lang.lpc.io.Resource;
import us.terebi.lang.lpc.io.ResourceFinder;
import us.terebi.lang.lpc.parser.jj.ParseException;
import us.terebi.lang.lpc.runtime.ClassInstance;
import us.terebi.lang.lpc.runtime.FieldDefinition;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectDefinition;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.MemberDefinition.Modifier;
import us.terebi.lang.lpc.runtime.jvm.context.RuntimeContext;
import us.terebi.lang.lpc.runtime.jvm.context.SystemContext;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;
import us.terebi.lang.lpc.runtime.jvm.parser.LiteralParser;
import us.terebi.lang.lpc.runtime.jvm.value.NilValue;
import us.terebi.lang.lpc.runtime.jvm.value.StringValue;
import us.terebi.lang.lpc.runtime.util.Apply;
import us.terebi.lang.lpc.runtime.util.reflect.TypeIntrospector;
import us.terebi.util.io.IOUtil;

import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isArray;
import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isClass;
import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isFloat;
import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isInteger;
import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isMapping;
import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isString;

/**
 * @version $Revision$
 */
public class ObjectSerializer
{
    private static final Logger LOG = Logger.getLogger(ObjectSerializer.class);

    private static final StringValue SAVE_EFUN = new StringValue("save_object");
    private static final StringValue RESTORE_EFUN = new StringValue("restore_object");
    private static final Apply CHECK_WRITE_ACCESS = new Apply("valid_write");
    private static final Apply CHECK_READ_ACCESS = new Apply("valid_read");

    private final String _file;

    public ObjectSerializer(String file)
    {
        SystemContext system = RuntimeContext.obtain().system();
        SaveBehaviour saveBehaviour = system.attachments().get(BehaviourOptions.class).getSaveBehaviour();

        _file = getFileName(file, saveBehaviour);
    }

    public boolean save(ObjectInstance object, boolean saveZero) throws IOException
    {
        if (_file == null)
        {
            return false;
        }

        if (!checkWriteAccess(object))
        {
            LOG.warn("Write access to file " + _file + " for object " + object + " is denied");
            return false;
        }

        Resource resource = getResource();
        LOG.info("Saving " + object + " to " + resource);
        OutputStream output = resource.write();
        try
        {
            Writer writer = new OutputStreamWriter(output);
            writerHeader(object, writer);
            persist(object, writer, saveZero);
        }
        finally
        {
            IOUtil.close(output);
        }
        return false;
    }

    private void writerHeader(ObjectInstance object, Writer writer) throws IOException
    {
        writer.write("# ");
        writer.write(object.getCanonicalName());
        writer.write(" ");
        writer.write(DateFormat.getDateTimeInstance().format(new Date()));
        writer.write("\n");
    }

    public boolean restore(ObjectInstance object, boolean zeroFirst) throws IOException
    {
        if (_file == null)
        {
            return false;
        }

        if (!checkReadAccess(object))
        {
            LOG.warn("Read access to file " + _file + " for object " + object + " is denied");
            return false;
        }

        Resource resource = getResource();
        LOG.info("Restoring " + object + " from " + resource + " (" + resource.getSizeInBytes() + " bytes)");
        if (resource.getSizeInBytes() < 3)
        {
            return false;
        }
        InputStream input = resource.read();
        try
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            restore(object, reader, zeroFirst);
            return true;
        }
        finally
        {
            IOUtil.close(input);
        }
    }

    private Resource getResource() throws IOException
    {
        SystemContext system = RuntimeContext.obtain().system();
        ResourceFinder finder = system.resourceFinder();
        Resource resource = finder.getResource(_file);
        return resource;
    }

    private boolean checkWriteAccess(ObjectInstance object)
    {
        return checkAccess(CHECK_WRITE_ACCESS, object, SAVE_EFUN);
    }

    private boolean checkReadAccess(ObjectInstance object)
    {
        return checkAccess(CHECK_READ_ACCESS, object, RESTORE_EFUN);
    }

    private boolean checkAccess(Apply apply, ObjectInstance object, StringValue name)
    {
        SystemContext system = RuntimeContext.obtain().system();
        ObjectInstance master = system.objectManager().getMasterObject();

        if (LOG.isDebugEnabled())
        {
            LOG.debug("checkAccess - " + apply + " : " + _file + " , " + object + " , " + name);
        }
        LpcValue access = apply.invoke(master, new StringValue(_file), object.asValue(), name);
        return access.asLong() != 0;
    }

    private void persist(ObjectInstance object, Writer writer, boolean saveZero) throws IOException
    {
        persist("", object, writer, saveZero);
        writer.flush();
    }

    private void persist(String prefix, ObjectInstance object, Writer writer, boolean saveZero) throws IOException
    {
        Map<String, ? extends ObjectInstance> inherited = object.getInheritedObjects();
        for (String inherit : inherited.keySet())
        {
            persist(prefix + inherit + "::", inherited.get(inherit), writer, saveZero);
        }

        Map<FieldDefinition, LpcValue> fields = object.getFieldValues();
        for (Entry<FieldDefinition, LpcValue> entry : fields.entrySet())
        {
            LpcValue value = entry.getValue();
            FieldDefinition field = entry.getKey();
            if (field.getModifiers().contains(Modifier.NOSAVE))
            {
                continue;
            }
            if (saveZero || value.asBoolean())
            {
                persist(prefix, field, value, writer);
            }
        }
    }

    public static LpcValue restore(ObjectInstance object, String value)
    {
        LiteralParser parser = new LiteralParser(object);
        return restore(value, parser);
    }

    private void restore(ObjectInstance object, BufferedReader reader, boolean zeroFirst) throws IOException
    {
        if (object == null)
        {
            throw new IllegalArgumentException("Cannot restore null object");
        }

        if (zeroFirst)
        {
            zeroVariables(object);
        }

        LiteralParser parser = new LiteralParser(object);
        for (;;)
        {
            String line = reader.readLine();
            if (line == null)
            {
                break;
            }
            if (line.startsWith("#"))
            {
                continue;
            }
            int eq = line.indexOf(' ');
            if (eq == -1)
            {
                eq = line.indexOf('=');
            }
            if (eq == -1 || eq == line.length() - 1)
            {
                LOG.warn("Invalid line (" + summarise(line) + ") in save file " + _file);
                continue;
            }
            LpcValue value = restore(line.substring(eq + 1), parser);
            if (value == null)
            {
                continue;
            }
            String name = line.substring(0, eq);
            setField(object, name, value);
        }
    }

    private static String summarise(String str)
    {
        if (str.length() <= 100)
        {
            return str;
        }
        else
        {
            return str.substring(0, 85) + " ... " + str.substring(str.length() - 10);
        }
    }

    private static LpcValue restore(String text, LiteralParser parser)
    {
        try
        {
            return parser.parse(text);
        }
        catch (ParseException e)
        {
            LOG.warn("Invalid literal '" + summarise(text) + "' - " + e.toString());
            return null;
        }
    }

    private void zeroVariables(ObjectInstance object)
    {
        for (ObjectInstance parent : object.getInheritedObjects().values())
        {
            zeroVariables(parent);
        }
        for (FieldDefinition field : object.getDefinition().getFields().values())
        {
            if (!field.getModifiers().contains(Modifier.NOSAVE))
            {
                field.setValue(object, NilValue.INSTANCE);
            }
        }
    }

    private void persist(String prefix, FieldDefinition field, LpcValue value, Writer writer) throws IOException
    {
        writer.write(prefix);
        writer.write(field.getName());
        writer.write(' ');
        writeValue(writer, value);
        writer.write('\n');
    }

    public static String save(LpcValue value)
    {
        StringWriter writer = new StringWriter();
        try
        {
            writeValue(writer, value);
        }
        catch (IOException e)
        {
            LOG.info("Cannot write variable: " + value, e);
            return null;
        }
        return writer.toString();
    }

    private static void writeValue(Writer writer, LpcValue value) throws IOException
    {
        if (isInteger(value))
        {
            writer.write(Long.toString(value.asLong()));
        }
        else if (isFloat(value))
        {
            writer.write(Double.toString(value.asDouble()));
        }
        else if (isString(value))
        {
            String str = StringValue.encodeString(value.asString()).toString();
            writer.write(str);
        }
        else if (isMapping(value))
        {
            writer.write("([");
            for (Entry<LpcValue, LpcValue> entry : value.asMap().entrySet())
            {
                writeValue(writer, entry.getKey());
                writer.write(':');
                writeValue(writer, entry.getValue());
                writer.write(',');
            }
            writer.write("])");
        }
        else if (isArray(value))
        {
            writer.write("({");
            for (LpcValue element : value.asList())
            {
                writeValue(writer, element);
                writer.write(',');
            }
            writer.write("})");
        }
        else if (isClass(value))
        {
            ClassInstance cls = value.asClass();
            writer.write("($");
            writer.write(cls.getDefinition().getName());
            writer.write(":");
            for (Entry<FieldDefinition, LpcValue> entry : cls.getFieldValues().entrySet())
            {
                writer.write(entry.getKey().getName());
                writer.write('=');
                writeValue(writer, entry.getValue());
                writer.write(',');
            }
            writer.write("$)");
        }
        else
        {
            /* TODO: Save extension types ? */
            LOG.warn("Cannot save value " + value);
        }
    }

    private boolean setField(ObjectInstance object, String name, LpcValue value)
    {
        int sep = name.indexOf("::");
        if (sep == -1)
        {
            ObjectDefinition definition = object.getDefinition();
            Map<String, ? extends FieldDefinition> fields = definition.getFields();
            FieldDefinition field = fields.get(name);
            if (field != null)
            {
                try
                {
                    field.setValue(object, value);
                    return true;
                }
                catch (LpcRuntimeException e)
                {
                    LOG.warn("Failed to set field " + field + " to " + value);
                    return false;
                }
            }
            else
            {
                LOG.debug("Object " + object + " does not have a field " + name);
            }
        }
        else
        {
            String scope = name.substring(0, sep);
            name = name.substring(sep + 2);
            ObjectInstance parent = object.getInheritedObjects().get(scope);
            if (parent != null)
            {
                if (setField(parent, name, value))
                {
                    return true;
                }
            }
            else
            {
                LOG.debug("The scope " + scope + " does not exist in " + object);
            }
        }
        TypeIntrospector introspector = new TypeIntrospector(object.getDefinition());
        FieldDefinition field = introspector.getField(name);
        if (field != null)
        {
            LOG.debug("Found field " + field + " for " + name);
            field.setValue(object, value);
            return true;
        }
        return false;
    }

    private static String getFileName(String file, SaveBehaviour behaviour)
    {
        if (file == null)
        {
            throw new NullPointerException("No file");
        }
        if (behaviour == null)
        {
            throw new NullPointerException("No behaviour");
        }
        /*
         * |  Ext |  Add  | Enforce | Disallow | Input : Result | Input : Result | Input : Result | Input : Result
         * | .dat | true  | true    | .c       | a.dat : a.dat  |   a   : a.dat  | a.sav : a.dat  |  a.c  : a.dat
         * | .dat | true  | false   | .c       | a.dat : a.dat  |   a   : a.dat  | a.sav : a.sav  |  a.c  : <fail>
         * | .dat | false | true    | .c       | a.dat : a.dat  |   a   : <fail> | a.sav : <fail> |  a.c  : <fail>
         * | .dat | false | false   | .c       | a.dat : a.dat  |   a   : a      | a.sav : a.sav  |  a.c  : <fail>
         */
        boolean hasExtension = FilenameUtils.getExtension(file).length() > 0;
        String base = FilenameUtils.removeExtension(file);
        if (behaviour.addExtension)
        {
            if (!hasExtension || behaviour.enforceExtension)
            {
                file = base + behaviour.extension;
            }
        }
        if (behaviour.enforceExtension && !file.endsWith(behaviour.extension))
        {
            LOG.warn("Bad save filename: " + file + ". Names must end with " + behaviour.extension);
            return null;
        }
        for (String ext : behaviour.disallowedExtensions)
        {
            if (file.endsWith(ext))
            {
                LOG.warn("Bad save filename: " + file + ". Names must not end with " + ext);
                return null;
            }
        }
        return file;
    }

}
