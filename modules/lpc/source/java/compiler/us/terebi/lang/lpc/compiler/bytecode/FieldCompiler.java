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
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.adjective.stout.builder.AnnotationSpec;
import org.adjective.stout.builder.ClassSpec;
import org.adjective.stout.builder.ElementBuilder;
import org.adjective.stout.builder.FieldSpec;
import org.adjective.stout.core.ConstructorSignature;
import org.adjective.stout.core.ElementModifier;
import org.adjective.stout.core.MethodSignature;
import org.adjective.stout.operation.Expression;
import org.adjective.stout.operation.Statement;
import org.adjective.stout.operation.VM;

import us.terebi.lang.lpc.compiler.CompileException;
import us.terebi.lang.lpc.compiler.bytecode.context.CompileContext;
import us.terebi.lang.lpc.compiler.java.context.ScopeLookup;
import us.terebi.lang.lpc.compiler.java.context.VariableResolver;
import us.terebi.lang.lpc.compiler.java.context.VariableResolver.VariableResolution;
import us.terebi.lang.lpc.compiler.util.MemberVisitor;
import us.terebi.lang.lpc.compiler.util.TypeSupport;
import us.terebi.lang.lpc.parser.ast.ASTFields;
import us.terebi.lang.lpc.parser.ast.ASTType;
import us.terebi.lang.lpc.parser.ast.ASTVariable;
import us.terebi.lang.lpc.parser.ast.ASTVariableAssignment;
import us.terebi.lang.lpc.parser.ast.ParserVisitor;
import us.terebi.lang.lpc.parser.ast.TokenNode;
import us.terebi.lang.lpc.parser.util.ASTUtil;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.MemberDefinition;
import us.terebi.lang.lpc.runtime.MemberDefinition.Modifier;
import us.terebi.lang.lpc.runtime.jvm.LpcField;
import us.terebi.lang.lpc.runtime.jvm.LpcMember;
import us.terebi.lang.lpc.runtime.jvm.LpcRuntimeSupport;

/**
 * 
 */
public class FieldCompiler extends MemberVisitor implements ParserVisitor
{
    static class FieldDescriptor
    {
        final String name;
        final LpcType type;
        final Expression initialiser;
        final Collection< ? extends Modifier> modifiers;

        public FieldDescriptor(String n, LpcType t, Collection< ? extends Modifier> mods, Expression init)
        {
            this.name = n;
            this.type = t;
            this.modifiers = mods;
            this.initialiser = init;
        }
    }

    private final CompileContext _context;

    public FieldCompiler(ScopeLookup scope, CompileContext context)
    {
        super(scope);
        _context = context;
    }

    public List<Statement> visit(ASTFields node, Object data)
    {
        ClassSpec classSpec = _context.publicClass();

        List<Statement> statements = new ArrayList<Statement>();

        final VariableResolver variables = getScope().variables();
        for (TokenNode child : ASTUtil.children(node))
        {
            assert child instanceof ASTVariable : "Child node is " + child.getClass() + " (but should be " + ASTVariable.class.getSimpleName() + ")";
            FieldDescriptor field = this.visit((ASTVariable) child, null);
            VariableResolution existingVar = variables.getVariableInFrame(field.name);
            if (existingVar != null)
            {
                throw new CompileException(child, "The field " + field.name + " (" + existingVar + ") has already been declared");
            }
            variables.declareField(field.name, field.type);

            FieldCompiler.addField(classSpec, field);

            statements.add(initialiseLpcField(field, EnclosingType.OBJECT).create());
        }

        return statements;
    }

    public FieldDescriptor visit(ASTVariable node, Object data)
    {
        Set< ? extends Modifier> modifiers = super.getModifiers(MemberDefinition.Kind.FIELD);
        return getFieldDescriptor(getScope(), _context, modifiers, getTypeNode(), node);
    }

    static void addField(ClassSpec spec, FieldDescriptor field)
    {
        FieldSpec fieldSpec = new FieldSpec(field.name).withType(LpcField.class).withModifiers(ElementModifier.PUBLIC);
        fieldSpec.withAnnotation(new AnnotationSpec(LpcMember.class) //
        .withAttribute("name", field.name) //
        .withAttribute("modifiers", field.modifiers.toArray(new Modifier[0])) // 
        );
        fieldSpec.withAnnotation(MethodCompiler.getMemberTypeAnnotation(field.type));

        spec.withField(fieldSpec);
    }

    public static FieldDescriptor getFieldDescriptor(ScopeLookup scope, CompileContext context, Collection< ? extends Modifier> modifiers,
            ASTType typeNode, ASTVariable node)
    {
        LpcType type = new TypeSupport(scope, typeNode, node.isArray()).getType();
        ASTVariableAssignment assignment = node.getAssignment();
        Expression init = null;
        if (assignment != null)
        {
            ExpressionCompiler compiler = new ExpressionCompiler(scope, context);
            LpcExpression expr = compiler.compile(assignment);
            TypeSupport.checkType(node, expr.type, type);
            init = ExpressionCompiler.getValue(expr);
        }
        return new FieldDescriptor(node.getVariableName(), type, modifiers, init);
    }

    public enum EnclosingType
    {
        OBJECT, CLASS, FUNCTION
    }

    public static ElementBuilder<Statement> initialiseLpcField(FieldDescriptor field, EnclosingType enclosingType)
    {
        return VM.Statement.assignField(field.name, createLpcField(field.type, field.name, field.initialiser, enclosingType));
    }

    private static Expression createLpcField(LpcType type, String name, Expression initialiser, EnclosingType enclosingType)
    {
        if (initialiser == null)
        {
            initialiser = ByteCodeConstants.NIL;
        }

        ConstructorSignature fieldConstructor = VM.Method.constructor(LpcField.class, String.class, LpcType.class, LpcValue.class);
        Expression typeExpr = typeExpression(type, enclosingType);
        Expression field = VM.Expression.construct(fieldConstructor, VM.Expression.constant(name), typeExpr, initialiser);

        return field;
    }

    public static Expression typeExpression(LpcType type, EnclosingType enclosingType)
    {
        MethodSignature objectDefinition;
        switch (enclosingType)
        {
            case OBJECT:
                objectDefinition = ByteCodeConstants.GET_OBJECT_DEFINITION;
                break;

            case CLASS:
                objectDefinition = ByteCodeConstants.CLASS_DECLARING_OBJECT;
                break;

            case FUNCTION:
                objectDefinition = ByteCodeConstants.FUNCTION_OWNER_DEFINITION;
                break;
            default:
                throw new IllegalArgumentException("Illegal enum value " + enclosingType);
        }

        return typeExpression(type, VM.Expression.callInherited(objectDefinition, ByteCodeConstants.NO_ARGUMENTS));
    }

    public static Expression typeExpression(LpcType type, Expression objectDefinition)
    {
        Expression typeExpr = VM.Expression.callStatic(LpcRuntimeSupport.class, //
                ByteCodeConstants.WITH_TYPE_4, //
                objectDefinition, //
                VM.Expression.getEnum(type.getKind()), //
                (type.getKind() == LpcType.Kind.CLASS ? VM.Expression.constant(type.getClassDefinition().getName()) : VM.Expression.nullObject()), //
                VM.Expression.constant(type.getArrayDepth()));
        return typeExpr;
    }
}
