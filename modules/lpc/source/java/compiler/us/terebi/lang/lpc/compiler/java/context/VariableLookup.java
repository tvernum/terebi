/* ------------------------------------------------------------------------
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

package us.terebi.lang.lpc.compiler.java.context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.ArgumentSemantics;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.ObjectDefinition;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;
import us.terebi.util.collection.ArrayStack;
import us.terebi.util.collection.Stack;

/**
 * 
 */
public class VariableLookup
{
    public enum Kind
    {
        FIELD, PARAMETER, REF, LOCAL;
    }

    public static class VariableReference
    {
        public final Kind kind;
        public final String name;
        public final String internalName;
        public final LpcType type;
        public final ObjectDefinition object;
        public final String[] objectPath;

        private VariableReference(Kind k, String n, String internal, LpcType t, ObjectDefinition o, String[] path)
        {
            kind = k;
            name = n;
            internalName = internal;
            type = t;
            object = o;
            objectPath = path;
        }

        public static VariableReference field(String name, LpcType type, ObjectDefinition o, List<String> objectPath)
        {
            String[] path = (objectPath == null ? null : objectPath.toArray(new String[objectPath.size()]));
            return new VariableReference(Kind.FIELD, name, "_f_" + name, type, o, path);
        }

        public static VariableReference parameter(String name, ArgumentSemantics semantics, LpcType type)
        {
            return new VariableReference(semantics == ArgumentSemantics.BY_VALUE ? Kind.PARAMETER : Kind.REF, name, "_p_" + name,
                    type, null, null);
        }

        public static VariableReference local(String name, LpcType type)
        {
            return new VariableReference(Kind.LOCAL, name, "_l_" + name, type, null, null);
        }

        public String toString()
        {
            return getClass().getSimpleName() + "{" + kind + " " + type + " " + name + "(" + internalName + ")}";
        }
    }

    private int _internalVariableCount;
    private Stack<Map<String, VariableReference>> _stack;

    public VariableLookup()
    {
        _internalVariableCount = 0;
        _stack = new ArrayStack<Map<String, VariableReference>>();
    }

    public String allocateInternalVariableName()
    {
        return "_lpc_v" + (++_internalVariableCount);
    }

    public void addInherit(@SuppressWarnings("unused")
    String name, @SuppressWarnings("unused")
    ObjectDefinition parent)
    {
        // @TODO Auto-generated method stub
    }

    public void pushScope()
    {
        HashMap<String, VariableReference> frame = new HashMap<String, VariableReference>();
        //  System.err.println("Adding frame " + frame);
        _stack.push(frame);
    }

    public void popScope()
    {
        @SuppressWarnings("unused")
        Map<String, VariableReference> frame = _stack.pop();
        //  System.err.println("Dropping frame " + frame);
    }

    public VariableReference getVariableInFrame(String name)
    {
        return _stack.peek().get(name);
    }

    public VariableReference findVariable(String name)
    {
        for (Map<String, VariableReference> frame : _stack)
        {
            VariableReference var = frame.get(name);
            if (var != null)
            {
                return var;
            }
        }
        // @TODO inherited ?
        return null;
    }

    public VariableReference declareLocal(String name, LpcType type)
    {
        VariableReference var = VariableReference.local(name, type);
        return store(name, var);
    }

    public VariableReference declareParameter(String name, ArgumentSemantics semantics, LpcType type)
    {
        VariableReference var = VariableReference.parameter(name, semantics, type);
        return store(name, var);
    }

    public VariableReference[] declareParameters(List< ? extends ArgumentDefinition> args)
    {
        VariableReference[] vars = new VariableReference[args.size()];
        for (int i = 0; i < vars.length; i++)
        {
            ArgumentDefinition arg = args.get(i);
            vars[i] = declareParameter(args.get(i).getName(), arg.getSemantics(), arg.getType());
        }
        return vars;
    }

    public VariableReference declareField(String name, LpcType type)
    {
        VariableReference var = VariableReference.field(name, type, null, null);
        return store(name, var);
    }

    private VariableReference store(String name, VariableReference var)
    {
        Map<String, VariableReference> frame = _stack.peek();
        if (frame.containsKey(name))
        {
            throw new LpcRuntimeException("Internal Error - Attempt to store two variables of the same name into a single frame");
        }
        //        System.err.println("Storing " + name + "(" + var + ") in frame " + frame);
        frame.put(name, var);
        return var;
    }

}
