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

import us.terebi.lang.lpc.compiler.CompileException;
import us.terebi.lang.lpc.compiler.java.StatementWriter.StatementResult.TerminationType;
import us.terebi.lang.lpc.compiler.java.context.CompileContext;
import us.terebi.lang.lpc.compiler.java.context.VariableLookup.VariableReference;
import us.terebi.lang.lpc.parser.ast.ASTConditionalStatement;
import us.terebi.lang.lpc.parser.ast.ASTControlStatement;
import us.terebi.lang.lpc.parser.ast.ASTDeclaration;
import us.terebi.lang.lpc.parser.ast.ASTExpressionStatement;
import us.terebi.lang.lpc.parser.ast.ASTFullType;
import us.terebi.lang.lpc.parser.ast.ASTIdentifier;
import us.terebi.lang.lpc.parser.ast.ASTLabel;
import us.terebi.lang.lpc.parser.ast.ASTLoopStatement;
import us.terebi.lang.lpc.parser.ast.ASTNoOpStatement;
import us.terebi.lang.lpc.parser.ast.ASTOptExpression;
import us.terebi.lang.lpc.parser.ast.ASTOptVariableOrExpression;
import us.terebi.lang.lpc.parser.ast.ASTStatementBlock;
import us.terebi.lang.lpc.parser.ast.ASTType;
import us.terebi.lang.lpc.parser.ast.ASTUtil;
import us.terebi.lang.lpc.parser.ast.ASTVariable;
import us.terebi.lang.lpc.parser.ast.ASTVariableDeclaration;
import us.terebi.lang.lpc.parser.ast.BaseASTVisitor;
import us.terebi.lang.lpc.parser.ast.Node;
import us.terebi.lang.lpc.parser.ast.ParserVisitor;
import us.terebi.lang.lpc.parser.ast.SimpleNode;
import us.terebi.lang.lpc.parser.jj.ParserConstants;
import us.terebi.lang.lpc.parser.jj.Token;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.util.Pair;

/**
 * 
 */
public class StatementWriter extends BaseASTVisitor implements ParserVisitor
{
    private final CompileContext _context;

    public static class StatementResult
    {
        static final StatementWriter.StatementResult NON_TERMINAL = new StatementWriter.StatementResult(
                TerminationType.NOT_TERMINATED);

        public enum TerminationType
        {
            NOT_TERMINATED, CONTINUE, BREAK, RETURN
        }

        public final TerminationType termination;

        public StatementResult(TerminationType term)
        {
            this.termination = term;
        }

        public boolean isTerminated()
        {
            return this.termination != TerminationType.NOT_TERMINATED;
        }

        public String toString()
        {
            return getClass().getSimpleName() + ":" + this.termination.name();
        }
    }

    public StatementWriter(CompileContext context)
    {
        _context = context;
    }

    public StatementResult visit(ASTStatementBlock node, Object data)
    {
        PrintWriter writer = _context.writer();
        writer.println(" { /* (Block " + ASTUtil.describe(node.jjtGetFirstToken()) + ") */");
        StatementResult last = writeBlock(node);
        writer.println(" } /* (Block " + ASTUtil.describe(node.jjtGetFirstToken()) + ") */");
        return last;
    }

    public StatementResult writeBlock(ASTStatementBlock node)
    {
        _context.variables().pushScope();
        StatementResult last = StatementResult.NON_TERMINAL;
        for (SimpleNode child : ASTUtil.children(node))
        {
            if (last.isTerminated())
            {
                throw new CompileException(child, "Unreachable statement");
            }
            last = executeStatement(child);
            if (last == null)
            {
                throw new CompileException(child, "Internal Error - No Statement Result from visiting " + child);
            }
            //            System.err.println("In block " + node + " result of " + ASTUtil.describe(child) + " is " + last);
        }
        _context.variables().popScope();
        return last;
    }

    public StatementResult visit(ASTControlStatement node, Object data)
    {
        PrintWriter writer = _context.writer();
        InternalVariable var = null;
        if (node.jjtGetNumChildren() != 0)
        {
            var = evaluateExpression(node.jjtGetChild(0));
        }

        Token token = node.jjtGetFirstToken();
        writer.print(token.image);

        if (var != null)
        {
            writer.print(" ");
            var.value(writer);
        }
        else if (node.jjtGetFirstToken().kind == ParserConstants.RETURN)
        {
            writer.print(" makeValue()");
        }

        writer.println(";");
        switch (token.kind)
        {
            case ParserConstants.BREAK:
                return new StatementResult(StatementResult.TerminationType.BREAK);
            case ParserConstants.CONTINUE:
                return new StatementResult(StatementResult.TerminationType.CONTINUE);
            case ParserConstants.RETURN:
                return new StatementResult(StatementResult.TerminationType.RETURN);
            default:
                throw new CompileException(node, "Internal Error - Unexpected control statement " + token);
        }
    }

    public StatementResult visit(ASTConditionalStatement node, Object data)
    {
        Token token = node.jjtGetFirstToken();

        StatementResult result;
        if (token.kind == ParserConstants.SWITCH)
        {
            result = visitSwitch(node);
        }
        else
        {
            result = visitIf(node, data);
        }

        _context.writer().println("/* " + ASTUtil.describe(token) + " */");
        return result;
    }

    @SuppressWarnings("unchecked")
    private StatementResult visitSwitch(ASTConditionalStatement node)
    {
        SimpleNode exprNode = (SimpleNode) node.jjtGetChild(0);
        ASTStatementBlock stmtNode = (ASTStatementBlock) node.jjtGetChild(1);

        InternalVariable exprVar = evaluateExpression(exprNode);

        //        System.err.println("BEGIN SWITCH: " + ASTUtil.describe(node) + " : " + ASTUtil.getCompleteImage(exprNode));

        List< ? extends ASTLabel> switchControl = ASTUtil.<ASTLabel> getChildren(stmtNode, ASTLabel.class);

        PrintWriter writer = _context.writer();

        List<Pair<InternalVariable, InternalVariable>> variables = new ArrayList<Pair<InternalVariable, InternalVariable>>();
        for (ASTLabel label : switchControl)
        {
            if (label.jjtGetFirstToken().kind == ParserConstants.IDENTIFIER)
            {
                continue;
            }
            InternalVariable caseStart = (label.jjtGetNumChildren() > 0) ? evaluateExpression(label.jjtGetChild(0)) : null;
            InternalVariable caseEnd = (label.jjtGetNumChildren() > 1) ? evaluateExpression(label.jjtGetChild(1)) : null;
            variables.add(new Pair(caseStart, caseEnd));
        }

        String jumpVar = _context.variables().allocateInternalVariableName();
        writer.print("int ");
        writer.print(jumpVar);
        writer.println(" = -1;");

        boolean hasDefaultCase = false;
        int index = 0;
        for (Pair<InternalVariable, InternalVariable> pair : variables)
        {
            index++;
            if (index != 1)
            {
                writer.print(" else ");
            }
            if (pair.getFirst() == null)
            {
                hasDefaultCase = true;
            }
            else
            {
                if (pair.getSecond() == null)
                {
                    writer.print("if(");
                    pair.getFirst().value(writer);
                    writer.print(".equals(");
                    exprVar.value(writer);
                    writer.print(")) ");
                }
                else
                {
                    writer.print("if(ComparisonSupport.isInRange(");
                    pair.getFirst().value(writer);
                    writer.print(",");
                    pair.getSecond().value(writer);
                    writer.print(",");
                    exprVar.value(writer);
                    writer.print(")) ");
                }
            }
            writer.print(jumpVar);
            writer.print("=");
            writer.print(index);
            writer.println(";");
        }

        writer.print("switch(");
        writer.print(jumpVar);
        writer.println(") {");

        boolean thisCaseTerminates = false;
        boolean allCasesReturn = true;
        index = 0;
        for (SimpleNode child : ASTUtil.children(stmtNode))
        {
            if (child instanceof ASTLabel)
            {
                if (child.jjtGetFirstToken().kind != ParserConstants.IDENTIFIER)
                {
                    //                    System.err.println("LABEL: " + ASTUtil.getCompleteImage(child));
                    index++;
                    writer.print("case ");
                    writer.print(index);
                    writer.print(" : ");
                    thisCaseTerminates = false;
                    continue;
                }
            }
            if (thisCaseTerminates)
            {
                if (child instanceof ASTControlStatement && child.jjtGetFirstToken().kind == ParserConstants.BREAK)
                {
                    continue;
                }
                else
                {
                    throw new CompileException(child, "Unreachable statement");
                }
            }
            StatementResult result = executeStatement(child);
            if (result.isTerminated())
            {
                //                System.err.println("TERMINATOR: " + ASTUtil.getCompleteImage(child) + " - " + result);
                if (!thisCaseTerminates && result.termination != StatementResult.TerminationType.RETURN)
                {
                    allCasesReturn = false;
                }
                thisCaseTerminates = true;
            }
        }
        if (hasDefaultCase)
        {
            writer.println("default: throw new "
                    + LpcRuntimeException.class.getName()
                    + "(\"Internal Error - case table did not match\");");
        }
        writer.println("}");

        if (!thisCaseTerminates)
        {
            // System.err.println("In switch statement " + ASTUtil.describe(exprNode) + " last case does not return");
            allCasesReturn = false;
        }

        if (allCasesReturn && hasDefaultCase)
        {
            // System.err.println("SWITCH: " + ASTUtil.getCompleteImage(exprNode) + " - RETURN");
            return new StatementResult(StatementResult.TerminationType.RETURN);
        }
        else
        {
            // System.err.println("SWITCH: " + ASTUtil.getCompleteImage(exprNode) + " - NON TERMINAL");
            return StatementResult.NON_TERMINAL;
        }
    }

    private StatementResult visitIf(ASTConditionalStatement node, Object data)
    {
        SimpleNode conditionNode = (SimpleNode) node.jjtGetChild(0);
        SimpleNode ifNode = (SimpleNode) node.jjtGetChild(1);
        SimpleNode elseNode = null;
        if (node.jjtGetNumChildren() > 2)
        {
            elseNode = (SimpleNode) node.jjtGetChild(2);
        }

        InternalVariable variable = evaluateExpression(conditionNode);
        PrintWriter writer = _context.writer();

        writer.print("if( ");
        variable.value(writer);
        writer.println(".asBoolean() ) {");

        StatementResult ifResult;
        if (ifNode instanceof ASTStatementBlock)
        {
            ifResult = this.writeBlock((ASTStatementBlock) ifNode);
        }
        else
        {
            ifResult = (StatementResult) ifNode.jjtAccept(this, data);
        }

        writer.println("} // (if " + variable.name + ")");
        if (elseNode == null)
        {
            return StatementResult.NON_TERMINAL;
        }

        writer.println("else {");
        StatementResult elseResult;
        if (elseNode instanceof ASTStatementBlock)
        {
            elseResult = this.writeBlock((ASTStatementBlock) elseNode);
        }
        else
        {
            elseResult = (StatementResult) elseNode.jjtAccept(this, data);
        }

        writer.println("} // (else)");

        int ordinal = Math.min(ifResult.termination.ordinal(), elseResult.termination.ordinal());
        TerminationType terminationType = StatementResult.TerminationType.values()[ordinal];
        return new StatementResult(terminationType);
    }

    public Object visit(ASTDeclaration node, Object data)
    {
        throw new UnsupportedOperationException("visit(" + node + ") - Not implemented");
    }

    public Object visit(ASTExpressionStatement node, Object data)
    {
        for (SimpleNode child : ASTUtil.children(node))
        {
            evaluateExpression(child);
        }
        return StatementResult.NON_TERMINAL;
    }

    private InternalVariable evaluateExpression(Node child)
    {
        return new ExpressionWriter(_context).evaluate(child);
    }

    public Object visit(ASTLabel node, Object data)
    {
        throw new UnsupportedOperationException("visit(" + node + ") - Not implemented");
    }

    public StatementResult visit(ASTVariableDeclaration node, Object data)
    {
        ASTType typeNode = ASTUtil.getChild(ASTType.class, node);
        assert (typeNode != null);

        VariableWriter writer = new VariableWriter(_context, typeNode);
        for (int i = 1; i < node.jjtGetNumChildren(); i++)
        {
            Node child = node.jjtGetChild(i);
            child.jjtAccept(this, writer);
        }

        return StatementResult.NON_TERMINAL;
    }

    public Object visit(ASTVariable node, Object data)
    {
        assert (data instanceof VariableWriter);
        VariableWriter writer = (VariableWriter) data;
        writer.printLocal(node);
        return null;
    }

    public Object visit(ASTLoopStatement node, Object data)
    {
        _context.variables().pushScope();
        try
        {
            switch (node.jjtGetFirstToken().kind)
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
                    throw new CompileException(node, "Internal Error - Unexpected loop type "
                            + ASTUtil.describe(node.jjtGetFirstToken()));
            }
        }
        finally
        {
            _context.variables().popScope();
        }
    }

    private StatementResult visitFor(ASTLoopStatement node)
    {
        //  <FOR> <LEFT_BRACKET> (Opt)VariableOrExpression() ] <SEMI> (Opt)Expression() <SEMI> (Opt)Expression() <RIGHT_BRACKET> Statement() 
        assert (node.jjtGetNumChildren() == 4);
        ASTOptVariableOrExpression part1 = (ASTOptVariableOrExpression) node.jjtGetChild(0);
        ASTOptExpression part2 = (ASTOptExpression) node.jjtGetChild(1);
        ASTOptExpression part3 = (ASTOptExpression) node.jjtGetChild(2);
        SimpleNode stmt = (SimpleNode) node.jjtGetChild(3);

        PrintWriter writer = _context.writer();
        writer.println("{");
        if (part1.jjtGetNumChildren() != 0)
        {
            executeStatement(part1.jjtGetChild(0));
        }
        InternalVariable first = new InternalVariable(_context, false, null);
        writer.println("boolean " + first.name + " = true;");
        writer.println("for(;;" + first.name + " = false) {");

        if (part2.jjtGetNumChildren() != 0)
        {
            InternalVariable cond = evaluateExpression(part2.jjtGetChild(0));
            writer.println("if( " + cond.name + ".asBoolean() ) break;");
        }

        if (part3.jjtGetNumChildren() != 0)
        {
            writer.println("if(!" + first.name + ") { ");
            evaluateExpression(part3.jjtGetChild(0));
            writer.println("}");
        }

        StatementResult result = executeStatement(stmt);

        writer.println("}");
        writer.println("}");

        if (result.termination == StatementResult.TerminationType.RETURN)
        {
            return result;
        }
        else
        {
            return StatementResult.NON_TERMINAL;
        }
    }

    private StatementResult visitForeach(ASTLoopStatement node)
    {
        // <FOREACH> <LEFT_BRACKET> [ FullType() ] Identifier() [ <COMMA> [ FullType() ] Identifier() ] <IN> Expression() <RIGHT_BRACKET> Statement() 
        ASTFullType typeNode1 = null;
        ASTIdentifier identNode1 = null;
        ASTFullType typeNode2 = null;
        ASTIdentifier identNode2 = null;
        SimpleNode expr = null;
        Node stmt = null;

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
        stmt = node.jjtGetChild(++index);

        assert (index == node.jjtGetNumChildren());

        PrintWriter writer = _context.writer();
        InternalVariable collection = evaluateExpression(expr);
        InternalVariable iterator = new InternalVariable(_context, false, null);
        boolean map;
        if (identNode2 == null)
        {
            ExpressionWriter.checkType(expr, collection, Types.MIXED_ARRAY, Types.STRING);
            writer.print("Iterable<LpcValue> " + iterator.name + " = ");
            collection.value(writer);
            writer.println(".asList();");
            map = false;
        }
        else
        {
            ExpressionWriter.checkType(expr, collection, Types.MAPPING);
            writer.print("Iterable<Map.Entry<LpcValue,LpcValue>> " + iterator.name + " = ");
            collection.value(writer);
            writer.println(".asMap().entrySet();");
            map = true;
        }

        LpcType type1 = (typeNode1 == null ? null : new TypeWriter(_context).getType(typeNode1));
        LpcType type2 = (typeNode2 == null ? null : new TypeWriter(_context).getType(typeNode2));

        String ident1 = ASTUtil.getImage(identNode1);
        String ident2 = (identNode2 == null ? null : ASTUtil.getImage(identNode2));

        VariableWriter variableWriter = new VariableWriter(_context, null);

        InternalVariable element = new InternalVariable(_context, false, null);
        writer.println("for("
                + (map ? "Map.Entry<LpcValue,LpcValue>" : "LpcValue")
                + " "
                + element.name
                + " : "
                + iterator.name
                + ") { ");

        VariableReference var1 = null, var2 = null;
        if (type1 != null)
        {
            var1 = variableWriter.declareLocalVariable(identNode1, ident1, type1, null);
        }
        else
        {
            var1 = _context.variables().findVariable(ident1);
        }

        if (type2 != null)
        {
            var2 = variableWriter.declareLocalVariable(identNode2, ident2, type2, null);
        }
        else if (map)
        {
            var2 = _context.variables().findVariable(ident2);
        }

        writer.print(var1.internalName);
        writer.print(".set(");
        writer.print(element.name);
        if (map)
        {
            writer.print(".getKey()");
        }
        writer.println(");");

        if (map)
        {
            writer.print(var2.internalName);
            writer.print(".set(");
            writer.print(element.name);
            writer.print(".getValue()");
            writer.println(");");
        }

        StatementResult result = this.executeStatement(stmt);

        writer.println("}");

        if (result.termination == StatementResult.TerminationType.RETURN)
        {
            return result;
        }
        else
        {
            return StatementResult.NON_TERMINAL;
        }
    }

    private StatementResult visitWhile(ASTLoopStatement node)
    {
        //  <WHILE> <LEFT_BRACKET> Expression() <RIGHT_BRACKET> Statement()
        Node exprNode = node.jjtGetChild(0);
        Node stmtNode = node.jjtGetChild(1);

        PrintWriter writer = _context.writer();
        writer.println("while(true) {");

        InternalVariable exprVar = evaluateExpression(exprNode);
        writer.print("if(");
        exprVar.value(writer);
        writer.println(".asBoolean()) break; ");

        StatementResult result = executeStatement(stmtNode);
        writer.println("}");

        if (result.termination == StatementResult.TerminationType.RETURN)
        {
            return result;
        }
        else
        {
            return StatementResult.NON_TERMINAL;
        }
    }

    private StatementResult visitDoWhile(@SuppressWarnings("unused")
    ASTLoopStatement node)
    {
        throw new UnsupportedOperationException("visitDoWhile - Not implemented");
    }

    private StatementResult executeStatement(Node stmt)
    {
        Object result = stmt.jjtAccept(this, null);
        return (StatementResult) result;
    }

    public StatementResult visit(ASTNoOpStatement node, Object data)
    {
        return StatementResult.NON_TERMINAL;
    }
}
