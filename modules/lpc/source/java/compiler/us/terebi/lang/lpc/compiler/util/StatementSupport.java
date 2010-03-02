/* ------------------------------------------------------------------------
 * Copyright 2009 Tim Vernum
 * ------------------------------------------------------------------------
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ------------------------------------------------------------------------
 */

package us.terebi.lang.lpc.compiler.util;

import org.apache.log4j.Logger;

import us.terebi.lang.lpc.compiler.CompileException;
import us.terebi.lang.lpc.compiler.java.context.ScopeLookup;
import us.terebi.lang.lpc.parser.ast.ASTStatementBlock;
import us.terebi.lang.lpc.parser.ast.Node;
import us.terebi.lang.lpc.parser.ast.ParserVisitor;
import us.terebi.lang.lpc.parser.ast.SimpleNode;
import us.terebi.lang.lpc.parser.ast.TokenNode;
import us.terebi.lang.lpc.parser.util.ASTUtil;

/**
 * 
 */
public class StatementSupport
{
    private final Logger LOG = Logger.getLogger(StatementSupport.class);
    
    private final ScopeLookup _scope;
    private final ParserVisitor _visitor;

    public StatementSupport(ScopeLookup scope, ParserVisitor visitor)
    {
        _scope = scope;
        _visitor = visitor;
    }

    public StatementResult handleBlock(ASTStatementBlock node)
    {
        _scope.variables().pushScope();
        StatementResult last = StatementResult.NON_TERMINAL;
        for (TokenNode child : ASTUtil.children(node))
        {
            if (last.isTerminated())
            {
                LOG.info("At " + child + " : Unreachable statement");
            }
            last = processStatement(child);
            if (last == null)
            {
                throw new CompileException(child, "Internal Error - No Statement Result from visiting " + child);
            }
        }
        _scope.variables().popScope();
        return last;
    }
    
    private StatementResult processStatement(Node stmt)
    {
        Object result = stmt.jjtAccept(_visitor, null);
        if (result == null)
        {
            throw new CompileException((SimpleNode) stmt, "Internal Error - No Statement Result from visiting " + stmt);
        }
        return (StatementResult) result;
    }
}
