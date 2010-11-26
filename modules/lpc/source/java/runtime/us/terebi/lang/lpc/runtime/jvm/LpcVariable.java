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

package us.terebi.lang.lpc.runtime.jvm;

import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.exception.InternalError;
import us.terebi.lang.lpc.runtime.jvm.value.TypedValue;

import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isMoreSpecific;

/**
 * 
 */
public class LpcVariable implements LpcReference
{
    private final String _name;
    private final LpcType _type;
    private LpcValue _value;

    public LpcVariable(String name, LpcType type)
    {
        this(name, type, null);
    }

    public LpcVariable(String name, LpcType type, LpcValue value)
    {
        _name = name;
        _type = type;
        set(value);
    }

    public String getName()
    {
        return _name;
    }

    public LpcValue get()
    {
        return _value;
    }

    public LpcType getType()
    {
        return _type;
    }

    public boolean isSet()
    {
        return _value != null;
    }

    public LpcValue set(LpcValue value)
    {
        if (isSet() && value == null)
        {
            throw new IllegalStateException("Internal Error - Attempt to unset variable: " + this);
        }
        if (value != null && !LpcValue.class.isInstance(value))
        {
            throw new InternalError("Attempt to set " + this + " to " + value.getClass() + " (" + value + ")");
        }
        if (value != null && isMoreSpecific(_type, value.getActualType()))
        {
            return _value = TypedValue.type(_type, value);
        }
        else
        {
            return _value = value;
        }
    }

    public String toString()
    {
        return getClass().getSimpleName() + "{" + _type + ' ' + _name + " = " + _value + "}";
    }
}
