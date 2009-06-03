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
import us.terebi.lang.lpc.parser.ast.ASTArrayStar;
import us.terebi.lang.lpc.parser.ast.ASTDeclaration;
import us.terebi.lang.lpc.parser.ast.ASTModifiers;
import us.terebi.lang.lpc.parser.ast.ASTType;
import us.terebi.lang.lpc.parser.ast.ASTUtil;
import us.terebi.lang.lpc.parser.ast.BaseASTVisitor;
import us.terebi.lang.lpc.parser.jj.ParserConstants;
import us.terebi.lang.lpc.parser.jj.Token;
import us.terebi.lang.lpc.runtime.MemberDefinition;
import us.terebi.lang.lpc.runtime.MemberDefinition.Modifier;

/**
 * 
 */
public class MemberWriter extends BaseASTVisitor
{
    private final CompileContext _context;
    private transient ASTModifiers _modifiers;
    private transient ASTType _type;
    private transient boolean _array;

    public MemberWriter(CompileContext context)
    {
        _context = context;
        _array = false;
    }

    protected CompileContext getContext()
    {
        return _context;
    }

    protected PrintWriter getWriter()
    {
        return _context.writer();
    }

    public ASTType getType()
    {
        return _type;
    }

    public boolean hasModifier(int mod)
    {
        for (Token token : ASTUtil.getTokens(_modifiers))
        {
            if (token.kind == mod)
            {
                return true;
            }
        }
        return false;
    }

    public boolean isArray()
    {
        return _array;
    }

    public Object visit(ASTDeclaration node, Object data)
    {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTModifiers node, Object data)
    {
        _modifiers = node;
        return data;
    }

    public Object visit(ASTType node, Object data)
    {
        _type = node;
        _array = false;
        return data;
    }

    public Object visit(ASTArrayStar node, Object data)
    {
        _array = true;
        return data;
    }

    protected CharSequence getModifierList(boolean field)
    {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (Token token : ASTUtil.getTokens(_modifiers))
        {
            if (first)
            {
                first = false;
            }
            else
            {
                builder.append(',');
            }
            builder.append(TypeWriter.fullyQualifiedName(getModifier(token, field)));
        }
        return builder;
    }

    private Modifier getModifier(Token token, boolean field)
    {

        switch (token.kind)
        {
            case ParserConstants.PUBLIC:
                return MemberDefinition.Modifier.PUBLIC;
            case ParserConstants.PROTECTED:
                return MemberDefinition.Modifier.PROTECTED;
            case ParserConstants.PRIVATE:
                return MemberDefinition.Modifier.PRIVATE;
            case ParserConstants.STATIC:
                if (field)
                {
                    return MemberDefinition.Modifier.NOSAVE;
                }
                else
                {
                    return MemberDefinition.Modifier.NOMASK;
                }
            case ParserConstants.NOSAVE:
                if (field)
                {
                    return MemberDefinition.Modifier.NOSAVE;
                }
                throw new CompileException(token, token.image + " is not a valid modifier for a field member");
            case ParserConstants.NOMASK:
                if (field)
                {
                    throw new CompileException(token, token.image + " is not a valid modifier for a field member");
                }
                return MemberDefinition.Modifier.NOMASK;
            case ParserConstants.VARARGS:
                if (field)
                {
                    throw new CompileException(token, token.image + " is not a valid modifier for a field member");
                }
                return MemberDefinition.Modifier.VARARGS;
            default:
                throw new CompileException(token, token.image
                        + " is not a valid modifier for a "
                        + (field ? "field" : "method")
                        + " member");
        }
    }

    protected void print(String string)
    {
        _context.writer().print(string);
    }

    protected void println(String string)
    {
        _context.writer().println(string);
    }
}
