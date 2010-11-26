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
import java.util.List;
import java.util.Set;

import us.terebi.lang.lpc.compiler.CompileException;
import us.terebi.lang.lpc.compiler.bytecode.context.MethodInfo;
import us.terebi.lang.lpc.compiler.java.context.LookupException;
import us.terebi.lang.lpc.compiler.java.context.ScopeLookup;
import us.terebi.lang.lpc.compiler.java.context.VariableResolver.VariableResolution;
import us.terebi.lang.lpc.parser.ast.ASTElementExpander;
import us.terebi.lang.lpc.parser.ast.ASTFullType;
import us.terebi.lang.lpc.parser.ast.ASTIdentifier;
import us.terebi.lang.lpc.parser.ast.ASTMethod;
import us.terebi.lang.lpc.parser.ast.ASTParameterDeclaration;
import us.terebi.lang.lpc.parser.ast.ASTParameterDeclarations;
import us.terebi.lang.lpc.parser.ast.ASTRef;
import us.terebi.lang.lpc.parser.ast.ASTStatementBlock;
import us.terebi.lang.lpc.parser.ast.TokenNode;
import us.terebi.lang.lpc.parser.jj.Token;
import us.terebi.lang.lpc.parser.util.ASTUtil;
import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.ArgumentSemantics;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.MemberDefinition.Modifier;
import us.terebi.lang.lpc.runtime.jvm.naming.MethodNamer;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;
import us.terebi.lang.lpc.runtime.util.Signature;

/**
 * 
 */
public class MethodSupport implements MethodInfo
{
    private final ASTMethod _node;
    private final String _name;
    private final ASTStatementBlock _body;
    private final ScopeLookup _scope;
    private final Set< ? extends Modifier> _modifiers;
    private final LpcType _returnType;
    private final List< ? extends ArgumentDefinition> _argumentDefinitions;
    private final MethodNamer _namer;

    public MethodSupport(ScopeLookup scope, ASTMethod node, Set< ? extends Modifier> modifiers, LpcType returnType)
    {
        _node = node;
        ASTIdentifier identifier = ASTUtil.getChild(ASTIdentifier.class, _node);
        Token token = identifier.jjtGetFirstToken();
        _name = token.image;
        _body = ASTUtil.getChild(ASTStatementBlock.class, node);
        _scope = scope;
        _modifiers = modifiers;
        _returnType = returnType;
        _namer = new MethodNamer(true, false);
        _argumentDefinitions = buildArgumentDefinitions();
    }

    public void setAvoidJavaReservedWords(boolean avoidJavaReservedWords)
    {
        _namer.setAvoidJavaReservedWords(avoidJavaReservedWords);
    }

    public void setAvoidStandardMethodNames(boolean avoidStandardMethodNames)
    {
        _namer.setAvoidStandardMethodNames(avoidStandardMethodNames);
    }

    public String getMethodName()
    {
        return _name;
    }

    public int getLineNumber()
    {
        return _node.jjtGetFirstToken().beginLine;
    }

    public LpcType getReturnType()
    {
        return _returnType;
    }

    public String getInternalName()
    {
        return _namer.getInternalName(_name);
    }

    public List< ? extends ArgumentDefinition> getArgumentDefinitions()
    {
        return _argumentDefinitions;
    }

    public boolean isPrototype()
    {
        return _body == null;
    }

    public ASTStatementBlock getBody()
    {
        return _body;
    }

    public void defineLocalMethod()
    {
        try
        {
            FunctionSignature signature = new Signature(hasModifier(Modifier.VARARGS), _returnType, _argumentDefinitions);
            _scope.functions().defineLocalMethod(this.getMethodName(), this.getInternalName(), signature, _modifiers);
        }
        catch (LookupException e)
        {
            throw new CompileException(_node, e.getMessage(), e);
        }
    }

    public VariableResolution[] declareParameters()
    {
        return _scope.variables().declareParameters(_argumentDefinitions);
    }

    private boolean hasModifier(Modifier modifier)
    {
        return _modifiers.contains(modifier);
    }

    private List< ? extends ArgumentDefinition> buildArgumentDefinitions()
    {
        ASTParameterDeclarations parameters = ASTUtil.getChild(ASTParameterDeclarations.class, _node);
        return buildArgumentDefinitions(parameters, _scope);
    }

    public static List< ? extends ArgumentDefinition> buildArgumentDefinitions(ASTParameterDeclarations parameters, ScopeLookup scope)
    {
        List<ArgumentDefinition> args = new ArrayList<ArgumentDefinition>(parameters.jjtGetNumChildren());
        for (TokenNode child : ASTUtil.children(parameters))
        {
            assert child instanceof ASTParameterDeclaration;
            args.add(buildArgumentDefinition((ASTParameterDeclaration) child, scope));
        }
        return args;
    }

    private static ArgumentDefinition buildArgumentDefinition(ASTParameterDeclaration node, ScopeLookup scope)
    {
        ASTFullType fullType = ASTUtil.getChild(ASTFullType.class, node);
        ASTIdentifier identifier = ASTUtil.getChild(ASTIdentifier.class, node);
        boolean ref = ASTUtil.hasChildType(ASTRef.class, node);
        boolean expander = ASTUtil.hasChildType(ASTElementExpander.class, node);

        String name = ASTUtil.getImage(identifier);

        LpcType type = new TypeSupport(scope, fullType).getType();
        if (expander && !type.isArray())
        {
            throw new CompileException(node, "Attempt to use expander (vargargs) '...' on argument '" + name + "' without declaring it as an array");
        }
        return new ArgumentSpec(name, type, expander, ref ? ArgumentSemantics.EXPLICIT_REFERENCE : ArgumentSemantics.BY_VALUE);
    }

}
