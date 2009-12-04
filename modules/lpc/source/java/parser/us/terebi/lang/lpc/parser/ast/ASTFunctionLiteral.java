package us.terebi.lang.lpc.parser.ast;

import us.terebi.lang.lpc.parser.jj.*;

public class ASTFunctionLiteral extends LiteralNode
{
    public ASTFunctionLiteral(int id)
    {
        super(id);
    }

    public ASTFunctionLiteral(Parser p, int id)
    {
        super(p, id);
    }

    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }

    public boolean isBlock()
    {
        return jjtGetNumChildren() == 2;
    }

    public ASTParameterDeclarations getBlockSignature()
    {
        assert jjtGetNumChildren() == 2;
        return (ASTParameterDeclarations) jjtGetChild(0);
    }

    public ASTStatementBlock getBlockBody()
    {
        assert jjtGetNumChildren() == 2;
        return (ASTStatementBlock) jjtGetChild(1);
    }

    public ExpressionNode getExpressionBody()
    {
        assert jjtGetNumChildren() == 1;
        return (ExpressionNode) jjtGetChild(0);
    }
}
