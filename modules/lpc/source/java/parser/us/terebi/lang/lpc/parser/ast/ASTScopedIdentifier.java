package us.terebi.lang.lpc.parser.ast;

import us.terebi.lang.lpc.parser.jj.Parser;
import us.terebi.lang.lpc.parser.util.ASTUtil;

public class ASTScopedIdentifier extends SimpleNode
{
    public ASTScopedIdentifier(int id)
    {
        super(id);
    }

    public ASTScopedIdentifier(Parser p, int id)
    {
        super(p, id);
    }

    public ASTScopedIdentifier(String var)
    {
        this(ParserTreeConstants.JJTSCOPEDIDENTIFIER);
        ASTIdentifier identifier = new ASTIdentifier(var);
        this.jjtAddChild(identifier, 0);
        identifier.jjtSetParent(this);
    }

    /** Accept the visitor. **/
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }

    public String getScope()
    {
        ASTScopeResolution resolution = ASTUtil.getChild(ASTScopeResolution.class, this);
        return (resolution == null) ? null : (resolution.jjtGetNumChildren() == 0) ? "" : ASTUtil.getImage(resolution);
    }

    public String getVariableName()
    {
        ASTIdentifier identifier = getIdentifier();
        return identifier.getIdentifierName();
    }

    public ASTIdentifier getIdentifier()
    {
        ASTIdentifier identifier = ASTUtil.getChild(ASTIdentifier.class, this);
        return identifier;
    }
}
