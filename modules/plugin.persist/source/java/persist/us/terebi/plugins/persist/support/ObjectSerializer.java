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
import java.io.Writer;
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
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.MemberDefinition.Modifier;
import us.terebi.lang.lpc.runtime.jvm.context.RuntimeContext;
import us.terebi.lang.lpc.runtime.jvm.context.SystemContext;
import us.terebi.lang.lpc.runtime.jvm.parser.LiteralParser;
import us.terebi.lang.lpc.runtime.jvm.value.NilValue;
import us.terebi.lang.lpc.runtime.jvm.value.ObjectValue;
import us.terebi.lang.lpc.runtime.jvm.value.StringValue;
import us.terebi.lang.lpc.runtime.util.Apply;
import us.terebi.util.io.IOUtil;

import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isArray;
import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isClass;
import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isFloat;
import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isInt;
import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isMapping;
import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isNil;
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
        OutputStream output = resource.openOutput();
        try
        {
            Writer writer = new OutputStreamWriter(output);
            persist(object, writer, saveZero);
        }
        finally
        {
            IOUtil.close(output);
        }
        return false;
    }

    public boolean restore(ObjectInstance object, boolean zeroNoSave) throws IOException
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
        LOG.info("Restoring " + object + " from " + resource);
        InputStream input = resource.openInput();
        try
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            restore(object, reader, zeroNoSave);
        }
        finally
        {
            IOUtil.close(input);
        }
        return false;
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

        LpcValue access = apply.invoke(master, new StringValue(_file), new ObjectValue(object), name);
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
            persist(inherit + "::", inherited.get(inherit), writer, saveZero);
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

    private void restore(ObjectInstance object, BufferedReader reader, boolean zeroNoSave) throws IOException
    {
        if (zeroNoSave)
        {
            zeroNoSave(object);
        }

        LiteralParser parser = new LiteralParser(object);
        for (;;)
        {
            String line = reader.readLine();
            if (line == null)
            {
                break;
            }
            int eq = line.indexOf('=');
            if (eq == -1)
            {
                LOG.warn("Invalid line (" + line + ") in save file " + _file);
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

    private LpcValue restore(String text, LiteralParser parser)
    {
        try
        {
            return parser.parse(text);
        }
        catch (ParseException e)
        {
            LOG.info("Invalid literal '" + text + "'" + e.toString());
            return null;
        }
    }

    private void zeroNoSave(ObjectInstance object)
    {
        for (ObjectInstance parent : object.getInheritedObjects().values())
        {
            zeroNoSave(parent);
        }
        for (FieldDefinition field : object.getDefinition().getFields().values())
        {
            if (field.getModifiers().contains(Modifier.NOSAVE))
            {
                field.setValue(object, NilValue.INSTANCE);
            }
        }
    }

    private void persist(String prefix, FieldDefinition field, LpcValue value, Writer writer) throws IOException
    {
        writer.write(prefix);
        writer.write(field.getName());
        writer.write('=');
        writeValue(writer, value);
        writer.write('\n');
    }

    private void writeValue(Writer writer, LpcValue value) throws IOException
    {
        if (isInt(value))
        {
            writer.write(Long.toString(value.asLong()));
        }
        else if (isFloat(value))
        {
            writer.write(Double.toString(value.asDouble()));
        }
        else if (isNil(value))
        {
            writer.write("0");
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
        /* TODO: Save extension types ? */
    }

    private void setField(ObjectInstance object, String name, LpcValue value)
    {
        int sep = name.indexOf("::");
        if (sep == -1)
        {
            FieldDefinition field = object.getDefinition().getFields().get(name);
            if (field == null)
            {
                LOG.info("Object " + object + " does not have a field " + name);
                LOG.info("Object " + object + " has " + object.getDefinition().getFields());
            }
            else
            {
                field.setValue(object, value);
            }
        }
        else
        {
            String scope = name.substring(0, sep);
            ObjectInstance parent = object.getInheritedObjects().get(scope);
            setField(parent, name.substring(sep + 2), value);
        }
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
