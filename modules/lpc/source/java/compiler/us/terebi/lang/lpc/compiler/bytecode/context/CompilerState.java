/* ------------------------------------------------------------------------
 * Copyright 2010 Tim Vernum
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

/**
 * 
 */
public class CompilerState implements CompileContext
{
    private final CompileSettings _settings;
    private final MethodInfo _method;
    private final boolean _functionLiteral;

    private CompilerState(CompileSettings settings, MethodInfo method, boolean functionLiteral)
    {
        _settings = settings;
        _method = method;
        _functionLiteral = functionLiteral;
    }

    public static CompilerState root(CompileSettings settings)
    {
        return new CompilerState(settings, null, false);
    }

    public CompilerState enterFunctionLiteral()
    {
        return new CompilerState(_settings, _method, true);
    }

    public CompilerState enterMethod(MethodInfo method)
    {
        return new CompilerState(_settings, method, false);
    }

    public ClassSpec currentClass()
    {
        return _settings.currentClass();
    }

    public DebugOptions debugOptions()
    {
        return _settings.getDebugOptions();
    }

    public boolean inFunctionLiteral()
    {
        return _functionLiteral;
    }

    public boolean isTimeCheckEnabled()
    {
        return _settings.isTimeCheckEnabled();
    }

    public LineMapping lineMapping()
    {
        return _settings.getLineMapping();
    }

    public MethodInfo method()
    {
        return _method;
    }

    public CompileOptions options()
    {
        return _settings.options();
    }

    public void popClass(ClassSpec spec)
    {
        _settings.popClass(spec);
    }

    public ClassSpec publicClass()
    {
        return _settings.publicClass();
    }

    public void pushClass(ClassSpec spec)
    {
        _settings.pushClass(spec);
    }

    public ClassStore store()
    {
        return _settings.store();
    }

    public ASTObjectDefinition tree()
    {
        return _settings.tree();
    }

}
