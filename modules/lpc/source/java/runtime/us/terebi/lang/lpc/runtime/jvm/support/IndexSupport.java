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

import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isString;

import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.LpcReference;
import us.terebi.lang.lpc.runtime.jvm.value.StringIndex;

/**
 * 
 */
public class IndexSupport
{
    public static LpcReference index(LpcReference element, LpcValue index, boolean reverse)
    {
        LpcValue value = element.get();
        if (isString(value))
        {
            return new StringIndex(element, index, reverse);
        }
        throw new UnsupportedOperationException("index - Not implemented");
    }

    public static LpcValue index(LpcValue element, LpcValue index, boolean reverse)
    {
        // @TODO Auto-generated method stub
        return null;
    }

    public static LpcReference index(LpcReference element, LpcValue startIndex, boolean startReverse, LpcValue endElement,
            boolean endReverse)
    {
        // @TODO Auto-generated method stub
        return null;
    }

    public static LpcValue index(LpcValue element, LpcValue startIndex, boolean startReverse, LpcValue endElement,
            boolean endReverse)
    {
        // @TODO Auto-generated method stub
        return null;
    }

}
