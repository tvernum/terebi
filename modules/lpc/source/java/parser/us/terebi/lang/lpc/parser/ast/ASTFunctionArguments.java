package us.terebi.lang.lpc.parser.ast;

import us.terebi.lang.lpc.parser.jj.*;

public class ASTFunctionArguments extends SimpleNode
{
    public ASTFunctionArguments(int id)
    {
        super(id);
    }

    public ASTFunctionArguments(Parser p, int id)
    {
        super(p, id);
    }

    /** Accept the visitor. **/
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }

    public boolean hasExpander()
    {
        for (int i = 0; i < this.jjtGetNumChildren(); i++)
        {
            Node node = this.jjtGetChild(i);
            if (node instanceof ASTArgumentExpression)
            {
                final ASTArgumentExpression arg = (ASTArgumentExpression) node;
                if (arg.hasExpander())
                {
                    return true;
                }
            }
        }
        return false;
    }
}
