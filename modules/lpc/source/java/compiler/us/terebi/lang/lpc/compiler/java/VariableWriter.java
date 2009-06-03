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

package us.terebi.lang.lpc.compiler.java;

import java.io.PrintWriter;

import us.terebi.lang.lpc.compiler.CompileException;
import us.terebi.lang.lpc.compiler.java.context.CompileContext;
import us.terebi.lang.lpc.compiler.java.context.VariableLookup;
import us.terebi.lang.lpc.compiler.java.context.VariableLookup.VariableReference;
import us.terebi.lang.lpc.parser.ast.ASTArrayStar;
import us.terebi.lang.lpc.parser.ast.ASTIdentifier;
import us.terebi.lang.lpc.parser.ast.ASTType;
import us.terebi.lang.lpc.parser.ast.ASTUtil;
import us.terebi.lang.lpc.parser.ast.ASTVariable;
import us.terebi.lang.lpc.parser.ast.ASTVariableAssignment;
import us.terebi.lang.lpc.parser.ast.Node;
import us.terebi.lang.lpc.parser.ast.SimpleNode;
import us.terebi.lang.lpc.runtime.LpcType;

/**
 * 
 */
public class VariableWriter
{
    private final CompileContext _context;
    private final ASTType _type;

    public VariableWriter(CompileContext context, ASTType type)
    {
        _context = context;
        _type = type;
    }

    public void writeField(ASTVariable node, CharSequence modifiers)
    {
        print(node, true, modifiers);
    }

    public void printLocal(ASTVariable node)
    {
        print(node, false, null);
    }

    private void print(ASTVariable node, boolean field, CharSequence modifiers)
    {
        PrintWriter writer = _context.writer();

        boolean array = ASTUtil.hasChildType(ASTArrayStar.class, node);
        ASTIdentifier identifier = ASTUtil.getChild(ASTIdentifier.class, node);
        ASTVariableAssignment assignment = ASTUtil.getChild(ASTVariableAssignment.class, node);

        InternalVariable assignmentVariable = null;
        if (assignment != null)
        {
            writer.println();
            Node exprNode = assignment.jjtGetChild(0);
            assignmentVariable = new ExpressionWriter(_context).evaluate(exprNode);
        }

        String name = identifier.jjtGetFirstToken().image;
        TypeWriter typeWriter = new TypeWriter(_context);
        LpcType type = typeWriter.getType(_type, array);

        declareVariable(identifier, field, modifiers, name, type, assignmentVariable);
    }

    public VariableReference declareLocalVariable(SimpleNode node, String name, LpcType type, InternalVariable init)
    {
        return this.declareVariable(node, false, null, name, type, init);
    }

    public VariableReference declareVariable(SimpleNode node, boolean field, CharSequence modifiers, String name, LpcType type,
            InternalVariable assignmentVariable)
    {
        VariableReference existing = _context.variables().getVariableInFrame(name);
        if (existing != null)
        {
            throw new CompileException(node, "Variable " + name + " is already declared (as type " + existing.type + ")");
        }

        VariableReference var;
        if (field)
        {
            var = _context.variables().declareField(name, type);
        }
        else
        {
            var = _context.variables().declareLocal(name, type);
        }

        return printVariable(modifiers, var, assignmentVariable);
    }

    public VariableReference printVariable(CharSequence modifiers, VariableReference var, InternalVariable init)
    {
        PrintWriter writer = _context.writer();
        TypeWriter typeWriter = new TypeWriter(_context);

        String javaType;
        if (var.kind == VariableLookup.Kind.FIELD)
        {
            javaType = "LpcField";
            writer.print("public ");
        }
        else
        {
            javaType = "LpcVariable";
        }
        writer.print("final ");
        writer.print(javaType);
        writer.print(" ");
        writer.print(var.internalName);
        writer.print(" = new ");
        writer.print(javaType);
        writer.print("( \"");
        writer.print(var.name);
        writer.print("\", ");
        if (var.kind == VariableLookup.Kind.FIELD)
        {
            writer.println();
            printModifers(writer, modifiers);
            writer.print(", ");
        }
        typeWriter.printType(var.type);

        if (init != null)
        {
            writer.print(", ");
            init.value(writer);
        }

        writer.println(");");
        return var;
    }

    private void printModifers(PrintWriter writer, CharSequence modifiers)
    {
        writer.print("withModifiers(");
        writer.print(modifiers);
        writer.println(")");
    }

}
