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

import us.terebi.lang.lpc.compiler.CompileException;
import us.terebi.lang.lpc.compiler.java.context.CompiledObjectDefinition;
import us.terebi.lang.lpc.compiler.java.context.ScopeLookup;
import us.terebi.lang.lpc.parser.ast.ASTInherit;
import us.terebi.lang.lpc.parser.util.ASTUtil;

/**
 * 
 */
public class InheritSupport
{
    private final ASTInherit _node;
    private final ScopeLookup _scope;

    public InheritSupport(ASTInherit node, ScopeLookup scope)
    {
        _node = node;
        _scope = scope;
    }

    public CompiledObjectDefinition getParentObject()
    {
        ConstantHandler constants = new ConstantHandler();
        String from = constants.getString(ASTUtil.children(_node)).toString();
        CompiledObjectDefinition parent = _scope.objectManager().findObject(from, true);
        
        if (parent == null)
        {
            // @TODO dynamically load the object
            throw new CompileException(_node, "Cannot find inherited object " + from);
        }
        
        _scope.addInherit(parent.getBaseName(), parent);
        return parent;
    }

}
