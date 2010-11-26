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
import org.adjective.stout.builder.ElementBuilder;
import org.adjective.stout.builder.MethodSpec;
import org.adjective.stout.builder.ParameterSpec;
import org.adjective.stout.core.ElementModifier;
import org.adjective.stout.core.MethodDescriptor;
import org.adjective.stout.operation.Expression;
import org.adjective.stout.operation.Statement;
import org.adjective.stout.operation.VM;

import us.terebi.lang.lpc.compiler.CompileException;
import us.terebi.lang.lpc.compiler.bytecode.context.CompileContext;
import us.terebi.lang.lpc.compiler.java.context.ScopeLookup;
import us.terebi.lang.lpc.compiler.util.MemberVisitor;
import us.terebi.lang.lpc.compiler.util.MethodSupport;
import us.terebi.lang.lpc.parser.ast.ASTMethod;
import us.terebi.lang.lpc.parser.ast.ASTStatementBlock;
import us.terebi.lang.lpc.parser.ast.ParserVisitor;
import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.MemberDefinition;
import us.terebi.lang.lpc.runtime.MemberDefinition.Kind;
import us.terebi.lang.lpc.runtime.MemberDefinition.Modifier;
import us.terebi.lang.lpc.runtime.jvm.LpcMember;
import us.terebi.lang.lpc.runtime.jvm.LpcMemberType;
import us.terebi.lang.lpc.runtime.jvm.LpcParameter;
import us.terebi.lang.lpc.runtime.jvm.LpcVariable;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;

/**
 * 
 */
public class MethodCompiler extends MemberVisitor implements ParserVisitor
{
    private static final String ATTRIBUTE_VARARGS = "varargs";
    private static final String ATTRIBUTE_SEMANTICS = "semantics";
    public static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_CLASS = "className";
    private static final String ATTRIBUTE_DEPTH = "depth";
    private static final String ATTRIBUTE_KIND = "kind";

    private static final Modifier[] MODIFIER_ARRAY = new Modifier[0];
    private final CompileContext _context;

    public MethodCompiler(ScopeLookup scope, CompileContext context)
    {
        super(scope);
        _context = context;
    }

    public Object visit(ASTMethod node, Object data)
    {
        ScopeLookup scope = getScope();

        scope.variables().pushScope();
        ElementBuilder<MethodDescriptor> method = buildMethod(node);
        if (method != null)
        {
            _context.publicClass().withMethod(method);
        }
        scope.variables().popScope();

        return null;
    }

    private ElementBuilder<MethodDescriptor> buildMethod(ASTMethod node)
    {
        MethodSupport support = new MethodSupport(getScope(), node, getModifiers(MemberDefinition.Kind.METHOD), getType());
        support.defineLocalMethod();
        if (support.isPrototype())
        {
            return null;
        }
        support.declareParameters();

        Modifier[] modifiers = super.getModifiers(Kind.METHOD).toArray(MODIFIER_ARRAY);
        LpcType type = getType();
        String name = support.getMethodName();
        String internalName = support.getInternalName();
        List< ? extends ArgumentDefinition> arguments = support.getArgumentDefinitions();
        List< ? extends ElementBuilder< ? extends Statement>> body = buildBody(support);

        MethodSpec method = buildMethodSpec(modifiers, type, name, internalName, arguments, body);

        return method;
    }

    public static MethodSpec buildMethodSpec(Modifier[] modifiers, LpcType type, String name, String internalName, List< ? extends ArgumentDefinition> arguments,
            List< ? extends ElementBuilder< ? extends Statement>> body)
    {
        MethodSpec method = new MethodSpec(internalName);
        method.withModifiers(ElementModifier.PUBLIC).withReturnType(LpcValue.class);

        method.withAnnotation(new AnnotationSpec(LpcMember.class).withRuntimeVisibility(true).withAttribute(ATTRIBUTE_NAME, name).withAttribute("modifiers",
                modifiers));

        AnnotationSpec typeAnnotation = getMemberTypeAnnotation(type);
        method.withAnnotation(typeAnnotation);
        
        ParameterSpec[] parameters = new ParameterSpec[arguments.size()];

        for (int i = 0; i < arguments.size(); i++)
        {
            ArgumentDefinition arg = arguments.get(i);
            LpcType argType = arg.getType();
            AnnotationSpec annotation = new AnnotationSpec(LpcParameter.class);
            annotation.withAttribute(ATTRIBUTE_KIND, argType.getKind());
            annotation.withAttribute(ATTRIBUTE_DEPTH, argType.getArrayDepth());
            if (argType.getKind() == LpcType.Kind.CLASS)
            {
                annotation.withAttribute(ATTRIBUTE_CLASS, argType.getClassDefinition().getName());
            }
            annotation.withAttribute(ATTRIBUTE_NAME, arg.getName());
            annotation.withAttribute(ATTRIBUTE_SEMANTICS, arg.getSemantics());
            annotation.withAttribute(ATTRIBUTE_VARARGS, arg.isVarArgs());
            parameters[i] = new ParameterSpec("p$" + arg.getName()).withType(LpcValue.class).withAnnotation(annotation);
        }
        method.withParameters(parameters);

        method.withBody(body);
        return method;
    }

    public static AnnotationSpec getMemberTypeAnnotation(LpcType type)
    {
        AnnotationSpec typeAnnotation = new AnnotationSpec(LpcMemberType.class) //
        .withRuntimeVisibility(true) //
        .withAttribute(ATTRIBUTE_KIND, type.getKind())//
        .withAttribute(ATTRIBUTE_DEPTH, type.getArrayDepth());
        if (type.getKind() == LpcType.Kind.CLASS)
        {
            typeAnnotation.withAttribute(ATTRIBUTE_CLASS, type.getClassDefinition().getName());
        }
        return typeAnnotation;
    }

    private List< ? extends ElementBuilder< ? extends Statement>> buildBody(MethodSupport support)
    {
        ASTStatementBlock body = support.getBody();
        List<ElementBuilder< ? extends Statement>> statements = new ArrayList<ElementBuilder< ? extends Statement>>(body.jjtGetNumChildren() * 2);

        for (ArgumentDefinition arg : support.getArgumentDefinitions())
        {
            statements.add(VM.Statement.declareVariable(LpcVariable.class, arg.getName()));
            Expression typeExpression = TypeCompiler.getTypeExpression(arg.getType());

            Expression init = VM.Expression.variable("p$" + arg.getName());
            statements.add( //
            VM.Statement.assignVariable(arg.getName(), //
                    VM.Expression.construct( //
                            VM.Method.constructor(LpcVariable.class, String.class, LpcType.class, LpcValue.class), //
                            VM.Expression.constant(arg.getName()), //
                            typeExpression, //
                            init//
                    )));
        }

        try
        {
            new StatementCompiler(getScope(), _context.enterMethod(support), statements).compileBlock(support.getBody());
        }
        catch (CompileException e)
        {
            throw new CompileException(e, "In method " + support.getMethodName());
        }
        catch (LpcRuntimeException e)
        {
            throw new LpcRuntimeException(e, "In method " + support.getMethodName());
        }

        statements.add(VM.Statement.returnObject(ByteCodeConstants.NIL));
        return statements;
    }
}
