package us.terebi.lang.lpc.parser.ast;

import us.terebi.lang.lpc.parser.jj.*;

public class ASTExpressionStatement extends StatementNode
{
    public ASTExpressionStatement(int id)
    {
        super(id);
    }

    public ASTExpressionStatement(Parser p, int id)
    {
        super(p, id);
    }

    public ASTExpressionStatement(ExpressionNode expression)
    {
        this(ParserTreeConstants.JJTEXPRESSIONSTATEMENT);
        jjtAddChild(expression, 0);
    }

    /** Accept the visitor. **/
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }
}
