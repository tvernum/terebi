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

package us.terebi.lang.lpc.parser;

import java.util.ArrayList;
import java.util.List;

import us.terebi.lang.lpc.parser.ast.PragmaNode;
import us.terebi.lang.lpc.parser.ast.TokenNode;
import us.terebi.lang.lpc.parser.jj.ParserConstants;
import us.terebi.lang.lpc.parser.jj.Token;
import us.terebi.lang.lpc.parser.util.ASTUtil;

/**
 * 
 */
public class PragmaResolver
{
    public void resolve(PragmaNode node)
    {
        resolve(node, node);
    }

    private void resolve(PragmaNode owner, TokenNode node)
    {
        List<Token> tokens = ASTUtil.getTokens(node);
        for (Token token : tokens)
        {
            resolve(owner, token);
        }

        for (TokenNode child : ASTUtil.children(node))
        {
            if (child instanceof PragmaNode)
            {
                resolve((PragmaNode) child);
            }
            else
            {
                resolve(owner, child);
            }
        }
    }

    private void resolve(PragmaNode owner, Token token)
    {
        if (token.specialToken == null)
        {
            return;
        }

        List<Token> pragma = new ArrayList<Token>();
        for (Token special = token.specialToken; special != null; special = special.specialToken)
        {
            if (special.kind == ParserConstants.PRAGMA_ARGUMENT)
            {
                pragma.add(0, special);
            }
        }

        if (!pragma.isEmpty())
        {
            owner.addPragma(pragma);
        }
    }
}
