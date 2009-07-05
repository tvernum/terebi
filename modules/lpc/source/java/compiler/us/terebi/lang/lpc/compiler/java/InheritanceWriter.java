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
import us.terebi.lang.lpc.compiler.java.context.CompiledObjectDefinition;
import us.terebi.lang.lpc.parser.ast.ASTInherit;
import us.terebi.lang.lpc.parser.ast.ASTUtil;
import us.terebi.lang.lpc.parser.ast.BaseASTVisitor;
import us.terebi.lang.lpc.parser.ast.ParserVisitor;
import us.terebi.lang.lpc.runtime.jvm.LpcInherited;

/**
 * 
 */
public class InheritanceWriter extends BaseASTVisitor implements ParserVisitor
{
    private final CompileContext _context;

    public InheritanceWriter(CompileContext context)
    {
        _context = context;
    }

    public Object visit(ASTInherit node, Object data)
    {
        _context.objectManager();
        ConstantHandler constants = new ConstantHandler(_context);
        String from = constants.getString(ASTUtil.children(node)).toString();
        CompiledObjectDefinition parent = _context.objectManager().findObject(from);

        if (parent == null)
        {
            // @TODO dynamically load the object
            throw new CompileException(node, "Cannot find inherited object " + from);
        }

        String name = getName(from);
        PrintWriter writer = _context.writer();
        writer.print("public @");
        writer.print(LpcInherited.class.getName());
        writer.print("(name=\"");
        writer.print(name);
        writer.print("\", lpc=\"");
        writer.print(from);
        writer.print("\", implementation=\"");
        String implementation = parent.getImplementationClass().getName();
        writer.print(implementation);
        writer.print("\") InheritedObject<");
        writer.print(implementation);
        writer.print("> inherit_");
        writer.print(name);
        writer.println(";");

        _context.addInherit(name, parent);

        return null;
    }

    private String getName(String path)
    {
        int s = path.lastIndexOf('/') + 1;
        int e = path.length();
        if (path.endsWith(".c"))
        {
            e -= 2;
        }
        return path.substring(s, e);
    }
}
