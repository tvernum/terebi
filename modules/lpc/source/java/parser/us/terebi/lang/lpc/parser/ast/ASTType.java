package us.terebi.lang.lpc.parser.ast;

import us.terebi.lang.lpc.parser.jj.*;
import us.terebi.lang.lpc.parser.util.ASTUtil;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcType.Kind;

public class ASTType extends SimpleNode
{
    public ASTType(int id)
    {
        super(id);
    }

    public ASTType(Parser p, int id)
    {
        super(p, id);
    }

    public ASTType(LpcType type)
    {
        this(ParserTreeConstants.JJTTYPE);

        Kind kind = type.getKind();
        Token token = new Token(getTokenType(kind), kind.name().toLowerCase());
        this.jjtSetFirstToken(token);

        if (type.isClass())
        {
            token.next = new Token(ParserConstants.IDENTIFIER, type.getClassDefinition().getName());
            token = token.next;
        }

        for (int i = 0; i < type.getArrayDepth(); i++)
        {
            token.next = new Token(ParserConstants.ARRAY, "array");
            token = token.next;
        }
        
        this.jjtSetLastToken(token);
    }

    private int getTokenType(Kind kind)
    {
        switch (kind)
        {
            case BUFFER:
                return ParserConstants.BUFFER;
            case CLASS:
                return ParserConstants.CLASS;
            case FLOAT:
                return ParserConstants.FLOAT;
            case FUNCTION:
                return ParserConstants.FUNCTION;
            case INT:
                return ParserConstants.INT;
            case MAPPING:
                return ParserConstants.MAPPING;
            case MIXED:
                return ParserConstants.MIXED;
            case OBJECT:
                return ParserConstants.OBJECT;
            case STRING:
                return ParserConstants.STRING;
            case VOID:
                return ParserConstants.VOID;
        }
        throw new IllegalArgumentException("Cannot create " + kind + " type in the AST");
    }

    /** Accept the visitor. **/
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }
    
    public String toString()
    {
        return super.toString() + " - " + ASTUtil.getCompleteImage(this);
    }
}
