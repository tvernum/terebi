package us.terebi.lang.lpc.parser.ast;

import us.terebi.lang.lpc.parser.jj.*;

public class ASTArrayLiteral extends LiteralNode
{
    public ASTArrayLiteral(int id)
    {
        super(id);
    }

    public ASTArrayLiteral(Parser p, int id)
    {
        super(p, id);
    }

    /** Accept the visitor. **/
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }
}
