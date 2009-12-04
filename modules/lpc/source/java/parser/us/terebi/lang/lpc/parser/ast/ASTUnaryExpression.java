package us.terebi.lang.lpc.parser.ast;

import us.terebi.lang.lpc.parser.jj.*;

public class ASTUnaryExpression extends ExpressionNode
{
    public ASTUnaryExpression(int id)
    {
        super(id);
    }

    public ASTUnaryExpression(Parser p, int id)
    {
        super(p, id);
    }

    /** Accept the visitor. **/
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }
}
