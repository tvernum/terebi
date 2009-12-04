package us.terebi.lang.lpc.parser.ast;

import us.terebi.lang.lpc.parser.jj.Parser;

public class ASTDeclaration extends SimpleNode implements PragmaNode
{
    public ASTDeclaration(int id)
    {
        super(id);
    }

    public ASTDeclaration(Parser p, int id)
    {
        super(p, id);
    }

    /** Accept the visitor. **/
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }
}
