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

import java.util.List;

import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.LpcReference;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;
import us.terebi.lang.lpc.runtime.jvm.type.Types;

/**
 * 
 */
public class ArrayElement implements LpcReference
{
    private final LpcReference _array;
    private final Index _index;

    public ArrayElement(LpcReference array, Index index)
    {
        _array = array;
        _index = index;
    }

    public LpcValue get()
    {
        List<LpcValue> list = _array.get().asList();
        int index = getIndex(list);
        return list.get(index);
    }

    private int getIndex(List<LpcValue> list)
    {
        long index = _index.evaluate(list.size());
        if (index < 0)
        {
            throw new LpcRuntimeException("Array index (" + index + ") out of bounds");
        }
        if (index >= list.size())
        {
            throw new LpcRuntimeException("Array index (" + index + ") out of bounds (" + list.size() + ")");
        }
        return (int) index;
    }

    public LpcType getType()
    {
        return Types.elementOf(_array.getType());
    }

    public boolean isSet()
    {
        return true;
    }

    public void set(LpcValue value)
    {
        List<LpcValue> list = _array.get().asList();
        int index = getIndex(list);
        list.set(index, value); 
        // @TODO - is this the right semantics? (change the array, rather than the value in the variable...)
    }

}
