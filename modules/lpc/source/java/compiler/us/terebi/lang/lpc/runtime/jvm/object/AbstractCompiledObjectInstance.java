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

import java.lang.ref.WeakReference;

import us.terebi.lang.lpc.compiler.java.context.CompiledObjectDefinition;
import us.terebi.lang.lpc.compiler.java.context.CompiledObjectInstance;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.value.ObjectValue;

/**
 * 
 */
public abstract class AbstractCompiledObjectInstance implements CompiledObjectInstance
{
    private final CompiledObjectDefinition _definition;
    private final long _id;
    private final long _creationTime;
    private WeakReference<ObjectValue> _value;

    public AbstractCompiledObjectInstance(CompiledObjectDefinition definition, long id)
    {
        _definition = definition;
        _id = id;
        _creationTime = System.currentTimeMillis();
        _value = new WeakReference<ObjectValue>(null);
    }

    public CompiledObjectDefinition getDefinition()
    {
        return _definition;
    }

    public long getId()
    {
        return _id;
    }

    public long getCreationTime()
    {
        return _creationTime;
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

    public LpcValue asValue()
    {
        ObjectValue value = _value.get();
        if (value == null)
        {
            value = new ObjectValue(this);
            _value = new WeakReference<ObjectValue>(value);
        }
        return value;
    }

}
