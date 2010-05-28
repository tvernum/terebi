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

package us.terebi.lang.lpc.compiler.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import us.terebi.lang.lpc.compiler.CompileException;
import us.terebi.lang.lpc.compiler.java.context.ScopeLookup;
import us.terebi.lang.lpc.compiler.java.context.FunctionLookup.FunctionReference;
import us.terebi.lang.lpc.parser.ast.ASTFunctionArguments;
import us.terebi.lang.lpc.parser.ast.ParserVisitor;
import us.terebi.lang.lpc.parser.ast.TokenNode;
import us.terebi.lang.lpc.parser.util.ASTUtil;
import us.terebi.lang.lpc.parser.util.BaseASTVisitor;
import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.MemberDefinition.Modifier;
import us.terebi.lang.lpc.runtime.jvm.exception.InternalError;
import us.terebi.lang.lpc.runtime.util.FunctionUtil;
import us.terebi.util.Range;

/**
 * 
 */
public class FunctionCallSupport extends BaseASTVisitor implements ParserVisitor
{
    private final ScopeLookup _scope;

    public static class ArgumentData
    {
        public final FunctionReference function;
        public ArgumentDefinition definition;
        public int index;

        ArgumentData(FunctionReference ref)
        {
            function = ref;
            definition = null;
            index = -1;
        }

        void set(ArgumentDefinition def, int idx)
        {
            if (def == null)
            {
                throw new NullPointerException("Null ArgumentDefinition provided to " + this);
            }
            definition = def;
            index = idx;
        }
    }

    public FunctionCallSupport(ScopeLookup scope)
    {
        _scope = scope;
    }

    public FunctionReference findInheritedFunction(String name, int argCount)
    {
        List<FunctionReference> functions = _scope.functions().getInheritedMethods(name, argCount);
        functions = filterVirtualFunctions(functions);
        if (functions.isEmpty())
        {
            throw new InternalError("No such function " + name);
        }
        return functions.get(functions.size() - 1);
    }

    public FunctionReference findFunction(TokenNode node, String scope, String name)
    {
        List<FunctionReference> functions = _scope.functions().findFunctions(scope, name, !_scope.isSecureObject());
        functions = filterVirtualFunctions(functions);
        String scopedName = ((scope != null) ? scope + "::" : "") + name;
        return extractFunctionReferences(node, scopedName, functions);
    }

    private FunctionReference extractFunctionReferences(TokenNode node, final String scopedName, List<FunctionReference> functions)
    {
        if (functions == null || functions.isEmpty())
        {
            throw new CompileException(node, "No such function " + scopedName);
        }
        if (functions.size() > 1)
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Multiple functions ").append(scopedName).append(" - ");
            for (FunctionReference functionReference : functions)
            {
                msg.append(functionReference.describe()).append(" , ");
            }
            throw new CompileException(node, msg.toString());
        }

        FunctionReference function = functions.get(0);
        return function;
    }

    private List<FunctionReference> filterVirtualFunctions(List<FunctionReference> functions)
    {
        if (functions.isEmpty() || functions.size() == 1)
        {
            return functions;
        }

        List<FunctionReference> nonVirtual = new ArrayList<FunctionReference>(functions.size());
        for (FunctionReference ref : functions)
        {
            if (!ref.modifiers.contains(Modifier.PURE_VIRTUAL))
            {
                nonVirtual.add(ref);
            }
        }
        if (nonVirtual.isEmpty())
        {
            // They're all pure-virtual, so it doesn't matter which one we return
            return Collections.singletonList(functions.get(0));
        }
        return nonVirtual;
    }

    public int getVarArgsIndex(FunctionSignature signature, int providedArgumentCount)
    {
        List< ? extends ArgumentDefinition> signatureArguments = signature.getArguments();
        if (signatureArguments.isEmpty())
        {
            return -1;
        }
        final int lastIndex = signatureArguments.size() - 1;
        ArgumentDefinition lastFormalArgument = signatureArguments.get(lastIndex);
        if (providedArgumentCount >= signatureArguments.size() && lastFormalArgument.isVarArgs())
        {
            return lastIndex;
        }
        return -1;
    }

    public void checkArgumentCount(final int providedArguments, FunctionReference function, ASTFunctionArguments args)
    {
        if (args.hasExpander())
        {
            return;
        }
        Range<Integer> allowedArgCount = FunctionUtil.getAllowedNumberOfArgument(function.signature);
        if (!allowedArgCount.inRange(providedArguments))
        {
            throw new CompileException(args, function.getTypeName()
                    + " "
                    + function.describe()
                    + " requires "
                    + allowedArgCount
                    + " argument(s) but "
                    + args.jjtGetNumChildren()
                    + ' '
                    + (args.jjtGetNumChildren() == 1 ? "was" : "were")
                    + " provided");
        }
    }

    public void processArguments(FunctionReference function, ASTFunctionArguments args, final ParserVisitor visitor, Object[] visitorResults,
            final int startingIndex)
    {
        final List< ? extends ArgumentDefinition> signatureArguments = function.signature.getArguments();

        ArgumentData data = new ArgumentData(function);
        int varIndex = startingIndex;
        int sigIndex = varIndex;
        for (TokenNode argNode : ASTUtil.children(args))
        {
            ArgumentDefinition argDef = signatureArguments.get(sigIndex);
            data.set(argDef, varIndex);
            Object var = argNode.jjtAccept(visitor, data);
            if (var == null)
            {
                throw new NullPointerException("Function argument " + argNode + " returned a null variable");
            }
            visitorResults[varIndex] = var;
            varIndex++;
            if (!argDef.isVarArgs())
            {
                sigIndex++;
            }
        }
    }
}
