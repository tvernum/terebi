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
public interface CompileContext
{
    public MethodInfo method();
    public boolean inFunctionLiteral();

    public CompileOptions options();
    public ClassStore store();

    public ASTObjectDefinition tree();

    public ClassSpec publicClass();
    public ClassSpec currentClass();
    public void popClass(ClassSpec spec);
    public void pushClass(ClassSpec spec);
    
    public boolean isTimeCheckEnabled();
    public DebugOptions debugOptions();
    public LineMapping lineMapping();
    
    public CompilerState enterFunctionLiteral();
    public CompilerState enterMethod(MethodInfo method);
}
