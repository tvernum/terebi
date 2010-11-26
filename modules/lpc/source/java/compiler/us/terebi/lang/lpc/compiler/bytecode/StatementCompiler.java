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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.objectweb.asm.Label;

import org.adjective.stout.builder.ElementBuilder;
import org.adjective.stout.core.ExtendedType;
import org.adjective.stout.core.MethodSignature;
import org.adjective.stout.impl.ParameterisedClassImpl;
import org.adjective.stout.loop.Condition;
import org.adjective.stout.loop.DoWhileLoopSpec;
import org.adjective.stout.loop.ForLoopSpec;
import org.adjective.stout.loop.IfElseSpec;
import org.adjective.stout.loop.IterableLoopSpec;
import org.adjective.stout.loop.WhileLoopSpec;
import org.adjective.stout.operation.EmptyStatement;
import org.adjective.stout.operation.Expression;
import org.adjective.stout.operation.LabelStatement;
import org.adjective.stout.operation.Statement;
import org.adjective.stout.operation.VM;

import us.terebi.lang.lpc.compiler.CompileException;
import us.terebi.lang.lpc.compiler.bytecode.context.CompileContext;
import us.terebi.lang.lpc.compiler.java.StatementVisitor;
import us.terebi.lang.lpc.compiler.java.context.ScopeLookup;
import us.terebi.lang.lpc.compiler.util.StatementResult;
import us.terebi.lang.lpc.compiler.util.StatementSupport;
import us.terebi.lang.lpc.compiler.util.TypeSupport;
import us.terebi.lang.lpc.compiler.util.VariableSupport;
import us.terebi.lang.lpc.compiler.util.VariableSupport.VariableDeclaration;
import us.terebi.lang.lpc.parser.ast.ASTConditionalStatement;
import us.terebi.lang.lpc.parser.ast.ASTControlStatement;
import us.terebi.lang.lpc.parser.ast.ASTFullType;
import us.terebi.lang.lpc.parser.ast.ASTIdentifier;
import us.terebi.lang.lpc.parser.ast.ASTLabel;
import us.terebi.lang.lpc.parser.ast.ASTLoopStatement;
import us.terebi.lang.lpc.parser.ast.ASTOptVariableOrExpression;
import us.terebi.lang.lpc.parser.ast.ASTStatementBlock;
import us.terebi.lang.lpc.parser.ast.ASTVariableAssignment;
import us.terebi.lang.lpc.parser.ast.ASTVariableDeclaration;
import us.terebi.lang.lpc.parser.ast.ExpressionNode;
import us.terebi.lang.lpc.parser.ast.Node;
import us.terebi.lang.lpc.parser.ast.ParserVisitor;
import us.terebi.lang.lpc.parser.ast.SimpleNode;
import us.terebi.lang.lpc.parser.ast.StatementNode;
import us.terebi.lang.lpc.parser.ast.TokenNode;
import us.terebi.lang.lpc.parser.jj.ParserConstants;
import us.terebi.lang.lpc.parser.jj.Token;
import us.terebi.lang.lpc.parser.util.ASTUtil;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.LpcReference;
import us.terebi.lang.lpc.runtime.jvm.LpcVariable;
import us.terebi.lang.lpc.runtime.jvm.exception.InternalError;
import us.terebi.lang.lpc.runtime.jvm.support.ComparisonSupport;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.TypedValue;
import us.terebi.util.Pair;

import static us.terebi.lang.lpc.compiler.bytecode.ByteCodeConstants.MAP_ENTRY_GET_KEY;
import static us.terebi.lang.lpc.compiler.bytecode.ByteCodeConstants.MAP_ENTRY_GET_VALUE;
import static us.terebi.lang.lpc.compiler.bytecode.ByteCodeConstants.MAP_ENTRY_SET;
import static us.terebi.lang.lpc.compiler.bytecode.ByteCodeConstants.VALUE_AS_LIST;
import static us.terebi.lang.lpc.compiler.bytecode.ByteCodeConstants.VALUE_AS_MAP;

/**
 * 
 */
public class StatementCompiler extends StatementVisitor<LpcExpression>
{
    private static final ExtendedType VARIABLE_TYPE = new ParameterisedClassImpl(LpcVariable.class);

    private final ScopeLookup _scope;
    private final List< ? super ElementBuilder< ? extends Statement>> _statements;

    private final CompileContext _context;

    public StatementCompiler(ScopeLookup scope, CompileContext context, List< ? super ElementBuilder< ? extends Statement>> statements)
    {
        super(scope);
        _scope = scope;
        _context = context;
        _statements = statements;
    }

    public StatementResult compileBlock(ASTStatementBlock node)
    {
        ParserVisitor proxy = createProxyVisitor();
        return new StatementSupport(_scope, proxy).handleBlock(node);
    }

    private ParserVisitor createProxyVisitor()
    {
        return StatementVisitorProxy.create(this, _context);
    }

    protected StatementResult compileStatement(StatementNode node)
    {
        ParserVisitor proxy = createProxyVisitor();
        Object result = node.jjtAccept(proxy, null);
        if (result == null)
        {
            throw new CompileException(node, "Internal Error - No Statement Result from visiting " + node);
        }
        return (StatementResult) result;
    }

    private StatementResult compileStatement(StatementNode node, List< ? super ElementBuilder< ? extends Statement>> statements)
    {
        StatementCompiler compiler = new StatementCompiler(_scope, _context, statements);
        return compiler.compileStatement(node);
    }

    public Statement compile(StatementNode node)
    {
        List<ElementBuilder< ? extends Statement>> statements = new ArrayList<ElementBuilder< ? extends Statement>>();
        compileStatement(node, statements);
        if (statements.isEmpty())
        {
            return EmptyStatement.INSTANCE;
        }
        if (statements.size() == 1)
        {
            return statements.get(0).create();
        }
        return VM.Statement.block(statements).create();
    }

    public StatementResult visit(ASTStatementBlock node, Object data)
    {
        return compileBlock(node);
    }

    protected void visitReturn(ASTControlStatement node)
    {
        Expression expr;
        LpcType type;
        if (node.jjtGetNumChildren() != 0)
        {
            LpcExpression lpcExpr = processExpression(node.jjtGetChild(0));
            type = lpcExpr.type;
            expr = lpcExpr.expression;
        }
        else
        {
            type = Types.VOID;
            expr = ByteCodeConstants.VOID;
        }

        if (!_context.inFunctionLiteral())
        {
            LpcType returnType = _context.method().getReturnType();
            TypeSupport.checkType(node, type, returnType);
            if (!Types.MIXED.equals(returnType) && !returnType.isClass())
            {
                MethodSignature method = VM.Method.find(TypedValue.class, "type", LpcType.Kind.class, Integer.TYPE, LpcValue.class);
                Expression kind = VM.Expression.getEnum(returnType.getKind());
                Expression depth = VM.Expression.constant(returnType.getArrayDepth());
                expr = VM.Expression.callStatic(TypedValue.class, method, kind, depth, expr);
            }
        }
        _statements.add(VM.Statement.returnObject(expr));
    }

    protected void visitBreak(ASTControlStatement node)
    {
        _statements.add(VM.Condition.breakLoop());
    }

    protected void visitContinue(ASTControlStatement node)
    {
        _statements.add(VM.Condition.continueLoop());
    }

    public StatementResult visit(ASTVariableDeclaration node, Object data)
    {
        VariableSupport variableSupport = new VariableSupport(_scope, node);

        for (VariableDeclaration variable : variableSupport.getVariables())
        {
            variable.declare();

            ASTVariableAssignment assignment = variable.getAssignment();
            Expression init = null;
            if (assignment != null)
            {
                LpcExpression expr = processExpression(assignment);
                TypeSupport.checkType(assignment, expr.type, variable.getType());
                init = expr.expression;
            }

            if (variable.isInternal())
            {
                declareInternalVariable(_statements, variable.getName(), init);
            }
            else
            {
                declareLpcVariable(_statements, variable.getType(), variable.getName(), init);
            }
        }

        return StatementResult.NON_TERMINAL;
    }

    private void declareInternalVariable(List< ? super ElementBuilder< ? extends Statement>> statements, String name, Expression init)
    {
        assert init != null;
        statements.add(VM.Statement.declareVariable(ByteCodeConstants.LPC_VALUE, name));
        statements.add(VM.Statement.assignVariable(name, init));
    }

    public void declareLpcVariable(List< ? super ElementBuilder< ? extends Statement>> statements, LpcType type, String name, Expression init)
    {
        statements.add(VM.Statement.declareVariable(VARIABLE_TYPE, name));
        statements.add(VM.Statement.assignVariable(name, //
                VM.Expression.construct( //
                        VM.Method.constructor(LpcVariable.class, String.class, LpcType.class), //
                        VM.Expression.constant(name), //
                        TypeCompiler.getTypeExpression(type) //
                )));
        if (init == null)
        {
            init = ByteCodeConstants.NIL;
        }
        assignVariable(statements, name, init);
    }

    private boolean assignVariable(List< ? super ElementBuilder< ? extends Statement>> statements, String name, Expression value)
    {
        return statements.add(VM.Statement.callMethod(VM.Expression.variable(name), LpcReference.class, ByteCodeConstants.REFERENCE_SET, value));
    }

    protected LpcExpression processExpression(Node node)
    {
        ExpressionCompiler compiler = new ExpressionCompiler(_scope, _context);
        LpcExpression expr = compiler.compile(node);
        return compiler.asValue(expr);
    }

    protected void makeStatement(LpcExpression expression)
    {
        _statements.add(VM.Statement.ignore(expression.expression));
    }

    protected StatementResult visitDoWhile(ASTLoopStatement node)
    {
        // <DO> Statement() <WHILE> <LEFT_BRACKET> Expression() <RIGHT_BRACKET> <SEMI> 
        StatementNode stmtNode = (StatementNode) node.jjtGetChild(0);
        ExpressionNode exprNode = (ExpressionNode) node.jjtGetChild(1);

        ElementBuilder<Condition> condition = getLoopCondition(exprNode);

        List<ElementBuilder< ? extends Statement>> body = new ArrayList<ElementBuilder< ? extends Statement>>();
        compileStatement(stmtNode, body);

        DoWhileLoopSpec loop = VM.Condition.doWhile().withCondition(condition).withBody(body);
        _statements.add(loop);
        return StatementResult.NON_TERMINAL;
    }

    protected StatementResult visitFor(ASTLoopStatement node)
    {
        //  <FOR> <LEFT_BRACKET> (Opt)VariableOrExpression() <SEMI> (Opt)Expression() <SEMI> (Opt)Expression() <RIGHT_BRACKET> Statement() 
        assert node.jjtGetNumChildren() == 4;

        Statement initialiser = compile((ASTOptVariableOrExpression) node.jjtGetChild(0));
        ElementBuilder<Condition> condition = getLoopCondition(node.jjtGetChild(1));
        ElementBuilder<Statement> increment = VM.Statement.ignore(processExpression(node.jjtGetChild(2)).expression);

        List<ElementBuilder< ? extends Statement>> body = new ArrayList<ElementBuilder< ? extends Statement>>();
        compileStatement((StatementNode) node.jjtGetChild(3), body);

        ForLoopSpec loop = VM.Condition.forLoop().withInitialiser(initialiser).withCondition(condition).withIncrement(increment).withBody(body);

        _statements.add(loop);
        return StatementResult.NON_TERMINAL;
    }

    protected StatementResult visitForeach(ASTFullType typeNode1, ASTIdentifier identNode1, ASTFullType typeNode2, ASTIdentifier identNode2,
            SimpleNode expr, StatementNode bodyNode)
    {
        String ident1 = ASTUtil.getImage(identNode1);
        String ident2 = (identNode2 == null ? null : ASTUtil.getImage(identNode2));
        LpcType type1 = (typeNode1 == null ? null : new TypeSupport(_scope, typeNode1).getType());
        LpcType type2 = (typeNode2 == null ? null : new TypeSupport(_scope, typeNode2).getType());

        List<ElementBuilder< ? extends Statement>> body = new ArrayList<ElementBuilder< ? extends Statement>>();

        _scope.variables().pushScope();

        if (type1 != null)
        {
            _scope.variables().declareLocal(ident1, type1);
            declareLpcVariable(body, type1, ident1, null);
        }
        if (type2 != null)
        {
            _scope.variables().declareLocal(ident2, type2);
            declareLpcVariable(body, type2, ident2, null);
        }

        String var = "$var$" + _scope.variables().allocateInternalVariableName();
        LpcExpression collection = processExpression(expr);
        Expression iterable;
        if (identNode2 == null)
        {
            TypeSupport.checkType(expr, collection.type, Types.MIXED_ARRAY, Types.STRING);
            iterable = VM.Expression.callMethod(collection.expression, LpcValue.class, VALUE_AS_LIST);
            assignVariable(body, ident1, VM.Expression.cast(ByteCodeConstants.LPC_VALUE, VM.Expression.variable(var)));
        }
        else
        {
            TypeSupport.checkType(expr, collection.type, Types.MAPPING);
            iterable = VM.Expression.callMethod( //
                    VM.Expression.callMethod(collection.expression, LpcValue.class, VALUE_AS_MAP), //
                    Map.class, MAP_ENTRY_SET);
            assignVariable(body, ident1, VM.Expression.callMethod(VM.Expression.variable(var), Map.Entry.class, MAP_ENTRY_GET_KEY));
            assignVariable(body, ident2, VM.Expression.callMethod(VM.Expression.variable(var), Map.Entry.class, MAP_ENTRY_GET_VALUE));
        }

        compileStatement(bodyNode, body);
        _scope.variables().popScope();

        IterableLoopSpec loop = VM.Condition.iterable().withVariableName(var).withIterable(iterable).withBody(body);
        _statements.add(loop);

        return StatementResult.NON_TERMINAL;
    }

    protected StatementResult visitWhile(ASTLoopStatement node)
    {
        Node exprNode = node.jjtGetChild(0);
        StatementNode stmtNode = (StatementNode) node.jjtGetChild(1);

        List<ElementBuilder< ? extends Statement>> body = new ArrayList<ElementBuilder< ? extends Statement>>();
        compileStatement(stmtNode, body);
        ElementBuilder<Condition> condition = getLoopCondition(exprNode);
        WhileLoopSpec loop = VM.Condition.whileLoop().withCondition(condition).withBody(body);

        _statements.add(loop);

        return StatementResult.NON_TERMINAL;
    }

    private ElementBuilder<Condition> getLoopCondition(Node exprNode)
    {
        return VM.Condition.expression(ExpressionCompiler.toBoolean(processExpression(exprNode)));
    }

    public StatementResult visit(ASTOptVariableOrExpression node, Object data)
    {
        if (node.jjtGetNumChildren() == 1)
        {
            return compileStatement((StatementNode) node.jjtGetChild(0));
        }
        return StatementResult.NON_TERMINAL;
    }

    protected StatementResult visitIf(ExpressionNode conditionNode, StatementNode ifNode, StatementNode elseNode, Object data)
    {
        LpcExpression condition = processExpression(conditionNode);

        List<ElementBuilder< ? extends Statement>> ifBody = new ArrayList<ElementBuilder< ? extends Statement>>();
        StatementResult ifResult = compileStatement(ifNode, ifBody);

        IfElseSpec ifElse = VM.Condition.ifElse().withCondition(VM.Condition.expression(ExpressionCompiler.toBoolean(condition))).whenTrue(ifBody);
        _statements.add(ifElse);

        if (elseNode == null)
        {
            return StatementResult.NON_TERMINAL;
        }

        List<ElementBuilder< ? extends Statement>> elseBody = new ArrayList<ElementBuilder< ? extends Statement>>();
        StatementResult elseResult = compileStatement(elseNode, elseBody);

        ifElse.whenFalse(elseBody);
        return conditionalResult(ifResult, elseResult);

    }

    protected StatementResult visitSwitch(ASTConditionalStatement node)
    {
        SimpleNode exprNode = (SimpleNode) node.jjtGetChild(0);
        ASTStatementBlock stmtNode = (ASTStatementBlock) node.jjtGetChild(1);
        // @TODO : Optimise for where a TABLESWITCH or LOOKUPSWITCH would do the job

        Map<Label, List<StatementNode>> statements = new LinkedHashMap<Label, List<StatementNode>>();
        Map<Pair<LpcExpression, LpcExpression>, Label> table = new LinkedHashMap<Pair<LpcExpression, LpcExpression>, Label>();

        // @TODO This could probably just sit on the stack...
        String var = "switch$" + _scope.variables().allocateInternalVariableName();
        _statements.add(VM.Statement.declareVariable(ByteCodeConstants.LPC_VALUE, var));
        _statements.add(VM.Statement.assignVariable(var, ExpressionCompiler.getValue(processExpression(exprNode))));

        boolean hasDefault = false;
        Label jump = null;
        List<StatementNode> block = null;
        for (TokenNode child : ASTUtil.children(stmtNode))
        {
            if (child instanceof ASTLabel)
            {
                Pair<LpcExpression, LpcExpression> pair;
                Token token = child.jjtGetFirstToken();
                switch (token.kind)
                {
                    case ParserConstants.DEFLT:
                        pair = new Pair<LpcExpression, LpcExpression>(null, null);
                        hasDefault = true;
                        break;
                    case ParserConstants.CASE:
                        LpcExpression caseStart = processExpression(child.jjtGetChild(0));
                        LpcExpression caseEnd = (child.jjtGetNumChildren() > 1) ? processExpression(child.jjtGetChild(1)) : null;
                        pair = new Pair<LpcExpression, LpcExpression>(caseStart, caseEnd);
                        break;
                    default:
                        continue;
                }
                if (block != null)
                {
                    block = null;
                    jump = null;
                }
                if (jump == null)
                {
                    jump = new Label();
                }
                table.put(pair, jump);
            }
            else if (jump == null)
            {
                throw new CompileException(child, "Cannot have a statement inside a switch without a label");
            }
            else if (child instanceof StatementNode)
            {
                if (block == null)
                {
                    block = new ArrayList<StatementNode>();
                    statements.put(jump, block);
                }
                block.add((StatementNode) child);
            }
            else
            {
                throw new InternalError("Compile error - Non statement " + child + " inside switch");
            }
        }

        for (Entry<Pair<LpcExpression, LpcExpression>, Label> entry : table.entrySet())
        {
            Pair<LpcExpression, LpcExpression> pair = entry.getKey();
            Label label = entry.getValue();

            ElementBuilder<Statement> go = VM.Statement.Goto(label);
            if (pair.getFirst() == null)
            {
                _statements.add(go);
            }
            else
            {
                Expression getVar = VM.Expression.variable(var);
                Expression first = ExpressionCompiler.getValue(pair.getFirst());
                Expression compare;
                if (pair.getSecond() == null)
                {
                    compare = VM.Expression.callMethod(getVar, ByteCodeConstants.LPC_VALUE, ByteCodeConstants.EQUALS, first);
                }
                else
                {
                    Expression second = ExpressionCompiler.getValue(pair.getSecond());
                    compare = VM.Expression.callStatic(ComparisonSupport.class, ByteCodeConstants.IS_IN_RANGE, first, second, getVar);
                }
                ElementBuilder<Condition> condition = VM.Condition.expression(compare);
                IfElseSpec ifElse = VM.Condition.ifElse().withCondition(condition).whenTrue(go.create());
                _statements.add(ifElse);
            }
        }

        Label endLabel = new Label();
        ElementBuilder<Statement> gotoEnd = VM.Statement.Goto(endLabel);
        if (!hasDefault)
        {
            _statements.add(gotoEnd);
        }

        boolean allCasesReturn = true;
        for (Entry<Label, List<StatementNode>> entry : statements.entrySet())
        {
            boolean thisCaseTerminates = false;
            boolean thisCaseReturns = false;
            Label label = entry.getKey();
            _statements.add(new LabelStatement(label));
            for (StatementNode stmt : entry.getValue())
            {
                if (stmt.jjtGetFirstToken().kind == ParserConstants.BREAK)
                {
                    thisCaseTerminates = true;
                    _statements.add(gotoEnd);
                }
                else
                {
                    if (thisCaseTerminates)
                    {
                        throw new CompileException(stmt, "Unreachable statement");
                    }
                    StatementResult result = this.compileStatement(stmt);
                    if (result.isTerminated())
                    {
                        if (result.termination == StatementResult.TerminationType.RETURN)
                        {
                            thisCaseReturns = true;
                        }
                        thisCaseTerminates = true;
                    }
                }
            }
            if (!thisCaseTerminates || !thisCaseReturns)
            {
                allCasesReturn = false;
            }
        }

        _statements.add(new LabelStatement(endLabel));

        if (allCasesReturn && hasDefault)
        {
            return new StatementResult(StatementResult.TerminationType.RETURN);
        }
        else
        {
            return StatementResult.NON_TERMINAL;
        }
    }
}
