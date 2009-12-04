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
import java.util.Map;

import us.terebi.lang.lpc.runtime.ByteSequence;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.ClassInstance;
import us.terebi.lang.lpc.runtime.ExtensionValue;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.LpcReference;
import us.terebi.lang.lpc.runtime.jvm.LpcReferenceValue;

/**
 * 
 */
public class ReferenceValue implements LpcReference, LpcValue, LpcReferenceValue
{
    private final LpcReference _reference;
    private LpcValue _value;

    public ReferenceValue(LpcValue value)
    {
        if (value == null)
        {
            throw new NullPointerException("Internal Error - Null value provided to reference value");
        }
        _value = value;
        _reference = null;
    }

    public ReferenceValue(LpcReference reference)
    {
        if (reference == null)
        {
            throw new NullPointerException("Internal Error - Null reference provided to reference value");
        }
        _reference = reference;
        _value = null;
    }

    public LpcValue get()
    {
        return this;
    }

    public LpcType getType()
    {
        if (_reference != null)
        {
            return _reference.getType();
        }
        return getActualType();
    }

    public boolean isSet()
    {
        if (_reference != null)
        {
            return _reference.isSet();
        }
        return true;

    }

    public LpcValue set(LpcValue value)
    {
        if (_reference == null)
        {
            return _value = value;
        }
        else
        {
            return _reference.set(value);
        }
    }

    private LpcValue getInternalValue()
    {
        if (_reference == null)
        {
            return _value;
        }
        else
        {
            LpcValue value = _reference.get();
            if (value == null)
            {
                throw new NullPointerException("Internal Error - Null value returned from reference " + _reference);
            }
            return value;
        }
    }

    public boolean asBoolean()
    {
        return getInternalValue().asBoolean();
    }

    public ByteSequence asBuffer()
    {
        return getInternalValue().asBuffer();
    }

    public Callable asCallable()
    {
        return getInternalValue().asCallable();
    }

    public ClassInstance asClass()
    {
        return getInternalValue().asClass();
    }

    public double asDouble()
    {
        return getInternalValue().asDouble();
    }

    public List<LpcValue> asList()
    {
        return getInternalValue().asList();
    }

    public long asLong()
    {
        return getInternalValue().asLong();
    }

    public Map<LpcValue, LpcValue> asMap()
    {
        return getInternalValue().asMap();
    }

    public ObjectInstance asObject()
    {
        return getInternalValue().asObject();
    }

    public String asString()
    {
        return getInternalValue().asString();
    }

    public LpcType getActualType()
    {
        return getInternalValue().getActualType();
    }

    public <T extends ExtensionValue> T asExtension(Class< ? extends T> type)
    {
        return getInternalValue().asExtension(type);
    }

    public CharSequence debugInfo()
    {
        return getInternalValue().debugInfo();
    }

}
