package us.terebi.lang.lpc.parser.ast;

import us.terebi.lang.lpc.parser.jj.Parser;
import us.terebi.lang.lpc.runtime.LpcType;

public class ASTVariableDeclaration extends StatementNode
{
    private final boolean _internal;

    public ASTVariableDeclaration(int id)
    {
        super(id);
        _internal = false;
    }

    public ASTVariableDeclaration(Parser p, int id)
    {
        super(p, id);
        _internal = false;
    }

    public ASTVariableDeclaration(LpcType type, String name, ExpressionNode value, boolean internal)
    {
        super(ParserTreeConstants.JJTVARIABLEDECLARATION);
        this._internal = internal;

        addChild(new ASTVariable(name, value), 1);
        addChild(new ASTType(type), 0);
    }

    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }

    public boolean isInternal()
    {
        return _internal;
    }

    public String toString()
    {
        if (_internal)
        {
            return super.toString() + " (internal)";
        }
        else
        {
            return super.toString();
        }
    }
}
