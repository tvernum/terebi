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

package us.terebi.lang.lpc.runtime.jvm;

import us.terebi.lang.lpc.compiler.java.context.CompiledObjectDefinition;
import us.terebi.lang.lpc.compiler.java.context.CompiledObjectInstance;

/**
 * 
 */
public class InheritedObject<T>
{
    private final String _name;
    private final CompiledObjectDefinition _definition;
    private final CompiledObjectInstance _objectInstance;
    private final T _instance;

    public InheritedObject(String name, Class< ? extends T> type, CompiledObjectDefinition definition, CompiledObjectInstance instance)
    {
        _name = name;
        _definition = definition;
        _objectInstance = _definition.getInheritableInstance(instance);
        _instance = type.cast(_objectInstance.getImplementingObject());
    }

    public T get()
    {
        return _instance;
    }

    public String toString()
    {
        return getClass().getSimpleName() + ":" + _objectInstance.toString();
    }
    
    public CompiledObjectInstance getObjectInstance()
    {
        return _objectInstance;
    }

    public String getName()
    {
        return _name;
    }
}
