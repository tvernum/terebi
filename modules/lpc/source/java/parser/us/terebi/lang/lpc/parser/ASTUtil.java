/* ------------------------------------------------------------------------
 * $Id$
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

import java.io.PrintStream;

import us.terebi.lang.lpc.parser.ast.Node;
import us.terebi.lang.lpc.parser.ast.SimpleNode;
import us.terebi.lang.lpc.parser.jj.ParserConstants;
import us.terebi.lang.lpc.parser.jj.Token;

/**
 */
public class ASTUtil
{
    public static void printTree(SimpleNode node)
    {
        printTree(System.out, node, "");
    }

    public static void printTree(PrintStream out, Node node, String prefix)
    {
        out.print(prefix);
        out.print(node);

        if (node instanceof SimpleNode)
        {
            printTokenStream(out, (SimpleNode) node);
        }

        prefix += " ";
        for (int i = 0; i < node.jjtGetNumChildren(); i++)
        {
            printTree(out, node.jjtGetChild(i), prefix);
        }
    }

    private static void printTokenStream(PrintStream out, SimpleNode node)
    {
        Token firstToken = node.jjtGetFirstToken();
        Token lastToken = node.jjtGetLastToken();

        out.print(" [");
        if (lastToken != null && firstToken != null && isOrdered(firstToken, lastToken))
        {
            for (Token t = firstToken; t != null; t = t.next)
            {
                out.print(' ');
                out.print(t.image);
                if (t == lastToken)
                {
                    break;
                }
            }
        }
        out.println(" ]");
    }

    private static boolean isOrdered(Token... tokens)
    {
        for (int i = 1; i < tokens.length; i++)
        {
            Token first = tokens[i - 1];
            Token second = tokens[i];
            if (first.beginLine < second.beginLine)
            {
                continue;
            }
            if (first.beginLine > second.beginLine)
            {
                return false;
            }
            if (first.beginColumn > second.beginColumn)
            {
                return false;
            }
        }
        return true;
    }

    private static String describe(Token token)
    {
        return "'" + token.image + "'@" + token.beginLine + "." + token.beginColumn;
    }

}
