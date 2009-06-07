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
import java.util.List;

import us.terebi.lang.lpc.compiler.CompileException;
import us.terebi.lang.lpc.compiler.java.context.CompileContext;
import us.terebi.lang.lpc.compiler.java.context.FunctionLookup.FunctionReference;
import us.terebi.lang.lpc.parser.ast.ASTArgumentExpression;
import us.terebi.lang.lpc.parser.ast.ASTFunctionArguments;
import us.terebi.lang.lpc.parser.ast.ASTFunctionCall;
import us.terebi.lang.lpc.parser.ast.ASTIdentifier;
import us.terebi.lang.lpc.parser.ast.ASTScopeResolution;
import us.terebi.lang.lpc.parser.ast.ASTScopedIdentifier;
import us.terebi.lang.lpc.parser.ast.ASTUtil;
import us.terebi.lang.lpc.parser.ast.BaseASTVisitor;
import us.terebi.lang.lpc.parser.ast.ParserVisitor;
import us.terebi.lang.lpc.parser.ast.SimpleNode;
import us.terebi.lang.lpc.parser.jj.ParserConstants;
import us.terebi.lang.lpc.parser.jj.Token;
import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.ArgumentSemantics;
import us.terebi.lang.lpc.runtime.ClassDefinition;
import us.terebi.lang.lpc.runtime.Callable.Kind;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.ReferenceValue;
import us.terebi.lang.lpc.runtime.util.FunctionUtil;
import us.terebi.util.Range;
import us.terebi.util.ToString;

/**
 * 
 */
public class FunctionCallWriter extends BaseASTVisitor implements ParserVisitor
{
    private final CompileContext _context;

    class ArgumentData
    {
        public final FunctionReference function;
        public ArgumentDefinition definition;
        public int index;

        public ArgumentData(FunctionReference ref)
        {
            function = ref;
            definition = null;
            index = -1;
        }

        public void set(ArgumentDefinition def, int idx)
        {
            if (def == null)
            {
                throw new NullPointerException("Null ArgumentDefinition provided to " + this);
            }
            definition = def;
            index = idx;
        }
    }

    public static class FunctionArgument
    {
        public final InternalVariable variable;
        public final boolean expand;
        public final boolean reference;

        public FunctionArgument(InternalVariable var, boolean ref, boolean expander)
        {
            this.variable = var;
            this.reference = ref;
            this.expand = expander;
        }
    }

    public FunctionCallWriter(CompileContext context)
    {
        _context = context;
    }

    public InternalVariable writeFunction(ASTFunctionCall node)
    {
        ASTScopedIdentifier scoped = ASTUtil.getChild(ASTScopedIdentifier.class, node);
        ASTScopeResolution resolution = ASTUtil.getChild(ASTScopeResolution.class, scoped);
        ASTIdentifier identifier = ASTUtil.getChild(ASTIdentifier.class, scoped);

        String scope = (resolution == null) ? null : (resolution.jjtGetNumChildren() == 0) ? "" : ASTUtil.getImage(resolution);
        String name = ASTUtil.getImage(identifier);

        FunctionReference function = findFunction(identifier, scope, name);
        ASTFunctionArguments args = ASTUtil.getChild(ASTFunctionArguments.class, node);

        InternalVariable var = writeFunction(function, args);
        return var;
    }

    public FunctionReference findFunction(SimpleNode node, String scope, String name)
    {
        List<FunctionReference> functions = _context.functions().findFunctions(scope, name, !_context.isSecureObject());
        if (functions == null || functions.isEmpty())
        {
            throw new CompileException(node, "No such function " + ((scope != null) ? scope + "::" : "") + name);
        }
        if (functions.size() > 1)
        {
            throw new CompileException(node, "Multiple functions "
                    + ((scope != null) ? scope + "::" : "")
                    + name
                    + " - "
                    + ToString.toString(functions));
        }

        FunctionReference function = functions.get(0);
        return function;
    }

    public InternalVariable writeFunction(FunctionReference function, ASTFunctionArguments args)
    {
        FunctionArgument[] argVars = processArguments(function, new InternalVariable[0], args);
        return writeFunction(function, argVars);
    }

    public InternalVariable writeFunction(FunctionReference function, FunctionArgument[] argVars)
    {
        InternalVariable var = new InternalVariable(_context, false, function.signature.getReturnType());
        var.declare(_context.writer());
        _context.writer().print(" = ");

        PrintWriter writer = _context.writer();
        boolean expand = false;
        for (FunctionArgument arg : argVars)
        {
            if (arg.expand)
            {
                expand = true;
                break;
            }
        }

        if (expand)
        {
            writer.print("call(");
            printCallReference(function.kind, function);
            writer.print(",");
        }
        else
        {
            switch (function.kind)
            {
                case EFUN:
                case SIMUL_EFUN:
                    printCallExecution(function.kind, function);
                    break;
                case METHOD:
                    if (function.object != null)
                    {
                        for (String inherit : function.objectPath)
                        {
                            writer.print("inherit_");
                            writer.print(inherit);
                            writer.print(".get().");
                        }
                    }
                    writer.print(function.internalName);
                    writer.print("(");
                    break;
            }
        }
        boolean first = true;
        for (FunctionArgument argVar : argVars)
        {
            if (first)
            {
                first = false;
            }
            else
            {
                writer.print(" , ");
            }
            if (expand)
            {
                if (argVar.expand)
                {
                    argVar.variable.value(writer);
                    writer.print(".asList()");
                }
                else
                {
                    writer.print("Collections.singletonList(");
                    argVar.variable.value(writer);
                    writer.print(")");
                }
            }
            else if (argVar.reference)
            {
                if (argVar.variable.reference)
                {
                    writer.print(argVar.variable.name);
                }
                else
                {
                    throw new CompileException((Token) null, "Internal error - expected reference but got " + argVar.variable);
                }
            }
            else
            {
                argVar.variable.value(writer);
            }
        }

        for (int i = argVars.length; i < function.signature.getArguments().size(); i++)
        {
            if (first)
            {
                first = false;
            }
            else
            {
                writer.print(" , ");
            }
            writer.print("nil()");
        }

        writer.println(");");
        return var;
    }

    public FunctionArgument[] processArguments(FunctionReference function, InternalVariable[] prelim, ASTFunctionArguments args)
    {
        List< ? extends ArgumentDefinition> signatureArguments = function.signature.getArguments();

        FunctionArgument[] argVars = new FunctionArgument[prelim.length + args.jjtGetNumChildren()];
        for (int i = 0; i < prelim.length; i++)
        {
            boolean ref = (signatureArguments.size() > i && signatureArguments.get(i).getSemantics() == ArgumentSemantics.IMPLICIT_REFERENCE);
            argVars[i] = new FunctionArgument(prelim[i], ref, false);
        }

        // @ TODO vargs
        // @ TODO expandos
        Range<Integer> allowedArgCount = FunctionUtil.getAllowedNumberOfArgument(function.signature);
        if (!allowedArgCount.inRange(argVars.length))
        {
            throw new CompileException(args, function.getTypeName()
                    + " "
                    + function.toString()
                    + " requires "
                    + allowedArgCount
                    + " argument(s) but "
                    + args.jjtGetNumChildren()
                    + " were provided");
        }

        ArgumentData data = new ArgumentData(function);
        int varIndex = prelim.length;
        int sigIndex = varIndex;
        for (SimpleNode argNode : ASTUtil.children(args))
        {
            ArgumentDefinition argDef = signatureArguments.get(sigIndex);
            data.set(argDef, varIndex);
            Object var = argNode.jjtAccept(this, data);
            if (var == null)
            {
                throw new NullPointerException("Function argument " + argNode + " returned a null variable");
            }
            argVars[varIndex] = (FunctionArgument) var;
            varIndex++;
            if (!argDef.isVarArgs())
            {
                sigIndex++;
            }
        }
        return argVars;
    }

    public FunctionArgument visit(ASTArgumentExpression node, Object obj)
    {
        assert (obj instanceof ArgumentData);
        ArgumentData data = (ArgumentData) obj;

        Token head = node.jjtGetFirstToken();
        if (head.kind == ParserConstants.REF)
        {
            // <REF> Identifier()
            return new FunctionArgument(printRef(node, data), true, false);
        }
        else if (head.kind == ParserConstants.CLASS)
        {
            // ClassType()
            // <CLASS> Identifier()
            return new FunctionArgument(printClass(node), false, false);
        }
        else
        {
            // SimpleExpression() [ ElementExpander() ]
            boolean expand = node.jjtGetNumChildren() == 2;
            SimpleNode expr = (SimpleNode) node.jjtGetChild(0);
            InternalVariable var = new ExpressionWriter(_context).evaluate(expr);
            if (data.definition.getSemantics() == ArgumentSemantics.IMPLICIT_REFERENCE)
            {
                if (expand)
                {
                    throw new CompileException(node, "Cannot apply expansion (...) to implicit reference argument "
                            + data.definition.getName()
                            + " to "
                            + data.function.name);
                }
                if (!var.reference)
                {
                    throw new CompileException(node, "Argument "
                            + data.definition.getName()
                            + " to "
                            + data.function.name
                            + " requires a reference value");
                }
                return new FunctionArgument(printRef(var.name), true, false);
            }
            else
            {
                return new FunctionArgument(var, false, expand);
            }
        }
    }

    private InternalVariable printClass(ASTArgumentExpression node)
    {
        ASTIdentifier identifier = (ASTIdentifier) node.jjtGetChild(0);
        ClassDefinition definition = _context.classes().findClass(identifier);
        String className = definition.getName();

        String varName = _context.variables().allocateInternalVariableName();
        InternalVariable var = new InternalVariable(varName, false, Types.classType(definition));

        PrintWriter writer = _context.writer();
        var.declare(writer);
        writer.print(" = new LpcClass(this, ");
        writer.print(className);
        writer.print(")");
        return var;
    }

    private InternalVariable printRef(ASTArgumentExpression node, ArgumentData data)
    {
        if (data.definition.getSemantics() == ArgumentSemantics.BY_VALUE)
        {
            throw new CompileException(node, "Argument "
                    + data.definition.getName()
                    + " to "
                    + data.function.toString()
                    + " does not accept references");
        }
        SimpleNode identifier = (SimpleNode) node.jjtGetChild(0);
        String init = ASTUtil.getImage(identifier);
        // @TODO check the symbol table for the type (and to check whether it's been declared, etc)
        return printRef(init);
    }

    private InternalVariable printRef(String initFrom)
    {
        String name = _context.variables().allocateInternalVariableName();
        _context.writer().print("final LpcReferenceValue ");
        _context.writer().print(name);
        _context.writer().print("= new " + ReferenceValue.class.getName() + "(");
        _context.writer().print(initFrom);
        _context.writer().print(");");

        return new InternalVariable(name, true, Types.MIXED);
    }

    private void printCallExecution(Kind kind, FunctionReference function)
    {
        printCallReference(kind, function);
        _context.writer().print(".execute(");
    }

    private void printCallReference(Kind kind, FunctionReference function)
    {
        PrintWriter writer = _context.writer();

        switch (kind)
        {
            case EFUN:
                writer.print("efun");
                break;
            case SIMUL_EFUN:
                writer.print("simul_efun");
                break;
            case METHOD:
                writer.print("method");
                break;
        }
        writer.print("(\"");
        writer.print(function.name);
        if (function.object != null)
        {
            writer.print("\", \"");
            writer.print(function.object.getName());
        }
        writer.print("\")");
    }
}
