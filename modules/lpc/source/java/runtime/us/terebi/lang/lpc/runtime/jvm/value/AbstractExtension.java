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

import us.terebi.lang.lpc.runtime.ExtensionType;
import us.terebi.lang.lpc.runtime.ExtensionValue;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.jvm.type.Types;

/**
 * 
 */
public abstract class AbstractExtension extends AbstractValue implements ExtensionValue
{
    private LpcType _type;

    public AbstractExtension(ExtensionType extension)
    {
        _type = Types.extensionType(extension, 0);
    }

    public LpcType getActualType()
    {
        return _type;
    }

    public <T extends ExtensionValue> T asExtension(Class< ? extends T> type)
    {
        if (type.isAssignableFrom(getClass()))
        {
            return type.cast(this);
        }
        else
        {
            throw isNot(type.getSimpleName());
        }
    }
}
