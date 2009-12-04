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

import org.adjective.stout.builder.ClassSpec;

import us.terebi.lang.lpc.compiler.ClassStore;
import us.terebi.lang.lpc.parser.ast.ASTObjectDefinition;

/**
 * 
 */
public class CompileContext
{
    private final ClassStore _store;
    private final CompileOptions _options;
    private final ASTObjectDefinition _tree;
    private final ClassSpec _class;

    public CompileContext(ClassStore store, CompileOptions options, ASTObjectDefinition tree, ClassSpec classSpec)
    {
        _store = store;
        _options = options;
        _tree = tree;
        _class = classSpec;
    }

    public ClassStore store()
    {
        return _store;
    }

    public CompileOptions options()
    {
        return _options;
    }

    public ASTObjectDefinition tree()
    {
        return _tree;
    }

    public ClassSpec publicClass()
    {
        return _class;
    }
}
