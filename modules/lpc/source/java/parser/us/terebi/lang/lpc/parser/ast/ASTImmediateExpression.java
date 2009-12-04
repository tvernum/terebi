package us.terebi.lang.lpc.parser.ast;

import us.terebi.lang.lpc.parser.jj.*;

public class ASTImmediateExpression extends ExpressionNode
{
    public ASTImmediateExpression(int id)
    {
        super(id);
    }

    public ASTImmediateExpression(Parser p, int id)
    {
        super(p, id);
    }

    /** Accept the visitor. **/
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }

    public ExpressionNode getBody()
    {
        return (ExpressionNode) jjtGetChild(0);
    }
}
