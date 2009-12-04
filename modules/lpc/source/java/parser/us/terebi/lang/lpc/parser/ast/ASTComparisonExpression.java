package us.terebi.lang.lpc.parser.ast;

import us.terebi.lang.lpc.parser.jj.*;

public class ASTComparisonExpression extends ExpressionNode
{
    public ASTComparisonExpression(int id)
    {
        super(id);
    }

    public ASTComparisonExpression(Parser p, int id)
    {
        super(p, id);
    }

    /** Accept the visitor. **/
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }

    public int getOperator()
    {
        SimpleNode operator = (SimpleNode) jjtGetChild(1);
        return operator.jjtGetFirstToken().kind;
    }

    public String getOperatorImage()
    {
        SimpleNode operator = (SimpleNode) jjtGetChild(1);
        return operator.jjtGetFirstToken().image;
    }
}
