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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.adjective.stout.core.UnresolvedType;
import org.adjective.stout.impl.ParameterisedClassImpl;
import org.adjective.stout.operation.Expression;
import org.adjective.stout.operation.ThisExpression;
import org.adjective.stout.operation.VM;

import us.terebi.lang.lpc.compiler.bytecode.ByteCodeConstants;
import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.ArgumentSemantics;
import us.terebi.lang.lpc.runtime.FieldDefinition;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.ObjectDefinition;
import us.terebi.lang.lpc.runtime.jvm.LpcFunction;
import us.terebi.lang.lpc.runtime.jvm.LpcObject;
import us.terebi.lang.lpc.runtime.jvm.exception.InternalError;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;
import us.terebi.util.Pair;
import us.terebi.util.collection.ArrayStack;
import us.terebi.util.collection.MultiHashMap;
import us.terebi.util.collection.MultiMap;
import us.terebi.util.collection.Stack;

/**
 * 
 */
public class VariableLookup implements VariableResolver
{
    public static class ObjectPath
    {
        enum Type
        {
            INHERIT, FIELD, ENCLOSING;
        }

        public final String lpcName;
        public final String internalName;
        public final Type type;
        public final Object definition;

        @SuppressWarnings("hiding")
        private ObjectPath(String lpcName, String internalName, Type type, Object definition)
        {
            this.lpcName = lpcName;
            this.internalName = internalName;
            this.type = type;
            this.definition = definition;
        }

        public String toString()
        {
            return lpcName + " (" + type + ";" + definition + ")";
        }

        public static Pair<Expression, UnresolvedType> findTarget(VariableLookup.ObjectPath[] path)
        {
            Expression target = ThisExpression.INSTANCE;
            UnresolvedType type = null;

            for (VariableLookup.ObjectPath step : path)
            {
                if (step.type == Type.INHERIT)
                {
                    Expression inherited = VM.Expression.getField(target, type, "inherit_" + step.internalName,
                            ByteCodeConstants.INHERITED_OBJECT_TYPE);
                    target = VM.Expression.callMethod(inherited, ByteCodeConstants.INHERITED_OBJECT_TYPE, ByteCodeConstants.INHERITED_OBJECT_GET);
                }
                else if (step.type == Type.ENCLOSING)
                {
                    target = VM.Expression.callMethod(VM.Expression.thisObject(), LpcFunction.class, ByteCodeConstants.FUNCTION_OWNER);
                    target = VM.Expression.cast(CompiledInstance.class ,target);
                    target = VM.Expression.callMethod(target,  CompiledInstance.class, ByteCodeConstants.GET_IMPLEMENTING_OBJECT);
                }
                else
                {
                    throw new UnsupportedOperationException("findTarget(" + step.type + ") - Not implemented");
                }
                if (step.definition instanceof UnresolvedType)
                {
                    type = (UnresolvedType) step.definition;
                }
                else if (step.definition instanceof CompiledObjectDefinition)
                {
                    CompiledObjectDefinition cod = (CompiledObjectDefinition) step.definition;
                    type = new ParameterisedClassImpl(cod.getImplementationClass());
                }
                else
                {
                    throw new UnsupportedOperationException("Calls to non compiled methods are not yet implemented (" + step + ")");
                }

                target = VM.Expression.cast(type, target);
            }

            return new Pair<Expression, UnresolvedType>(target, type);
        }

        public static ObjectPath inherit(String scope, ObjectDefinition object)
        {
            return new ObjectPath(scope, scope, Type.INHERIT, object);
        }

        public static ObjectPath enclosing(String name, UnresolvedType enclosingType)
        {
            if (enclosingType == null)
            {
                throw new InternalError("Attempt to create enclosing Path '" + name + "' with to no object");
            }
            return new ObjectPath("", name, Type.ENCLOSING, enclosingType);
        }

        public static ObjectPath enclosing(String name, ObjectDefinition enclosingType)
        {
            if (enclosingType == null)
            {
                throw new InternalError("Attempt to create enclosing Path '" + name + "' with to no object");
            }
            return new ObjectPath("", name, Type.ENCLOSING, enclosingType);
        }

        public ObjectDefinition getDefinition()
        {
            return (ObjectDefinition) this.definition;
        }
    }

    public static class VariableReference implements VariableResolution
    {
        private static final ObjectPath[] EMPTY_PATH = new ObjectPath[0];

        public final VariableResolution.Kind kind;
        public final String name;
        public final String _internalName;
        public final LpcType type;
        public final ObjectPath[] objectPath;

        private VariableReference(VariableResolution.Kind k, String n, String internal, LpcType t, ObjectPath[] path)
        {
            kind = k;
            name = n;
            _internalName = internal;
            type = t;
            objectPath = (path == null ? EMPTY_PATH : path);
        }

        public static VariableReference field(String name, String internalName, LpcType type, List<ObjectPath> objectPath)
        {
            ObjectPath[] path = (objectPath == null ? null : objectPath.toArray(new ObjectPath[objectPath.size()]));
            if (internalName == null)
            {
                internalName = name;
            }
            return new VariableReference(VariableResolution.Kind.FIELD, name, internalName, type, path);
        }

        public static VariableReference parameter(String name, ArgumentSemantics semantics, LpcType type)
        {
            Kind kind = semantics == ArgumentSemantics.BY_VALUE ? VariableResolution.Kind.PARAMETER : VariableResolution.Kind.REF;
            return new VariableReference(kind, name, name, type, null);
        }

        public static VariableReference local(String name, LpcType type)
        {
            return new VariableReference(VariableResolution.Kind.LOCAL, name, name, type, null);
        }

        public static VariableReference enclosing(String name, String internalName, LpcType type)
        {
            return new VariableReference(Kind.ENCLOSING, name, internalName, type, null);
        }

        public static VariableReference internal(String name, LpcType type)
        {
            return new VariableReference(Kind.INTERNAL, name, name, type, null);
        }

        public static VariableReference enclosed(VariableReference ref, ObjectDefinition enclosingObject, String enclosingPath)
        {
            ObjectPath[] path = new ObjectPath[1 + (ref.objectPath == null ? 0 : ref.objectPath.length)];
            path[0] = ObjectPath.enclosing(enclosingPath, enclosingObject);
            if (path.length > 1)
            {
                System.arraycopy(ref.objectPath, 0, path, 1, ref.objectPath.length);
            }
            return new VariableReference(ref.kind, ref.name, ref._internalName, ref.type, path);
        }

        public String toString()
        {
            return getClass().getSimpleName() + "{" + kind + " " + type + " " + name + "(" + _internalName + ")}";
        }

        public Expression access()
        {
            if (kind == VariableResolution.Kind.FIELD)
            {
                Pair<Expression, UnresolvedType> target = ObjectPath.findTarget(objectPath);
                return VM.Expression.getField(target.getFirst(), target.getSecond(), _internalName, ByteCodeConstants.LPC_FIELD);
            }
            else if (kind == VariableResolution.Kind.ENCLOSING)
            {
                Pair<Expression, UnresolvedType> target = ObjectPath.findTarget(objectPath);
                return VM.Expression.getField(target.getFirst(), target.getSecond(), _internalName, ByteCodeConstants.LPC_REFERENCE);
            }
            else
            {
                return VM.Expression.variable(_internalName);
            }
        }

        public String internalName()
        {
            return _internalName;
        }

        public String lpcName()
        {
            return name;
        }

        public LpcType type()
        {
            return type;
        }

        public VariableResolution.Kind kind()
        {
            return kind;
        }

    }

    private int _internalVariableCount;
    private final MultiMap<String, VariableReference> _inherited;
    private final Stack<Map<String, VariableReference>> _stack;

    public VariableLookup()
    {
        _internalVariableCount = 0;
        _inherited = new MultiHashMap<String, VariableReference>();
        _stack = new ArrayStack<Map<String, VariableReference>>();
    }

    public String allocateInternalVariableName()
    {
        return "lpc$" + (++_internalVariableCount);
    }

    public void addInherit(String name, ObjectDefinition parent)
    {
        for (FieldDefinition field : parent.getFields().values())
        {
            List<ObjectPath> path = Collections.singletonList(ObjectPath.inherit(name, parent));
            _inherited.add(field.getName(), VariableReference.field(field.getName(), null, field.getType(), path));
        }
    }

    public void pushScope()
    {
        HashMap<String, VariableReference> frame = new HashMap<String, VariableReference>();
        _stack.push(frame);
    }

    public void popScope()
    {
        @SuppressWarnings("unused")
        Map<String, VariableReference> frame = _stack.pop();
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
        Collection<VariableReference> inherit = _inherited.get(name);
        if (inherit != null)
        {
            switch (inherit.size())
            {
                case 0:
                    break;

                case 1:
                    return inherit.iterator().next();

                default:
                    {
                        StringBuilder where = new StringBuilder();
                        for (VariableReference var : inherit)
                        {
                            where.append(var.objectPath[0].lpcName);
                            where.append(',');
                        }
                        throw new LookupException("The inherited variable " + name + " exists in multiple places: " + where);
                    }
            }
        }

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

    public VariableResolution declareField(String name, LpcType type)
    {
        VariableReference var = VariableReference.field(name, null, type, null);
        return store(name, var);
    }

    public VariableReference declareField(String name, String internalName, LpcType type)
    {
        VariableReference var = VariableReference.field(name, internalName, type, null);
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

    public VariableResolution declareEnclosing(String name, String internalName, LpcType type)
    {
        VariableReference var = VariableReference.enclosing(name, internalName, type);
        return store(name, var);
    }

    public VariableResolution declareInternal(String name, LpcType type)
    {
        VariableReference var = VariableReference.internal(name, type);
        return store(name, var);
    }

}
