package us.terebi.lang.lpc.parser.ast;

import us.terebi.lang.lpc.parser.jj.*;
import us.terebi.lang.lpc.parser.util.ASTUtil;

public class ASTConstant extends LiteralNode
{
    public ASTConstant(int id)
    {
        super(id);
    }

    public ASTConstant(Parser p, int id)
    {
        super(p, id);
    }

    /** Accept the visitor. **/
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }
    
    public String toString()
    {
        return super.toString() + " {" + ASTUtil.getCompleteImage(this) + "}";
    }
}
