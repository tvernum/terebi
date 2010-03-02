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

import org.adjective.stout.core.UnresolvedType;

import us.terebi.lang.lpc.compiler.CompilerObjectManager;
import us.terebi.lang.lpc.compiler.java.context.ClassLookup;
import us.terebi.lang.lpc.compiler.java.context.FunctionLookup;
import us.terebi.lang.lpc.compiler.java.context.ScopeLookup;
import us.terebi.lang.lpc.compiler.java.context.VariableLookup;
import us.terebi.lang.lpc.runtime.ObjectDefinition;

/**
 * 
 */
public class InnerClassScopeLookup implements ScopeLookup
{
    private static final String OWNER = "{" + InnerClassScopeLookup.class.getSimpleName() + ":parent}";

    private final ScopeLookup _parent;
    private final FunctionLookup _functions;
    private final VariableLookup _variables;

    public InnerClassScopeLookup(ScopeLookup parentScope, UnresolvedType outer)
    {
        _parent = parentScope;
        _functions = FunctionLookup.enclosing(_parent.functions(), OWNER, outer);
        _variables = new VariableLookup();
    }

    public void addInherit(String name, ObjectDefinition parent)
    {
        throw new UnsupportedOperationException("Cannot add inherit to inner class");
    }

    public ClassLookup classes()
    {
        return _parent.classes();
    }

    public FunctionLookup functions()
    {
        return _functions;
    }

    public boolean isSecureObject()
    {
        return _parent.isSecureObject();
    }

    public CompilerObjectManager objectManager()
    {
        return _parent.objectManager();
    }

    public VariableLookup variables()
    {
        return _variables;
    }

    public ObjectDefinition getInherit(String name)
    {
        return null;
    }

    public Collection<String> getInheritNames()
    {
        return Collections.emptySet();
    }

}
