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

package us.terebi.lang.lpc.parser.ast;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
        out.print(" [");
        List<Token> tokens = getTokens(node);
        for (Token t : tokens)
        {
            out.print(' ');
            out.print(t.image);
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

    public static String describe(Token token)
    {
        return "'" + token.image + "'@" + token.beginLine + "." + token.beginColumn;
    }

    public static List<Token> getTokens(SimpleNode node)
    {
        Token firstToken = node.jjtGetFirstToken();
        Token lastToken = node.jjtGetLastToken();
        List<Token> tokens = new ArrayList<Token>();

        if (lastToken != null && firstToken != null && isOrdered(firstToken, lastToken))
        {
            for (Token t = firstToken; t != null; t = t.next)
            {
                tokens.add(t);
                if (t == lastToken)
                {
                    break;
                }
            }
        }
        return tokens;
    }

    public static Iterable<SimpleNode> children(final SimpleNode parent)
    {
        return new Iterable<SimpleNode>()
        {
            public Iterator<SimpleNode> iterator()
            {
                return new NodeIterator(parent);
            }
        };
    }

    public static String describe(SimpleNode node)
    {
        List<Token> tokens = getTokens(node);
        if (tokens.isEmpty())
        {
            return "Empty node (" + node + ") - child of " + describe((SimpleNode) node.parent);
        }
        else
        {
            return "Node (" + node + ") : " + getCompleteImage(node);
        }
    }

    public static <N extends Node> N getChild(Class<N> type, SimpleNode node)
    {
        for (Node child : children(node))
        {
            if (type.isInstance(child))
            {
                return type.cast(child);
            }
        }
        return null;
    }

    public static boolean hasChildType(Class< ? extends Node> type, SimpleNode node)
    {
        return getChild(type, node) != null;
    }

    public static boolean hasTokenKind(SimpleNode node, int kind)
    {
        for (Token token : getTokens(node))
        {
            if (token.kind == kind)
            {
                return true;
            }
        }
        return false;
    }

    public static String getImage(SimpleNode node)
    {
        if (node == null)
        {
            return null;
        }
        return node.jjtGetFirstToken().image;
    }

    public static <N extends SimpleNode> Collection<N> findDescendants(Class<N> type, SimpleNode parent)
    {
        Set<N> set = new HashSet<N>();
        findDescendants(type, children(parent), set);
        return set;
    }

    public static <N extends SimpleNode> void findDescendants(Class<N> type, Iterable<SimpleNode> of, Set<N> set)
    {
        for (SimpleNode node : of)
        {
            if (type.isInstance(node))
            {
                set.add(type.cast(node));
            }
            findDescendants(type, children(node), set);
        }
    }

    public static <N extends SimpleNode> List< ? extends N> getChildren(SimpleNode parent, Class< ? extends N>... types)
    {
        List<N> list = new ArrayList<N>();
        for (SimpleNode node : children(parent))
        {
            for (Class< ? extends N> type : types)
            {
                if (type.isInstance(node))
                {
                    list.add(type.cast(node));
                    break;
                }
            }
        }
        return list;
    }

    public static CharSequence getCompleteImage(SimpleNode node)
    {
        if (node == null)
        {
            return "<<no node>>";
        }

        StringBuilder builder = new StringBuilder();
        for (Token token : getTokens(node))
        {
            builder.append(token.image);
            builder.append(' ');
        }
        if (builder.length() > 0)
        {
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder;
    }

}
