/* ------------------------------------------------------------------------
 * $Id$
 * Copyright 2008 Tim Vernum
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

package us.terebi.lang.lpc.parser.util;

import java.util.List;

import us.terebi.lang.lpc.parser.ast.ASTArgumentExpression;
import us.terebi.lang.lpc.parser.ast.ASTArithmeticExpression;
import us.terebi.lang.lpc.parser.ast.ASTArithmeticOperator;
import us.terebi.lang.lpc.parser.ast.ASTArrayElement;
import us.terebi.lang.lpc.parser.ast.ASTArrayLiteral;
import us.terebi.lang.lpc.parser.ast.ASTArrayStar;
import us.terebi.lang.lpc.parser.ast.ASTAssignmentExpression;
import us.terebi.lang.lpc.parser.ast.ASTAssignmentOperator;
import us.terebi.lang.lpc.parser.ast.ASTBinaryAndExpression;
import us.terebi.lang.lpc.parser.ast.ASTBinaryOrExpression;
import us.terebi.lang.lpc.parser.ast.ASTCallOther;
import us.terebi.lang.lpc.parser.ast.ASTCastExpression;
import us.terebi.lang.lpc.parser.ast.ASTCatch;
import us.terebi.lang.lpc.parser.ast.ASTClassBody;
import us.terebi.lang.lpc.parser.ast.ASTClassElement;
import us.terebi.lang.lpc.parser.ast.ASTClassLiteral;
import us.terebi.lang.lpc.parser.ast.ASTComparisonExpression;
import us.terebi.lang.lpc.parser.ast.ASTComparisonOperator;
import us.terebi.lang.lpc.parser.ast.ASTCompoundExpression;
import us.terebi.lang.lpc.parser.ast.ASTConditionalStatement;
import us.terebi.lang.lpc.parser.ast.ASTConstant;
import us.terebi.lang.lpc.parser.ast.ASTControlStatement;
import us.terebi.lang.lpc.parser.ast.ASTDeclaration;
import us.terebi.lang.lpc.parser.ast.ASTElementExpander;
import us.terebi.lang.lpc.parser.ast.ASTExclusiveOrExpression;
import us.terebi.lang.lpc.parser.ast.ASTExpressionCall;
import us.terebi.lang.lpc.parser.ast.ASTExpressionStatement;
import us.terebi.lang.lpc.parser.ast.ASTFields;
import us.terebi.lang.lpc.parser.ast.ASTFullType;
import us.terebi.lang.lpc.parser.ast.ASTFunctionArguments;
import us.terebi.lang.lpc.parser.ast.ASTFunctionCall;
import us.terebi.lang.lpc.parser.ast.ASTFunctionLiteral;
import us.terebi.lang.lpc.parser.ast.ASTIdentifier;
import us.terebi.lang.lpc.parser.ast.ASTImmediateExpression;
import us.terebi.lang.lpc.parser.ast.ASTIndexExpression;
import us.terebi.lang.lpc.parser.ast.ASTIndexPostfix;
import us.terebi.lang.lpc.parser.ast.ASTInherit;
import us.terebi.lang.lpc.parser.ast.ASTLabel;
import us.terebi.lang.lpc.parser.ast.ASTLiteralValue;
import us.terebi.lang.lpc.parser.ast.ASTLogicalAndExpression;
import us.terebi.lang.lpc.parser.ast.ASTLogicalOrExpression;
import us.terebi.lang.lpc.parser.ast.ASTLoopStatement;
import us.terebi.lang.lpc.parser.ast.ASTMappingElement;
import us.terebi.lang.lpc.parser.ast.ASTMappingLiteral;
import us.terebi.lang.lpc.parser.ast.ASTMethod;
import us.terebi.lang.lpc.parser.ast.ASTModifiers;
import us.terebi.lang.lpc.parser.ast.ASTNoOpStatement;
import us.terebi.lang.lpc.parser.ast.ASTObjectDefinition;
import us.terebi.lang.lpc.parser.ast.ASTOptExpression;
import us.terebi.lang.lpc.parser.ast.ASTOptVariableOrExpression;
import us.terebi.lang.lpc.parser.ast.ASTParameterDeclaration;
import us.terebi.lang.lpc.parser.ast.ASTParameterDeclarations;
import us.terebi.lang.lpc.parser.ast.ASTPostfixExpression;
import us.terebi.lang.lpc.parser.ast.ASTPostfixIncrementOperator;
import us.terebi.lang.lpc.parser.ast.ASTPrefixIncrementOperator;
import us.terebi.lang.lpc.parser.ast.ASTRange;
import us.terebi.lang.lpc.parser.ast.ASTRef;
import us.terebi.lang.lpc.parser.ast.ASTReverseIndex;
import us.terebi.lang.lpc.parser.ast.ASTScopeResolution;
import us.terebi.lang.lpc.parser.ast.ASTScopedIdentifier;
import us.terebi.lang.lpc.parser.ast.ASTStatementBlock;
import us.terebi.lang.lpc.parser.ast.ASTTernaryExpression;
import us.terebi.lang.lpc.parser.ast.ASTType;
import us.terebi.lang.lpc.parser.ast.ASTUnaryExpression;
import us.terebi.lang.lpc.parser.ast.ASTUnaryOperator;
import us.terebi.lang.lpc.parser.ast.ASTVariable;
import us.terebi.lang.lpc.parser.ast.ASTVariableAssignment;
import us.terebi.lang.lpc.parser.ast.ASTVariableDeclaration;
import us.terebi.lang.lpc.parser.ast.ASTVariableReference;
import us.terebi.lang.lpc.parser.ast.SimpleNode;
import us.terebi.lang.lpc.parser.jj.Token;

/**
 * 
 */
public class BaseASTVisitor implements NodeVisitor
{
    public void visitSpecial(List<Token> tokens)
    {
        // Do nothing
    }

    public Object visit(SimpleNode node, Object data)
    {
        return null;
    }

    public Object visit(ASTObjectDefinition node, Object data)
    {
        return null;
    }

    public Object visit(ASTInherit node, Object data)
    {
        return null;
    }

    public Object visit(ASTDeclaration node, Object data)
    {
        return null;
    }

    public Object visit(ASTFields node, Object data)
    {
        return null;
    }

    public Object visit(ASTVariable node, Object data)
    {
        return null;
    }

    public Object visit(ASTMethod node, Object data)
    {
        return null;
    }

    public Object visit(ASTModifiers node, Object data)
    {
        return null;
    }

    public Object visit(ASTType node, Object data)
    {
        return null;
    }

    public Object visit(ASTIdentifier node, Object data)
    {
        return null;
    }

    public Object visit(ASTParameterDeclarations node, Object data)
    {
        return null;
    }

    public Object visit(ASTParameterDeclaration node, Object data)
    {
        return null;
    }

    public Object visit(ASTFullType node, Object data)
    {
        return null;
    }

    public Object visit(ASTClassBody node, Object data)
    {
        return null;
    }

    public Object visit(ASTStatementBlock node, Object data)
    {
        return null;
    }

    public Object visit(ASTLabel node, Object data)
    {

        return null;
    }

    public Object visit(ASTConditionalStatement node, Object data)
    {

        return null;
    }

    public Object visit(ASTLoopStatement node, Object data)
    {

        return null;
    }

    public Object visit(ASTControlStatement node, Object data)
    {

        return null;
    }

    public Object visit(ASTVariableDeclaration node, Object data)
    {

        return null;
    }

    public Object visit(ASTCompoundExpression node, Object data)
    {

        return null;
    }

    public Object visit(ASTAssignmentExpression node, Object data)
    {

        return null;
    }

    public Object visit(ASTAssignmentOperator node, Object data)
    {

        return null;
    }

    public Object visit(ASTTernaryExpression node, Object data)
    {

        return null;
    }

    public Object visit(ASTLogicalOrExpression node, Object data)
    {

        return null;
    }

    public Object visit(ASTLogicalAndExpression node, Object data)
    {

        return null;
    }

    public Object visit(ASTBinaryOrExpression node, Object data)
    {

        return null;
    }

    public Object visit(ASTExclusiveOrExpression node, Object data)
    {

        return null;
    }

    public Object visit(ASTBinaryAndExpression node, Object data)
    {

        return null;
    }

    public Object visit(ASTComparisonExpression node, Object data)
    {

        return null;
    }

    public Object visit(ASTComparisonOperator node, Object data)
    {

        return null;
    }

    public Object visit(ASTArithmeticExpression node, Object data)
    {

        return null;
    }

    public Object visit(ASTArithmeticOperator node, Object data)
    {

        return null;
    }

    public Object visit(ASTCastExpression node, Object data)
    {

        return null;
    }

    public Object visit(ASTUnaryExpression node, Object data)
    {

        return null;
    }

    public Object visit(ASTUnaryOperator node, Object data)
    {

        return null;
    }

    public Object visit(ASTPostfixExpression node, Object data)
    {

        return null;
    }

    public Object visit(ASTScopeResolution node, Object data)
    {

        return null;
    }

    public Object visit(ASTIndexPostfix node, Object data)
    {
        return null;
    }

    public Object visit(ASTIndexExpression node, Object data)
    {
        return null;
    }

    public Object visit(ASTRange node, Object data)
    {
        return null;
    }

    public Object visit(ASTFunctionArguments node, Object data)
    {
        return null;
    }

    public Object visit(ASTArgumentExpression node, Object data)
    {
        return null;
    }

    public Object visit(ASTConstant node, Object data)
    {
        return null;
    }

    public Object visit(ASTArrayLiteral node, Object data)
    {
        return null;
    }

    public Object visit(ASTArrayElement node, Object data)
    {
        return null;
    }

    public Object visit(ASTMappingLiteral node, Object data)
    {
        return null;
    }

    public Object visit(ASTMappingElement node, Object data)
    {
        return null;
    }

    public Object visit(ASTFunctionLiteral node, Object data)
    {
        return null;
    }

    public Object visit(ASTElementExpander node, Object data)
    {
        return null;
    }

    public Object visit(ASTPostfixIncrementOperator node, Object data)
    {
        return null;
    }

    public Object visit(ASTCallOther node, Object data)
    {
        return null;
    }

    public Object visit(ASTScopedIdentifier node, Object data)
    {
        return null;
    }

    public Object visit(ASTExpressionCall node, Object data)
    {
        return null;
    }

    public Object visit(ASTCatch node, Object data)
    {
        return null;
    }

    public Object visit(ASTFunctionCall node, Object data)
    {
        return null;
    }

    public Object visit(ASTVariableReference node, Object data)
    {
        return null;
    }

    public Object visit(ASTReverseIndex node, Object data)
    {
        return null;
    }

    public Object visit(ASTPrefixIncrementOperator node, Object data)
    {
        return null;
    }

    public Object visit(ASTArrayStar node, Object data)
    {
        return null;
    }

    public Object visit(ASTRef node, Object data)
    {
        return null;
    }

    public Object visit(ASTExpressionStatement node, Object data)
    {
        return null;
    }

    public Object visit(ASTVariableAssignment node, Object data)
    {
        return null;
    }

    public Object visit(ASTImmediateExpression node, Object data)
    {
        return null;
    }

    public Object visit(ASTOptVariableOrExpression node, Object data)
    {
        return null;
    }

    public Object visit(ASTOptExpression node, Object data)
    {
        return null;
    }

    public Object visit(ASTNoOpStatement node, Object data)
    {
        return null;
    }

    public Object visit(ASTClassLiteral node, Object data)
    {
        return null;
    }

    public Object visit(ASTClassElement node, Object data)
    {
        return null;
    }

    public Object visit(ASTLiteralValue node, Object data)
    {
        return null;
    }
}
