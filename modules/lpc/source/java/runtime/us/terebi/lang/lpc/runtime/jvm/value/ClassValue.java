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

package us.terebi.lang.lpc.runtime.jvm.value;

import java.util.Iterator;
import java.util.Map.Entry;

import us.terebi.lang.lpc.runtime.ClassInstance;
import us.terebi.lang.lpc.runtime.FieldDefinition;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.type.Types;

/**
 * 
 */
public class ClassValue extends AbstractValue implements LpcValue
{
    private final ClassInstance _instance;
    private final LpcType _type;

    public ClassValue(ClassInstance instance)
    {
        _instance = instance;
        _type = Types.classType(instance.getDefinition(), 0);
    }
    
    public ClassInstance asClass()
    {
        return _instance;
    }

    protected CharSequence getDescription()
    {
        return "class " + _instance.getDefinition().getName() + " { .. }";
    }

    protected boolean valueEquals(LpcValue other)
    {
        return this._instance.equals(other.asClass());
    }

    protected int valueHashCode()
    {
        return _instance.hashCode();
    }

    public LpcType getActualType()
    {
        return _type;
    }

    public CharSequence debugInfo()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("class ");
        builder.append(_instance.getDefinition().getName());
        builder.append(" { ");
        
        Iterator<Entry<FieldDefinition, LpcValue>> iterator = _instance.getFieldValues().entrySet().iterator();
        while (iterator.hasNext())
        {
            Entry<FieldDefinition, LpcValue> entry = iterator.next();
            builder.append(entry.getKey().getName());
            builder.append(" = ");
            builder.append(entry.getValue().debugInfo());
            if (iterator.hasNext())
            {
                builder.append(" , ");
            }
            else
            {
                builder.append(' ');
            }
        }
        
        builder.append("}");
        return builder;
    }

}
