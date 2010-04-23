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
import java.util.List;

import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.support.ValueSupport;

/**
 * 
 */
public class ArrayBuilder
{
    private final LpcType _type;
    private final List<LpcValue> _list;

    public ArrayBuilder(LpcType type, int capacity)
    {
        _type = type;
        _list = new ArrayList<LpcValue>(capacity);
    }

    public ArrayValue toArray()
    {
        return new ArrayValue(_type, _list);
    }

    public void add(LpcValue value)
    {
        _list.add(value);
    }

    public void add(long value)
    {
        add(ValueSupport.intValue(value));
    }

    public void add(String value)
    {
        add(new StringValue(value));
    }

    public void add(ArrayBuilder array)
    {
        add(array.toArray());
    }

    public void add(ObjectInstance instance)
    {
        add(instance.asValue());
    }

}
