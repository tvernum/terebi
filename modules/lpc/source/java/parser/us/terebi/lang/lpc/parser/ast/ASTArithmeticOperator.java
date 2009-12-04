package us.terebi.lang.lpc.parser.ast;

import us.terebi.lang.lpc.parser.jj.*;

public
class ASTArithmeticOperator extends OperatorNode {
  public ASTArithmeticOperator(int id) {
    super(id);
  }

  public ASTArithmeticOperator(Parser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(ParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
