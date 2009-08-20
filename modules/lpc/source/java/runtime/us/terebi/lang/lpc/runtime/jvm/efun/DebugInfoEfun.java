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

package us.terebi.lang.lpc.runtime.jvm.efun;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import us.terebi.lang.lpc.compiler.java.context.CompiledObjectInstance;
import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.AttributeMap;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.FieldDefinition;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectDefinition;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.StringValue;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;

/**
 * 
 */
public class DebugInfoEfun extends AbstractEfun implements FunctionSignature, Callable
{
    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        ArrayList<ArgumentDefinition> list = new ArrayList<ArgumentDefinition>();
        list.add(new ArgumentSpec("flag", Types.INT));
        list.add(new ArgumentSpec("arg", Types.MIXED));
        return list;
    }

    public LpcType getReturnType()
    {
        return Types.STRING;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        checkArguments(arguments);
        long flag = arguments.get(0).asLong();
        ObjectInstance obj = arguments.get(1).asObject();
        CharSequence info = getDebugInfo(flag, obj);
        return new StringValue(info);
    }

    private CharSequence getDebugInfo(long flag, ObjectInstance obj)
    {
        if (flag == 0)
        {
            return getAttributes(obj);
        }
        if (flag == 1)
        {
            return getStats(obj);
        }
        if (flag == 1)
        {
            return getFields(obj);
        }
        return "";
    }

    private CharSequence getAttributes(ObjectInstance obj)
    {
        StringBuilder builder = new StringBuilder();
        AttributeMap attributes = obj.getAttributes();
        Set<Entry<String, Object>> entries = attributes.asMap().entrySet();
        for (Entry<String, Object> entry : entries)
        {
            builder.append(entry.getKey());
            builder.append(": ");
            builder.append(entry.getValue());
        }
        return builder;
    }

    private CharSequence getStats(ObjectInstance instance)
    {
        StringBuilder builder = new StringBuilder();
        ObjectDefinition definition = instance.getDefinition();
        builder.append("Object: ");
        builder.append(instance.getCanonicalName());
        builder.append('\n');

        if (instance instanceof CompiledObjectInstance)
        {
            CompiledObjectInstance cInstance = (CompiledObjectInstance) instance;
            Object implementingObject = cInstance.getImplementingObject();
            Class< ? extends Object> implementingClass = implementingObject.getClass();

            builder.append("Implemented By: ");
            builder.append(implementingClass);
            builder.append('@');
            builder.append(System.identityHashCode(implementingObject));
            builder.append('\n');
            builder.append("Loaded By: ");
            builder.append(implementingClass.getClassLoader());
            builder.append('\n');
            builder.append("Derived from: ");
            builder.append(implementingClass.getSuperclass());
            for (Class< ? > iface : implementingClass.getInterfaces())
            {
                builder.append(",");
                builder.append(iface);
            }
            builder.append('\n');
        }
        builder.append("Attributes: ");
        builder.append(instance.getAttributes().size());

        builder.append("Definition: ");
        builder.append(definition.getName());
        builder.append('\n');
        builder.append("Inherits: ");
        for (ObjectDefinition parent : definition.getInheritedObjects().values())
        {
            builder.append(parent.getName());
            builder.append(",");
        }
        builder.append('\n');
        builder.append("Methods: ");
        builder.append(definition.getMethods().size());
        builder.append('\n');
        builder.append("Fields: ");
        builder.append(definition.getFields().size());
        builder.append('\n');
        builder.append("Classes: ");
        builder.append(definition.getDefinedClasses().size());
        builder.append('\n');

        return builder;
    }

    private CharSequence getFields(ObjectInstance instance)
    {
        StringBuilder builder = new StringBuilder();
        for (Entry<FieldDefinition, LpcValue> entry : instance.getFieldValues().entrySet())
        {
            builder.append(entry.getKey().getName());
            builder.append(": ");
            builder.append(entry.getValue().debugInfo());
            builder.append('\n');
        }
        return builder;
    }
}
