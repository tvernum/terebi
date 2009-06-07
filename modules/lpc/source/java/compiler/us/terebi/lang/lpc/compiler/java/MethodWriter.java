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
import java.util.ArrayList;
import java.util.List;

import static us.terebi.lang.lpc.compiler.java.TypeWriter.fullyQualifiedName;

import us.terebi.lang.lpc.compiler.CompileException;
import us.terebi.lang.lpc.compiler.java.StatementWriter.StatementResult;
import us.terebi.lang.lpc.compiler.java.context.CompileContext;
import us.terebi.lang.lpc.compiler.java.context.VariableLookup.VariableReference;
import us.terebi.lang.lpc.parser.ast.ASTElementExpander;
import us.terebi.lang.lpc.parser.ast.ASTFullType;
import us.terebi.lang.lpc.parser.ast.ASTIdentifier;
import us.terebi.lang.lpc.parser.ast.ASTMethod;
import us.terebi.lang.lpc.parser.ast.ASTParameterDeclaration;
import us.terebi.lang.lpc.parser.ast.ASTParameterDeclarations;
import us.terebi.lang.lpc.parser.ast.ASTRef;
import us.terebi.lang.lpc.parser.ast.ASTStatementBlock;
import us.terebi.lang.lpc.parser.ast.ASTUtil;
import us.terebi.lang.lpc.parser.ast.ParserVisitor;
import us.terebi.lang.lpc.parser.ast.SimpleNode;
import us.terebi.lang.lpc.parser.jj.ParserConstants;
import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcType.Kind;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;
import us.terebi.lang.lpc.runtime.util.Signature;

/**
 * 
 */
public class MethodWriter extends MemberWriter implements ParserVisitor
{
    public MethodWriter(CompileContext context)
    {
        super(context);
    }

    public Object visit(ASTMethod node, Object data)
    {
        getContext().variables().pushScope();
        writeMethod(node);
        getContext().variables().popScope();
        return null;
    }

    private void writeMethod(ASTMethod node)
    {
        ASTIdentifier identifier = ASTUtil.getChild(ASTIdentifier.class, node);
        ASTParameterDeclarations parameterDeclarations = ASTUtil.getChild(ASTParameterDeclarations.class, node);
        ASTStatementBlock body = ASTUtil.getChild(ASTStatementBlock.class, node);

        LpcType type = new TypeWriter(getContext()).getType(getType(), isArray());
        List< ? extends ArgumentDefinition> args = getArgumentDefinitions(parameterDeclarations);

        String name = identifier.jjtGetFirstToken().image;
        String internalName = name + "_";
        FunctionSignature signature = getFunctionSignature(type, args);
        getContext().functions().defineLocalMethod(name, internalName, signature);
        //        System.err.println("Signature of " + name + " is " + signature);
        VariableReference[] parameterVariables = getContext().variables().declareParameters(args);

        if (body == null)
        {
            return;
        }

        print("public @LpcMethod(name=\"" + name + "\", modifiers={");
        printModifierList();
        println("})");

        print("@LpcReturn(");
        print("kind=");
        print(TypeWriter.fullyQualifiedName(type.getKind()));
        print(", depth=");
        print(Integer.toString(type.getArrayDepth()));
        if (type.isClass())
        {
            print(", className=");
            print(type.getClassDefinition().getName());
        }
        println(")");

        print(" LpcValue ");
        print(internalName);
        print("( ");

        printArguments(args, parameterVariables);

        println(" )");

        MethodInfo info = new MethodInfo(name, type);
        writeMethodBody(body, info, parameterVariables);
    }

    public void writeMethodBody(ASTStatementBlock body, MethodInfo info, VariableReference[] parameterVariables)
    {
        StatementWriter statementWriter = new StatementWriter(getContext());

        PrintWriter writer = getWriter();
        writer.println(" {");

        VariableWriter variableWriter = new VariableWriter(getContext(), null);
        for (VariableReference variableReference : parameterVariables)
        {
            InternalVariable init = new InternalVariable(variableReference.internalName + "_v", false, variableReference.type);
            variableWriter.printVariable(null, variableReference, init);
        }

        StatementResult result = statementWriter.writeBlock(body);
        //        System.err.println("Result of block " + body + " is " + result);
        if (result == null)
        {
            throw new CompileException(body, "Internal Error - No statement result from writing block " + body);
        }
        if (!result.isTerminated())
        {
            if (info.returnType.getKind() != LpcType.Kind.VOID)
            {
                throw new CompileException(body, "Missing return statement (" + info.name + ")");
            }
            else
            {
                writer.println("return makeValue();");
            }
        }
        writer.println("}");
    }

    protected void printModifierList()
    {
        CharSequence builder = getModifierList(false);
        getWriter().print(builder);
    }

    public List< ? extends ArgumentDefinition> getArgumentDefinitions(ASTParameterDeclarations signature)
    {
        List<ArgumentDefinition> args = new ArrayList<ArgumentDefinition>(signature.jjtGetNumChildren());
        Iterable<SimpleNode> children = ASTUtil.children(signature);
        for (SimpleNode child : children)
        {
            assert (child instanceof ASTParameterDeclaration);
            args.add(getArgumentDefinition((ASTParameterDeclaration) child));
        }
        return args;
    }

    private FunctionSignature getFunctionSignature(LpcType type, List< ? extends ArgumentDefinition> args)
    {
        boolean varargs = hasModifier(ParserConstants.VARARGS);
        return new Signature(varargs, type, args);
    }

    public void printArguments(List< ? extends ArgumentDefinition> args, VariableReference[] parameterVariables)
    {
        int i = 0;
        for (ArgumentDefinition argumentDefinition : args)
        {
            if (i > 0)
            {
                println(" ,");
            }
            print(argumentDefinition, parameterVariables[i].internalName + "_v");
            i++;
        }
    }

    public void print(ArgumentDefinition arg, String internalName)
    {
        LpcType type = arg.getType();

        print("@LpcParameter(kind=");
        print(TypeWriter.fullyQualifiedName(type.getKind()));
        print(", depth=");
        print(Integer.toString(type.getArrayDepth()));
        if (type.getKind() == Kind.CLASS)
        {
            print(", className=\"");
            print(type.getClassDefinition().getName());
            print("\"");
        }
        print(", name=\"");
        print(arg.getName());
        print("\"");
        print(", semantics=");
        print(fullyQualifiedName(arg.getSemantics()));
        if (arg.isVarArgs())
        {
            print(", varargs=true");
        }
        print(") LpcValue ");
        print(internalName);
    }

    private ArgumentDefinition getArgumentDefinition(ASTParameterDeclaration node)
    {
        ASTFullType fullType = ASTUtil.getChild(ASTFullType.class, node);
        ASTIdentifier identifier = ASTUtil.getChild(ASTIdentifier.class, node);
        boolean ref = ASTUtil.hasChildType(ASTRef.class, node);
        boolean expander = ASTUtil.hasChildType(ASTElementExpander.class, node);

        String name = ASTUtil.getImage(identifier);

        LpcType type = new TypeWriter(getContext()).getType(fullType);
        return new ArgumentSpec(name, type, ref, expander);
    }
}
