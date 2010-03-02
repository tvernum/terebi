package us.terebi.lang.lpc.parser.ast;

import us.terebi.lang.lpc.parser.jj.*;
import us.terebi.lang.lpc.parser.util.ASTUtil;

public class ASTFunctionCall extends ExpressionNode
{
    public ASTFunctionCall(int id)
    {
        super(id);
    }

    public ASTFunctionCall(Parser p, int id)
    {
        super(p, id);
    }

    /** Accept the visitor. **/
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }

    public String getFunctionScope()
    {
        ASTScopeResolution resolution = ASTUtil.getChild(ASTScopeResolution.class, getScopedIdentifier());
        if (resolution == null)
        {
            return null;
        }
        else
        {
            Token token = resolution.jjtGetFirstToken();
            return (token.kind == ParserConstants.SCOPE) ? "" : token.image;
        }
    }

    public String getFunctionName()
    {
        ASTIdentifier identifier = ASTUtil.getChild(ASTIdentifier.class, getScopedIdentifier());
        return ASTUtil.getImage(identifier);
    }

    private ASTScopedIdentifier getScopedIdentifier()
    {
        ASTScopedIdentifier scoped = ASTUtil.getChild(ASTScopedIdentifier.class, this);
        return scoped;
    }

    public ASTFunctionArguments getArguments()
    {
        return ASTUtil.getChild(ASTFunctionArguments.class, this);
    }
}
