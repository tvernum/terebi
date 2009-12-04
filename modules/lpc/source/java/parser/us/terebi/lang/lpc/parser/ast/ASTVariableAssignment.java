package us.terebi.lang.lpc.parser.ast;

import us.terebi.lang.lpc.parser.jj.*;

public class ASTVariableAssignment extends ExpressionNode
{
    public ASTVariableAssignment(int id)
    {
        super(id);
    }

    public ASTVariableAssignment(Parser p, int id)
    {
        super(p, id);
    }

    public ASTVariableAssignment()
    {
        this(ParserTreeConstants.JJTVARIABLEASSIGNMENT);
    }

    public ASTVariableAssignment(ExpressionNode value)
    {
        this();
        addChild(value, 0);
    }

    /** Accept the visitor. **/
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }
}
