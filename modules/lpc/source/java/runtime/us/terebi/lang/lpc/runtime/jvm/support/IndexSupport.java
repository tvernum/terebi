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

package us.terebi.lang.lpc.runtime.jvm.support;

import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.LpcReference;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.ArrayElement;
import us.terebi.lang.lpc.runtime.jvm.value.Index;
import us.terebi.lang.lpc.runtime.jvm.value.MappingElement;
import us.terebi.lang.lpc.runtime.jvm.value.StringChar;
import us.terebi.lang.lpc.runtime.jvm.value.StringSubstring;
import us.terebi.lang.lpc.runtime.jvm.value.SubArray;

import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isString;

/**
 * 
 */
public class IndexSupport
{
    public static LpcReference index(LpcReference element, LpcValue indexValue, boolean reverse)
    {
        assert (LpcValue.class.isInstance(indexValue));

        LpcValue value = element.get();
        if (isString(value))
        {
            Index index = new Index(indexValue, reverse);
            return new StringChar(element, index);
        }
        if (MiscSupport.isArray(value))
        {
            Index index = new Index(indexValue, reverse);
            return new ArrayElement(element, index);
        }
        if (MiscSupport.isMapping(value))
        {
            if (reverse)
            {
                throw new LpcRuntimeException("Error - Cannot apply a reverse index to a mapping");
            }
            return new MappingElement(element, indexValue);
        }
        throw new UnsupportedOperationException("index (" + value.getActualType() + ") - Not implemented");
    }

    public static LpcValue index(LpcValue element, LpcValue indexValue, boolean reverse)
    {
        if (isString(element))
        {
            Index index = new Index(indexValue, reverse);
            return StringChar.get(element, index);
        }
        if (MiscSupport.isArray(element))
        {
            Index index = new Index(indexValue, reverse);
            return ArrayElement.get(element, index);
        }
        if (MiscSupport.isMapping(element))
        {
            if (reverse)
            {
                throw new LpcRuntimeException("Error - Cannot apply a reverse index to a mapping");
            }
            return MappingElement.get(element, indexValue);
        }
        throw new UnsupportedOperationException("index (" + element.getActualType() + ") - Not implemented");
    }

    public static LpcReference index(LpcReference element, LpcValue startIndex, boolean startReverse, LpcValue endIndex, boolean endReverse)
    {
        LpcType type = element.get().getActualType();
        if (Types.STRING.equals(type))
        {
            return new StringSubstring(element, new Index(startIndex, startReverse), new Index(endIndex, endReverse));
        }
        if (type.getArrayDepth() > 0)
        {
            return new SubArray(element, new Index(startIndex, startReverse), new Index(endIndex, endReverse));
        }
        throw new UnsupportedOperationException("index (" + type + ") - Not implemented");
    }

    public static LpcValue index(LpcValue element, LpcValue startIndex, boolean startReverse, LpcValue endIndex, boolean endReverse)
    {
        LpcType type = element.getActualType();
        if (Types.STRING.equals(type))
        {
            return StringSubstring.substring(element, new Index(startIndex, startReverse), new Index(endIndex, endReverse));
        }
        if (type.getArrayDepth() > 0)
        {
            return SubArray.subArray(element, new Index(startIndex, startReverse), new Index(endIndex, endReverse));
        }
        throw new UnsupportedOperationException("index (" + type + ") - Not implemented");
    }

}
