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
import us.terebi.lang.lpc.runtime.jvm.LpcMember;
import us.terebi.lang.lpc.runtime.jvm.LpcMemberType;

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

    public VariableReference writeField(ASTVariable node, CharSequence modifiers)
    {
        return print(node, true, modifiers);
    }

    public void printLocal(ASTVariable node)
    {
        print(node, false, null);
    }

    private VariableReference print(ASTVariable node, boolean field, CharSequence modifiers)
    {
        PrintWriter writer = _context.writer();
        boolean array = ASTUtil.hasChildType(ASTArrayStar.class, node);
        ASTIdentifier identifier = ASTUtil.getChild(ASTIdentifier.class, node);
        ASTVariableAssignment assignment = ASTUtil.getChild(ASTVariableAssignment.class, node);

        if (field)
        {
            writer.println();
            writer.println("/* Field " + ASTUtil.getCompleteImage(identifier) + " (Line:" + identifier.jjtGetFirstToken().beginLine + ") */");
        }
        
        InternalVariable assignmentVariable = null;
        if (assignment != null && !field)
        {
            assignmentVariable = evaluate(assignment);
        }

        String name = identifier.jjtGetFirstToken().image;
        TypeWriter typeWriter = new TypeWriter(_context);
        LpcType type = typeWriter.getType(_type, array);

        VariableReference var = declareVariable(identifier, field, modifiers, name, type, assignmentVariable);
        if (assignment != null && field)
        {
            writer.println("{");
            assignmentVariable = evaluate(assignment);
            writer.print(var.internalName);
            writer.print(".set(");
            assignmentVariable.value(writer);
            writer.println(");");
            writer.println("}");
        }

        return var;
    }

    private InternalVariable evaluate(ASTVariableAssignment assignment)
    {
        _context.writer().println();
        Node exprNode = assignment.jjtGetChild(0);
        return new ExpressionWriter(_context).evaluate(exprNode);
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

        String javaVarType;
        String javaObjectType;
        if (var.kind == VariableLookup.Kind.FIELD)
        {
            javaVarType = javaObjectType = "LpcField";
            writer.print("public @");
            writer.print(LpcMember.class.getName());
            writer.print("(name=\"");
            writer.print(var.name);
            writer.print("\", modifiers={");
            writer.print(modifiers);
            writer.print("})\n@");
            writer.print(LpcMemberType.class.getName() + "(");
            writer.print("kind=");
            writer.print(TypeWriter.fullyQualifiedName(var.type.getKind()));
            writer.print(", depth=");
            writer.print(Integer.toString(var.type.getArrayDepth()));
            if (var.type.isClass())
            {
                writer.print(", className=\"");
                writer.print(var.type.getClassDefinition().getName());
                writer.print("\"");
            }
            writer.println(")");
        }
        else
        {
            javaObjectType = "LpcVariable";
            if (var.kind == VariableLookup.Kind.REF)
            {
                javaVarType = "LpcReference";
            }
            else
            {
                javaVarType = javaObjectType;
            }
        }
        writer.print("final ");
        writer.print(javaVarType);
        writer.print(" ");
        writer.print(var.internalName);
        if (var.kind == VariableLookup.Kind.REF)
        {
            writer.println(";");
            writer.println("if( "
                    + init.name
                    + " instanceof LpcReference ) "
                    + var.internalName
                    + " = (LpcReference)"
                    + init.name
                    + ";");
            writer.print("else " + var.internalName);
        }
        writer.print(" = new ");
        writer.print(javaObjectType);
        writer.print("( \"");
        writer.print(var.name);
        writer.print("\", ");
        typeWriter.printType(var.type);

        if (init != null && var.kind != VariableLookup.Kind.FIELD)
        {
            writer.print(", ");
            init.value(writer);
        }

        writer.println(");");
        return var;
    }

    public static void printModifers(PrintWriter writer, CharSequence modifiers)
    {
        writer.print("withModifiers(");
        writer.print(modifiers);
        writer.println(")");
    }

}
