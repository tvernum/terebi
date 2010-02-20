package us.terebi.lang.lpc.parser.ast;

import java.util.List;

import us.terebi.lang.lpc.parser.jj.*;
import us.terebi.lang.lpc.parser.util.ASTUtil;

public class ASTCompoundExpression extends ExpressionNode
{
    public ASTCompoundExpression(int id)
    {
        super(id);
    }

    public ASTCompoundExpression(Parser p, int id)
    {
        super(p, id);
    }

    /** Accept the visitor. **/
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }

    public List<ExpressionNode> getSubsequentExpressions()
    {
        return ASTUtil.<ExpressionNode> childList(this, 1);
    }

    public ExpressionNode getFirstExpression()
    {
        return (ExpressionNode) jjtGetChild(0);
    }
}
