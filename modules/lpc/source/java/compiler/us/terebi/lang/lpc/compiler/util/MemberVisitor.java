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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.adjective.stout.operation.Statement;

import us.terebi.lang.lpc.compiler.CompileException;
import us.terebi.lang.lpc.compiler.java.context.ScopeLookup;
import us.terebi.lang.lpc.parser.ast.ASTArrayStar;
import us.terebi.lang.lpc.parser.ast.ASTDeclaration;
import us.terebi.lang.lpc.parser.ast.ASTModifiers;
import us.terebi.lang.lpc.parser.ast.ASTType;
import us.terebi.lang.lpc.parser.ast.TokenNode;
import us.terebi.lang.lpc.parser.jj.ParserConstants;
import us.terebi.lang.lpc.parser.jj.Token;
import us.terebi.lang.lpc.parser.util.ASTUtil;
import us.terebi.lang.lpc.parser.util.BaseASTVisitor;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.MemberDefinition;
import us.terebi.lang.lpc.runtime.MemberDefinition.Kind;
import us.terebi.lang.lpc.runtime.MemberDefinition.Modifier;

/**
 * 
 */
public class MemberVisitor extends BaseASTVisitor
{
    private final ScopeLookup _scope;

    private transient ASTModifiers _modifiers;
    private transient ASTType _type;
    private transient boolean _array;

    public MemberVisitor(ScopeLookup scope)
    {
        _scope = scope;
        _array = false;
    }

    public ScopeLookup getScope()
    {
        return _scope;
    }

    public ASTType getTypeNode()
    {
        return _type;
    }

    public LpcType getType()
    {
        return new TypeSupport(_scope, _type, _array).getType();
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

    @SuppressWarnings("unchecked")
    public Object visit(ASTDeclaration node, Object data)
    {
        List<Statement> statements = null;
        for (TokenNode child : ASTUtil.children(node))
        {
            final Object result = child.jjtAccept(this, data);
            if (result != null)
            {
                if (statements == null)
                {
                    statements = new ArrayList<Statement>();
                }
                statements.addAll((List<Statement>) result);
            }
        }
        return statements;
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

    protected Set< ? extends Modifier> getModifiers(Kind kind)
    {
        Set<Modifier> modifiers = new HashSet<Modifier>();
        for (Token token : ASTUtil.getTokens(_modifiers))
        {
            Modifier modifier = getModifier(token, kind);
            modifiers.add(modifier);
        }
        return modifiers;
    }

    private Modifier getModifier(Token token, Kind kind)
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
                switch (kind)
                {
                    case FIELD:
                        return MemberDefinition.Modifier.NOSAVE;
                    case METHOD:
                        return MemberDefinition.Modifier.NOMASK;
                }
                break;
            case ParserConstants.NOSAVE:
                if (kind == MemberDefinition.Kind.FIELD)
                {
                    return MemberDefinition.Modifier.NOSAVE;
                }
                break;
            case ParserConstants.NOMASK:
                if (kind == MemberDefinition.Kind.METHOD)
                {
                    return MemberDefinition.Modifier.NOMASK;
                }
                break;
            case ParserConstants.VARARGS:
                if (kind == MemberDefinition.Kind.METHOD)
                {
                    return MemberDefinition.Modifier.VARARGS;
                }
                break;
        }
        throw new CompileException(token, token.image + " is not a valid modifier for a " + kind.name().toLowerCase());
    }

}
