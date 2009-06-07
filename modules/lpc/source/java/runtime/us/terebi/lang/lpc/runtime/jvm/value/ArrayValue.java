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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;

/**
 * 
 */
public class ArrayValue extends AbstractValue implements LpcValue
{
    private final List<LpcValue> _list;
    private final LpcType _type;

    public ArrayValue(LpcType type, Collection<? extends LpcValue> collection)
    {
        this(type, new ArrayList<LpcValue>(collection));
    }

    public ArrayValue(LpcType type, List<LpcValue> list)
    {
        _type = type;
        _list = list;
    }

    public ArrayValue(LpcType type, int size)
    {
        this(type, new ArrayList<LpcValue>(size));
    }

    public List<LpcValue> asList()
    {
        return _list;
    }
    
    protected CharSequence getDescription()
    {
        return _type.toString();
    }

    public LpcType getActualType()
    {
        return _type;
    }

    protected boolean valueEquals(LpcValue other)
    {
        return this.asList().equals(other.asList());
    }

    protected int valueHashCode()
    {
        return _list.hashCode();
    }

}
