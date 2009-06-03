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

package us.terebi.lang.lpc.parser.jj;

import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import us.terebi.lang.lpc.parser.ast.ASTArithmeticExpression;
import us.terebi.lang.lpc.parser.ast.ASTArithmeticOperator;
import us.terebi.lang.lpc.parser.ast.ASTAssignmentExpression;
import us.terebi.lang.lpc.parser.ast.ASTAssignmentOperator;
import us.terebi.lang.lpc.parser.ast.ASTConstant;
import us.terebi.lang.lpc.parser.ast.ASTDeclaration;
import us.terebi.lang.lpc.parser.ast.ASTExpressionStatement;
import us.terebi.lang.lpc.parser.ast.ASTFields;
import us.terebi.lang.lpc.parser.ast.ASTObjectDefinition;
import us.terebi.lang.lpc.parser.ast.ASTIdentifier;
import us.terebi.lang.lpc.parser.ast.ASTMethod;
import us.terebi.lang.lpc.parser.ast.ASTModifiers;
import us.terebi.lang.lpc.parser.ast.ASTParameterDeclarations;
import us.terebi.lang.lpc.parser.ast.ASTScopedIdentifier;
import us.terebi.lang.lpc.parser.ast.ASTStatementBlock;
import us.terebi.lang.lpc.parser.ast.ASTType;
import us.terebi.lang.lpc.parser.ast.ASTUtil;
import us.terebi.lang.lpc.parser.ast.ASTVariable;
import us.terebi.lang.lpc.parser.ast.ASTVariableReference;
import us.terebi.lang.lpc.parser.ast.SimpleNode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @version $Revision$
 */
public class ParserTest
{
    @SuppressWarnings("unchecked")
    @Test
    public void testParserProducesExpectedAST() throws Exception
    {
        String lpc = "private int x ; void create() { x = 1 + 5 * 6 ; }";
        StringReader reader = new StringReader(lpc);
        Parser parser = new Parser(reader);
        ASTObjectDefinition file = parser.ObjectDefinition();

        assertChildTypes(file, ASTDeclaration.class, ASTDeclaration.class);

        ASTDeclaration fieldDeclaration = (ASTDeclaration) file.jjtGetChild(0);
        assertChildTypes(fieldDeclaration, ASTModifiers.class, ASTType.class, ASTFields.class);

        ASTModifiers modifiers = (ASTModifiers) fieldDeclaration.jjtGetChild(0);
        assertChildTypes(modifiers);
        assertTokenTypes(modifiers, ParserConstants.PRIVATE);

        ASTType type = (ASTType) fieldDeclaration.jjtGetChild(1);
        assertChildTypes(type);
        assertTokenTypes(type, ParserConstants.INT);

        ASTFields fields = (ASTFields) fieldDeclaration.jjtGetChild(2);
        assertChildTypes(fields, ASTVariable.class);

        ASTVariable variable = (ASTVariable) fields.jjtGetChild(0);
        assertChildTypes(variable, ASTIdentifier.class);

        ASTIdentifier identifier = (ASTIdentifier) variable.jjtGetChild(0);
        assertChildTypes(identifier);
        assertTokenTypes(identifier, ParserConstants.IDENTIFIER);
        assertTokenImages(identifier, "x");

        ASTDeclaration methodDeclaration = (ASTDeclaration) file.jjtGetChild(1);
        assertChildTypes(methodDeclaration, ASTModifiers.class, ASTType.class, ASTMethod.class);

        modifiers = (ASTModifiers) methodDeclaration.jjtGetChild(0);
        assertChildTypes(modifiers);
        assertTokenTypes(modifiers);

        type = (ASTType) methodDeclaration.jjtGetChild(1);
        assertChildTypes(type);
        assertTokenTypes(type, ParserConstants.VOID);

        ASTMethod method = (ASTMethod) methodDeclaration.jjtGetChild(2);
        assertChildTypes(method, ASTIdentifier.class, ASTParameterDeclarations.class, ASTStatementBlock.class);

        identifier = (ASTIdentifier) method.jjtGetChild(0);
        assertChildTypes(identifier);
        assertTokenTypes(identifier, ParserConstants.IDENTIFIER);
        assertTokenImages(identifier, "create");

        ASTParameterDeclarations parameters = (ASTParameterDeclarations) method.jjtGetChild(1);
        assertChildTypes(parameters);

        ASTStatementBlock block = (ASTStatementBlock) method.jjtGetChild(2);
        assertChildTypes(block, ASTExpressionStatement.class);

        ASTExpressionStatement statement = (ASTExpressionStatement) block.jjtGetChild(0);
        assertChildTypes(statement, ASTAssignmentExpression.class);

        ASTAssignmentExpression assignment = (ASTAssignmentExpression) statement.jjtGetChild(0);
        assertChildTypes(assignment, ASTVariableReference.class, ASTAssignmentOperator.class, ASTArithmeticExpression.class);

        ASTVariableReference var = (ASTVariableReference) assignment.jjtGetChild(0);
        assertChildTypes(var, ASTScopedIdentifier.class);

        ASTScopedIdentifier scoped = (ASTScopedIdentifier) var.jjtGetChild(0);
        assertChildTypes(scoped, ASTIdentifier.class);

        identifier = (ASTIdentifier) scoped.jjtGetChild(0);
        assertTokenTypes(identifier, ParserConstants.IDENTIFIER);
        assertTokenImages(identifier, "x");

        ASTAssignmentOperator assignmentOperator = (ASTAssignmentOperator) assignment.jjtGetChild(1);
        assertChildTypes(assignmentOperator);
        assertTokenTypes(assignmentOperator, ParserConstants.ASSIGN);
        assertTokenImages(assignmentOperator, "=");

        ASTArithmeticExpression arithmeticExpression = (ASTArithmeticExpression) assignment.jjtGetChild(2);
        assertChildTypes(arithmeticExpression, ASTConstant.class, ASTArithmeticOperator.class, ASTArithmeticExpression.class);

        ASTConstant constant = (ASTConstant) arithmeticExpression.jjtGetChild(0);
        assertChildTypes(constant);
        assertTokenTypes(constant, ParserConstants.DECIMAL_LITERAL);
        assertTokenImages(constant, "1");

        ASTArithmeticOperator operator = (ASTArithmeticOperator) arithmeticExpression.jjtGetChild(1);
        assertChildTypes(operator);
        assertTokenTypes(operator, ParserConstants.PLUS);
        assertTokenImages(operator, "+");

        arithmeticExpression = (ASTArithmeticExpression) arithmeticExpression.jjtGetChild(2);
        assertChildTypes(arithmeticExpression, ASTConstant.class, ASTArithmeticOperator.class, ASTConstant.class);

        constant = (ASTConstant) arithmeticExpression.jjtGetChild(0);
        assertChildTypes(constant);
        assertTokenTypes(constant, ParserConstants.DECIMAL_LITERAL);
        assertTokenImages(constant, "5");

        operator = (ASTArithmeticOperator) arithmeticExpression.jjtGetChild(1);
        assertChildTypes(operator);
        assertTokenTypes(operator, ParserConstants.STAR);
        assertTokenImages(operator, "*");

        constant = (ASTConstant) arithmeticExpression.jjtGetChild(2);
        assertChildTypes(constant);
        assertTokenTypes(constant, ParserConstants.DECIMAL_LITERAL);
        assertTokenImages(constant, "6");
    }

    private void assertTokenImages(SimpleNode node, String... images)
    {
        List<Token> tokens = ASTUtil.getTokens(node);
        checkTokenCount(tokens, images);
        for (int i = 0; i < images.length; i++)
        {
            assertEquals(images[i], tokens.get(i).image);
        }
    }

    private void assertTokenTypes(SimpleNode node, Integer... types)
    {
        List<Token> tokens = ASTUtil.getTokens(node);
        checkTokenCount(tokens, types);
        for (int i = 0; i < types.length; i++)
        {
            assertEquals(types[i].intValue(), tokens.get(i).kind);
        }
    }

    private void checkTokenCount(List<Token> tokens, Object[] types)
    {
        if (types.length != tokens.size())
        {
            fail("Expected "
                    + types.length
                    + " tokens ("
                    + Arrays.asList(types)
                    + ") but was "
                    + tokens.size()
                    + " ("
                    + tokens
                    + ")");
        }
    }

    private void assertChildTypes(SimpleNode parent, Class< ? extends SimpleNode>... children)
    {
        if (children.length != parent.jjtGetNumChildren())
        {
            Class< ? >[] actualChildren = new Class[parent.jjtGetNumChildren()];
            for (int i = 0; i < actualChildren.length; i++)
            {
                actualChildren[i] = parent.jjtGetChild(i).getClass();
            }
            fail("Expected "
                    + children.length
                    + " children ("
                    + Arrays.asList(children)
                    + ") but was "
                    + actualChildren.length
                    + " ("
                    + Arrays.asList(actualChildren)
                    + ")");
        }
        for (int i = 0; i < children.length; i++)
        {
            assertEquals(children[i], parent.jjtGetChild(i).getClass());
        }
    }
}
