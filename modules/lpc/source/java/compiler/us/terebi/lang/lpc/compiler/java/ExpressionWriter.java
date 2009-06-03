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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import us.terebi.lang.lpc.compiler.CompileException;
import us.terebi.lang.lpc.compiler.java.FunctionCallWriter.FunctionArgument;
import us.terebi.lang.lpc.compiler.java.context.CompileContext;
import us.terebi.lang.lpc.compiler.java.context.FunctionLookup.FunctionReference;
import us.terebi.lang.lpc.compiler.java.context.VariableLookup.VariableReference;
import us.terebi.lang.lpc.parser.ast.ASTArithmeticExpression;
import us.terebi.lang.lpc.parser.ast.ASTArrayElement;
import us.terebi.lang.lpc.parser.ast.ASTArrayLiteral;
import us.terebi.lang.lpc.parser.ast.ASTAssignmentExpression;
import us.terebi.lang.lpc.parser.ast.ASTBinaryOrExpression;
import us.terebi.lang.lpc.parser.ast.ASTCallOther;
import us.terebi.lang.lpc.parser.ast.ASTCastExpression;
import us.terebi.lang.lpc.parser.ast.ASTCatch;
import us.terebi.lang.lpc.parser.ast.ASTComparisonExpression;
import us.terebi.lang.lpc.parser.ast.ASTCompoundExpression;
import us.terebi.lang.lpc.parser.ast.ASTConstant;
import us.terebi.lang.lpc.parser.ast.ASTFullType;
import us.terebi.lang.lpc.parser.ast.ASTFunctionArguments;
import us.terebi.lang.lpc.parser.ast.ASTFunctionCall;
import us.terebi.lang.lpc.parser.ast.ASTFunctionLiteral;
import us.terebi.lang.lpc.parser.ast.ASTIdentifier;
import us.terebi.lang.lpc.parser.ast.ASTImmediateExpression;
import us.terebi.lang.lpc.parser.ast.ASTIndexExpression;
import us.terebi.lang.lpc.parser.ast.ASTIndexPostfix;
import us.terebi.lang.lpc.parser.ast.ASTLogicalAndExpression;
import us.terebi.lang.lpc.parser.ast.ASTLogicalOrExpression;
import us.terebi.lang.lpc.parser.ast.ASTMappingElement;
import us.terebi.lang.lpc.parser.ast.ASTMappingLiteral;
import us.terebi.lang.lpc.parser.ast.ASTParameterDeclarations;
import us.terebi.lang.lpc.parser.ast.ASTPostfixExpression;
import us.terebi.lang.lpc.parser.ast.ASTPostfixIncrementOperator;
import us.terebi.lang.lpc.parser.ast.ASTPrefixIncrementOperator;
import us.terebi.lang.lpc.parser.ast.ASTScopeResolution;
import us.terebi.lang.lpc.parser.ast.ASTScopedIdentifier;
import us.terebi.lang.lpc.parser.ast.ASTStatementBlock;
import us.terebi.lang.lpc.parser.ast.ASTTernaryExpression;
import us.terebi.lang.lpc.parser.ast.ASTUnaryExpression;
import us.terebi.lang.lpc.parser.ast.ASTUtil;
import us.terebi.lang.lpc.parser.ast.ASTVariableReference;
import us.terebi.lang.lpc.parser.ast.BaseASTVisitor;
import us.terebi.lang.lpc.parser.ast.Node;
import us.terebi.lang.lpc.parser.ast.ParserVisitor;
import us.terebi.lang.lpc.parser.ast.SimpleNode;
import us.terebi.lang.lpc.parser.jj.ParserConstants;
import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.ClassDefinition;
import us.terebi.lang.lpc.runtime.ExtensionType;
import us.terebi.lang.lpc.runtime.FieldDefinition;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;
import us.terebi.util.Pair;

/**
 * 
 */
public class ExpressionWriter extends BaseASTVisitor implements ParserVisitor
{
    private final CompileContext _context;
    private final Map<Node, InternalVariable> _evaluationCache;

    public ExpressionWriter(CompileContext context)
    {
        _context = context;
        _evaluationCache = new HashMap<Node, InternalVariable>();
    }

    public InternalVariable visit(ASTFunctionCall node, Object data)
    {
        return new FunctionCallWriter(_context).writeFunction(node);
    }

    public Object visit(ASTVariableReference node, Object data)
    {
        ASTScopedIdentifier scoped = ASTUtil.getChild(ASTScopedIdentifier.class, node);
        ASTScopeResolution resolution = ASTUtil.getChild(ASTScopeResolution.class, scoped);
        ASTIdentifier identifier = ASTUtil.getChild(ASTIdentifier.class, scoped);

        String scope = (resolution == null) ? null : (resolution.jjtGetNumChildren() == 0) ? "" : ASTUtil.getImage(resolution);
        String name = ASTUtil.getImage(identifier);

        if (scope == null)
        {
            int index = getPositionalIndex(identifier);
            if (index == 0)
            {
                VariableReference var = _context.variables().findVariable(name);
                if (var == null)
                {
                    throw new CompileException(node, "Reference to undeclared variable '" + name + "'");
                }
                return new InternalVariable(var);
            }
            else
            {
                return getPositionalVariable(index);
            }
        }
        else
        {
            // @TODO !!
            throw new CompileException(node, "Internal Error - Scoped variables are not implemented (yet)");
        }
    }

    private InternalVariable getPositionalVariable(int index)
    {
        return new InternalVariable("getArg(args, " + index + ")", false, Types.MIXED);
    }

    public InternalVariable visit(ASTPostfixExpression node, Object data)
    {
        InternalVariable previous = new InternalVariable("", false, Types.MIXED);
        for (SimpleNode child : ASTUtil.children(node))
        {
            InternalVariable var = (InternalVariable) child.jjtAccept(this, previous);
            if (var == null)
            {
                throw new NullPointerException("Child " + child + " did not return a variable");
            }
            previous = var;
        }
        return previous;
    }

    public Object visit(ASTPostfixIncrementOperator node, Object data)
    {
        assert (data instanceof InternalVariable);
        InternalVariable prefix = (InternalVariable) data;

        if (!prefix.reference)
        {
            throw new CompileException(node, "Cannot increment/decrement value " + prefix);
        }

        PrintWriter writer = _context.writer();

        InternalVariable preValue = new InternalVariable(_context, false, prefix.type);
        preValue.declare(writer);
        writer.print(" = ");
        prefix.value(writer);
        writer.println(";");

        writer.print(prefix.name);
        writer.print(".set(MathSupport.add(");

        switch (node.jjtGetFirstToken().kind)
        {
            case ParserConstants.INCREMENT:
                writer.print("1");
                break;
            case ParserConstants.DECREMENT:
                writer.print("-1");
                break;

            default:
                throw new CompileException(node, "Internal Error - invalid postfix operator " + ASTUtil.getImage(node));
        }
        writer.print(",");
        preValue.value(writer);
        writer.println("));");

        return preValue;
    }

    public Object visit(ASTIndexPostfix node, Object data)
    {
        assert (data instanceof InternalVariable);
        InternalVariable previous = (InternalVariable) data;
        PrintWriter writer = _context.writer();

        // IndexExpression() [ <RANGE> [ IndexExpression() ] ]
        if (node.jjtGetNumChildren() == 1)
        {
            ASTIndexExpression indexNode = (ASTIndexExpression) node.jjtGetChild(0);
            InternalVariable index = getIndexVariable(indexNode);
            InternalVariable result = new InternalVariable(_context, previous.reference, /*@TODO*/Types.MIXED);
            result.declare(writer);
            writer.print(" = IndexSupport.index(");
            writer.print(previous.name);
            writer.print(",");
            index.value(writer);
            writer.print(",");
            writer.print((isReverseIndex(indexNode)));
            writer.println(");");
            return result;
        }
        else
        {
            ASTIndexExpression indexStartNode = (ASTIndexExpression) node.jjtGetChild(0);
            ASTIndexExpression indexEndNode = (ASTIndexExpression) node.jjtGetChild(1);
            InternalVariable indexStart = getIndexVariable(indexStartNode);
            InternalVariable indexEnd = getIndexVariable(indexEndNode);
            InternalVariable result = new InternalVariable(_context, previous.reference, /*@TODO*/Types.MIXED);
            result.declare(writer);
            writer.print(" = IndexSupport.index(");
            writer.print(previous.name);
            writer.print(",");
            indexStart.value(writer);
            writer.print(",");
            writer.print((isReverseIndex(indexStartNode)));
            writer.print(",");
            indexEnd.value(writer);
            writer.print(",");
            writer.print((isReverseIndex(indexEndNode)));
            writer.println(");");
            return result;
        }
    }

    private InternalVariable getIndexVariable(ASTIndexExpression indexNode)
    {
        return evaluate(indexNode.jjtGetChild(indexNode.jjtGetNumChildren() - 1));
    }

    private boolean isReverseIndex(ASTIndexExpression indexNode)
    {
        return indexNode.jjtGetNumChildren() == 2;
    }

    public InternalVariable visit(ASTCallOther node, Object data)
    {
        assert (data instanceof InternalVariable);
        InternalVariable previous = (InternalVariable) data;

        ASTIdentifier identifier = (ASTIdentifier) node.jjtGetChild(0);

        if (node.jjtGetNumChildren() == 1)
        {
            return printFieldAccess(previous, identifier);
        }

        PrintWriter writer = _context.writer();

        ASTFunctionArguments args = (ASTFunctionArguments) node.jjtGetChild(1);

        InternalVariable fname = new InternalVariable(_context, false, Types.STRING);
        fname.declare(writer);
        writer.print(" = makeValue(\"");
        writer.print(ASTUtil.getImage(identifier));
        writer.println("\");");

        FunctionCallWriter fcw = new FunctionCallWriter(_context);
        FunctionReference function = fcw.findFunction(node, "efun", "call_other");
        FunctionArgument[] argVars = fcw.processArguments(function, new InternalVariable[] { previous, fname }, args);

        return fcw.writeFunction(function, argVars);
    }

    private InternalVariable printFieldAccess(InternalVariable previous, ASTIdentifier identifier)
    {
        String fieldName = ASTUtil.getImage(identifier);

        LpcType type = Types.MIXED;

        ClassDefinition classDefinition = previous.type.getClassDefinition();
        if (classDefinition != null)
        {
            FieldDefinition fieldDefinition = classDefinition.getFields().get(fieldName);
            if (fieldDefinition == null)
            {
                throw new CompileException(identifier, "Class "
                        + classDefinition.getDeclaringType().getName()
                        + "::"
                        + classDefinition.getName()
                        + " does not have a field "
                        + fieldName);
            }
            else
            {
                type = fieldDefinition.getType();
            }
        }
        else if (!Types.MIXED.equals(previous.type))
        {
            throw new CompileException(identifier, "Left hand side of field access is the wrong type (expected class, got "
                    + previous.type
                    + ")");
        }

        String varName = _context.variables().allocateInternalVariableName();
        InternalVariable var = new InternalVariable(varName, true, type);

        PrintWriter writer = _context.writer();

        var.declare(writer);
        writer.print(" = ClassSupport.getField( ");
        previous.value(writer);
        writer.print(" , ");
        writer.print(fieldName);
        writer.println(";");
        return var;
    }

    public Object visit(ASTConstant node, Object data)
    {
        Object constant = new ConstantHandler(_context).getConstant(node);
        if (constant instanceof Number)
        {
            return printNumber((Number) constant);
        }
        if (constant instanceof Number)
        {
            return printNumber((Number) constant);
        }
        else if (constant instanceof Character)
        {
            Character ch = (Character) constant;
            return printNumber(new Integer(ch.charValue()));
        }
        else if (constant instanceof CharSequence)
        {
            CharSequence str = (CharSequence) constant;
            return printString(str);
        }
        else
        {
            throw new CompileException(node, "Internal error - unexpected constant type "
                    + constant.getClass().getSimpleName()
                    + " ["
                    + constant
                    + "]");
        }
    }

    private Object printString(CharSequence str)
    {
        StringBuilder builder = new StringBuilder();
        builder.append('\"');
        for (int i = 0; i < str.length(); i++)
        {
            char ch = str.charAt(i);
            if (Character.isJavaIdentifierPart(ch) || ch == ' ')
            {
                builder.append(ch);
                continue;
            }
            // These special cases a are annoying, but the compilers treat '\u0022' as '"' rather than '\"'
            switch (ch)
            {
                case '"':
                    builder.append("\\\"");
                    break;
                case '\n':
                    builder.append("\\n");
                    break;
                case '\r':
                    builder.append("\\n");
                    break;
                case '\\':
                    builder.append("\\\\");
                    break;
                default:
                    builder.append("\\u");
                    String digits = "000" + Integer.toHexString(ch);
                    if (digits.length() > 4)
                    {
                        digits = digits.substring(digits.length() - 4);
                    }
                    builder.append(digits);
            }
        }
        builder.append('\"');

        return printValue(builder, Types.STRING);
    }

    private Object printNumber(Number number)
    {
        if (isZero(number))
        {
            return printValue(number, Types.MIXED);
        }
        return printValue(number, Types.INT);
    }

    private boolean isZero(Number number)
    {
        return number instanceof Long && ((Long) number).longValue() == 0;
    }

    private Object printValue(Object value, LpcType type)
    {
        PrintWriter writer = _context.writer();
        InternalVariable var = new InternalVariable(_context, false, type);
        var.declare(writer);
        writer.print(" = makeValue(");
        writer.print(value);
        writer.println(");");
        return var;
    }

    public Object visit(ASTFunctionLiteral node, Object data)
    {
        evaluateImmediates(node);
        if (node.jjtGetNumChildren() == 1)
        {
            return visitExpressionFunction(node);
        }
        else
        {
            return visitBlockFunction(node);
        }
    }

    private InternalVariable visitExpressionFunction(ASTFunctionLiteral node)
    {
        int argCount = 0;
        Collection<ASTIdentifier> identifiers = ASTUtil.findDescendants(ASTIdentifier.class, node);
        for (ASTIdentifier identifier : identifiers)
        {
            int index = getPositionalIndex(identifier);
            if (index > argCount)
            {
                argCount++;
            }
        }

        PrintWriter writer = _context.writer();
        InternalVariable var = new InternalVariable(_context, false, Types.FUNCTION);
        var.declare(writer);
        writer.print(" = new LpcFunction(");
        writer.print(argCount);
        writer.println(") {\npublic LpcValue execute(List<? extends LpcValue> args) {");

        Node expr = node.jjtGetChild(0);
        InternalVariable eVar;
        if (expr instanceof ASTCompoundExpression)
        {
            eVar = visitOldStyleFunctionLiteral((ASTCompoundExpression) expr);
        }
        else
        {
            eVar = evaluate(expr);
        }
        writer.print("return ");
        eVar.value(writer);
        writer.println(";");
        writer.println("} /* " + var.name + ".execute() */");
        writer.println("}; /* " + var.name + " */");
        return var;
    }

    private void evaluateImmediates(ASTFunctionLiteral node)
    {
        Collection<ASTImmediateExpression> immediates = ASTUtil.findDescendants(ASTImmediateExpression.class, node);
        for (ASTImmediateExpression expression : immediates)
        {
            evaluate(expression, true);
        }
    }

    private InternalVariable visitOldStyleFunctionLiteral(ASTCompoundExpression list)
    {
        SimpleNode functionNode = (SimpleNode) list.jjtGetChild(0);
        assert (functionNode instanceof ASTIdentifier);
        String functionName = ASTUtil.getImage(functionNode);
        FunctionCallWriter fcw = new FunctionCallWriter(_context);
        FunctionReference func = fcw.findFunction(functionNode, null, functionName);
        FunctionArgument[] vars = new FunctionArgument[list.jjtGetNumChildren() - 1];
        for (int i = 0; i < vars.length; i++)
        {
            InternalVariable var = evaluate(list.jjtGetChild(i + 1));
            vars[i] = new FunctionArgument(var, false);
        }
        return fcw.writeFunction(func, vars);
    }

    private int getPositionalIndex(ASTIdentifier identifier)
    {
        String image = ASTUtil.getImage(identifier);
        if (image.charAt(0) == '$')
        {
            return Integer.parseInt(image.substring(1));
        }
        else
        {
            return 0;
        }
    }

    private InternalVariable visitBlockFunction(ASTFunctionLiteral node)
    {
        ASTParameterDeclarations signatureNode = (ASTParameterDeclarations) node.jjtGetChild(0);
        ASTStatementBlock blockNode = (ASTStatementBlock) node.jjtGetChild(1);

        MethodWriter methodWriter = new MethodWriter(_context);
        List< ? extends ArgumentDefinition> argumentDefinitions = methodWriter.getArgumentDefinitions(signatureNode);

        PrintWriter writer = _context.writer();
        InternalVariable var = new InternalVariable(_context, false, Types.FUNCTION);
        var.declare(writer);
        writer.print(" = new LpcFunction(");

        boolean first = true;
        for (ArgumentDefinition arg : argumentDefinitions)
        {
            if (first)
            {
                first = false;
            }
            else
            {
                writer.print(",");
            }

            writer.print("new ");
            writer.print(ArgumentSpec.class.getName());
            writer.print("( \"");
            writer.print(arg.getName());
            writer.print("\",");
            new TypeWriter(_context).printType(arg.getType());
            writer.print(",");
            writer.print(arg.isRef());
            writer.print(",");
            writer.print(arg.isVarArgs());
            writer.print(")");
        }

        writer.println(") {\npublic LpcValue execute(List<? extends LpcValue> args) {");

        VariableWriter variableWriter = new VariableWriter(_context, null);
        int index = 0;
        for (ArgumentDefinition arg : argumentDefinitions)
        {
            variableWriter.declareLocalVariable(signatureNode, arg.getName(), arg.getType(), getPositionalVariable(index));
            index++;
        }

        new StatementWriter(_context).writeBlock(blockNode);

        writer.println("} /* " + var.name + ".execute() */");
        writer.println("}; /* " + var.name + " */");

        return var;
    }

    public InternalVariable evaluate(Node expr)
    {
        return evaluate(expr, false);
    }

    private InternalVariable evaluate(Node expr, boolean cacheResult)
    {
        if (_evaluationCache.containsKey(expr))
        {
            return _evaluationCache.get(expr);
        }

        InternalVariable eVar = (InternalVariable) expr.jjtAccept(this, null);
        if (eVar == null)
        {
            throw new NullPointerException("No variable returned by visiting " + ASTUtil.describe((SimpleNode) expr));
        }
        if (cacheResult)
        {
            _evaluationCache.put(expr, eVar);
        }
        return eVar;
    }

    public Object visit(ASTTernaryExpression node, Object data)
    {
        PrintWriter writer = _context.writer();

        Node cond = node.jjtGetChild(0);
        Node ifTrue = node.jjtGetChild(1);
        Node ifFalse = node.jjtGetChild(2);

        InternalVariable condVar = evaluate(cond);

        InternalVariable var = new InternalVariable(_context, false, Types.MIXED);
        var.declare(writer);
        writer.println(";");

        writer.print("if( ");
        condVar.value(writer);
        writer.println(".asBoolean() ) {");
        {
            InternalVariable trueVar = evaluate(ifTrue);
            writer.print(var.name);
            writer.print(" = ");
            trueVar.value(writer);
            writer.println(";");
        }
        writer.println("} else {");
        {
            InternalVariable falseVar = evaluate(ifFalse);
            writer.print(var.name);
            writer.print(" = ");
            falseVar.value(writer);
            writer.println(";");
        }
        writer.println("}");

        return var;
    }

    public Object visit(ASTComparisonExpression node, Object data)
    {
        PrintWriter writer = _context.writer();

        Node leftNode = node.jjtGetChild(0);
        SimpleNode opNode = (SimpleNode) node.jjtGetChild(1);
        Node rightNode = node.jjtGetChild(2);

        InternalVariable leftVar = evaluate(leftNode);
        InternalVariable rightVar = evaluate(rightNode);

        InternalVariable var = new InternalVariable(_context, false, Types.INT);
        var.declare(writer);
        writer.print(" = ComparisonSupport.");
        switch (opNode.jjtGetFirstToken().kind)
        {
            case ParserConstants.LESS_THAN:
                writer.print("lessThan");
                break;

            case ParserConstants.LESS_OR_EQUAL:
                writer.print("lessThanOrEqual");
                break;

            case ParserConstants.GREATER_THAN:
                writer.print("greaterThan");
                break;

            case ParserConstants.GREATER_OR_EQUAL:
                writer.print("greaterThanOrEqual");
                break;

            case ParserConstants.EQUAL:
                writer.print("equal");
                break;

            case ParserConstants.NOT_EQUAL:
                writer.print("notEqual");
                break;

            default:
                throw new CompileException(node, "Internal Error - invalid comparison operator " + ASTUtil.getImage(opNode));
        }

        writer.print("(");
        leftVar.value(writer);
        writer.print(",");
        rightVar.value(writer);
        writer.println(");");

        return var;
    }

    public InternalVariable visit(ASTUnaryExpression node, Object data)
    {
        if (node.jjtGetChild(0) instanceof ASTPrefixIncrementOperator)
        {
            return visitPrefixIncrement(node);
        }
        else
        {
            return visitUnaryOperation(node);
        }
    }

    private InternalVariable visitPrefixIncrement(ASTUnaryExpression node)
    {
        SimpleNode opNode = (SimpleNode) node.jjtGetChild(0);
        Node exprNode = node.jjtGetChild(1);

        InternalVariable exprVar = evaluate(exprNode);
        if (!exprVar.reference)
        {
            throw new CompileException(node, "Cannot increment/decrement value " + exprNode);
        }

        PrintWriter writer = _context.writer();

        writer.print(exprVar.name);
        writer.print(".set(MathSupport.add(");
        switch (opNode.jjtGetFirstToken().kind)
        {
            case ParserConstants.INCREMENT:
                writer.print("1");
                break;
            case ParserConstants.DECREMENT:
                writer.print("-1");
                break;

            default:
                throw new CompileException(node, "Internal Error - invalid prefix operator " + ASTUtil.getImage(opNode));
        }
        writer.print(",");
        exprVar.value(writer);
        writer.println("));");

        return new InternalVariable(exprVar.name, false, exprVar.type);
    }

    private InternalVariable visitUnaryOperation(ASTUnaryExpression node)
    {
        SimpleNode opNode = (SimpleNode) node.jjtGetChild(0);
        SimpleNode exprNode = (SimpleNode) node.jjtGetChild(1);

        PrintWriter writer = _context.writer();

        InternalVariable exprVar = evaluate(exprNode);
        int kind = opNode.jjtGetFirstToken().kind;
        LpcType type = null;
        switch (kind)
        {
            case ParserConstants.BINARY_NOT:
                checkType(exprNode, exprVar, Types.INT);
            case ParserConstants.NOT:
                type = Types.INT;
                break;
            case ParserConstants.PLUS:
            case ParserConstants.MINUS:
                checkType(exprNode, exprVar, Types.INT, Types.FLOAT);
                type = exprVar.type;
                break;
        }

        String helper = null;
        switch (kind)
        {
            case ParserConstants.NOT:
                helper = "LogicSupport.not";
                break;
            case ParserConstants.BINARY_NOT:
                helper = "BinarySupport.not";
                break;
            case ParserConstants.PLUS:
                helper = "MathSupport.noop";
                break;
            case ParserConstants.MINUS:
                helper = "MathSupport.negate";
                break;
        }

        InternalVariable var = new InternalVariable(_context, false, type);
        var.declare(writer);
        writer.print(" = ");
        writer.print(helper);
        writer.print("(");
        exprVar.value(writer);
        writer.println(");");
        return var;
    }

    public static boolean checkType(Node node, InternalVariable var, LpcType... allowedTypes)
    {
        if (Types.MIXED.equals(var))
        {
            return true;
        }

        LpcType type = var.type;
        for (LpcType allowed : allowedTypes)
        {
            if (Types.MIXED.equals(allowed))
            {
                return true;
            }
            if (allowed.equals(type))
            {
                return true;
            }
            if (type.getKind() == LpcType.Kind.MIXED && type.getArrayDepth() <= allowed.getArrayDepth())
            {
                return true;
            }
            if (allowed.getKind() == LpcType.Kind.MIXED && type.getArrayDepth() <= allowed.getArrayDepth())
            {
                return true;
            }
        }
        StringBuilder expected = new StringBuilder();
        for (LpcType lpcType : allowedTypes)
        {
            expected.append(lpcType);
            expected.append("|");
        }

        expected.deleteCharAt(expected.length() - 1);
        throw new CompileException((SimpleNode) node, "Type mismatch - expected " + expected + " but was " + type);
    }

    public InternalVariable visit(ASTAssignmentExpression node, Object data)
    {
        SimpleNode leftNode = (SimpleNode) node.jjtGetChild(0);
        SimpleNode opNode = (SimpleNode) node.jjtGetChild(1);
        Node rightNode = node.jjtGetChild(2);

        InternalVariable leftVar = evaluate(leftNode);
        if (!leftVar.reference)
        {
            throw new CompileException(node, "Cannot assign to value \"" + ASTUtil.getCompleteImage(leftNode) + "\"");
        }

        InternalVariable rightVar = evaluate(rightNode);
        checkType(node, rightVar, leftVar.type);

        PrintWriter writer = _context.writer();

        writer.print(leftVar.name);
        writer.print(".set(");

        switch (opNode.jjtGetFirstToken().kind)
        {
            case ParserConstants.ASSIGN:
                rightVar.value(writer);
                break;
            case ParserConstants.PLUS_ASSIGN:
                mathFunction("add", leftVar, rightVar);
                break;
            case ParserConstants.MINUS_ASSIGN:
                mathFunction("subtract", leftVar, rightVar);
                break;
            case ParserConstants.MULTIPLY_ASSIGN:
                mathFunction("multiply", leftVar, rightVar);
                break;
            case ParserConstants.DIVIDE_ASSIGN:
                mathFunction("divide", leftVar, rightVar);
                break;
            case ParserConstants.MODULUS_ASSIGN:
                mathFunction("modulus", leftVar, rightVar);
                break;
            case ParserConstants.XOR_ASSIGN:
                binaryFunction("xor", leftVar, rightVar);
                break;
            case ParserConstants.AND_ASSIGN:
                binaryFunction("and", leftVar, rightVar);
                break;
            case ParserConstants.OR_ASSIGN:
                binaryFunction("or", leftVar, rightVar);
                break;
            case ParserConstants.LEFT_SHIFT_ASSIGN:
                binaryFunction("leftShift", leftVar, rightVar);
                break;
            case ParserConstants.RIGHT_SHIFT_ASSIGN:
                binaryFunction("rightShift", leftVar, rightVar);
                break;
        }

        writer.println(");");
        // @TODO Add "Assignable" flag (false in this case)
        return leftVar;
    }

    private void mathFunction(String functionName, InternalVariable leftVar, InternalVariable rightVar)
    {
        printFunction("MathSupport." + functionName, leftVar, rightVar);
    }

    private void binaryFunction(String functionName, InternalVariable leftVar, InternalVariable rightVar)
    {
        printFunction("BinarySupport." + functionName, leftVar, rightVar);
    }

    private void printFunction(String function, InternalVariable leftVar, InternalVariable rightVar)
    {
        PrintWriter writer = _context.writer();
        writer.print(function);
        writer.print("(");
        leftVar.value(writer);
        writer.print(",");
        rightVar.value(writer);
        writer.print(")");
    }

    public Object visit(ASTArrayLiteral node, Object data)
    {
        InternalVariable[] elements = new InternalVariable[node.jjtGetNumChildren()];
        int i = 0;
        LpcType elementType = null;
        for (SimpleNode child : ASTUtil.children(node))
        {
            elements[i] = evaluate(child);
            if (elementType == null)
            {
                elementType = elements[i].type;
            }
            else if (!elementType.equals(elements[i].type))
            {
                elementType = Types.MIXED;
            }
            i++;
        }

        if (elementType == null)
        {
            elementType = Types.MIXED;
        }

        InternalVariable array = new InternalVariable(_context, false, Types.arrayOf(elementType));
        PrintWriter writer = _context.writer();
        array.declare(writer);
        writer.print(" = makeArray( ");
        boolean first = true;
        for (InternalVariable element : elements)
        {
            if (first)
            {
                first = false;
            }
            else
            {
                writer.print(" , ");
            }
            element.value(writer);
        }
        writer.println(" );");

        return array;
    }

    public Object visit(ASTArrayElement node, Object data)
    {
        if (node.jjtGetNumChildren() == 2)
        {
            // @TODO expando...
        }
        return evaluate(node.jjtGetChild(0));
    }

    @SuppressWarnings("unchecked")
    public Object visit(ASTMappingLiteral node, Object data)
    {
        Pair<InternalVariable, InternalVariable>[] elements = new Pair[node.jjtGetNumChildren()];
        int i = 0;
        for (SimpleNode child : ASTUtil.children(node))
        {
            assert (child instanceof ASTMappingElement);
            assert (child.jjtGetNumChildren() == 2);

            Node keyNode = child.jjtGetChild(0);
            Node valueNode = child.jjtGetChild(1);

            InternalVariable keyVar = evaluate(keyNode);
            InternalVariable valueVar = evaluate(valueNode);

            elements[i] = new Pair(keyVar, valueVar);
            i++;
        }

        PrintWriter writer = _context.writer();

        InternalVariable mapping = new InternalVariable(_context, false, Types.MAPPING);
        mapping.declare(writer);
        writer.print(" = makeMapping( ");
        boolean first = true;
        for (Pair<InternalVariable, InternalVariable> element : elements)
        {
            if (first)
            {
                first = false;
            }
            else
            {
                writer.print(" , ");
            }
            element.getFirst().value(writer);
            writer.print(" , ");
            element.getSecond().value(writer);
        }
        writer.println(" );");

        return mapping;
    }

    public Object visit(ASTArithmeticExpression node, Object data)
    {
        assert (node.jjtGetNumChildren() >= 3);
        Node leftNode = node.jjtGetChild(0);
        InternalVariable leftVar = evaluate(leftNode);

        for (int i = 1; i < node.jjtGetNumChildren(); i += 2)
        {
            SimpleNode opNode = (SimpleNode) node.jjtGetChild(i);
            Node rightNode = node.jjtGetChild(i + 1);

            InternalVariable rightVar = evaluate(rightNode);

            PrintWriter writer = _context.writer();

            // @TODO (type)
            InternalVariable result = new InternalVariable(_context, false, Types.MIXED);

            result.declare(writer);
            writer.print(" = ");
            switch (opNode.jjtGetFirstToken().kind)
            {
                case ParserConstants.LEFT_SHIFT:
                    binaryFunction("leftShift", leftVar, rightVar);
                    break;
                case ParserConstants.RIGHT_SHIFT:
                    binaryFunction("rightShift", leftVar, rightVar);
                    break;

                case ParserConstants.PLUS:
                    mathFunction("add", leftVar, rightVar);
                    break;
                case ParserConstants.MINUS:
                    mathFunction("subtract", leftVar, rightVar);
                    break;

                case ParserConstants.STAR:
                    mathFunction("multiply", leftVar, rightVar);
                    break;
                case ParserConstants.SLASH:
                    mathFunction("divide", leftVar, rightVar);
                    break;
                case ParserConstants.MODULUS:
                    mathFunction("modulus", leftVar, rightVar);
                    break;
            }
            writer.println(";");
            leftVar = result;
        }
        return leftVar;
    }

    public Object visit(ASTLogicalAndExpression node, Object data)
    {
        PrintWriter writer = _context.writer();
        InternalVariable result = new InternalVariable(_context, false, Types.INT);
        result.declare(writer, false);
        writer.print(" = makeValue(0);");

        writer.println("do {");
        for (int i = 0; i < node.jjtGetNumChildren(); i++)
        {
            InternalVariable var = evaluate(node.jjtGetChild(i));
            writer.print("if( !");
            var.value(writer);
            writer.println(".asBoolean() ) break;");
        }
        writer.print(result.name);
        writer.println(" = makeValue(1);");
        writer.println("} while(false);");

        return result;
    }

    public Object visit(ASTLogicalOrExpression node, Object data)
    {
        PrintWriter writer = _context.writer();
        InternalVariable result = new InternalVariable(_context, false, Types.MIXED);
        result.declare(writer, false);
        writer.print(" = makeValue(1);");

        int branchCount = node.jjtGetNumChildren();
        LpcType[] types = new LpcType[branchCount];
        writer.println("do {");
        for (int i = 0; i < branchCount; i++)
        {
            InternalVariable var = evaluate(node.jjtGetChild(i));
            types[i] = var.type;
            writer.print("if(");
            var.value(writer);
            writer.println(".asBoolean() ) break;");
        }
        writer.print(result.name);
        writer.println(" = makeValue(0);");
        writer.println("} while(false);");

        return new InternalVariable(result.name, false, commonType(types));
    }

    private LpcType commonType(LpcType[] types)
    {
        if (types.length == 0)
        {
            return Types.MIXED;
        }
        if (types.length == 1)
        {
            return types[0];
        }

        LpcType.Kind kind = types[0].getKind();
        int depth = types[0].getArrayDepth();
        ClassDefinition cls = types[0].getClassDefinition();
        ExtensionType ext = types[0].getExtensionType();

        for (int i = 1; i < types.length; i++)
        {
            LpcType type = types[i];
            if (type.getArrayDepth() < depth)
            {
                depth = type.getArrayDepth();
                kind = LpcType.Kind.MIXED;
                continue;
            }
            if (type.getKind() != kind)
            {
                kind = LpcType.Kind.MIXED;
            }
            else if (kind == LpcType.Kind.CLASS && type.getClassDefinition() != cls)
            {
                kind = LpcType.Kind.MIXED;
            }
            else if (kind == LpcType.Kind.EXTENSION && type.getExtensionType() != ext)
            {
                kind = LpcType.Kind.MIXED;
            }
        }

        switch (kind)
        {
            case CLASS:
                return Types.classType(cls, depth);
            case EXTENSION:
                return Types.extensionType(ext, depth);
            default:
                return Types.getType(kind, null, depth);
        }
    }

    public Object visit(ASTCastExpression node, Object data)
    {
        ASTFullType typeNode = (ASTFullType) node.jjtGetChild(0);
        Node exprNode = node.jjtGetChild(1);

        InternalVariable exprVar = evaluate(exprNode);

        LpcType type = new TypeWriter(_context).getType(typeNode);
        InternalVariable result = new InternalVariable(_context, false, type);

        PrintWriter writer = _context.writer();
        result.declare(writer);
        writer.print(" = ");

        if (Types.INT.equals(type))
        {
            writer.print("makeValue( ");
            exprVar.value(writer);
            writer.print(".asLong())");
        }
        else if (Types.FLOAT.equals(type))
        {
            writer.print("makeValue( ");
            exprVar.value(writer);
            writer.print(".asDouble())");
        }
        else
        {
            // @TODO check the type...
            exprVar.value(writer);
        }
        writer.println(";");
        return result;
    }

    public InternalVariable visit(ASTImmediateExpression node, Object data)
    {
        // @TODO make this immediate
        return evaluate(node.jjtGetChild(0));
    }

    public Object visit(ASTCompoundExpression node, Object data)
    {
        InternalVariable var = null;
        for (SimpleNode child : ASTUtil.children(node))
        {
            var = evaluate(child);
        }
        return var;
    }

    public Object visit(ASTCatch node, Object data)
    {
        assert (node.jjtGetNumChildren() == 1);
        Node child = node.jjtGetChild(0);

        PrintWriter writer = _context.writer();

        InternalVariable var = new InternalVariable(_context, false, Types.STRING);
        var.declare(writer, false);
        writer.println(" = nil();");

        writer.println("try {");

        if (child instanceof ASTStatementBlock)
        {
            new StatementWriter(_context).writeBlock((ASTStatementBlock) child);
        }
        else
        {
            evaluate(child);
        }

        String exName = _context.variables().allocateInternalVariableName();

        writer.println("} catch( " + LpcRuntimeException.class.getName() + " " + exName + " ) { ");
        writer.println(var.name + " = makeValue(\"*\" + " + exName + ".getMessage()); ");
        writer.println("}");

        return var;
    }

    public Object visit(ASTBinaryOrExpression node, Object data)
    {
        InternalVariable[] vars = new InternalVariable[node.jjtGetNumChildren()];

        for (int i = 0; i < vars.length; i++)
        {
            Node child = node.jjtGetChild(i);
            vars[i] = evaluate(child);
            checkType(child, vars[i], Types.INT);
        }

        PrintWriter writer = _context.writer();

        InternalVariable result = new InternalVariable(_context, false, Types.INT);
        result.declare(writer);
        writer.print(" = BinarySupport.binaryOr( ");

        boolean first = true;
        for (InternalVariable internalVariable : vars)
        {
            if (first)
            {
                first = false;
            }
            else
            {
                writer.print(",");
            }
            internalVariable.value(writer);
        }
        writer.println(");");

        return result;
    }
}
