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

import java.util.HashMap;
import java.util.Map;

import us.terebi.lang.lpc.compiler.ObjectCompiler;

/**
 * 
 */
public class CompilerObjectManager implements ObjectManager
{
    private final Map<String, CompiledObjectDefinition> _definitions;
    private ObjectCompiler _compiler;

    public CompilerObjectManager()
    {
        _definitions = new HashMap<String, CompiledObjectDefinition>();
        _compiler = null;
    }

    public void setCompiler(ObjectCompiler compiler)
    {
        _compiler = compiler;
    }

    public CompiledObjectDefinition findObject(String name)
    {
        CompiledObjectDefinition definition = _definitions.get(normalise(name));
        if (definition == null && _compiler != null)
        {
            definition = _compiler.compile(name);
            registerObject(definition);
        }
        return definition;
    }

    public void registerObject(CompiledObjectDefinition object)
    {
        _definitions.put(normalise(object.getName()), object);
    }

    private String normalise(String name)
    {
        if (name.endsWith(".c"))
        {
            name = name.substring(0, name.length() - 2);
        }
        name = name.replace("//", "/");
        if (name.charAt(0) != '/')
        {
            name = "/" + name;
        }
        return name;
    }
}
