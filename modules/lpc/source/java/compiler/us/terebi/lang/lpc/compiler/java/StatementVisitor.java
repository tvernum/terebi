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

package us.terebi.lang.lpc.compiler.java;

import us.terebi.lang.lpc.compiler.CompileException;
import us.terebi.lang.lpc.compiler.java.context.ScopeLookup;
import us.terebi.lang.lpc.compiler.util.StatementResult;
import us.terebi.lang.lpc.compiler.util.StatementResult.TerminationType;
import us.terebi.lang.lpc.parser.ast.ASTConditionalStatement;
import us.terebi.lang.lpc.parser.ast.ASTControlStatement;
import us.terebi.lang.lpc.parser.ast.ASTExpressionStatement;
import us.terebi.lang.lpc.parser.ast.ASTFullType;
import us.terebi.lang.lpc.parser.ast.ASTIdentifier;
import us.terebi.lang.lpc.parser.ast.ASTLoopStatement;
import us.terebi.lang.lpc.parser.ast.ASTNoOpStatement;
import us.terebi.lang.lpc.parser.ast.ExpressionNode;
import us.terebi.lang.lpc.parser.ast.Node;
import us.terebi.lang.lpc.parser.ast.SimpleNode;
import us.terebi.lang.lpc.parser.ast.StatementNode;
import us.terebi.lang.lpc.parser.ast.TokenNode;
import us.terebi.lang.lpc.parser.jj.ParserConstants;
import us.terebi.lang.lpc.parser.jj.Token;
import us.terebi.lang.lpc.parser.util.ASTUtil;
import us.terebi.lang.lpc.parser.util.BaseASTVisitor;
import us.terebi.lang.lpc.runtime.jvm.exception.InternalError;

/**
 * 
 */
public abstract class StatementVisitor<EXPR_TYPE> extends BaseASTVisitor
{
    private ScopeLookup _scope;

    public StatementVisitor(ScopeLookup scope)
    {
        _scope = scope;
    }

    public ScopeLookup getScope()
    {
        return _scope;
    }

    protected abstract EXPR_TYPE processExpression(Node child);

    protected abstract StatementResult visitDoWhile(ASTLoopStatement node);

    protected abstract StatementResult visitWhile(ASTLoopStatement node);

    protected abstract StatementResult visitFor(ASTLoopStatement node);

    protected StatementResult visitForeach(ASTLoopStatement node)
    {
        // <FOREACH> <LEFT_BRACKET> [ FullType() ] Identifier() [ <COMMA> [ FullType() ] Identifier() ] <IN> Expression() <RIGHT_BRACKET> Statement() 
        ASTFullType typeNode1 = null;
        ASTIdentifier identNode1 = null;
        ASTFullType typeNode2 = null;
        ASTIdentifier identNode2 = null;
        SimpleNode expr = null;
        StatementNode stmt = null;

        int index = 0;
        Node child = node.jjtGetChild(index);
        if (child instanceof ASTFullType)
        {
            typeNode1 = (ASTFullType) child;
            child = node.jjtGetChild(++index);
        }

        assert (child instanceof ASTIdentifier);
        identNode1 = (ASTIdentifier) child;
        child = node.jjtGetChild(++index);

        if (child instanceof ASTFullType)
        {
            typeNode2 = (ASTFullType) child;
            child = node.jjtGetChild(++index);
        }

        if (child instanceof ASTIdentifier)
        {
            identNode2 = (ASTIdentifier) child;
            child = node.jjtGetChild(++index);
        }

        expr = (SimpleNode) child;

        stmt = (StatementNode) node.jjtGetChild(++index);

        if (index != node.jjtGetNumChildren() - 1)
        {
            StringBuilder text = new StringBuilder();
            text.append("Index = " + index + " ; Node (" + node.getClass().getSimpleName() + ") has " + node.jjtGetNumChildren());
            text.append("\nTN1: " + typeNode1 + " - " + ASTUtil.describe(typeNode1));
            text.append("\nIN1: " + identNode1 + " - " + ASTUtil.describe(identNode1));
            text.append("\nTN2: " + typeNode2 + " - " + ASTUtil.describe(typeNode2));
            text.append("\nIN2: " + identNode2 + " - " + ASTUtil.describe(identNode2));
            text.append("\nEXP: " + expr + " - " + ASTUtil.describe(expr));
            text.append("\nSTM: " + stmt);
            text.append("\nChildren = ");
            for (TokenNode c : ASTUtil.children(node))
            {
                text.append(c);
                text.append(" ");
            }
            throw new InternalError(text.toString());
        }

        return visitForeach(typeNode1, identNode1, typeNode2, identNode2, expr, stmt);
    }

    protected abstract StatementResult visitForeach(ASTFullType typeNode1, ASTIdentifier identNode1, ASTFullType typeNode2, ASTIdentifier identNode2,
            SimpleNode expr, StatementNode stmt);

    protected abstract void visitReturn(ASTControlStatement node);
    protected abstract void visitContinue(ASTControlStatement node);
    protected abstract void visitBreak(ASTControlStatement node);

    public Object visit(ASTExpressionStatement node, Object data)
    {
        for (TokenNode child : ASTUtil.children(node))
        {
            makeStatement(processExpression(child));
        }
        return StatementResult.NON_TERMINAL;
    }

    protected abstract void makeStatement(EXPR_TYPE expression);

    public StatementResult visit(ASTControlStatement node, Object data)
    {
        Token token = node.jjtGetFirstToken();
        switch (node.getControlType())
        {
            case ParserConstants.BREAK:
                {
                    visitBreak(node);
                    return new StatementResult(StatementResult.TerminationType.BREAK);
                }
            case ParserConstants.CONTINUE:
                {
                    visitContinue(node);
                    return new StatementResult(StatementResult.TerminationType.CONTINUE);
                }
            case ParserConstants.RETURN:
                {
                    visitReturn(node);
                    return new StatementResult(StatementResult.TerminationType.RETURN);
                }
            default:
                throw new CompileException(node, "Internal Error - Unexpected control statement " + token);
        }
    }

    public Object visit(ASTLoopStatement node, Object data)
    {
        _scope.variables().pushScope();
        try
        {
            switch (node.getLoopType())
            {
                case ParserConstants.FOR:
                    return visitFor(node);
                case ParserConstants.FOREACH:
                    return visitForeach(node);
                case ParserConstants.WHILE:
                    return visitWhile(node);
                case ParserConstants.DO:
                    return visitDoWhile(node);
                default:
                    throw new CompileException(node, "Internal Error - Unexpected loop type " + ASTUtil.describe(node.jjtGetFirstToken()));
            }
        }
        finally
        {
            _scope.variables().popScope();
        }
    }

    public StatementResult visit(ASTNoOpStatement node, Object data)
    {
        return StatementResult.NON_TERMINAL;
    }

    public StatementResult visit(ASTConditionalStatement node, Object data)
    {
        if (node.getConditionType() == ParserConstants.SWITCH)
        {
            return visitSwitch(node);
        }
        else
        {
            return visitIf(node, data);
        }
    }

    protected abstract StatementResult visitSwitch(ASTConditionalStatement node);

    protected StatementResult visitIf(ASTConditionalStatement node, Object data)
    {
        ExpressionNode conditionNode = (ExpressionNode) node.jjtGetChild(0);
        StatementNode ifNode = (StatementNode) node.jjtGetChild(1);
        StatementNode elseNode = null;
        if (node.jjtGetNumChildren() > 2)
        {
            elseNode = (StatementNode) node.jjtGetChild(2);
        }

        return visitIf(conditionNode, ifNode, elseNode, data);
    }

    protected abstract StatementResult visitIf(ExpressionNode conditionNode, StatementNode ifNode, StatementNode elseNode, Object data);

    protected StatementResult conditionalResult(StatementResult ifResult, StatementResult elseResult)
    {
        int ordinal = Math.min(ifResult.termination.ordinal(), elseResult.termination.ordinal());
        TerminationType terminationType = StatementResult.TerminationType.values()[ordinal];
        return new StatementResult(terminationType);
    }

}
