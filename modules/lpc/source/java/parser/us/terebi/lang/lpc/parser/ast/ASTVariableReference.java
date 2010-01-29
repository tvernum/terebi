package us.terebi.lang.lpc.parser.ast;

import us.terebi.lang.lpc.compiler.util.Positional;
import us.terebi.lang.lpc.parser.jj.Parser;
import us.terebi.lang.lpc.parser.util.ASTUtil;

public class ASTVariableReference extends ExpressionNode
{
    private final boolean _internal;

    public ASTVariableReference(int id)
    {
        super(id);
        _internal = false;
    }

    public ASTVariableReference(Parser p, int id)
    {
        super(p, id);
        _internal = false;
    }

    public ASTVariableReference()
    {
        this(ParserTreeConstants.JJTVARIABLEREFERENCE);
    }

    public ASTVariableReference(String var, boolean internal)
    {
        super(ParserTreeConstants.JJTVARIABLEREFERENCE);
        _internal = internal;
        ASTScopedIdentifier identifier = new ASTScopedIdentifier(var);
        jjtAddChild(identifier, 0);
        identifier.jjtSetParent(this);
    }

    /** Accept the visitor. **/
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }

    public String getVariableName()
    {
        ASTScopedIdentifier scoped = ASTUtil.getChild(ASTScopedIdentifier.class, this);
        return scoped.getVariableName();
    }

    public String getScope()
    {
        ASTScopedIdentifier scoped = ASTUtil.getChild(ASTScopedIdentifier.class, this);
        return scoped.getScope();
    }

    public ASTIdentifier getIdentifier()
    {
        ASTScopedIdentifier scoped = ASTUtil.getChild(ASTScopedIdentifier.class, this);
        return scoped.getIdentifier();
    }

    public boolean isPositionalVariable()
    {
        return getPositional().isPositionalVariable();
    }

    public Positional getPositional()
    {
        return new Positional(getIdentifier());
    }

    public boolean isInternal()
    {
        return _internal;
    }

    public String toString()
    {
        String var = "";
        ASTScopedIdentifier scoped = ASTUtil.getChild(ASTScopedIdentifier.class, this);
        if (scoped != null)
        {
            var = scoped.getVariableName();
        }

        if (_internal)
        {
            return super.toString() + " (internal:" + var + ")";
        }
        else
        {
            return super.toString() + " (" + var + ")";
        }
    }
}
