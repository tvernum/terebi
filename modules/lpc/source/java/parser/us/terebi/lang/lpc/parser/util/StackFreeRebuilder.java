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

package us.terebi.lang.lpc.parser.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import us.terebi.lang.lpc.parser.ast.ASTObjectDefinition;
import us.terebi.lang.lpc.parser.ast.ASTVariableAssignment;
import us.terebi.lang.lpc.parser.ast.ASTVariableDeclaration;
import us.terebi.lang.lpc.parser.ast.ASTVariableReference;
import us.terebi.lang.lpc.parser.ast.ExpressionNode;
import us.terebi.lang.lpc.parser.ast.Node;
import us.terebi.lang.lpc.parser.ast.StackFree;
import us.terebi.lang.lpc.parser.ast.StatementNode;
import us.terebi.lang.lpc.parser.ast.TokenNode;
import us.terebi.lang.lpc.runtime.jvm.type.Types;

/**
 * 
 */
public class StackFreeRebuilder
{
    private int _variableCount;

    public StackFreeRebuilder()
    {
        _variableCount = 0;
    }

    public ASTObjectDefinition rebuild(ASTObjectDefinition ast)
    {
        Collection<StackFree> stackFreeList = ASTUtil.findDescendants(StackFree.class, ast);
        for (StackFree stackFree : stackFreeList)
        {
            StatementNode stmt = findStatement(stackFree);
            List<Node> children = rebuildNode(stmt);
            this.replace(stmt, children);
        }
        return ast;
    }

    private StatementNode findStatement(Node node)
    {
        while (node != null && !(node instanceof StatementNode))
        {
            node = node.jjtGetParent();
        }
        return (StatementNode) node;
    }

    private void replace(StatementNode stmt, List<Node> replacement)
    {
        Node parent = stmt.jjtGetParent();
        List<Node> children = new ArrayList<Node>(parent.jjtGetNumChildren() + replacement.size());
        for (TokenNode child : ASTUtil.children(parent))
        {
            if (child == stmt)
            {
                children.addAll(replacement);
            }
            else
            {
                children.add(child);
            }
        }

        int i = 0;
        for (Node child : children)
        {
            parent.jjtAddChild(child, i);
            child.jjtSetParent(parent);
            i++;
        }
    }

    private List<Node> rebuildNode(Node node)
    {
        List<Node> list = new ArrayList<Node>();
        rebuildNode(node, list);
        return list;
    }

    private void rebuildNode(Node node, List<Node> block)
    {
        // @TODO - No need to rebuild after the last StackFree
        // @TODO - There's a bit too much voodoo going on here - need to make this work clearly and reliably
        if (ASTUtil.hasDescendant(StackFree.class, node))
        {
            List<TokenNode> children = new ArrayList<TokenNode>();
            addAll(children, ASTUtil.children(node));
            for (TokenNode child : children)
            {
                rebuildNode(child, block);
            }
            if (node instanceof StatementNode)
            {
                block.add(node);
            }
        }

        if (node instanceof ASTVariableAssignment)
        {
            return;
        }
        if (node instanceof ASTVariableReference && ((ASTVariableReference) node).isInternal())
        {
            return;
        }
        if (node instanceof ExpressionNode)
        {
            String var = "rebuild$" + (++_variableCount);

            Node parent = node.jjtGetParent();
            ASTVariableReference replace = new ASTVariableReference(var, true);
            int count = ASTUtil.replace(parent, node, replace);
            if (count == 0)
            {
                throw new IllegalStateException("Node " + node + " not found in " + parent);
            }
            block.add(new ASTVariableDeclaration(Types.MIXED, var, (ExpressionNode) node, true));
        }
        else
        {
            return;
        }
    }

    private <T> void addAll(Collection< ? super T> collection, Iterable< ? extends T> iterable)
    {
        for (T element : iterable)
        {
            collection.add(element);
        }
    }

}
