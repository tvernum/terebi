package us.terebi.lang.lpc.parser.ast;

import us.terebi.lang.lpc.parser.jj.*;
import us.terebi.lang.lpc.parser.util.ASTUtil;

public class ASTIdentifier extends SimpleNode
{
    public ASTIdentifier(int id)
    {
        super(id);
    }

    public ASTIdentifier(Parser p, int id)
    {
        super(p, id);
    }

    public ASTIdentifier(String var)
    {
        this(ParserTreeConstants.JJTIDENTIFIER);
        this.jjtSetFirstToken(new Token(ParserConstants.IDENTIFIER, var));
    }

    /** Accept the visitor. **/
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }

    public String getIdentifierName()
    {
        return ASTUtil.getImage(this);
    }

    public String toString()
    {
        return super.toString() + " {" + getIdentifierName() + "}";
    }
}
