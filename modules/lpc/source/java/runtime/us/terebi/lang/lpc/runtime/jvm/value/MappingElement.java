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

import java.util.Map;

import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.LpcReference;
import us.terebi.lang.lpc.runtime.jvm.type.Types;

/**
 * 
 */
public class MappingElement implements LpcReference
{
    private final LpcReference _element;
    private final LpcValue _index;

    public MappingElement(LpcReference element, LpcValue index)
    {
        _element = element;
        _index = index;
    }

    public LpcValue get()
    {
        return get(_element.get(), _index);
    }

    public static LpcValue get(LpcValue mapping, LpcValue index)
    {
        Map<LpcValue, LpcValue> map = mapping.asMap();
        LpcValue value = map.get(index);
        if (value == null)
        {
            return NilValue.INSTANCE;
        }
        return value;
    }

    public LpcType getType()
    {
        return Types.MIXED;
    }

    public boolean isSet()
    {
        return true;
    }

    public LpcValue set(LpcValue value)
    {
        Map<LpcValue, LpcValue> map = _element.get().asMap();
        map.put(_index, value);
        // @TODO what if "value" is NIL ?
        return value;
    }

}
