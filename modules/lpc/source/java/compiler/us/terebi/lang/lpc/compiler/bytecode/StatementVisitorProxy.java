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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import us.terebi.lang.lpc.compiler.bytecode.context.CompileContext;
import us.terebi.lang.lpc.parser.ast.Node;
import us.terebi.lang.lpc.parser.ast.ParserVisitor;
import us.terebi.lang.lpc.parser.ast.PragmaNode;

/**
 * 
 */
public class StatementVisitorProxy
{
    public static class Handler implements InvocationHandler
    {
        private final ParserVisitor _delegate;
        private final CompileContext _context;

        public Handler(ParserVisitor delegate, CompileContext context)
        {
            _delegate = delegate;
            _context = context;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            if (args.length > 0 && args[0] instanceof Node)
            {
                Node node = (Node) args[0];
                if (node instanceof PragmaNode)
                {
                    PragmaNode pragma = (PragmaNode) args[0];
                    _context.options().processPragmas(pragma);
                }
            }
            try
            {
                return method.invoke(_delegate, args);
            }
            catch (InvocationTargetException e)
            {
                throw e.getCause();
            }
        }
    }

    public static ParserVisitor create(ParserVisitor delegate, CompileContext context)
    {
        Class< ? >[] interfaces = new Class[] { ParserVisitor.class };
        Object proxy = Proxy.newProxyInstance(StatementVisitorProxy.class.getClassLoader(), interfaces, new Handler(delegate, context));
        return (ParserVisitor) proxy;
    }
}
