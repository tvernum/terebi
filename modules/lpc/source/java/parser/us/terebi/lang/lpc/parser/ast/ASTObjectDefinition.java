package us.terebi.lang.lpc.parser.ast;

import us.terebi.lang.lpc.parser.jj.*;

public class ASTObjectDefinition extends SimpleNode implements PragmaNode
{
    public ASTObjectDefinition(int id)
    {
        super(id);
    }

    public ASTObjectDefinition(Parser p, int id)
    {
        super(p, id);
    }

    /** Accept the visitor. **/
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }
}
