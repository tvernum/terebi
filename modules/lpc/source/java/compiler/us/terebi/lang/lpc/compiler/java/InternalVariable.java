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

package us.terebi.lang.lpc.compiler.java;

import java.io.PrintWriter;

import us.terebi.lang.lpc.compiler.java.context.CompileContext;
import us.terebi.lang.lpc.compiler.java.context.VariableLookup.VariableReference;
import us.terebi.lang.lpc.runtime.LpcType;

class InternalVariable
{
    public final String name;
    public final boolean reference;
    public final LpcType type;

    public InternalVariable(String varName, boolean ref, LpcType varType)
    {
        this.name = varName;
        this.reference = ref;
        this.type = varType;
    }

    public InternalVariable(String varName)
    {
        this(varName, false, null);
    }

    public InternalVariable(CompileContext context, boolean ref, LpcType varType)
    {
        this(context.variables().allocateInternalVariableName(), ref, varType);
    }

    public InternalVariable(VariableReference var)
    {
        this(getName(var), true, var.type);
    }

    private static String getName(VariableReference var)
    {
        if (var.objectPath == null)
        {
            return var.internalName;
        }
        else
        {
            StringBuilder builder = new StringBuilder();
            for (String element : var.objectPath)
            {
                builder.append("inherit_");
                builder.append(element);
                builder.append(".get().");
            }
            builder.append(var.internalName);
            return builder.toString();
        }
    }

    public void declare(PrintWriter writer)
    {
        declare(writer, true);
    }

    public void declare(PrintWriter writer, boolean declareAsFinal)
    {
        // @TODO Handle cases when this is outside a method (i.e. we're declaring a field...)
        // Either make the variables private, or use an initialiser block...
        // -- This is probably done. Need to check whether we have all the cases.
        if (declareAsFinal)
        {
            writer.print("final ");
        }
        if (reference)
        {
            writer.print("LpcReference ");
        }
        else
        {
            writer.print("LpcValue ");
        }
        writer.print(name);
    }

    public void value(PrintWriter writer)
    {
        writer.print(name);
        if (reference)
        {
            writer.print(".get()");
        }
    }

    public String toString()
    {
        return getClass().getSimpleName() + "{" + type + (reference ? " ref " : " val ") + name + "}";
    }
}
