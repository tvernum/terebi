package us.terebi.lang.lpc.parser.ast;

import us.terebi.lang.lpc.parser.jj.*;

public class ASTControlStatement extends StatementNode
{
    public ASTControlStatement(int id)
    {
        super(id);
    }

    public ASTControlStatement(Parser p, int id)
    {
        super(p, id);
    }

    /** Accept the visitor. **/
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }

    public int getControlType()
    {
        return jjtGetFirstToken().kind;
    }
    
    public String toString()
    {
        return super.toString() + " (" + jjtGetFirstToken().image + ")";
    }
    
}
