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

package us.terebi.lang.lpc.runtime.jvm.object;

import us.terebi.lang.lpc.compiler.java.context.CompiledObjectDefinition;

/**
 * 
 */
public class AbstractCompiledObjectInstance
{
    private final CompiledObjectDefinition _definition;
    private  final long _id;

    public AbstractCompiledObjectInstance(CompiledObjectDefinition definition, long id)
    {
        _definition = definition;
        _id = id;
    }

    public CompiledObjectDefinition getDefinition()
    {
        return _definition;
    }

    public long getId()
    {
        return _id;
    }

    public String getCanonicalName()
    {
        if (_id == 0)
        {
            return _definition.getName();
        }
        else
        {
            return _definition.getName() + "#" + _id;
        }
    }

    public String toString()
    {
        return getCanonicalName();
    }

}
