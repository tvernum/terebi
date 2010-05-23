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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.adjective.stout.builder.AnnotationSpec;
import org.adjective.stout.builder.ClassSpec;
import org.adjective.stout.builder.ElementBuilder;
import org.adjective.stout.builder.MethodSpec;
import org.adjective.stout.builder.ParameterSpec;
import org.adjective.stout.core.ClassDescriptor;
import org.adjective.stout.core.ConstructorSignature;
import org.adjective.stout.core.ElementModifier;
import org.adjective.stout.core.MethodDescriptor;
import org.adjective.stout.operation.Statement;
import org.adjective.stout.operation.VM;

import us.terebi.lang.lpc.compiler.CompileException;
import us.terebi.lang.lpc.compiler.bytecode.FieldCompiler.FieldDescriptor;
import us.terebi.lang.lpc.compiler.bytecode.context.CompileContext;
import us.terebi.lang.lpc.compiler.java.CompileTimeField;
import us.terebi.lang.lpc.compiler.java.context.ScopeLookup;
import us.terebi.lang.lpc.compiler.util.MemberVisitor;
import us.terebi.lang.lpc.parser.ast.ASTClassBody;
import us.terebi.lang.lpc.parser.ast.ASTType;
import us.terebi.lang.lpc.parser.ast.ASTVariable;
import us.terebi.lang.lpc.parser.ast.ASTVariableDeclaration;
import us.terebi.lang.lpc.parser.ast.ParserVisitor;
import us.terebi.lang.lpc.parser.ast.TokenNode;
import us.terebi.lang.lpc.parser.jj.ParserConstants;
import us.terebi.lang.lpc.parser.jj.Token;
import us.terebi.lang.lpc.parser.util.ASTUtil;
import us.terebi.lang.lpc.runtime.MemberDefinition;
import us.terebi.lang.lpc.runtime.ObjectDefinition;
import us.terebi.lang.lpc.runtime.MemberDefinition.Modifier;
import us.terebi.lang.lpc.runtime.jvm.LpcClass;
import us.terebi.lang.lpc.runtime.jvm.LpcMember;
import us.terebi.lang.lpc.runtime.util.type.DynamicClassDefinition;

/**
 * 
 */
public class ClassCompiler extends MemberVisitor implements ParserVisitor
{
    private final CompileContext _context;

    public ClassCompiler(ScopeLookup scope, CompileContext context)
    {
        super(scope);
        _context = context;
    }

    public Object visit(ASTClassBody node, Object data)
    {
        ASTType type = getTypeNode();
        Token token = type.jjtGetFirstToken();
        assert (token.kind == ParserConstants.CLASS);
        token = token.next;
        String lpcName = token.image;

        String internalName = _context.publicClass().getName() + "$" + lpcName;
        ClassSpec spec = ClassSpec.newClass(_context.publicClass().getPackage(), internalName);
        _context.pushClass(spec);

        DynamicClassDefinition classDefinition = new CompiledClassDefinition(lpcName, spec);

        spec.withModifiers(ElementModifier.PUBLIC, ElementModifier.FINAL);
        spec.withSuperClass(LpcClass.class);
        spec.withOuterClass(_context.publicClass());

        AnnotationSpec annotation = new AnnotationSpec(LpcMember.class);
        annotation.withRuntimeVisibility(true);
        annotation.withAttribute("name", lpcName);
        annotation.withAttribute("modifiers", new MemberDefinition.Modifier[0]);
        spec.withAnnotation(annotation);

        Set<FieldDescriptor> allFields = new HashSet<FieldDescriptor>();
        for (TokenNode child : ASTUtil.children(node))
        {
            FieldDescriptor[] fields = (FieldDescriptor[]) child.jjtAccept(this, null);
            for (FieldDescriptor field : fields)
            {
                classDefinition.addField(new CompileTimeField(classDefinition, field.name, field.type));
                FieldCompiler.addField(spec, field);
                allFields.add(field);
            }
        }

        spec.withMethod(getConstructor(allFields));

        node.childrenAccept(this, classDefinition);

        getScope().classes().defineClass(classDefinition);

        try
        {
            ClassDescriptor descriptor = ByteCodeCompiler.store(spec, _context);
            _context.publicClass().withInnerClass(descriptor);
        }
        catch (IOException e)
        {
            throw new CompileException("Cannot write class file " + spec, e);
        }

        _context.popClass(spec);

        return null;
    }

    @SuppressWarnings("unchecked")
    private MethodDescriptor getConstructor(Set<FieldDescriptor> fields)
    {
        MethodSpec constructor = new MethodSpec(ClassSpec.CONSTRUCTOR_NAME);
        constructor.withModifiers(ElementModifier.PUBLIC);
        ParameterSpec parameter = new ParameterSpec("owner").withType(ObjectDefinition.class);
        constructor.withParameters(parameter.create());

        ElementBuilder<Statement>[] body = new ElementBuilder[2 + fields.size()];

        ConstructorSignature superConstructor = VM.Method.constructor(LpcClass.class, ObjectDefinition.class);
        body[0] = VM.Statement.superConstructor(superConstructor, VM.Expression.variable("owner"));

        int i = 1;
        for (FieldDescriptor field : fields)
        {
            body[i] = FieldCompiler.initialiseLpcField(field, FieldCompiler.EnclosingType.CLASS);
            i++;
        }

        body[body.length - 1] = VM.Statement.returnVoid();

        constructor.withBody(body);
        return constructor.create();
    }

    public FieldDescriptor[] visit(ASTVariableDeclaration node, Object data)
    {
        ASTType typeNode = ASTUtil.getChild(ASTType.class, node);
        assert (typeNode != null);

        FieldDescriptor[] fields = new FieldDescriptor[node.jjtGetNumChildren() - 1];
        for (int i = 1; i < node.jjtGetNumChildren(); i++)
        {
            fields[i - 1] = (FieldDescriptor) node.jjtGetChild(i).jjtAccept(this, typeNode);
        }

        return fields;
    }

    public FieldDescriptor visit(ASTVariable node, Object data)
    {
        ASTType typeNode = (ASTType) data;
        Set<Modifier> modifiers = Collections.emptySet();
        return FieldCompiler.getFieldDescriptor(getScope(), _context, modifiers, typeNode, node);
    }

}
