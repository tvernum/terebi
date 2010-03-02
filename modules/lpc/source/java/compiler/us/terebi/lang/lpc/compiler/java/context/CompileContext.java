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

package us.terebi.lang.lpc.compiler.java.context;

import java.io.Closeable;
import java.io.PrintWriter;
import java.util.Collection;

import us.terebi.lang.lpc.compiler.CompilerObjectManager;
import us.terebi.lang.lpc.runtime.ObjectDefinition;


/**
 * 
 */
public class CompileContext implements Closeable, ScopeLookup
{
    private final ScopeLookup _scope;
    private final PrintWriter _writer;
    
    public CompileContext(PrintWriter writer, CompilerObjectManager manager)
    {
        _scope = new BasicScopeLookup(manager);
        _writer = writer;
    }

    public PrintWriter writer()
    {
        return _writer;
    }

    public void close()
    {
        _writer.close();
    }

    public void addInherit(String name, ObjectDefinition parent)
    {
        _scope.addInherit(name, parent);
    }

    public ClassLookup classes()
    {
        return _scope.classes();
    }

    public FunctionLookup functions()
    {
        return _scope.functions();
    }

    public boolean isSecureObject()
    {
        return _scope.isSecureObject();
    }

    public CompilerObjectManager objectManager()
    {
        return _scope.objectManager();
    }

    public VariableResolver variables()
    {
        return _scope.variables();
    }

    public ObjectDefinition getInherit(String name)
    {
        return _scope.getInherit(name);
    }

    public Collection<String> getInheritNames()
    {
        return _scope.getInheritNames();
    }
    
    
}
