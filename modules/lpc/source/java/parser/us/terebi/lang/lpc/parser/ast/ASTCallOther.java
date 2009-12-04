package us.terebi.lang.lpc.parser.ast;

import us.terebi.lang.lpc.parser.jj.*;

public class ASTCallOther extends PostfixNode
{
    public ASTCallOther(int id)
    {
        super(id);
    }

    public ASTCallOther(Parser p, int id)
    {
        super(p, id);
    }

    /** Accept the visitor. **/
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }

    public ASTIdentifier getIdentifier()
    {
        return (ASTIdentifier) jjtGetChild(0);
    }

    public ASTFunctionArguments getArgs()
    {
        return (ASTFunctionArguments) jjtGetChild(1);
    }
}
