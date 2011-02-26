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

package us.terebi.lang.lpc.compiler.bytecode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import org.adjective.stout.builder.AnnotationSpec;
import org.adjective.stout.builder.ClassSpec;
import org.adjective.stout.builder.ElementBuilder;
import org.adjective.stout.builder.FieldSpec;
import org.adjective.stout.builder.MethodSpec;
import org.adjective.stout.builder.ParameterSpec;
import org.adjective.stout.core.AnnotationDescriptor;
import org.adjective.stout.core.ClassDescriptor;
import org.adjective.stout.core.ElementModifier;
import org.adjective.stout.core.MethodDescriptor;
import org.adjective.stout.core.Parameter;
import org.adjective.stout.core.SimpleType;
import org.adjective.stout.core.AnnotationDescriptor.Attribute;
import org.adjective.stout.core.UnresolvedType.Sort;
import org.adjective.stout.impl.ParameterisedClassImpl;
import org.adjective.stout.loop.Condition;
import org.adjective.stout.operation.Expression;
import org.adjective.stout.operation.InvokeSuperConstructorStatement;
import org.adjective.stout.operation.Statement;
import org.adjective.stout.operation.VM;

import us.terebi.lang.lpc.compiler.CompileException;
import us.terebi.lang.lpc.compiler.bytecode.context.CompileContext;
import us.terebi.lang.lpc.compiler.java.context.CompiledObjectDefinition;
import us.terebi.lang.lpc.compiler.java.context.FunctionLookup;
import us.terebi.lang.lpc.compiler.java.context.ScopeLookup;
import us.terebi.lang.lpc.compiler.java.context.FunctionLookup.FunctionReference;
import us.terebi.lang.lpc.compiler.util.DelegatingDeclarationVisitor;
import us.terebi.lang.lpc.compiler.util.FunctionCallSupport;
import us.terebi.lang.lpc.compiler.util.InheritSupport;
import us.terebi.lang.lpc.parser.ast.ASTDeclaration;
import us.terebi.lang.lpc.parser.ast.ASTInherit;
import us.terebi.lang.lpc.parser.util.BaseASTVisitor;
import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.MethodDefinition;
import us.terebi.lang.lpc.runtime.ObjectDefinition;
import us.terebi.lang.lpc.runtime.MemberDefinition.Modifier;
import us.terebi.lang.lpc.runtime.jvm.Dispatch;
import us.terebi.lang.lpc.runtime.jvm.InheritedObject;
import us.terebi.lang.lpc.runtime.jvm.LpcInherited;
import us.terebi.lang.lpc.runtime.jvm.LpcMember;
import us.terebi.lang.lpc.runtime.jvm.exception.InternalError;
import us.terebi.lang.lpc.runtime.jvm.naming.MethodNamer;

/**
 * 
 */
public class ClassBuilder extends BaseASTVisitor
{
    private final Logger LOG = Logger.getLogger(ClassBuilder.class);
    
    private static final String INIT_METHOD_NAME = "init";

    private final ScopeLookup _scope;
    private final ClassSpec _spec;
    private final ClassSpec _interface;
    private final DelegatingDeclarationVisitor _declarationVisitor;
    private final CompileContext _context;
    private final List<Statement> _initialisers;

    public ClassBuilder(CompileContext context, ScopeLookup scope, ClassSpec spec)
    {
        _context = context;
        _scope = scope;
        _spec = spec;
        _interface = ClassSpec.newInterface(_spec.getPackage(), _spec.getName() + "$I").withModifiers(ElementModifier.PUBLIC);
        _declarationVisitor = new DelegatingDeclarationVisitor(//
                new ClassCompiler(_scope, context), new FieldCompiler(_scope, context), new MethodCompiler(_scope, context));
        _initialisers = new ArrayList<Statement>();
    }

    public void compile()
    {
        _scope.variables().pushScope();
        _spec.withField(createThisField());
        _context.tree().childrenAccept(this, null);
        createPureVirtualMethodStubs();
        _spec.withInterface(generateInterface());
        createDispatchMethodStubs();
        _scope.variables().popScope();
        _spec.withMethod(getConstructor());
        _spec.withMethod(getInitMethod());
    }

    private FieldSpec createThisField()
    {
        return new FieldSpec(getThisFieldName(_spec)) //
        .withType(_interface) //
        .withModifiers(ElementModifier.FINAL, ElementModifier.PRIVATE, ElementModifier.TRANSIENT);
    }

    public static String getThisFieldName(ClassSpec cls)
    {
        return cls.getName() + "$this";
    }

    public static SimpleType getInterfaceType(ClassSpec spec)
    {
        return new SimpleType(Sort.INTERFACE, spec.getPackage(), spec.getName() + "$I");
    }

    private ClassDescriptor generateInterface()
    {
        Collection<String> inherits = _scope.getInheritNames();
        for (String inherit : inherits)
        {
            ObjectDefinition parent = _scope.getInherit(inherit);
            _interface.withInterface(getInterface(parent));
        }

        List<MethodDescriptor> methods = _spec.getMethods();
        for (MethodDescriptor method : methods)
        {
            if (FunctionCallCompiler.requiresDispatch(getLpcModifiers(method)))
            {
                MethodSpec spec = new MethodSpec(method.getName());
                spec.withReturnType(method.getReturnType());
                spec.withParameters(method.getParameters());
                spec.withModifiers(ElementModifier.ABSTRACT, ElementModifier.PUBLIC);
                _interface.withMethod(spec);
            }
        }

        try
        {
            return ByteCodeCompiler.store(_interface, _context);
        }
        catch (IOException e)
        {
            throw new CompileException("Cannot write bytecode for interface " + _interface, e);
        }
    }

    private Set<Modifier> getLpcModifiers(MethodDescriptor method)
    {
        AnnotationDescriptor[] annotations = method.getAnnotations();
        for (AnnotationDescriptor annotation : annotations)
        {
            if (annotation.getType() == LpcMember.class)
            {
                for (Attribute attribute : annotation.getAttributes())
                {
                    if (attribute.getName() == "modifiers")
                    {
                        return new HashSet<Modifier>(Arrays.asList((Modifier[]) attribute.getValue()));
                    }
                }
            }
        }
        return Collections.<Modifier> emptySet();
    }

    private Class< ? > getInterface(ObjectDefinition definition)
    {
        // @TODO is this a leaky abstractio?
        assert definition instanceof CompiledObjectDefinition;
        CompiledObjectDefinition def = (CompiledObjectDefinition) definition;
        Class< ? > impl = def.getImplementationClass();
        String name = impl.getName() + "$I";
        try
        {
            return impl.getClassLoader().loadClass(name);
        }
        catch (ClassNotFoundException e)
        {
            throw new CompileException("Cannot get interface " + name + " for object definition " + definition, e);
        }
    }

    private static class MethodKey
    {
        public final String lpcName;
        public final String byteCodeName;
        public final int argumentCount;

        public MethodKey(String name, String internalName, int count)
        {
            if (name == null)
            {
                throw new NullPointerException("Cannot have null method lpc-name");
            }
            if (internalName == null)
            {
                throw new NullPointerException("Cannot have null method byte-code-name");
            }
            this.lpcName = name;
            this.byteCodeName = internalName;
            this.argumentCount = count;
        }

        public MethodKey(MethodDescriptor methodDescriptor)
        {
            this(getLpcName(methodDescriptor), methodDescriptor.getName(), methodDescriptor.getParameters().length);
        }

        private static String getLpcName(MethodDescriptor methodDescriptor)
        {
            for (AnnotationDescriptor annotation : methodDescriptor.getAnnotations())
            {
                if (annotation.getType() == LpcMember.class)
                {
                    for (Attribute attribute : annotation.getAttributes())
                    {
                        if (attribute.getName() == MethodCompiler.ATTRIBUTE_NAME)
                        {
                            return attribute.getValue().toString();
                        }
                    }
                }

            }
            return methodDescriptor.getName();
        }

        public int hashCode()
        {
            return lpcName.hashCode() ^ argumentCount;
        }

        public boolean equals(Object obj)
        {
            if (obj == null)
            {
                return false;
            }
            if (obj == this)
            {
                return true;
            }
            if (obj instanceof ClassBuilder.MethodKey)
            {
                ClassBuilder.MethodKey other = (ClassBuilder.MethodKey) obj;
                return this.lpcName.equals(other.lpcName)
                        && this.byteCodeName.equals(other.byteCodeName)
                        && this.argumentCount == other.argumentCount;
            }
            return false;
        }

        public String toString()
        {
            return getClass().getSimpleName() + ':' + lpcName + '(' + argumentCount + ')';
        }
    }

    private void createDispatchMethodStubs()
    {
        Set<MethodKey> required = new HashSet<MethodKey>();
        for (String inherit : _scope.getInheritNames())
        {
            ObjectDefinition parent = _scope.getInherit(inherit);
            getAllMethodNames(required, parent);
        }

        Set<MethodKey> implemented = getImplementedMethods(_spec);
        required.removeAll(implemented);

        FunctionCallSupport support = new FunctionCallSupport(_scope);
        for (MethodKey key : required)
        {
            FunctionReference reference = support.findInheritedFunction(key.lpcName, key.argumentCount);

            LOG.info("Dispatch of " + this._spec.getInternalName() + "->" + key.lpcName + " = " + reference.toString());
            
            List< ? extends ArgumentDefinition> arguments = reference.signature.getArguments();
            if (arguments.size() != key.argumentCount)
            {
                throw new InternalError("Method reference "
                        + reference
                        + " has "
                        + arguments.size()
                        + " arguments, but dispatch is expecting "
                        + key.argumentCount);
            }
            MethodSpec method = new MethodSpec(key.byteCodeName);

            method.withModifiers(ElementModifier.PUBLIC, ElementModifier.FINAL, ElementModifier.SYNTHETIC);
            method.withAnnotation(new AnnotationSpec(Dispatch.class));
            method.withReturnType(ByteCodeConstants.LPC_VALUE);

            Parameter[] parameters = new Parameter[arguments.size()];
            for (int i = 0; i < parameters.length; i++)
            {
                String name = "arg$" + arguments.get(i).getName();
                parameters[i] = new ParameterSpec(name).withType(ByteCodeConstants.LPC_VALUE).create();
            }
            method.withParameters(parameters);

            Expression[] args = new Expression[parameters.length];
            for (int i = 0; i < args.length; i++)
            {
                args[i] = VM.Expression.variable(parameters[i].getName());
            }
            ElementBuilder<Statement> body = VM.Statement.returnObject(FunctionCallCompiler.callFunction(reference, args, _context));
            method.withBody(body.create());

            _spec.withMethod(method);
        }
    }

    private void getAllMethodNames(Set<MethodKey> names, ObjectDefinition obj)
    {
        // @TODO this shouldn't be here
        MethodNamer namer = new MethodNamer(true, false);
        for (String name : obj.getMethods().keySet())
        {
            MethodDefinition definition = obj.getMethods().get(name);
            MethodKey key = new MethodKey(name, namer.getInternalName(name), definition.getSignature().getArguments().size());
            if (names.contains(key))
            {
                continue;
            }
            if (FunctionCallCompiler.requiresDispatch(definition.getModifiers()))
            {
                names.add(key);
            }
        }
        for (ObjectDefinition inherit : (Collection< ? extends ObjectDefinition>) obj.getInheritedObjects().values())
        {
            getAllMethodNames(names, inherit);
        }
    }

    private void createPureVirtualMethodStubs()
    {
        final FunctionLookup functions = _scope.functions();

        Set<MethodKey> implemented = getImplementedMethods(_spec);
        Set<MethodKey> unimplemented = new HashSet<MethodKey>();

        Set<String> declared = functions.getLocalMethodNames();
        for (String dec : declared)
        {
            FunctionSignature sig = functions.getLocalMethodSignature(dec);
            MethodKey key = new MethodKey(dec, functions.getInternalName(sig), sig.getArguments().size());
            if (!implemented.contains(key))
            {
                unimplemented.add(key);
            }
        }

        for (MethodKey key : unimplemented)
        {
            List<FunctionReference> inherited = functions.findFunctions("", key.lpcName, false);
            if (!inherited.isEmpty())
            {
                continue;
            }
            FunctionSignature signature = functions.getLocalMethodSignature(key.lpcName);
            Set< ? extends Modifier> modifiers = functions.getLocalMethodModifiers(key.lpcName);
            addEmptyMethod(key, signature, modifiers);
        }
    }

    private Set<MethodKey> getImplementedMethods(ClassSpec spec)
    {
        Set<MethodKey> implemented = new HashSet<MethodKey>();
        for (MethodDescriptor methodDescriptor : spec.getMethods())
        {
            implemented.add(new MethodKey(methodDescriptor));
        }
        return implemented;
    }

    private void addEmptyMethod(MethodKey key, FunctionSignature signature, Set< ? extends Modifier> modifiers)
    {
        Modifier[] modifierArray = new Modifier[modifiers.size() + 1];
        modifiers.toArray(modifierArray);
        modifierArray[modifierArray.length - 1] = Modifier.PURE_VIRTUAL;

        List< ? extends ElementBuilder< ? extends Statement>> body = Collections.singletonList(VM.Statement.returnObject(ByteCodeConstants.NIL));

        MethodSpec method = MethodCompiler.buildMethodSpec(modifierArray, signature.getReturnType(), key.lpcName, key.byteCodeName,
                signature.getArguments(), body);
        _spec.withMethod(method);
    }

    @SuppressWarnings("unchecked")
    private MethodDescriptor getConstructor()
    {
        Statement[] statements = new Statement[3];
        statements[0] = VM.Statement.superConstructor(new Class[] { CompiledObjectDefinition.class }, VM.Expression.variable("definition")).create();

        Condition isNull = VM.Condition.isNull(VM.Expression.variable("this"));
        statements[1] = VM.Statement.assignField( //
                getThisFieldName(_spec), //
                VM.Condition.conditional(isNull, VM.Expression.thisObject(), VM.Expression.variable("this")) //
        ).create();

        statements[2] = VM.Statement.returnVoid().create();

        MethodSpec method = new MethodSpec(InvokeSuperConstructorStatement.CONSTRUCTOR_NAME);
        method.withParameters( //
                new ParameterSpec("this").withType(_interface),//
                new ParameterSpec("definition").withType(CompiledObjectDefinition.class) //
        );
        method.withModifiers(ElementModifier.PUBLIC).withBody(statements);
        return method.create();
    }

    private MethodDescriptor getInitMethod()
    {
        Statement[] statements = new Statement[_initialisers.size() + 1];
        int i = 0;
        for (Statement statement : _initialisers)
        {
            statements[i] = statement;
            i++;
        }
        statements[i] = VM.Statement.returnVoid().create();

        MethodSpec method = new MethodSpec(INIT_METHOD_NAME);
        method.withReturnType(Void.TYPE);
        method.withModifiers(ElementModifier.PUBLIC).withBody(statements);
        return method.create();
    }

    public Object visit(ASTInherit node, Object data)
    {
        InheritSupport util = new InheritSupport(node, _scope);
        CompiledObjectDefinition parent = util.getParentObject();

        String name = parent.getBaseName();
        FieldSpec field = new FieldSpec("inherit_" + name);
        field.withType(new ParameterisedClassImpl(InheritedObject.class, parent.getImplementationClass()));
        field.withModifiers(ElementModifier.PUBLIC);
        AnnotationSpec annotation = new AnnotationSpec(LpcInherited.class);
        annotation.withAttribute("name", name);
        annotation.withAttribute("lpc", parent.getName());
        annotation.withAttribute("implementation", parent.getImplementationClass().getName());
        field.withAnnotation(annotation);
        _spec.withField(field);

        return null;
    }

    @SuppressWarnings("unchecked")
    public Object visit(ASTDeclaration node, Object data)
    {
        Object result = _declarationVisitor.visit(node, data);
        if (result != null)
        {
            _initialisers.addAll((List<Statement>) result);
        }
        return result;
    }

}
