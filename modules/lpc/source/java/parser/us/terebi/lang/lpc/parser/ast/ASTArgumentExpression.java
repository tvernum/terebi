package us.terebi.lang.lpc.parser.ast;

import us.terebi.lang.lpc.parser.jj.Parser;
import us.terebi.lang.lpc.parser.jj.ParserConstants;

public class ASTArgumentExpression extends SimpleNode
{
    public ASTArgumentExpression(int id)
    {
        super(id);
    }

    public ASTArgumentExpression(Parser p, int id)
    {
        super(p, id);
    }

    /** Accept the visitor. **/
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }

    public int getArgumentType()
    {
        final int kind = jjtGetFirstToken().kind;
        if (kind == ParserConstants.REF || kind == ParserConstants.CLASS)
        {
            return kind;
        }
        if (jjtGetNumChildren() == 2)
        {
            return ParserConstants.EXPANDO;
        }
        return 0;
    }

    public boolean hasExpander()
    {
        return getArgumentType() == ParserConstants.EXPANDO;
    }
}
