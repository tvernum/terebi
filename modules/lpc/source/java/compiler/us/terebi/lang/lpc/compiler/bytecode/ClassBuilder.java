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
import us.terebi.lang.lpc.compiler.java.context.CompiledObjectDefinition;
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

/**
 * 
 */
public class ClassBuilder extends BaseASTVisitor
{
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
        _declarationVisitor = new DelegatingDeclarationVisitor(new ClassCompiler(_scope, context), new FieldCompiler(_scope, context),
                new MethodCompiler(_scope, context));
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

    private void createDispatchMethodStubs()
    {
        Set<String> required = new HashSet<String>();
        for (String inherit : _scope.getInheritNames())
        {
            ObjectDefinition parent = _scope.getInherit(inherit);
            getAllMethodNames(required, parent);
        }

        required.removeAll(getImplementedMethods(_spec));

        FunctionCallSupport support = new FunctionCallSupport(_scope);
        for (String methodName : required)
        {
            FunctionReference reference = support.findFunction(null, null, methodName);
            MethodSpec method = new MethodSpec(methodName);

            method.withModifiers(ElementModifier.PUBLIC, ElementModifier.FINAL, ElementModifier.SYNTHETIC);
            method.withAnnotation(new AnnotationSpec(Dispatch.class));
            method.withReturnType(ByteCodeConstants.LPC_VALUE);

            List< ? extends ArgumentDefinition> arguments = reference.signature.getArguments();
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

    private void getAllMethodNames(Set<String> names, ObjectDefinition obj)
    {
        for (String name : obj.getMethods().keySet())
        {
            if (names.contains(name))
            {
                continue;
            }
            MethodDefinition definition = obj.getMethods().get(name);
            if (FunctionCallCompiler.requiresDispatch(definition.getModifiers()))
            {
                names.add(name);
            }
        }
        for (ObjectDefinition inherit : (Collection< ? extends ObjectDefinition>) obj.getInheritedObjects().values())
        {
            getAllMethodNames(names, inherit);
        }
    }

    private void createPureVirtualMethodStubs()
    {
        Set<String> declared = _scope.functions().getLocalMethodNames();

        Set<String> implemented = getImplementedMethods(_spec);

        Set<String> unimplemented = new HashSet<String>(declared);
        unimplemented.removeAll(implemented);

        for (String name : unimplemented)
        {
            List<FunctionReference> inherited = _scope.functions().findFunctions("", name, false);
            if (!inherited.isEmpty())
            {
                continue;
            }
            FunctionSignature signature = _scope.functions().getLocalMethodSignature(name);
            Set< ? extends Modifier> modifiers = _scope.functions().getLocalMethodModifiers(name);
            addEmptyMethod(name, signature, modifiers);
        }
    }

    private Set<String> getImplementedMethods(ClassSpec spec)
    {
        Set<String> implemented = new HashSet<String>();
        for (MethodDescriptor methodDescriptor : spec.getMethods())
        {
            implemented.add(methodDescriptor.getName());
        }
        return implemented;
    }

    private void addEmptyMethod(String name, FunctionSignature signature, Set< ? extends Modifier> modifiers)
    {
        Modifier[] modifierArray = new Modifier[modifiers.size() + 1];
        modifiers.toArray(modifierArray);
        modifierArray[modifierArray.length - 1] = Modifier.PURE_VIRTUAL;

        List< ? extends ElementBuilder< ? extends Statement>> body = Collections.singletonList(VM.Statement.returnObject(ByteCodeConstants.NIL));

        MethodSpec method = MethodCompiler.buildMethodSpec(modifierArray, signature.getReturnType(), name, signature.getArguments(), body);
        _spec.withMethod(method);
    }

    @SuppressWarnings("unchecked")
    private MethodDescriptor getConstructor()
    {
        Statement[] statements = new Statement[3 + _initialisers.size()];
        statements[0] = VM.Statement.superConstructor(new Class[] { CompiledObjectDefinition.class }, VM.Expression.variable("definition")).create();

        Condition isNull = VM.Condition.isNull(VM.Expression.variable("this"));
        statements[1] = VM.Statement.assignField( //
                getThisFieldName(_spec), //
                VM.Condition.conditional(isNull, VM.Expression.thisObject(), VM.Expression.variable("this")) //
        ).create();

        int i = 2;
        for (Statement statement : _initialisers)
        {
            statements[i] = statement;
            i++;
        }

        statements[i] = VM.Statement.returnVoid().create();

        MethodSpec method = new MethodSpec(InvokeSuperConstructorStatement.CONSTRUCTOR_NAME);
        method.withParameters( //
                new ParameterSpec("this").withType(_interface),//
                new ParameterSpec("definition").withType(CompiledObjectDefinition.class) //
        );
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
