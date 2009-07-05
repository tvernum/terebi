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

import us.terebi.lang.lpc.compiler.java.context.CompileContext;
import us.terebi.lang.lpc.parser.ast.ASTArrayStar;
import us.terebi.lang.lpc.parser.ast.ASTClassBody;
import us.terebi.lang.lpc.parser.ast.ASTIdentifier;
import us.terebi.lang.lpc.parser.ast.ASTType;
import us.terebi.lang.lpc.parser.ast.ASTUtil;
import us.terebi.lang.lpc.parser.ast.ASTVariable;
import us.terebi.lang.lpc.parser.ast.ASTVariableDeclaration;
import us.terebi.lang.lpc.parser.ast.ParserVisitor;
import us.terebi.lang.lpc.parser.jj.ParserConstants;
import us.terebi.lang.lpc.parser.jj.Token;
import us.terebi.lang.lpc.runtime.ClassDefinition;
import us.terebi.lang.lpc.runtime.FieldDefinition;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.MemberDefinition;
import us.terebi.lang.lpc.runtime.jvm.LpcMember;
import us.terebi.lang.lpc.runtime.util.DynamicClassDefinition;

/**
 * 
 */
public class ClassWriter extends MemberWriter implements ParserVisitor
{
    public ClassWriter(CompileContext context)
    {
        super(context);
    }

    @SuppressWarnings("unchecked")
    public Object visit(ASTClassBody node, Object data)
    {

        ASTType type = getType();
        Token token = type.jjtGetFirstToken();
        assert (token.kind == ParserConstants.CLASS);
        token = token.next;
        String className = token.image;

        ClassDefinition classDefinition = new DynamicClassDefinition(className, getModifiers(MemberDefinition.Kind.CLASS));

        PrintWriter writer = getWriter();
        writer.println();
        writer.println("/* Class " + className + " (Line:" + token.beginLine + ") */");

        writer.print("@");
        writer.print(LpcMember.class.getName());
        writer.print("(name=\"");
        writer.print(className);
        writer.print("\", modifiers={");
        writer.print(getModifierList(classDefinition.getModifiers()));
        writer.println("})");

        writer.print("public class cls_");
        writer.print(className);
        writer.println(" extends LpcClassObject {");

        writer.print("public cls_");
        writer.print(className);
        writer.println("() { super( getObjectDefinition() ); }");

        node.childrenAccept(this, classDefinition);
        writer.println("} /* class " + className + " */");
        writer.println();

        getContext().classes().defineClass(classDefinition);

        return null;
    }

    public Object visit(ASTVariableDeclaration node, Object data)
    {
        ASTType typeNode = ASTUtil.getChild(ASTType.class, node);
        assert (typeNode != null);

        Object[] info = new Object[] { data, typeNode };

        for (int i = 1; i < node.jjtGetNumChildren(); i++)
        {
            node.jjtGetChild(i).jjtAccept(this, info);
        }

        return null;
    }

    public Object visit(ASTVariable node, Object data)
    {
        assert (data instanceof Object[]);
        Object[] info = (Object[]) data;
        DynamicClassDefinition classDefinition = (DynamicClassDefinition) info[0];
        ASTType typeNode = (ASTType) info[1];

        boolean array = ASTUtil.hasChildType(ASTArrayStar.class, node);
        LpcType type = new TypeWriter(getContext()).getType(typeNode, array);

        ASTIdentifier identifier = ASTUtil.getChild(ASTIdentifier.class, node);

        String name = ASTUtil.getImage(identifier);
        FieldDefinition field = new CompileTimeField(classDefinition, name, type);
        classDefinition.addField(field);

        new VariableWriter(getContext(), typeNode).writeField(node, "");

        return null;
    }

    public String getInternalName(ClassDefinition classDefinition)
    {
        return "cls_" + classDefinition.getName();
    }

}
