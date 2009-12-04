package us.terebi.lang.lpc.parser.ast;

import us.terebi.lang.lpc.parser.jj.*;

public class ASTConditionalStatement extends StatementNode
{
    public ASTConditionalStatement(int id)
    {
        super(id);
    }

    public ASTConditionalStatement(Parser p, int id)
    {
        super(p, id);
    }

    /** Accept the visitor. **/
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }

    public String getConditionImage()
    {
        return jjtGetFirstToken().image;
    }

    public int getConditionType()
    {
        return jjtGetFirstToken().kind;
    }
}
