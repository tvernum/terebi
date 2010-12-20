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

package us.terebi.lang.lpc.compiler.bytecode;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.adjective.stout.builder.ClassSpec;
import org.adjective.stout.core.ConstructorSignature;
import org.adjective.stout.core.ElementModifier;
import org.adjective.stout.core.ExtendedType;
import org.adjective.stout.core.MethodSignature;
import org.adjective.stout.core.SimpleType;
import org.adjective.stout.core.UnresolvedType;
import org.adjective.stout.impl.MethodSignatureImpl;
import org.adjective.stout.operation.Expression;
import org.adjective.stout.operation.VM;

import us.terebi.lang.lpc.compiler.CompileException;
import us.terebi.lang.lpc.compiler.bytecode.context.CompileContext;
import us.terebi.lang.lpc.compiler.java.context.ScopeLookup;
import us.terebi.lang.lpc.compiler.java.context.FunctionLookup.FunctionReference;
import us.terebi.lang.lpc.compiler.java.context.VariableLookup.ObjectPath;
import us.terebi.lang.lpc.compiler.util.FunctionCallSupport;
import us.terebi.lang.lpc.compiler.util.TypeSupport;
import us.terebi.lang.lpc.compiler.util.FunctionCallSupport.ArgumentData;
import us.terebi.lang.lpc.parser.ast.ASTArgumentExpression;
import us.terebi.lang.lpc.parser.ast.ASTFunctionArguments;
import us.terebi.lang.lpc.parser.ast.ASTFunctionCall;
import us.terebi.lang.lpc.parser.ast.ASTIdentifier;
import us.terebi.lang.lpc.parser.ast.ExpressionNode;
import us.terebi.lang.lpc.parser.ast.Node;
import us.terebi.lang.lpc.parser.jj.ParserConstants;
import us.terebi.lang.lpc.parser.util.BaseASTVisitor;
import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.ArgumentSemantics;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.ClassDefinition;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.MemberDefinition.Modifier;
import us.terebi.lang.lpc.runtime.jvm.LpcObject;
import us.terebi.lang.lpc.runtime.jvm.LpcReference;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.ReferenceValue;
import us.terebi.util.Pair;

/**
 * 
 */
public class FunctionCallCompiler extends BaseASTVisitor
{
    public static class FunctionArgument
    {
        public final LpcExpression expression;
        public final boolean expand;
        public final boolean reference;

        public FunctionArgument(LpcExpression expr, boolean ref, boolean expander)
        {
            this.expression = expr;
            this.reference = ref;
            this.expand = expander;
        }

        public String toString()
        {
            return getClass().getSimpleName() + "{" + (reference ? "ref " : "") + expression + (expand ? "..." : "") + "}";
        }
    }

    private static final LpcExpression[] EMPTY_EXPRESSION_ARRAY = new LpcExpression[0];

    private final ScopeLookup _scope;
    private final FunctionCallSupport _support;
    private final ExpressionCompiler _expressionCompiler;

    public FunctionCallCompiler(ScopeLookup scope, ExpressionCompiler expressionCompiler)
    {
        _scope = scope;
        _expressionCompiler = expressionCompiler;
        _support = new FunctionCallSupport(scope);
    }

    public LpcExpression compileFunction(ASTFunctionCall node)
    {
        String scope = node.getFunctionScope();
        String name = node.getFunctionName();
        ASTFunctionArguments args = node.getArguments();

        FunctionReference function = _support.findFunction(node, scope, name);

        LpcExpression expr = compileFunction(function, args);
        return expr;
    }

    private LpcExpression compileFunction(FunctionReference function, ASTFunctionArguments args)
    {
        return compileFunction(function, EMPTY_EXPRESSION_ARRAY, args);
    }

    public LpcExpression compileFunction(FunctionReference function, LpcExpression[] initial, ASTFunctionArguments args)
    {
        FunctionArgument[] argVars = processArguments(function, initial, args);

        boolean expand = requiresExpansion(argVars);

        Expression expression;
        if (function.kind == Callable.Kind.METHOD && !expand)
        {
            expression = compileDirectMethodCall(function, argVars);
        }
        else
        {
            expression = compileIndirectMethodCall(function, argVars, expand);
        }
        return new LpcExpression(function.signature.getReturnType(), expression);
    }

    private Expression compileDirectMethodCall(FunctionReference function, FunctionArgument[] argVars)
    {
        List< ? extends ArgumentDefinition> signatureArguments = function.signature.getArguments();
        if (!function.signature.acceptsLessArguments())
        {
            assert signatureArguments.size() <= argVars.length : "Received " + argVars.length + " arguments to " + function;
        }

        Expression[] arguments = new Expression[signatureArguments.size()];
        for (int i = 0; i < arguments.length; i++)
        {
            ArgumentDefinition argDef = function.signature.getArguments().get(i);
            if (argDef.isVarArgs())
            {
                Expression[] elements = new Expression[argVars.length - arguments.length + 1];
                for (int j = 0; j < elements.length; j++)
                {
                    elements[j] = ExpressionCompiler.getValue(argVars[i + j].expression);
                }
                arguments[i] = ExpressionCompiler.makeLpcArray(elements);
            }
            else if (i >= argVars.length)
            {
                arguments[i] = ByteCodeConstants.NIL;
            }
            else
            {
                arguments[i] = ExpressionCompiler.getValue(argVars[i].expression);
            }
        }

        return callFunction(function, arguments, _expressionCompiler.getContext());
    }

    public static Expression callFunction(FunctionReference function, Expression[] arguments, CompileContext context)
    {
        MethodSignature signature = getSignature(function);
        if (function.isLocalMethod())
        {
            if (requiresDispatch(function.modifiers))
            {
                ClassSpec spec = context.currentClass();
                SimpleType ifc = ClassBuilder.getInterfaceType(spec);
                String self = ClassBuilder.getThisFieldName(spec);
                Expression call = VM.Expression.callMethod(VM.Expression.getField(self, ifc), ifc, signature, arguments);
                return call;
            }
            else
            {
                Expression call = VM.Expression.callInherited(signature, arguments);
                return call;
            }
        }

        Pair<Expression, UnresolvedType> target = findTarget(function);
        Expression call = VM.Expression.callMethod(target.getFirst(), target.getSecond(), signature, arguments);
        return call;
    }

    public static boolean requiresDispatch(Set< ? extends Modifier> modifiers)
    {
        if (modifiers.contains(Modifier.PRIVATE))
        {
            return false;
        }
        return true;
    }

    private static Pair<Expression, UnresolvedType> findTarget(FunctionReference function)
    {
        return ObjectPath.findTarget(function.objectPath);
    }

    private static MethodSignature getSignature(FunctionReference function)
    {
        ExtendedType[] parameterTypes = new ExtendedType[function.signature.getArguments().size()];
        for (int i = 0; i < parameterTypes.length; i++)
        {
            parameterTypes[i] = ByteCodeConstants.LPC_VALUE;
        }
        Set<ElementModifier> modifiers = Collections.singleton(ElementModifier.PUBLIC);
        return new MethodSignatureImpl(modifiers, ByteCodeConstants.LPC_VALUE, function.internalName, parameterTypes);
    }

    private Expression compileIndirectMethodCall(FunctionReference function, FunctionArgument[] argVars, boolean expand)
    {
        Expression callable = getCallable(function);
        return executeCallable(function, callable, argVars, expand);
    }

    public static Expression getCallable(FunctionReference function)
    {
        MethodSignature lookup = VM.Method.find(LpcObject.class, function.kind.name().toLowerCase(), String.class);
        Expression callable = VM.Expression.callInherited(lookup, VM.Expression.constant(function.name));
        return callable;
    }

    public Expression executeCallable(FunctionReference function, Expression callable, FunctionArgument[] argVars, boolean expand)
    {
        if (expand)
        {
            Expression[] collections = new Expression[argVars.length];
            for (int i = 0; i < collections.length; i++)
            {
                Expression expr = ExpressionCompiler.getValue(argVars[i].expression);
                if (argVars[i].expand)
                {
                    collections[i] = VM.Expression.callMethod(expr, LpcValue.class, ByteCodeConstants.VALUE_AS_LIST);
                }
                else
                {
                    collections[i] = VM.Expression.callStatic(Collections.class, ByteCodeConstants.SINGLETON_LIST, expr);
                }
            }
            Expression arguments = VM.Expression.array(Collection.class, collections);
            Expression call = VM.Expression.callInherited(ByteCodeConstants.CALL_WITH_COLLECTIONS, callable, arguments);
            return call;
        }
        else if (function.signature.hasUnstructuredArguments())
        {
            Expression[] elements = new Expression[argVars.length];
            for (int i = 0; i < elements.length; i++)
            {
                elements[i] = ExpressionCompiler.getValue(argVars[i].expression);
            }
            Expression arguments = VM.Expression.array(LpcValue.class, elements);
            Expression call = VM.Expression.callMethod(callable, Callable.class, ByteCodeConstants.CALLABLE_EXECUTE, arguments);
            return call;
        }
        else
        {
            int varargs = _support.getVarArgsIndex(function.signature, argVars.length);
            Expression[] elements = new Expression[function.signature.getArguments().size()];
            for (int i = 0; i < elements.length; i++)
            {
                if (i >= argVars.length)
                {
                    if (function.signature.getArguments().get(i).isVarArgs())
                    {
                        elements[i] = ExpressionCompiler.makeLpcArray(new Expression[0]);
                    }
                    else
                    {
                        elements[i] = ByteCodeConstants.NIL;
                    }
                }
                else if (i == varargs)
                {
                    Expression[] collapse = new Expression[argVars.length - i];
                    for (int j = i; j < argVars.length; j++)
                    {
                        collapse[j - i] = ExpressionCompiler.getValue(argVars[j].expression);
                    }
                    elements[i] = ExpressionCompiler.makeLpcArray(collapse);
                }
                else
                {
                    elements[i] = ExpressionCompiler.getValue(argVars[i].expression);
                }
            }
            Expression arguments = VM.Expression.array(LpcValue.class, elements);
            Expression call = VM.Expression.callMethod(callable, Callable.class, ByteCodeConstants.CALLABLE_EXECUTE, arguments);
            return call;
        }
    }

    public boolean requiresExpansion(FunctionArgument[] argVars)
    {
        for (FunctionArgument arg : argVars)
        {
            if (arg.expand)
            {
                return true;
            }
        }
        return false;
    }

    public FunctionArgument[] processArguments(FunctionReference function, LpcExpression[] prelim, ASTFunctionArguments args)
    {
        List< ? extends ArgumentDefinition> signatureArguments = function.signature.getArguments();

        if (prelim == null)
        {
            prelim = EMPTY_EXPRESSION_ARRAY;
        }

        FunctionArgument[] argVars = new FunctionArgument[prelim.length + args.jjtGetNumChildren()];
        for (int i = 0; i < prelim.length; i++)
        {
            ArgumentDefinition argument = getArgument(signatureArguments, i);
            boolean ref = (argument != null && argument.getSemantics() == ArgumentSemantics.IMPLICIT_REFERENCE);
            if (ref)
            {
                if (!prelim[i].reference)
                {
                    throw new CompileException(args, "Argument "
                            + signatureArguments.get(i).getName()
                            + " to "
                            + function.name
                            + " requires a reference value");
                }
            }
            argVars[i] = new FunctionArgument(prelim[i], ref, false);
        }

        _support.checkArgumentCount(argVars.length, function, args);
        _support.processArguments(function, args, this, argVars, prelim.length);

        return argVars;
    }

    private ArgumentDefinition getArgument(List< ? extends ArgumentDefinition> signatureArguments, int index)
    {
        if (signatureArguments.size() > index)
        {
            return signatureArguments.get(index);
        }
        ArgumentDefinition last = signatureArguments.get(signatureArguments.size() - 1);
        if (last.isVarArgs())
        {
            return last;
        }
        return null;
    }

    public FunctionArgument visit(ASTArgumentExpression node, Object obj)
    {
        assert (obj instanceof ArgumentData);
        ArgumentData data = (ArgumentData) obj;

        switch (node.getArgumentType())
        {
            case ParserConstants.REF:
                return new FunctionArgument(compileRef(node, data), true, false);
            case ParserConstants.CLASS:
                return new FunctionArgument(compileClassReference(node), false, false);
            case ParserConstants.EXPANDO:
                if (data.definition.getSemantics() == ArgumentSemantics.IMPLICIT_REFERENCE)
                {
                    throw new CompileException(node, "Cannot apply expansion (...) to implicit reference argument "
                            + data.definition.getName()
                            + " to "
                            + data.function.name);
                }
                return new FunctionArgument(compileExpandoArg(node), false, true);
            default:
                if (data.definition.getSemantics() == ArgumentSemantics.IMPLICIT_REFERENCE)
                {
                    return new FunctionArgument(compileRef(node, data), true, false);
                }
                return new FunctionArgument(compileSimpleArg(node, data), false, false);
        }
    }

    private LpcExpression compileExpandoArg(ASTArgumentExpression node)
    {
        // @TODO Check type
        return compileExpression(node.jjtGetChild(0));
    }

    private LpcExpression compileSimpleArg(ASTArgumentExpression node, ArgumentData data)
    {
        LpcType requiredType = data.definition.getType();
        if (data.definition.isVarArgs())
        {
            requiredType = Types.elementOf(requiredType);
        }
        LpcExpression expr = compileExpression(node.jjtGetChild(0));
        TypeSupport.checkType(node, expr.type, requiredType);
        return expr;
    }

    private LpcExpression compileRef(ASTArgumentExpression node, ArgumentData data)
    {
        if (data.definition.getSemantics() == ArgumentSemantics.BY_VALUE)
        {
            throw new CompileException(node, "Argument "
                    + data.definition.getName()
                    + " to "
                    + data.function.toString()
                    + " does not accept references");
        }
        ExpressionNode exprNode = (ExpressionNode) node.jjtGetChild(0);
        return makeRef(exprNode, data);
    }

    private LpcExpression makeRef(ExpressionNode exprNode, ArgumentData data)
    {
        LpcExpression expr = compileExpression(exprNode);
        if (!expr.reference)
        {
            throw new CompileException(exprNode, "Argument "
                    + data.definition.getName()
                    + " to "
                    + data.function.name
                    + " requires a reference value");
        }

        ConstructorSignature constructor = VM.Method.constructor(ReferenceValue.class, LpcReference.class);
        return new LpcExpression(expr.type, VM.Expression.construct(constructor, expr.expression), true);
    }

    private LpcExpression compileExpression(Node node)
    {
        return _expressionCompiler.compile(node);
    }

    private LpcExpression compileClassReference(ASTArgumentExpression node)
    {
        ASTIdentifier identifier = (ASTIdentifier) node.jjtGetChild(0);
        ClassDefinition definition = _scope.classes().findClass(identifier);

        if (definition instanceof CompiledClassDefinition)
        {
            MethodSignature method = VM.Method.find(LpcObject.class, "classReference", Class.class);
            UnresolvedType implementing = ((CompiledClassDefinition) definition).getImplementatingClass();
            Expression expr = VM.Expression.callInherited(method, VM.Expression.classObject(implementing));
            return new LpcExpression(Types.classType(definition), expr, false);
        }
        else
        {
            // @TODO
            throw new CompileException(node, "Don't know how to compile a refernce to the class " + definition);
        }
    }
}
