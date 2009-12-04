package us.terebi.lang.lpc.compiler.util;

import us.terebi.lang.lpc.compiler.CompileException;
import us.terebi.lang.lpc.parser.ast.ASTClassBody;
import us.terebi.lang.lpc.parser.ast.ASTDeclaration;
import us.terebi.lang.lpc.parser.ast.ASTFields;
import us.terebi.lang.lpc.parser.ast.ASTMethod;
import us.terebi.lang.lpc.parser.ast.ParserVisitor;
import us.terebi.lang.lpc.parser.util.ASTUtil;
import us.terebi.lang.lpc.parser.util.BaseASTVisitor;

/**
 * 
 */
public final class DelegatingDeclarationVisitor extends BaseASTVisitor
{
    private final ParserVisitor _classWriter;
    private final ParserVisitor _fieldWriter;
    private final ParserVisitor _methodWriter;

    public DelegatingDeclarationVisitor(ParserVisitor classWriter, ParserVisitor fieldWriter, ParserVisitor methodWriter)
    {
        _classWriter = classWriter;
        _fieldWriter = fieldWriter;
        _methodWriter = methodWriter;
    }

    public Object visit(ASTDeclaration node, Object data)
    {
        if (ASTUtil.hasChildType(ASTMethod.class, node))
        {
            return _methodWriter.visit(node, data);
        }
        if (ASTUtil.hasChildType(ASTFields.class, node))
        {
            return _fieldWriter.visit(node, data);
        }
        if (ASTUtil.hasChildType(ASTClassBody.class, node))
        {
            return _classWriter.visit(node, data);
        }
        throw new CompileException(node, "Internal Error - Unknown declaration type");
    }
}
