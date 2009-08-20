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

package us.terebi.plugins.persist.efun;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import us.terebi.engine.objects.BehaviourOptions;
import us.terebi.engine.objects.BehaviourOptions.SaveBehaviour;
import us.terebi.lang.lpc.io.Resource;
import us.terebi.lang.lpc.io.ResourceFinder;
import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.ClassInstance;
import us.terebi.lang.lpc.runtime.FieldDefinition;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.MemberDefinition.Modifier;
import us.terebi.lang.lpc.runtime.jvm.LpcConstants;
import us.terebi.lang.lpc.runtime.jvm.context.RuntimeContext;
import us.terebi.lang.lpc.runtime.jvm.context.SystemContext;
import us.terebi.lang.lpc.runtime.jvm.context.ThreadContext;
import us.terebi.lang.lpc.runtime.jvm.efun.AbstractEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.ThisObjectEfun;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.ObjectValue;
import us.terebi.lang.lpc.runtime.jvm.value.StringValue;
import us.terebi.lang.lpc.runtime.util.Apply;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;
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
public class SaveObjectEfun extends AbstractEfun
{
    private final static Logger LOG = Logger.getLogger(SaveObjectEfun.class);

    private static final StringValue EFUN_NAME = new StringValue("save_object");
    private static final Apply CHECK_ACCESS = new Apply("valid_write");

    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        return Arrays.asList( //
                new ArgumentSpec("file", Types.STRING), //
                new ArgumentSpec("flag", Types.INT) //
        );
    }

    public boolean isVarArgs()
    {
        return true;
    }

    public LpcType getReturnType()
    {
        return Types.INT;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        checkArguments(arguments, 1);
        String file = arguments.get(0).asString();
        long flag = arguments.get(1).asLong();
        ThreadContext threadContext = RuntimeContext.obtain();
        ObjectInstance object = ThisObjectEfun.this_object(threadContext);
        SystemContext system = threadContext.system();
        BehaviourOptions options = system.attachments().get(BehaviourOptions.class);
        ResourceFinder finder = system.resourceFinder();
        try
        {
            return getValue(save_object(object, file, finder, flag, system.objectManager().getMasterObject(), options.getSaveBehaviour()));
        }
        catch (IOException e)
        {
            LOG.warn("I/O error during save_object(" + arguments + ")", e);
            return LpcConstants.INT.FALSE;
        }
    }

    public static boolean save_object(ObjectInstance object, String file, ResourceFinder finder, long flag,
            ObjectInstance master, SaveBehaviour behaviour) throws IOException
    {
        file = getFileName(file, behaviour);
        if (file == null)
        {
            return false;
        }

        LpcValue access = CHECK_ACCESS.invoke(master, new StringValue(file), new ObjectValue(object), EFUN_NAME);
        if (access.asLong() == 0)
        {
            LOG.warn("Write access to file " + file + " for object " + object + " is denied");
            return false;
        }

        Resource resource = finder.getResource(file);
        LOG.info("Saving " + object + " to " + resource);
        OutputStream output = resource.openOutput();
        try
        {
            Writer writer = new OutputStreamWriter(output);
            persist(object, writer, flag == 1);
        }
        finally
        {
            IOUtil.close(output);
        }
        return false;
    }

    private static void persist(ObjectInstance object, Writer writer, boolean saveZero) throws IOException
    {
        persist("", object, writer, saveZero);
        writer.flush();
    }

    private static void persist(String prefix, ObjectInstance object, Writer writer, boolean saveZero) throws IOException
    {
        int count = 0 ;
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
                count++;
            }
        }
        LOG.debug("Saved " + count + " variables for " + object.getCanonicalName() + " " + prefix);
    }

    private static void persist(String prefix, FieldDefinition field, LpcValue value, Writer writer) throws IOException
    {
        writer.write(prefix);
        writer.write(field.getName());
        writer.write('=');
        writeValue(writer, value);
        writer.write('\n');
    }

    private static void writeValue(Writer writer, LpcValue value) throws IOException
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
            writer.write("(|");
            writer.write(cls.getDefinition().getName());
            writer.write("|");
            for (Entry<FieldDefinition, LpcValue> entry : cls.getFieldValues().entrySet())
            {
                writer.write(entry.getKey().getName());
                writer.write(':');
                writeValue(writer, entry.getValue());
                writer.write(',');
            }
            writer.write(")");
        }
        /* TODO: Save extension types ? */
    }

    private static String getFileName(String file, SaveBehaviour behaviour)
    {
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
