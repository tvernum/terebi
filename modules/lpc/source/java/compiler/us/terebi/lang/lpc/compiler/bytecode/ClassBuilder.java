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

import java.util.ArrayList;
import java.util.List;

import org.adjective.stout.builder.AnnotationSpec;
import org.adjective.stout.builder.ClassSpec;
import org.adjective.stout.builder.FieldSpec;
import org.adjective.stout.builder.MethodSpec;
import org.adjective.stout.builder.ParameterSpec;
import org.adjective.stout.core.ElementModifier;
import org.adjective.stout.core.MethodDescriptor;
import org.adjective.stout.impl.ParameterisedClassImpl;
import org.adjective.stout.operation.InvokeSuperConstructorStatement;
import org.adjective.stout.operation.Statement;
import org.adjective.stout.operation.VM;

import us.terebi.lang.lpc.compiler.java.context.CompiledObjectDefinition;
import us.terebi.lang.lpc.compiler.java.context.ScopeLookup;
import us.terebi.lang.lpc.compiler.util.DelegatingDeclarationVisitor;
import us.terebi.lang.lpc.compiler.util.InheritSupport;
import us.terebi.lang.lpc.parser.ast.ASTDeclaration;
import us.terebi.lang.lpc.parser.ast.ASTInherit;
import us.terebi.lang.lpc.parser.util.BaseASTVisitor;
import us.terebi.lang.lpc.runtime.jvm.InheritedObject;
import us.terebi.lang.lpc.runtime.jvm.LpcInherited;

/**
 * 
 */
public class ClassBuilder extends BaseASTVisitor
{
    private final ScopeLookup _scope;
    private final ClassSpec _spec;
    private final DelegatingDeclarationVisitor _declarationVisitor;
    private final CompileContext _context;
    private final List<Statement> _initialisers;

    public ClassBuilder(CompileContext context, ScopeLookup scope, ClassSpec spec)
    {
        _context = context;
        _scope = scope;
        _spec = spec;
        _declarationVisitor = new DelegatingDeclarationVisitor(new ClassCompiler(_scope, context), new FieldCompiler(_scope, context),
                new MethodCompiler(_scope, context));
        _initialisers = new ArrayList<Statement>();
    }

    public void compile()
    {
        _scope.variables().pushScope();
        _context.tree().childrenAccept(this, null);
        _scope.variables().popScope();
        _spec.withMethod(getConstructor());
    }

    @SuppressWarnings("unchecked")
    private MethodDescriptor getConstructor()
    {
        Statement[] statements = new Statement[2 + _initialisers.size()];
        statements[0] = VM.Statement.superConstructor(new Class[] { CompiledObjectDefinition.class }, VM.Expression.variable("definition")).create();

        int i = 1;
        for (Statement statement : _initialisers)
        {
            statements[i] = statement;
            i++;
        }

        statements[i] = VM.Statement.returnVoid().create();

        MethodSpec method = new MethodSpec(InvokeSuperConstructorStatement.CONSTRUCTOR_NAME);
        method.withParameters(new ParameterSpec("definition").withType(CompiledObjectDefinition.class));
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
