package us.terebi.lang.lpc.parser.ast;

import us.terebi.lang.lpc.parser.jj.*;

public class ASTCatch extends ExpressionNode  
{
    public ASTCatch(int id)
    {
        super(id);
    }

    public ASTCatch(Parser p, int id)
    {
        super(p, id);
    }

    /** Accept the visitor. **/
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }
}
