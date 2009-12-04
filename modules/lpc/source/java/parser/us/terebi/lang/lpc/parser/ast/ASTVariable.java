package us.terebi.lang.lpc.parser.ast;

import us.terebi.lang.lpc.parser.jj.Parser;
import us.terebi.lang.lpc.parser.util.ASTUtil;

public class ASTVariable extends SimpleNode
{
    public ASTVariable(int id)
    {
        super(id);
    }

    public ASTVariable(Parser p, int id)
    {
        super(p, id);
    }

    public ASTVariable(String name, ExpressionNode value)
    {
        this(ParserTreeConstants.JJTVARIABLE);
        addChild(new ASTVariableAssignment(value), 1);
        addChild(new ASTIdentifier(name), 0);
    }

    /** Accept the visitor. **/
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }

    public boolean isArray()
    {
        return ASTUtil.hasChildType(ASTArrayStar.class, this);
    }

    public String getVariableName()
    {
        ASTIdentifier identifier = ASTUtil.getChild(ASTIdentifier.class, this);
        return identifier.getIdentifierName();
    }

    public ASTVariableAssignment getAssignment()
    {
        return ASTUtil.getChild(ASTVariableAssignment.class, this);
    }
}
