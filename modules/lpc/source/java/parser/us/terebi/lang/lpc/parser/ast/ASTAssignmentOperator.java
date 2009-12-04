package us.terebi.lang.lpc.parser.ast;

import us.terebi.lang.lpc.parser.jj.Parser;
import us.terebi.lang.lpc.parser.jj.ParserConstants;
import us.terebi.lang.lpc.parser.jj.Token;

public class ASTAssignmentOperator extends OperatorNode
{
    public ASTAssignmentOperator(int id)
    {
        super(id);
    }

    public ASTAssignmentOperator(Parser p, int id)
    {
        super(p, id);
    }

    public ASTAssignmentOperator()
    {
        this(ParserTreeConstants.JJTASSIGNMENTOPERATOR);
        this.jjtSetFirstToken(new Token(ParserConstants.ASSIGN, "="));
    }

    /** Accept the visitor. **/
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }
}
