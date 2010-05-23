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

package us.terebi.lang.lpc.compiler.bytecode.context;

import org.adjective.stout.builder.ClassSpec;

import us.terebi.lang.lpc.compiler.ClassStore;
import us.terebi.lang.lpc.compiler.bytecode.CompileOptions;
import us.terebi.lang.lpc.parser.LineMapping;
import us.terebi.lang.lpc.parser.ast.ASTObjectDefinition;
import us.terebi.util.collection.ArrayStack;
import us.terebi.util.collection.Stack;

/**
 * 
 */
public class CompileSettings
{
    private final ClassStore _store;
    private final CompileOptions _options;
    private final ASTObjectDefinition _tree;
    private final ClassSpec _publicClass;
    private final Stack<ClassSpec> _classes;
    private final LineMapping _lineMapping;
    private final DebugOptions _debug;
    private final boolean _timeCheck;

    public CompileSettings(ClassStore store, CompileOptions options, ASTObjectDefinition tree, ClassSpec classSpec, LineMapping lineMapping,
            DebugOptions debug, boolean timeCheck)
    {
        _store = store;
        _options = options;
        _tree = tree;
        _publicClass = classSpec;
        _lineMapping = lineMapping;
        _debug = debug;
        _timeCheck = timeCheck;
        _classes = new ArrayStack<ClassSpec>();
        _classes.push(classSpec);
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
        return _publicClass;
    }

    public ClassSpec currentClass()
    {
        return _classes.peek();
    }

    public void pushClass(ClassSpec spec)
    {
        _classes.push(spec);
    }

    public void popClass(ClassSpec spec)
    {
        ClassSpec pop = _classes.pop();
        assert pop == spec;
    }

    public LineMapping getLineMapping()
    {
        return _lineMapping;
    }

    public DebugOptions getDebugOptions()
    {
        return _debug;
    }
    

    public boolean isTimeCheckEnabled()
    {
        return _timeCheck;
    }
}
