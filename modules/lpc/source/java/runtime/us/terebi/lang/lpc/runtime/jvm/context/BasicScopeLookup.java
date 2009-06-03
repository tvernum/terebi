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

package us.terebi.lang.lpc.runtime.jvm.context;

import us.terebi.lang.lpc.compiler.java.context.ClassLookup;
import us.terebi.lang.lpc.compiler.java.context.FunctionLookup;
import us.terebi.lang.lpc.compiler.java.context.ObjectManager;
import us.terebi.lang.lpc.compiler.java.context.VariableLookup;
import us.terebi.lang.lpc.runtime.ObjectDefinition;

/**
 * 
 */
public class BasicScopeLookup implements ScopeLookup
{
    protected final ObjectManager _manager;
    protected final FunctionLookup _functions;
    protected final VariableLookup _variables;
    protected final ClassLookup _classes;
    protected final boolean _secure;

    public BasicScopeLookup(ObjectManager manager)
    {
        _manager = manager;
        _functions = new FunctionLookup();
        _variables = new VariableLookup();
        _classes = new ClassLookup();
        _secure = false; // @TODO
    }

    public FunctionLookup functions()
    {
        return _functions;
    }

    public ObjectManager objectManager()
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
        _classes.addInherit(name, parent);
        _functions.addInherit(name, parent);
        _variables.addInherit(name, parent);
    }

}
