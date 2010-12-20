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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.adjective.stout.builder.ClassSpec;
import org.adjective.stout.builder.ElementBuilder;
import org.adjective.stout.builder.FieldSpec;
import org.adjective.stout.builder.MethodSpec;
import org.adjective.stout.builder.ParameterSpec;
import org.adjective.stout.core.ConstructorSignature;
import org.adjective.stout.core.ElementModifier;
import org.adjective.stout.core.MethodDescriptor;
import org.adjective.stout.core.MethodSignature;
import org.adjective.stout.core.Parameter;
import org.adjective.stout.operation.Expression;
import org.adjective.stout.operation.Statement;
import org.adjective.stout.operation.VM;

import us.terebi.lang.lpc.compiler.CompileException;
import us.terebi.lang.lpc.compiler.bytecode.context.CompileContext;
import us.terebi.lang.lpc.compiler.java.context.LookupException;
import us.terebi.lang.lpc.compiler.java.context.ScopeLookup;
import us.terebi.lang.lpc.compiler.java.context.VariableResolver;
import us.terebi.lang.lpc.compiler.java.context.FunctionLookup.FunctionReference;
import us.terebi.lang.lpc.compiler.java.context.VariableResolver.VariableResolution;
import us.terebi.lang.lpc.compiler.util.FunctionCallSupport;
import us.terebi.lang.lpc.compiler.util.MethodSupport;
import us.terebi.lang.lpc.compiler.util.Positional;
import us.terebi.lang.lpc.parser.LineMapping;
import us.terebi.lang.lpc.parser.ast.ASTCompoundExpression;
import us.terebi.lang.lpc.parser.ast.ASTFunctionLiteral;
import us.terebi.lang.lpc.parser.ast.ASTImmediateExpression;
import us.terebi.lang.lpc.parser.ast.ASTParameterDeclarations;
import us.terebi.lang.lpc.parser.ast.ASTStatementBlock;
import us.terebi.lang.lpc.parser.ast.ASTVariableReference;
import us.terebi.lang.lpc.parser.ast.ExpressionNode;
import us.terebi.lang.lpc.parser.ast.TokenNode;
import us.terebi.lang.lpc.parser.jj.Token;
import us.terebi.lang.lpc.parser.util.ASTUtil;
import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.ArgumentSemantics;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.LpcFunction;
import us.terebi.lang.lpc.runtime.jvm.LpcObject;
import us.terebi.lang.lpc.runtime.jvm.LpcReference;
import us.terebi.lang.lpc.runtime.jvm.support.CallableSupport;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.FunctionValue;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;

/**
 * 
 */
public class FunctionLiteralCompiler
{
    public static final String POSITIONAL_ARGUMENT_COLLECTION = "args";
    private final ScopeLookup _scope;
    private final ScopeLookup _parentScope;
    private final CompileContext _context;

    public FunctionLiteralCompiler(ScopeLookup parentScope, CompileContext context)
    {
        _parentScope = parentScope;
        _context = context.enterFunctionLiteral();
        _scope = new InnerClassScopeLookup(parentScope, context.publicClass());
    }

    public LpcExpression compile(ASTFunctionLiteral node)
    {
        if (node.isBlock())
        {
            return visitBlockFunction(node);
        }
        else
        {
            return visitExpressionFunction(node);
        }
    }

    private LpcExpression visitExpressionFunction(ASTFunctionLiteral node)
    {
        //        <LEFT_BRACKET> <COLON> Expression() <COLON> <RIGHT_BRACKET>
        ExpressionNode exprNode = node.getExpressionBody();

        // Handle (: someFunction [ , arg1 , arg2 ] :)
        LpcExpression expression = checkOldStyleFunctionReference(exprNode, Collections.<ExpressionNode> emptyList());
        if (expression != null)
        {
            return expression;
        }
        if (exprNode instanceof ASTCompoundExpression)
        {
            ASTCompoundExpression compound = (ASTCompoundExpression) exprNode;
            expression = checkOldStyleFunctionReference(compound.getFirstExpression(), compound.getSubsequentExpressions());
            if (expression != null)
            {
                return expression;
            }
        }

        Collection<ASTVariableReference> vars = findReferencedVariables(node);
        int positionalCount = countPositionalArguments(vars);
        ClassSpec spec = createClassSpec(node);

        List<Parameter> parameters = new ArrayList<Parameter>();

        parameters.add(new ParameterSpec("s$owner").withType(LpcObject.class).create());
        parameters.add(new ParameterSpec("s$argCount").withType(Integer.TYPE).create());

        Map<String, VariableResolution> variables = getReferencedVariables(vars, spec, parameters);

        ExpressionCompiler compiler = new ExpressionCompiler(_scope, _context);
        LpcExpression[] immediates = getImmediateVariables(node, spec, compiler);

        MethodSpec constructor = getExpressionConstructor(parameters, immediates);
        spec.withMethod(constructor);

        LpcExpression expr = compile(exprNode, compiler);
        _scope.variables().popScope();
        MethodSpec execute = getExpressionExecute(expr);
        spec.withMethod(execute);

        store(spec);

        Expression[] args = new Expression[parameters.size()];
        args[0] = VM.Expression.thisObject();
        args[1] = VM.Expression.constant(positionalCount);
        for (int i = 2; i < args.length; i++)
        {
            args[i] = variables.get(parameters.get(i).getName()).access();
        }

        Expression function = VM.Expression.construct(spec, constructor, args);
        return new LpcExpression(Types.FUNCTION, function);
    }

    private LpcExpression checkOldStyleFunctionReference(ExpressionNode exprNode, List<ExpressionNode> arguments)
    {
        if (!(exprNode instanceof ASTVariableReference))
        {
            return null;
        }

        ASTVariableReference ref = (ASTVariableReference) exprNode;
        if (ref.isPositionalVariable())
        {
            return null;
        }

        VariableResolution var = _parentScope.variables().findVariable(ref.getVariableName());
        if (var != null)
        {
            return null;
        }

        return getFunctionReference(ref, arguments);
    }

    private LpcExpression getFunctionReference(ASTVariableReference ref, List<ExpressionNode> arguments)
    {
        FunctionCallSupport fcs = new FunctionCallSupport(_scope);
        FunctionReference function = fcs.findFunction(ref, ref.getScope(), ref.getVariableName());
        Expression callable = FunctionCallCompiler.getCallable(function);
        
        MethodSignature pointer = VM.Method.find(CallableSupport.class, "pointer", Callable.class, ObjectInstance.class);
        Expression owner = VM.Expression.callInherited(ByteCodeConstants.GET_OBJECT_INSTANCE, new Expression[0]); 
        callable = VM.Expression.callStatic(CallableSupport.class, pointer, callable, owner);

        if (!arguments.isEmpty())
        {
            ExpressionCompiler compiler = new ExpressionCompiler(_parentScope, _context);
            Expression[] elements = new Expression[arguments.size()];
            for (int i = 0; i < elements.length; i++)
            {
                elements[i] = ExpressionCompiler.getValue(compiler.compile(arguments.get(i)));
            }
            Expression array = VM.Expression.array(LpcValue.class, elements);
            MethodSignature bind = VM.Method.find(CallableSupport.class, "bindArguments", Callable.class, LpcValue[].class);
            callable = VM.Expression.callStatic(CallableSupport.class, bind, callable, array);
        }

        Expression value = VM.Expression.construct(VM.Method.constructor(FunctionValue.class, Callable.class), callable);
        return new LpcExpression(Types.FUNCTION, value);
    }

    private LpcExpression compile(ExpressionNode exprNode, ExpressionCompiler compiler)
    {
        return compiler.compile(exprNode);
    }

    public static Collection<ASTVariableReference> findReferencedVariables(TokenNode node)
    {
        Collection<ASTVariableReference> vars = ASTUtil.findDescendants(ASTVariableReference.class, node);
        return vars;
    }

    private void store(ClassSpec spec)
    {
        _context.popClass(spec);
        try
        {
            ByteCodeCompiler.store(spec, _context);
        }
        catch (IOException e)
        {
            throw new CompileException("Cannot write class file " + spec, e);
        }
    }

    private LpcExpression[] getImmediateVariables(ASTFunctionLiteral node, ClassSpec spec, ExpressionCompiler compiler)
    {
        LpcExpression[] immediates = this.evaluateImmediates(node, compiler);

        for (int i = 0; i < immediates.length; i++)
        {
            FieldSpec field = new FieldSpec("p$" + i);
            field.withType(LpcValue.class);
            field.withModifiers(ElementModifier.PRIVATE, ElementModifier.FINAL);
            spec.withField(field);
        }
        return immediates;
    }

    private Map<String, VariableResolution> getReferencedVariables(Collection<ASTVariableReference> vars, ClassSpec spec, List<Parameter> parameters)
    {
        Set<VariableResolution> pass = getReferencedVariables(vars);
        _scope.variables().pushScope();
        Map<String, VariableResolution> result = new HashMap<String, VariableResolution>(pass.size());
        for (VariableResolution var : pass)
        {
            result.put(var.internalName(), var);
            String fieldName = "e$" + var.internalName();
            _scope.variables().declareEnclosing(var.lpcName(), fieldName, var.type());
            FieldSpec field = new FieldSpec(fieldName);
            field.withType(LpcReference.class);
            field.withModifiers(ElementModifier.PRIVATE, ElementModifier.FINAL);
            spec.withField(field);

            ParameterSpec param = new ParameterSpec(var.internalName());
            param.withType(LpcReference.class);
            parameters.add(param.create());
        }
        return result;
    }

    private ClassSpec createClassSpec(ASTFunctionLiteral node)
    {
        Token token = node.jjtGetFirstToken();
        String className = _context.publicClass().getName() + "$f" + pad(token.beginLine, 4) + "_" + pad(token.beginColumn, 3);
        ClassSpec spec = ClassSpec.newClass(_context.publicClass().getPackage(), className);
        spec.withSuperClass(LpcFunction.class);
        spec.withModifiers(ElementModifier.PUBLIC, ElementModifier.FINAL);
        _context.pushClass(spec);
        
        MethodSpec getLocation = getLocationMethod(token);
        spec.withMethod(getLocation.create());
        return spec;
    }

    @SuppressWarnings("unchecked")
    private MethodSpec getLocationMethod(Token token)
    {
        LineMapping lines = _context.lineMapping();
        String location = lines.getFile(token.beginLine) + ':' + lines.getLine(token.beginLine) + ':' + token.beginColumn;
        MethodSpec getLocation = new MethodSpec("getLocation");
        getLocation.withReturnType(String.class);
        getLocation.withBody(VM.Statement.returnObject(VM.Expression.constant(location)));
        return getLocation;
    }

    private Set<VariableResolution> getReferencedVariables(Collection<ASTVariableReference> vars)
    {
        VariableResolver variables = _parentScope.variables();
        Set<VariableResolution> pass = getReferencedVariables(vars, variables);
        return pass;
    }

    public static Set<VariableResolution> getReferencedVariables(Collection<ASTVariableReference> vars, VariableResolver variables)
    {
        Set<VariableResolution> pass = new HashSet<VariableResolution>();
        for (ASTVariableReference variableReference : vars)
        {
            Positional positional = variableReference.getPositional();
            if (!positional.isPositionalVariable())
            {
                try
                {
                    VariableResolution var = variables.findVariable(variableReference.getVariableName());
                    if (var != null)
                    {
                        pass.add(var);
                    }
                }
                catch (LookupException lookup)
                {
                    // Ignore
                    // - it will either be satisfied by a variable local to the function, or will cause an error when we compile the relevant expression
                }
            }
        }
        return pass;
    }

    public static int countPositionalArguments(Collection<ASTVariableReference> vars)
    {
        int positionalCount = 0;
        for (ASTVariableReference variableReference : vars)
        {
            Positional positional = variableReference.getPositional();
            if (positional.isPositionalVariable())
            {
                int index = positional.getIndex();
                if (index > positionalCount)
                {
                    positionalCount = index;
                }
            }
        }
        return positionalCount;
    }

    private CharSequence pad(int value, int digits)
    {
        // @TODO make this more efficient...
        StringBuilder builder = new StringBuilder();
        builder.append(value);
        while (builder.length() < digits)
        {
            builder.insert(0, "0");
        }
        return builder;
    }

    private LpcExpression[] evaluateImmediates(ASTFunctionLiteral node, ExpressionCompiler compiler)
    {
        Collection<ASTImmediateExpression> immediates = ASTUtil.findDescendants(ASTImmediateExpression.class, node);
        LpcExpression[] values = new LpcExpression[immediates.size()];
        int i = 0;
        for (ASTImmediateExpression expression : immediates)
        {
            ExpressionNode body = expression.getBody();
            LpcExpression compiled = compile(body, compiler);

            LpcExpression precompile = new LpcExpression(compiled.type, VM.Expression.getField("p$" + i, ByteCodeConstants.LPC_VALUE));
            compiler.precompile(expression, precompile);

            values[i++] = compiled;
        }
        return values;
    }

    private MethodSpec getExpressionExecute(LpcExpression expr)
    {
        List<ElementBuilder<Statement>> body = Collections.singletonList(VM.Statement.returnObject(ExpressionCompiler.getValue(expr)));
        return getInvoke(body);
    }

    private MethodSpec getInvoke(List< ? extends ElementBuilder< ? extends Statement>> statements)
    {
        Parameter parameter = new ParameterSpec(POSITIONAL_ARGUMENT_COLLECTION).withType(LpcValue[].class).create();
        MethodSpec execute = new MethodSpec("invoke").withModifiers(ElementModifier.PROTECTED).withParameters(parameter);
        return execute.withReturnType(ByteCodeConstants.LPC_VALUE).withBody(statements);
    }

    private MethodSpec getExpressionConstructor(List<Parameter> parameters, LpcExpression[] immediates)
    {
        ConstructorSignature superConstructor = VM.Method.constructor(LpcFunction.class, ObjectInstance.class, Integer.TYPE);
        Expression owner = getOwner(parameters.get(0).getName());
        Expression argCount = VM.Expression.variable(parameters.get(1).getName());
        ElementBuilder<Statement> superCall = VM.Statement.superConstructor(superConstructor, owner, argCount);

        ElementBuilder<Statement>[] body = getConstructorBody(superCall, parameters.subList(2, parameters.size()), immediates);

        return getConstructor(parameters, body);
    }

    private MethodSpec getConstructor(List<Parameter> parameters, ElementBuilder<Statement>[] body)
    {
        MethodSpec constructor = new MethodSpec(ClassSpec.CONSTRUCTOR_NAME);
        constructor.withModifiers(ElementModifier.PUBLIC);
        constructor.withParameters(parameters);
        constructor.withBody(body);
        return constructor;
    }

    @SuppressWarnings("unchecked")
    private ElementBuilder<Statement>[] getConstructorBody(ElementBuilder<Statement> superCall, List<Parameter> parameters, LpcExpression[] immediates)
    {
        ElementBuilder<Statement>[] body = new ElementBuilder[1 + parameters.size() + immediates.length + 1];
        body[0] = superCall;
        int j = 1;
        for (int i = 0; i < parameters.size(); i++, j++)
        {
            Parameter parameter = parameters.get(i);
            body[j] = VM.Statement.assignField("e$" + parameter.getName(), VM.Expression.variable(parameter.getName()));
        }
        for (int i = 0; i < immediates.length; j++, i++)
        {
            body[j] = VM.Statement.assignField("p$" + i, ExpressionCompiler.getValue(immediates[i]));
        }
        body[body.length - 1] = VM.Statement.returnVoid();
        return body;
    }

    private Expression getOwner(String enclosingObjectName)
    {
        return VM.Expression.callMethod(VM.Expression.variable(enclosingObjectName), LpcObject.class, ByteCodeConstants.GET_OBJECT_INSTANCE);
    }

    private LpcExpression visitBlockFunction(ASTFunctionLiteral node)
    {
        ASTParameterDeclarations signatureNode = node.getBlockSignature();
        ASTStatementBlock blockNode = node.getBlockBody();
        Collection<ASTVariableReference> vars = findReferencedVariables(node);
        ClassSpec spec = createClassSpec(node);

        List<Parameter> parameters = new ArrayList<Parameter>();
        
        parameters.add(new ParameterSpec("s$owner").withType(LpcObject.class).create());
        Map<String, VariableResolution> variables = getReferencedVariables(vars, spec, parameters);

        ExpressionCompiler compiler = new ExpressionCompiler(_scope, _context);
        LpcExpression[] immediates = getImmediateVariables(node, spec, compiler);

        List< ? extends ArgumentDefinition> argumentDefinitions = MethodSupport.buildArgumentDefinitions(signatureNode, _scope);
        MethodSpec constructor = getBlockConstructor(parameters, immediates, argumentDefinitions);
        spec.withMethod(constructor);

        _scope.variables().pushScope();
        MethodSpec execute = getBlockExecute(blockNode, argumentDefinitions, compiler);
        _scope.variables().popScope();
        
        spec.withMethod(execute);
        store(spec);

        Expression[] args = new Expression[parameters.size()];
        args[0] = VM.Expression.thisObject();
        for (int i = 1; i < args.length; i++)
        {
            args[i] = variables.get(parameters.get(i).getName()).access();
        }

        Expression function = VM.Expression.construct(spec, constructor, args);
        return new LpcExpression(Types.FUNCTION, function);
    }

    private MethodSpec getBlockExecute(ASTStatementBlock blockNode, List< ? extends ArgumentDefinition> argumentDefinitions,
            ExpressionCompiler expressionCompiler)
    {
        int sizeGuess = argumentDefinitions.size() + blockNode.jjtGetNumChildren() * 2;
        List<ElementBuilder< ? extends Statement>> statements = new ArrayList<ElementBuilder< ? extends Statement>>(sizeGuess);
        StatementCompiler statementCompiler = new StatementCompiler(_scope, _context, statements);

        int i = 1;
        for (ArgumentDefinition argumentDefinition : argumentDefinitions)
        {
            String name = argumentDefinition.getName();
            LpcType type = argumentDefinition.getType();
            Expression init = expressionCompiler.getPositionalVariable(i);
            _scope.variables().declareLocal(name, type);
            statementCompiler.declareLpcVariable(statements, type, name, init);
            i++;
        }

        statementCompiler.compileBlock(blockNode);
        _scope.variables().popScope();

        statements.add(VM.Statement.returnObject(ByteCodeConstants.VOID));

        return getInvoke(statements);
    }

    private MethodSpec getBlockConstructor(List<Parameter> parameters, LpcExpression[] immediates,
            List< ? extends ArgumentDefinition> argumentDefinitions)
    {
        ConstructorSignature superConstructor = VM.Method.constructor(LpcFunction.class, ObjectInstance.class, ArgumentDefinition[].class);
        Expression owner = getOwner(parameters.get(0).getName());
        Expression objectDefinition = VM.Expression.callMethod(owner, ObjectInstance.class, ByteCodeConstants.GET_INSTANCE_DEFINITION);

        ConstructorSignature argumentSpecConstructor = VM.Method.constructor(ArgumentSpec.class, String.class, LpcType.class, Boolean.TYPE,
                ArgumentSemantics.class);
        Expression[] defs = new Expression[argumentDefinitions.size()];
        for (int i = 0; i < defs.length; i++)
        {
            ArgumentDefinition arg = argumentDefinitions.get(i);
            defs[i] = VM.Expression.construct( //
                    argumentSpecConstructor, //
                    VM.Expression.constant(arg.getName()), //
                    FieldCompiler.typeExpression(arg.getType(), objectDefinition), //
                    VM.Expression.constant(arg.isVarArgs()), //
                    VM.Expression.getEnum(arg.getSemantics())//
            );
        }
        ElementBuilder<Statement> superCall = VM.Statement.superConstructor(superConstructor, owner, VM.Expression.array(ArgumentDefinition.class,
                defs));

        ElementBuilder<Statement>[] body = getConstructorBody(superCall, parameters.subList(1, parameters.size()), immediates);
        return getConstructor(parameters, body);
    }

}
