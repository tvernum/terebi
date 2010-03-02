package us.terebi.lang.lpc.parser.ast;

import us.terebi.lang.lpc.parser.jj.*;

public class ASTAssignmentExpression extends ExpressionNode
{
    public ASTAssignmentExpression(int id)
    {
        super(id);
    }

    public ASTAssignmentExpression(Parser p, int id)
    {
        super(p, id);
    }

    public ASTAssignmentExpression()
    {
        this(ParserTreeConstants.JJTASSIGNMENTEXPRESSION);
    }

    public ASTAssignmentExpression(String var, ExpressionNode expr)
    {
        this();
        jjtAddChild(expr, 2);
        jjtAddChild(new ASTAssignmentOperator(), 1);
        jjtAddChild(new ASTVariableReference(var, false), 0);
    }

    /** Accept the visitor. **/
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }

    public TokenNode getLeftNode()
    {
        return (TokenNode) this.jjtGetChild(0);
    }

    public OperatorNode getOperatorNode()
    {
        return (OperatorNode) this.jjtGetChild(1);
    }

    public TokenNode getRightNode()
    {
        return (TokenNode) this.jjtGetChild(2);
    }
}
