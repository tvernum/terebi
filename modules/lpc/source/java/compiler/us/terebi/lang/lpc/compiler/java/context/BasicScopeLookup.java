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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import us.terebi.lang.lpc.compiler.CompilerObjectManager;
import us.terebi.lang.lpc.runtime.ObjectDefinition;

/**
 * 
 */
public class BasicScopeLookup implements ScopeLookup
{
    private final CompilerObjectManager _manager;
    private final Map<String, ObjectDefinition> _inherit;
    protected final FunctionLookup _functions;
    protected final VariableLookup _variables;
    protected final ClassLookup _classes;
    protected final boolean _secure;

    public BasicScopeLookup(CompilerObjectManager manager)
    {
        if (manager == null)
        {
            throw new IllegalArgumentException("No " + CompilerObjectManager.class.getSimpleName() + " supplied to " + getClass().getName());
        }
        _manager = manager;
        _inherit = new HashMap<String, ObjectDefinition>();
        _functions = new FunctionLookup();
        _variables = new VariableLookup();
        _classes = new ClassLookup();
        _secure = false; // @TODO
    }

    public FunctionLookup functions()
    {
        return _functions;
    }

    public CompilerObjectManager objectManager()
    {
        return _manager;
    }

    public boolean isSecureObject()
    {
        return _secure;
    }

    public VariableLookup variables()
    {
        return _variables;
    }

    public ClassLookup classes()
    {
        return _classes;
    }

    public void addInherit(String name, ObjectDefinition parent)
    {
        _inherit.put(name, parent);
        _classes.addInherit(name, parent);
        _functions.addInherit(name, parent);
        _variables.addInherit(name, parent);
    }

    public ObjectDefinition getInherit(String name)
    {
        return _inherit.get(name);
    }

    public Collection<String> getInheritNames()
    {
        return _inherit.keySet();
    }

    public String toString()
    {
        return getClass().getSimpleName();
    }
}
