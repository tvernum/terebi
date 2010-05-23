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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.adjective.stout.builder.ClassSpec;
import org.adjective.stout.builder.ElementBuilder;
import org.adjective.stout.builder.MethodSpec;
import org.adjective.stout.builder.ParameterSpec;
import org.adjective.stout.core.ElementModifier;
import org.adjective.stout.core.ExtendedType;
import org.adjective.stout.core.MethodSignature;
import org.adjective.stout.core.Parameter;
import org.adjective.stout.impl.MethodSignatureImpl;
import org.adjective.stout.loop.Condition;
import org.adjective.stout.operation.CreateArrayExpression;
import org.adjective.stout.operation.EmptyExpression;
import org.adjective.stout.operation.Expression;
import org.adjective.stout.operation.LineNumberExpression;
import org.adjective.stout.operation.Statement;
import org.adjective.stout.operation.TryCatchSpec;
import org.adjective.stout.operation.VM;

import us.terebi.lang.lpc.compiler.CompileException;
import us.terebi.lang.lpc.compiler.bytecode.FunctionCallCompiler.FunctionArgument;
import us.terebi.lang.lpc.compiler.bytecode.context.CompileContext;
import us.terebi.lang.lpc.compiler.bytecode.stout.LogicalAndExpression;
import us.terebi.lang.lpc.compiler.bytecode.stout.LogicalOrExpression;
import us.terebi.lang.lpc.compiler.java.context.ScopeLookup;
import us.terebi.lang.lpc.compiler.java.context.FunctionLookup.FunctionReference;
import us.terebi.lang.lpc.compiler.java.context.VariableResolver.VariableResolution;
import us.terebi.lang.lpc.compiler.java.context.VariableResolver.VariableResolution.Kind;
import us.terebi.lang.lpc.compiler.util.ConstantHandler;
import us.terebi.lang.lpc.compiler.util.FunctionCallSupport;
import us.terebi.lang.lpc.compiler.util.MathLength;
import us.terebi.lang.lpc.compiler.util.Positional;
import us.terebi.lang.lpc.compiler.util.TypeSupport;
import us.terebi.lang.lpc.parser.ast.ASTArithmeticExpression;
import us.terebi.lang.lpc.parser.ast.ASTArrayElement;
import us.terebi.lang.lpc.parser.ast.ASTArrayLiteral;
import us.terebi.lang.lpc.parser.ast.ASTAssignmentExpression;
import us.terebi.lang.lpc.parser.ast.ASTBinaryAndExpression;
import us.terebi.lang.lpc.parser.ast.ASTBinaryOrExpression;
import us.terebi.lang.lpc.parser.ast.ASTCallOther;
import us.terebi.lang.lpc.parser.ast.ASTCastExpression;
import us.terebi.lang.lpc.parser.ast.ASTCatch;
import us.terebi.lang.lpc.parser.ast.ASTComparisonExpression;
import us.terebi.lang.lpc.parser.ast.ASTCompoundExpression;
import us.terebi.lang.lpc.parser.ast.ASTConstant;
import us.terebi.lang.lpc.parser.ast.ASTElementExpander;
import us.terebi.lang.lpc.parser.ast.ASTExclusiveOrExpression;
import us.terebi.lang.lpc.parser.ast.ASTExpressionCall;
import us.terebi.lang.lpc.parser.ast.ASTFullType;
import us.terebi.lang.lpc.parser.ast.ASTFunctionArguments;
import us.terebi.lang.lpc.parser.ast.ASTFunctionCall;
import us.terebi.lang.lpc.parser.ast.ASTFunctionLiteral;
import us.terebi.lang.lpc.parser.ast.ASTIdentifier;
import us.terebi.lang.lpc.parser.ast.ASTIndexExpression;
import us.terebi.lang.lpc.parser.ast.ASTIndexPostfix;
import us.terebi.lang.lpc.parser.ast.ASTLogicalAndExpression;
import us.terebi.lang.lpc.parser.ast.ASTLogicalOrExpression;
import us.terebi.lang.lpc.parser.ast.ASTMappingElement;
import us.terebi.lang.lpc.parser.ast.ASTMappingLiteral;
import us.terebi.lang.lpc.parser.ast.ASTOptExpression;
import us.terebi.lang.lpc.parser.ast.ASTPostfixExpression;
import us.terebi.lang.lpc.parser.ast.ASTPostfixIncrementOperator;
import us.terebi.lang.lpc.parser.ast.ASTPrefixIncrementOperator;
import us.terebi.lang.lpc.parser.ast.ASTTernaryExpression;
import us.terebi.lang.lpc.parser.ast.ASTUnaryExpression;
import us.terebi.lang.lpc.parser.ast.ASTVariableAssignment;
import us.terebi.lang.lpc.parser.ast.ASTVariableReference;
import us.terebi.lang.lpc.parser.ast.ExpressionNode;
import us.terebi.lang.lpc.parser.ast.Node;
import us.terebi.lang.lpc.parser.ast.OperatorNode;
import us.terebi.lang.lpc.parser.ast.SimpleNode;
import us.terebi.lang.lpc.parser.ast.StatementNode;
import us.terebi.lang.lpc.parser.ast.TokenNode;
import us.terebi.lang.lpc.parser.jj.ParserConstants;
import us.terebi.lang.lpc.parser.jj.Token;
import us.terebi.lang.lpc.parser.util.ASTUtil;
import us.terebi.lang.lpc.parser.util.BaseASTVisitor;
import us.terebi.lang.lpc.runtime.ClassDefinition;
import us.terebi.lang.lpc.runtime.FieldDefinition;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.LpcConstants;
import us.terebi.lang.lpc.runtime.jvm.LpcFunction;
import us.terebi.lang.lpc.runtime.jvm.LpcObject;
import us.terebi.lang.lpc.runtime.jvm.LpcReference;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;
import us.terebi.lang.lpc.runtime.jvm.support.BinarySupport;
import us.terebi.lang.lpc.runtime.jvm.support.CallableSupport;
import us.terebi.lang.lpc.runtime.jvm.support.ClassSupport;
import us.terebi.lang.lpc.runtime.jvm.support.ComparisonSupport;
import us.terebi.lang.lpc.runtime.jvm.support.IndexSupport;
import us.terebi.lang.lpc.runtime.jvm.support.LogicSupport;
import us.terebi.lang.lpc.runtime.jvm.support.MathSupport;
import us.terebi.lang.lpc.runtime.jvm.support.MiscSupport;
import us.terebi.lang.lpc.runtime.jvm.support.ValueSupport;
import us.terebi.lang.lpc.runtime.jvm.type.Types;

import static java.util.Collections.singleton;
import static us.terebi.lang.lpc.compiler.bytecode.ByteCodeConstants.EXCEPTION_GET_LPC_MESSAGE;
import static us.terebi.lang.lpc.compiler.bytecode.ByteCodeConstants.LPC_REFERENCE;
import static us.terebi.lang.lpc.compiler.bytecode.ByteCodeConstants.LPC_RUNTIME_EXCEPTION;
import static us.terebi.lang.lpc.compiler.bytecode.ByteCodeConstants.LPC_VALUE;
import static us.terebi.lang.lpc.compiler.bytecode.ByteCodeConstants.STRING_VALUE_CONSTRUCTOR;
import static us.terebi.lang.lpc.compiler.bytecode.FunctionLiteralCompiler.findReferencedVariables;
import static us.terebi.lang.lpc.compiler.bytecode.FunctionLiteralCompiler.getReferencedVariables;
import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.commonType;

/**
 * 
 */
public class ExpressionCompiler extends BaseASTVisitor
{
    private final ScopeLookup _scope;
    private final CompileContext _context;
    private final Map<ExpressionNode, LpcExpression> _precompile;

    public ExpressionCompiler(ScopeLookup scope, CompileContext context)
    {
        _scope = scope;
        _context = context;
        _precompile = new HashMap<ExpressionNode, LpcExpression>();
    }

    public LpcExpression compile(Node node)
    {
        assert node instanceof ExpressionNode;

        ExpressionNode expr = (ExpressionNode) node;

        _context.options().processPragmas(expr);

        LpcExpression precompile = _precompile.get(expr);
        if (precompile != null)
        {
            return precompile;
        }

        Object result = expr.jjtAccept(this, null);
        if (result == null)
        {
            throw new CompileException(expr, "Internal Error - No expression returned from visiting " + expr);
        }

        LpcExpression lpcExpr = (LpcExpression) result;
        Token token = expr.jjtGetFirstToken();
        if (token == null)
        {
            return lpcExpr;
        }
        int line = _context.lineMapping().getLine(token.beginLine);
        return new LpcExpression(lpcExpr.type, new LineNumberExpression(lpcExpr.expression, line), lpcExpr.reference);
    }

    private LpcExpression expression(Expression expr, LpcType type)
    {
        return new LpcExpression(type, expr);
    }

    private LpcExpression zero()
    {
        Expression zero = VM.Expression.getStaticField(LpcConstants.MIXED.class, "ZERO", LpcValue.class);
        return expression(zero, Types.MIXED);
    }

    public void precompile(ExpressionNode node, LpcExpression expression)
    {
        _precompile.put(node, expression);
    }

    public static Expression getValue(LpcExpression expr)
    {
        if (expr.reference)
        {
            return VM.Expression.callMethod(expr.expression, ByteCodeConstants.LPC_REFERENCE, ByteCodeConstants.REFERENCE_GET);
        }
        else
        {
            return expr.expression;
        }
    }

    public LpcExpression asValue(LpcExpression expr)
    {
        if (expr.reference)
        {
            return expression(VM.Expression.callMethod(expr.expression, ByteCodeConstants.LPC_REFERENCE, ByteCodeConstants.REFERENCE_GET), expr.type);
        }
        else
        {
            return expr;
        }
    }

    public static Expression makeLpcArray(Expression[] elements)
    {
        return VM.Expression.callInherited(ByteCodeConstants.MAKE_ARRAY, VM.Expression.array(LpcValue.class, elements));
    }

    public static Expression toBoolean(LpcExpression expression)
    {
        return toBoolean(getValue(expression));
    }

    public static Expression toBoolean(Expression expression)
    {
        MethodSignature asBoolean = VM.Method.find(LpcValue.class, "asBoolean");
        return VM.Expression.callMethod(expression, LpcValue.class, asBoolean);
    }

    public LpcExpression visit(ASTOptExpression node, Object data)
    {
        if (node.jjtGetNumChildren() == 1)
        {
            return compile(node.jjtGetChild(0));
        }
        else
        {
            return expression(VM.Expression.constant(true), Types.MIXED);
        }
    }

    public LpcExpression visit(ASTConstant node, Object data)
    {
        Object constant = new ConstantHandler().getConstant(node, _context.options().getMath());

        if ((constant instanceof Float) || (constant instanceof Double))
        {
            double d = ((Number) constant).doubleValue();
            return expression(makeValue(Double.TYPE, VM.Expression.constant(d)), Types.FLOAT);
        }
        else if (constant instanceof Integer)
        {
            int i = ((Number) constant).intValue();
            if (i == 0)
            {
                return zero();
            }
            return expression(makeValue(Integer.TYPE, VM.Expression.constant(i)), Types.INT);
        }
        if ((constant instanceof Number))
        {
            long l = ((Number) constant).longValue();
            if (l == 0)
            {
                return zero();
            }
            return expression(makeValue(Long.TYPE, VM.Expression.constant(l)), Types.INT);
        }
        else if (constant instanceof Character)
        {
            long l = ((Character) constant).charValue();
            return expression(makeValue(Long.TYPE, VM.Expression.constant(l)), Types.INT);
        }
        else if (constant instanceof CharSequence)
        {
            CharSequence str = (CharSequence) constant;
            return expression(makeValue(String.class, VM.Expression.constant(str.toString())), Types.STRING);
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

    private Expression makeValue(Class< ? > type, Expression expr)
    {
        return VM.Expression.callInherited( //
                VM.Method.find(LpcObject.class, "makeValue", type), //
                expr //
        );
    }

    private Expression makeLpcString(String javaString)
    {
        return makeLpcString(VM.Expression.constant(javaString));
    }

    private Expression makeLpcString(Expression javaString)
    {
        return VM.Expression.construct(STRING_VALUE_CONSTRUCTOR, javaString);
    }

    public LpcExpression visit(ASTArrayLiteral node, Object data)
    {
        Expression[] elements = new Expression[node.jjtGetNumChildren()];
        boolean needsExpand = ASTUtil.hasDescendant(ASTElementExpander.class, node);

        int i = 0;
        LpcType elementType = null;
        for (TokenNode child : ASTUtil.children(node))
        {
            assert child instanceof ASTArrayElement;
            ASTArrayElement element = (ASTArrayElement) child;
            LpcExpression expr = this.compile(element.jjtGetChild(0));
            elements[i] = getValue(expr);
            if (needsExpand)
            {
                if (element.jjtGetNumChildren() == 2)
                {
                    elements[i] = VM.Expression.callMethod(elements[i], LPC_VALUE, ByteCodeConstants.AS_LIST);
                }
                else
                {
                    elements[i] = VM.Expression.callStatic(ByteCodeConstants.COLLECTIONS, ByteCodeConstants.SINGLETON_LIST, elements[i]);
                }
            }

            if (elementType == null)
            {
                elementType = expr.type;
            }
            else if (!elementType.equals(expr.type))
            {
                elementType = Types.MIXED;
            }
            i++;
        }

        if (elementType == null)
        {
            elementType = Types.MIXED;
        }

        Expression array = new CreateArrayExpression( needsExpand ? ByteCodeConstants.LIST : ByteCodeConstants.LPC_VALUE, elements);

        MethodSignature makeArray = needsExpand //
        ? VM.Method.find(LpcObject.class, "makeArray", List[].class)
                : VM.Method.find(LpcObject.class, "makeArray", LpcValue[].class);

        Expression result = VM.Expression.callInherited(makeArray, array);
        return expression(result, Types.arrayOf(elementType));
    }

    public LpcExpression visit(ASTArrayElement node, Object data)
    {
        throw new UnsupportedOperationException("Cannot visit " + node);
    }

    public Object visit(ASTMappingLiteral node, Object data)
    {
        Expression[] elements = new Expression[2 * node.jjtGetNumChildren()];
        int i = 0;
        for (TokenNode child : ASTUtil.children(node))
        {
            assert (child instanceof ASTMappingElement);
            assert (child.jjtGetNumChildren() == 2);

            Node keyNode = child.jjtGetChild(0);
            Node valueNode = child.jjtGetChild(1);

            elements[i + 0] = getValue(compile(keyNode));
            elements[i + 1] = getValue(compile(valueNode));
            i += 2;
        }

        Expression mapping = VM.Expression.callInherited(ByteCodeConstants.MAKE_MAPPING, VM.Expression.array(LpcValue.class, elements));
        return expression(mapping, Types.MAPPING);
    }

    public LpcExpression visit(ASTFunctionLiteral node, Object data)
    {
        FunctionLiteralCompiler compiler = new FunctionLiteralCompiler(_scope, _context);
        return compiler.compile(node);
    }

    public LpcExpression visit(ASTVariableReference node, Object data)
    {
        String scope = node.getScope();

        if (scope == null)
        {
            if (node.isPositionalVariable())
            {
                Positional positional = new Positional(node.getIdentifier());
                return expression(getPositionalVariable(positional.getIndex()), Types.MIXED);
            }

            String name = node.getVariableName();
            VariableResolution var = _scope.variables().findVariable(name);
            if (var == null)
            {
                throw undeclaredVariable(node, name);
            }
            if (node.isInternal())
            {
                if (var.kind() == Kind.INTERNAL)
                {
                    return new LpcExpression(var.type(), var.access(), false);
                }
                else
                {
                    throw undeclaredVariable(node, name);
                }
            }
            else
            {
                if (var.kind() == Kind.INTERNAL)
                {
                    throw undeclaredVariable(node, name);
                }
                return new LpcExpression(var.type(), var.access(), true);
            }
        }
        else
        {
            // @TODO !!
            throw new CompileException(node, "Internal Error - Scoped variables are not implemented (yet)");
        }
    }

    private CompileException undeclaredVariable(ASTVariableReference node, String name)
    {
        return new CompileException(node, "Reference to undeclared variable '" + name + "'");
    }

    public Expression getPositionalVariable(int index)
    {
        return VM.Expression.callInherited( //
                VM.Method.find(LpcFunction.class, "getArg", LpcValue[].class, Integer.TYPE), //
                VM.Expression.variable(FunctionLiteralCompiler.POSITIONAL_ARGUMENT_COLLECTION), VM.Expression.constant(index)//
        );
    }

    public LpcExpression visit(ASTArithmeticExpression node, Object data)
    {
        assert (node.jjtGetNumChildren() >= 3);
        Node leftNode = node.jjtGetChild(0);
        LpcExpression leftExpr = compile(leftNode);

        for (int i = 1; i < node.jjtGetNumChildren(); i += 2)
        {
            SimpleNode opNode = (SimpleNode) node.jjtGetChild(i);
            Node rightNode = node.jjtGetChild(i + 1);

            LpcExpression rightExpr = compile(rightNode);

            switch (opNode.jjtGetFirstToken().kind)
            {
                case ParserConstants.LEFT_SHIFT:
                    leftExpr = binaryFunction("leftShift", leftExpr, rightExpr);
                    break;
                case ParserConstants.RIGHT_SHIFT:
                    leftExpr = binaryFunction("rightShift", leftExpr, rightExpr);
                    break;

                case ParserConstants.PLUS:
                    leftExpr = mathFunction("add", leftExpr, rightExpr);
                    break;
                case ParserConstants.MINUS:
                    leftExpr = mathFunction("subtract", leftExpr, rightExpr);
                    break;

                case ParserConstants.STAR:
                    leftExpr = mathFunction("multiply", leftExpr, rightExpr);
                    break;
                case ParserConstants.SLASH:
                    leftExpr = mathFunction("divide", leftExpr, rightExpr);
                    break;
                case ParserConstants.MODULUS:
                    leftExpr = mathFunction("modulus", leftExpr, rightExpr);
                    break;
            }
        }
        return leftExpr;
    }

    private LpcExpression mathFunction(String functionName, LpcExpression leftVar, LpcExpression rightVar)
    {
        return callStatic(MathSupport.class, functionName, leftVar, rightVar);
    }

    private LpcExpression binaryFunction(String functionName, LpcExpression leftVar, LpcExpression rightVar)
    {
        return callStatic(BinarySupport.class, functionName, leftVar, rightVar);
    }

    private LpcExpression callStatic(Class< ? > owner, String function, LpcExpression left, LpcExpression right)
    {
        // @TODO (type)
        return expression( // 
                VM.Expression.callStatic(owner, //
                        VM.Method.find(owner, function, LpcValue.class, LpcValue.class), //
                        getValue(left), getValue(right)),//
                Types.MIXED);
    }

    public LpcExpression visit(ASTBinaryOrExpression node, Object data)
    {
        return binaryExpression(node, "binaryOr", false);
    }

    public LpcExpression visit(ASTBinaryAndExpression node, Object data)
    {
        return binaryExpression(node, "binaryAnd", false);
    }

    public LpcExpression visit(ASTExclusiveOrExpression node, Object data)
    {
        return binaryExpression(node, "xor", true);
    }

    private LpcExpression binaryExpression(SimpleNode node, String binaryFunction, boolean passMath)
    {
        LpcExpression[] operands = new LpcExpression[node.jjtGetNumChildren()];
        for (int i = 0; i < operands.length; i++)
        {
            Node child = node.jjtGetChild(i);
            LpcExpression expr = compile(child);
            operands[i] = expr;
        }
        return binaryExpression(node, binaryFunction, passMath, operands);
    }

    private LpcExpression binaryExpression(SimpleNode node, String binaryFunction, boolean passMath, LpcExpression... operands)
    {
        boolean integer = false;
        boolean array = false;
        LpcType[] types = new LpcType[operands.length];
        Expression[] expressions = new Expression[operands.length];
        for (int i = 0; i < operands.length; i++)
        {
            LpcExpression expr = operands[i];
            checkType(node, expr, Types.INT, Types.MIXED_ARRAY);
            types[i] = expr.type;
            if (Types.INT.equals(expr.type))
            {
                integer = true;
            }
            else if (expr.type.isArray())
            {
                array = true;
            }
            expressions[i] = getValue(expr);
        }

        if (array && integer)
        {
            throw new CompileException(node, "Incompatible types to binary operator (int and array)");
        }

        Expression result;
        if (passMath)
        {
            result = VM.Expression.callStatic( //
                    BinarySupport.class, //
                    VM.Method.find(BinarySupport.class, binaryFunction, MathLength.class, LpcValue[].class), //
                    VM.Expression.getEnum(_context.options().getMath()), VM.Expression.array(LpcValue.class, expressions));

        }
        else
        {
            result = VM.Expression.callStatic( //
                    BinarySupport.class, //
                    VM.Method.find(BinarySupport.class, binaryFunction, LpcValue[].class), //
                    VM.Expression.array(LpcValue.class, expressions));
        }

        return expression(result, commonType(types));
    }

    public LpcExpression visit(ASTLogicalOrExpression node, Object data)
    {
        return visitLogicalExpression(node, true);
    }

    public LpcExpression visit(ASTLogicalAndExpression node, Object data)
    {
        return visitLogicalExpression(node, false);
    }

    private LpcExpression visitLogicalExpression(ExpressionNode node, boolean or)
    {
        int branchCount = node.jjtGetNumChildren();
        LpcType[] types = new LpcType[branchCount];
        Expression[] branches = new Expression[branchCount];
        for (int i = 0; i < branchCount; i++)
        {
            Node branch = node.jjtGetChild(i);
            LpcExpression expression = compile(branch);
            types[i] = expression.type;
            branches[i] = getValue(expression);
        }
        Expression logic = (or ? new LogicalOrExpression(branches) : new LogicalAndExpression(branches));
        return expression(logic, MiscSupport.commonType(types));
    }

    public LpcExpression visit(ASTUnaryExpression node, Object data)
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

    private LpcExpression visitPrefixIncrement(ASTUnaryExpression node)
    {
        OperatorNode opNode = (OperatorNode) node.jjtGetChild(0);
        ExpressionNode exprNode = (ExpressionNode) node.jjtGetChild(1);

        return increment(opNode, compile(exprNode), false);
    }

    @SuppressWarnings("unchecked")
    private LpcExpression increment(OperatorNode opNode, LpcExpression expr, boolean postFix)
    {
        if (!expr.reference)
        {
            throw new CompileException(opNode, "Cannot increment/decrement value " + expr);
        }

        Expression value;
        if (postFix)
        {
            value = getValue(expr);
            ElementBuilder<Statement> increment = VM.Statement.ignore(getIncrementExpression(opNode, expr));
            value = VM.Expression.chain(value, increment);
        }
        else
        {
            value = getIncrementExpression(opNode, expr);
        }
        return expression(value, expr.type);
    }

    private Expression getIncrementExpression(OperatorNode opNode, LpcExpression expr)
    {
        Expression amount;
        switch (opNode.jjtGetFirstToken().kind)
        {
            case ParserConstants.INCREMENT:
                amount = VM.Expression.constant((long) 1);
                break;
            case ParserConstants.DECREMENT:
                amount = VM.Expression.constant((long) -1);
                break;
            default:
                throw new CompileException(opNode, "Internal Error - invalid prefix operator " + ASTUtil.getImage(opNode));
        }
        MethodSignature addMethod = VM.Method.find(MathSupport.class, "add", LpcValue.class, Long.TYPE);
        Expression add = VM.Expression.callStatic(MathSupport.class, addMethod, getValue(expr), amount);
        Expression increment = setReference(expr, add);
        return increment;
    }

    private Expression setReference(LpcExpression ref, Expression value)
    {
        assert ref.reference;
        return VM.Expression.callMethod(ref.expression, ByteCodeConstants.LPC_REFERENCE, ByteCodeConstants.REFERENCE_SET, value);
    }

    private LpcExpression visitUnaryOperation(ASTUnaryExpression node)
    {
        SimpleNode opNode = (SimpleNode) node.jjtGetChild(0);
        SimpleNode exprNode = (SimpleNode) node.jjtGetChild(1);

        LpcExpression expr = compile(exprNode);
        int kind = opNode.jjtGetFirstToken().kind;

        LpcType type;
        Class< ? > cls;
        String method;
        boolean passMath = false;
        switch (kind)
        {
            case ParserConstants.NOT:
                type = Types.INT;
                cls = LogicSupport.class;
                method = "not";
                break;
            case ParserConstants.BINARY_NOT:
                checkType(exprNode, expr, Types.INT);
                type = Types.INT;
                cls = BinarySupport.class;
                method = "not";
                passMath = true;
                break;
            case ParserConstants.PLUS:
                checkType(exprNode, expr, Types.INT, Types.FLOAT);
                type = expr.type;
                cls = MathSupport.class;
                method = "noop";
                break;
            case ParserConstants.MINUS:
                checkType(exprNode, expr, Types.INT, Types.FLOAT);
                type = expr.type;
                cls = MathSupport.class;
                method = "negate";
                break;
            default:
                throw new CompileException(opNode, "Internal error - invalid unary operator " + ASTUtil.describe(opNode));
        }

        Expression math = VM.Expression.getEnum(_context.options().getMath());
        MethodSignature sig = (passMath ? VM.Method.find(cls, method, LpcValue.class, MathLength.class) : VM.Method.find(cls, method, LpcValue.class));
        Expression result = (passMath ? VM.Expression.callStatic(cls, sig, getValue(expr), math) : VM.Expression.callStatic(cls, sig, getValue(expr)));
        return expression(result, type);
    }

    public LpcExpression visit(ASTVariableAssignment node, Object data)
    {
        ExpressionNode expr = (ExpressionNode) node.jjtGetChild(0);
        return compile(expr);
    }

    void checkType(Node child, LpcExpression expression, LpcType... allowsTypes)
    {
        TypeSupport.checkType(child, expression.type, allowsTypes);
    }

    public Object visit(ASTAssignmentExpression node, Object data)
    {
        TokenNode leftNode = node.getLeftNode();
        OperatorNode opNode = node.getOperatorNode();
        TokenNode rightNode = node.getRightNode();

        LpcExpression leftVar = compile(leftNode);
        if (!leftVar.reference)
        {
            throw new CompileException(node, "Cannot assign to value \"" + ASTUtil.getCompleteImage(leftNode) + "\"");
        }

        LpcExpression rightVar = compile(rightNode);
        checkType(node, rightVar, leftVar.type);

        LpcExpression operation = evaluateOperation(leftVar, opNode, rightVar);
        return expression(setReference(leftVar, getValue(operation)), leftVar.type);
    }

    private LpcExpression evaluateOperation(LpcExpression leftVar, OperatorNode node, LpcExpression rightVar)
    {
        Token token = node.getOperator();
        switch (token.kind)
        {
            case ParserConstants.ASSIGN:
                return rightVar;
            case ParserConstants.PLUS_ASSIGN:
                return mathFunction("add", leftVar, rightVar);
            case ParserConstants.MINUS_ASSIGN:
                return mathFunction("subtract", leftVar, rightVar);
            case ParserConstants.MULTIPLY_ASSIGN:
                return mathFunction("multiply", leftVar, rightVar);
            case ParserConstants.DIVIDE_ASSIGN:
                return mathFunction("divide", leftVar, rightVar);
            case ParserConstants.MODULUS_ASSIGN:
                return mathFunction("modulus", leftVar, rightVar);
            case ParserConstants.XOR_ASSIGN:
                return binaryExpression(node, "xor", true, leftVar, rightVar);
            case ParserConstants.AND_ASSIGN:
                return binaryExpression(node, "binaryAnd", false, leftVar, rightVar);
            case ParserConstants.OR_ASSIGN:
                return binaryExpression(node, "binaryOr", false, leftVar, rightVar);
            case ParserConstants.LEFT_SHIFT_ASSIGN:
                return binaryFunction("leftShift", leftVar, rightVar);
            case ParserConstants.RIGHT_SHIFT_ASSIGN:
                return binaryFunction("rightShift", leftVar, rightVar);
        }
        throw new CompileException(token, "Unknown operator " + token);
    }

    public Object visit(ASTComparisonExpression node, Object data)
    {
        Node leftNode = node.jjtGetChild(0);
        Node rightNode = node.jjtGetChild(2);

        LpcExpression leftExpr = compile(leftNode);
        LpcExpression rightExpr = compile(rightNode);

        MethodSignature method = VM.Method.find(ComparisonSupport.class, getComparisonMethod(node), LpcValue.class, LpcValue.class);
        Expression expr = VM.Expression.callStatic(ComparisonSupport.class, method, getValue(leftExpr), getValue(rightExpr));

        return expression(expr, Types.INT);
    }

    private String getComparisonMethod(ASTComparisonExpression node)
    {
        switch (node.getOperator())
        {
            case ParserConstants.LESS_THAN:
                return "lessThan";

            case ParserConstants.LESS_OR_EQUAL:
                return "lessThanOrEqual";

            case ParserConstants.GREATER_THAN:
                return "greaterThan";

            case ParserConstants.GREATER_OR_EQUAL:
                return "greaterThanOrEqual";

            case ParserConstants.EQUAL:
                return "equal";

            case ParserConstants.NOT_EQUAL:
                return "notEqual";

            default:
                throw new CompileException(node, "Internal Error - invalid comparison operator " + node.getOperatorImage());
        }
    }

    public LpcExpression visit(ASTPostfixExpression node, Object data)
    {
        LpcExpression expr = this.compile(node.jjtGetChild(0));
        boolean first = true;
        for (TokenNode child : ASTUtil.children(node))
        {
            if (first)
            {
                first = false;
                continue;
            }
            LpcExpression var = (LpcExpression) child.jjtAccept(this, expr);
            if (var == null)
            {
                throw new NullPointerException("Child " + child + " did not return a variable");
            }
            expr = var;
        }
        return expr;
    }

    public LpcExpression visit(ASTPostfixIncrementOperator node, Object data)
    {
        assert (data instanceof LpcExpression);
        LpcExpression prefix = (LpcExpression) data;
        return increment(node, prefix, true);
    }

    public LpcExpression visit(ASTIndexPostfix node, Object data)
    {
        assert (data instanceof LpcExpression);
        LpcExpression prefix = (LpcExpression) data;

        Expression expr;
        if (node.jjtGetNumChildren() == 1)
        {
            ASTIndexExpression indexNode = (ASTIndexExpression) node.jjtGetChild(0);
            LpcExpression index = getIndexVariable(indexNode);
            MethodSignature method = prefix.reference ? ByteCodeConstants.INDEX_REFERENCE_1 : ByteCodeConstants.INDEX_VALUE_1;
            expr = VM.Expression.callStatic(IndexSupport.class, method, prefix.expression, getValue(index),
                    VM.Expression.constant(isReverseIndex(indexNode)));
        }
        else
        {
            ASTIndexExpression indexStartNode = (ASTIndexExpression) node.jjtGetChild(0);
            ASTIndexExpression indexEndNode = (node.jjtGetNumChildren() == 3 ? (ASTIndexExpression) node.jjtGetChild(2) : null);
            LpcExpression indexStart = getIndexVariable(indexStartNode);
            LpcExpression indexEnd = getIndexVariable(indexEndNode);
            MethodSignature method = prefix.reference ? ByteCodeConstants.INDEX_REFERENCE_2 : ByteCodeConstants.INDEX_VALUE_2;
            expr = VM.Expression.callStatic(IndexSupport.class, method, prefix.expression, //
                    getValue(indexStart), VM.Expression.constant(isReverseIndex(indexStartNode)),//
                    getValue(indexEnd), VM.Expression.constant(isReverseIndex(indexEndNode)) //
            );
        }
        return new LpcExpression(Types.MIXED, expr, prefix.reference);
    }

    private LpcExpression getIndexVariable(ASTIndexExpression indexNode)
    {
        if (indexNode == null)
        {
            return new LpcExpression(Types.MIXED, VM.Expression.nullObject());
        }
        else
        {
            return compile(indexNode.jjtGetChild(indexNode.jjtGetNumChildren() - 1));
        }
    }

    private boolean isReverseIndex(ASTIndexExpression indexNode)
    {
        return indexNode != null && indexNode.jjtGetNumChildren() == 2;
    }

    public LpcExpression visit(ASTCallOther node, Object data)
    {
        assert (data instanceof LpcExpression);

        LpcExpression prefix = (LpcExpression) data;
        ASTIdentifier identifier = node.getIdentifier();

        if (node.jjtGetNumChildren() == 1)
        {
            return visitFieldAccess(prefix, identifier);
        }
        else
        {
            return visitCallOther(prefix, identifier, node.getArgs());
        }
    }

    private LpcExpression visitCallOther(LpcExpression prefix, ASTIdentifier identifier, ASTFunctionArguments args)
    {
        FunctionReference callOther = new FunctionCallSupport(_scope).findFunction(identifier, "efun", "call_other");
        LpcExpression[] initial = { prefix, expression(makeLpcString(identifier.getIdentifierName()), Types.STRING) };
        return new FunctionCallCompiler(_scope, this).compileFunction(callOther, initial, args);
    }

    private LpcExpression visitFieldAccess(LpcExpression prefix, ASTIdentifier identifier)
    {
        String fieldName = identifier.getIdentifierName();
        LpcType fieldType = Types.MIXED;
        LpcType lhsType = prefix.type;

        ClassDefinition classDefinition = lhsType.getClassDefinition();

        if (classDefinition != null)
        {
            FieldDefinition fieldDefinition = classDefinition.getFields().get(fieldName);
            if (fieldDefinition == null)
            {
                throw new CompileException(identifier, "Class " + classDefinition + " does not have a field " + fieldName);
            }
            else
            {
                fieldType = fieldDefinition.getType();
            }
        }
        else if (!Types.MIXED.equals(lhsType))
        {
            throw new CompileException(identifier, "Left hand side of field access is the wrong type (expected class, got " + lhsType + ")");
        }

        Expression field = VM.Expression.callStatic(ClassSupport.class, ByteCodeConstants.CLASS_GET_FIELD, getValue(prefix),
                VM.Expression.constant(fieldName));
        return new LpcExpression(fieldType, field, true);
    }

    public LpcExpression visit(ASTFunctionCall node, Object data)
    {
        return new FunctionCallCompiler(_scope, this).compileFunction(node);
    }

    public LpcExpression visit(ASTExpressionCall node, Object data)
    {
        assert node.jjtGetNumChildren() == 2 : "Incorrect number of children in " + node;

        SimpleNode expression = (SimpleNode) node.jjtGetChild(0);
        ASTFunctionArguments arguments = (ASTFunctionArguments) node.jjtGetChild(1);

        LpcExpression expr = compile(expression);

        FunctionCallCompiler fcc = new FunctionCallCompiler(_scope, this);
        FunctionReference function = FunctionReference.function(ASTUtil.getCompleteImage(expression).toString());
        FunctionArgument[] argValues = fcc.processArguments(function, null, arguments);

        MethodSignature asCallable = VM.Method.find(CallableSupport.class, "asCallable", LpcValue.class);
        Expression callable = VM.Expression.callStatic(CallableSupport.class, asCallable, getValue(expr));

        Expression result = fcc.executeCallable(function, callable, argValues, fcc.requiresExpansion(argValues));
        return expression(result, Types.MIXED);
    }

    public Object visit(ASTTernaryExpression node, Object data)
    {
        Node condNode = node.jjtGetChild(0);
        Node trueNode = node.jjtGetChild(1);
        Node falseNode = node.jjtGetChild(2);

        LpcExpression condExpr = compile(condNode);
        LpcExpression trueExpr = compile(trueNode);
        LpcExpression falseExpr = compile(falseNode);

        Condition condition = VM.Condition.expression(toBoolean(condExpr)).create();
        Expression ternary = VM.Condition.conditional(condition, getValue(trueExpr), getValue(falseExpr));
        if (trueExpr.type.equals(falseExpr.type))
        {
            return expression(ternary, trueExpr.type);
        }
        else
        {
            return expression(ternary, Types.MIXED);
        }
    }

    @SuppressWarnings("unchecked")
    public LpcExpression visit(ASTCatch node, Object data)
    {
        Token token = node.jjtGetFirstToken();
        String methodName = "lpc$catch_" + token.beginLine + "_" + token.beginColumn;

        Collection<ASTVariableReference> referenced = findReferencedVariables(node);
        boolean positional = FunctionLiteralCompiler.countPositionalArguments(referenced) > 0;
        Set<VariableResolution> variables = getReferencedVariables(referenced, _scope.variables());

        int argCount = variables.size();
        if (positional)
        {
            argCount++;
        }
        List<Parameter> parameters = new ArrayList<Parameter>(argCount);
        ExtendedType[] parameterTypes = new ExtendedType[argCount];
        Expression[] arguments = new Expression[argCount];

        int i = 0;
        for (VariableResolution var : variables)
        {
            parameters.add(new ParameterSpec(var.internalName()).withType(LpcReference.class).create());
            parameterTypes[i] = LPC_REFERENCE;
            arguments[i] = var.access();
            i++;
        }
        if (positional)
        {
            parameters.add(new ParameterSpec(FunctionLiteralCompiler.POSITIONAL_ARGUMENT_COLLECTION).withType(LpcValue[].class).create());
            parameterTypes[i] = ByteCodeConstants.LPC_VALUE_ARRAY;
            arguments[i] = VM.Expression.variable(FunctionLiteralCompiler.POSITIONAL_ARGUMENT_COLLECTION);
        }
        MethodSignature signature = new MethodSignatureImpl(singleton(ElementModifier.PRIVATE), LPC_VALUE, methodName, parameterTypes);

        MethodSpec method = new MethodSpec(methodName);
        method.withParameters(parameters);
        method.withModifiers(ElementModifier.PRIVATE, ElementModifier.SYNTHETIC);
        method.withReturnType(LPC_VALUE);

        Node bodyNode = node.jjtGetChild(0);
        Statement bodyStmt = null;
        if (bodyNode instanceof ExpressionNode)
        {
            LpcExpression expr = compile(bodyNode);
            bodyStmt = VM.Statement.ignore(getValue(expr)).create();
        }
        else if (bodyNode instanceof StatementNode)
        {
            bodyStmt = new StatementCompiler(_scope, _context, null).compile((StatementNode) bodyNode);
        }

        String exceptionValue = _scope.variables().allocateInternalVariableName();
        List<ElementBuilder< ? extends Statement>> statements = new ArrayList<ElementBuilder< ? extends Statement>>();
        statements.add(VM.Statement.declareVariable(LpcValue.class, exceptionValue));
        statements.add(VM.Statement.assignVariable(exceptionValue, ByteCodeConstants.NIL));

        TryCatchSpec tryCatch = VM.Statement.attempt(bodyStmt);
        String exceptionName = _scope.variables().allocateInternalVariableName();
        ElementBuilder<Statement> storeException = VM.Statement.assignVariable(exceptionName, EmptyExpression.INSTANCE);
        Expression message = VM.Expression.callMethod(VM.Expression.variable(exceptionName), LPC_RUNTIME_EXCEPTION, EXCEPTION_GET_LPC_MESSAGE);
        Expression string = makeLpcString(message);
        ElementBuilder<Statement> block = VM.Statement.block( //
                VM.Statement.declareVariable(LpcRuntimeException.class, exceptionName), //
                storeException, //
                VM.Statement.assignVariable(exceptionValue, string), //
                VM.Statement.callStatic(ByteCodeConstants.LOG_CATCH_TYPE, ByteCodeConstants.LOG_CATCH_METHOD, VM.Expression.variable(exceptionName)) //
        );
        tryCatch.on(ByteCodeConstants.LPC_RUNTIME_EXCEPTION, block);
        statements.add(tryCatch);
        statements.add(VM.Statement.returnObject(VM.Expression.variable(exceptionValue)));
        method.withBody(statements);

        ClassSpec thisClass = _context.currentClass();
        thisClass.withMethod(method);

        Expression call = VM.Expression.callInherited(signature, arguments);
        return expression(call, Types.STRING);
    }

    public Object visit(ASTCastExpression node, Object data)
    {
        ASTFullType typeNode = (ASTFullType) node.jjtGetChild(0);
        Node exprNode = node.jjtGetChild(1);

        LpcExpression expr = compile(exprNode);
        LpcType type = new TypeSupport(_scope, typeNode).getType();

        if (Types.INT.equals(type))
        {
            Expression asLong = VM.Expression.callMethod(getValue(expr), LpcValue.class, ByteCodeConstants.AS_LONG);
            return expression(VM.Expression.callStatic(ValueSupport.class, ByteCodeConstants.INT_VALUE, asLong), type);
        }
        else if (Types.FLOAT.equals(type))
        {
            Expression asDouble = VM.Expression.callMethod(getValue(expr), LpcValue.class, ByteCodeConstants.AS_DOUBLE);
            return expression(VM.Expression.callStatic(ValueSupport.class, ByteCodeConstants.FLOAT_VALUE, asDouble), type);
        }
        else
        {
            // @TODO check the type...
            return expression(getValue(expr), type);
        }
    }

    public Object visit(ASTCompoundExpression node, Object data)
    {
        LpcExpression var = null;
        List<ElementBuilder< ? extends Statement>> statements = new ArrayList<ElementBuilder< ? extends Statement>>();
        for (TokenNode child : ASTUtil.children(node))
        {
            if (var != null)
            {
                statements.add(VM.Statement.ignore(getValue(var)));
            }
            var = compile(child);
        }
        Expression expression = VM.Expression.chain(statements, getValue(var));
        return expression(expression, var.type);
    }

    public CompileContext getContext()
    {
        return _context;
    }
}
