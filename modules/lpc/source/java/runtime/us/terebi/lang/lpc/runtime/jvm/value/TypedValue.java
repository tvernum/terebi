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
import us.terebi.lang.lpc.runtime.LpcType.Kind;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;
import us.terebi.lang.lpc.runtime.jvm.support.MiscSupport;
import us.terebi.lang.lpc.runtime.jvm.type.Types;

import static us.terebi.lang.lpc.compiler.util.TypeSupport.isMatchingType;

/**
 * 
 */
public class TypedValue implements LpcValue
{
    private final LpcType _type;
    private final LpcValue _value;

    private TypedValue(LpcType type, LpcValue value)
    {
        if (!isMatchingType(value.getActualType(), type))
        {
            throw new LpcRuntimeException("Invalid type: Expected " + type + " but is " + value.getActualType());
        }
        _type = type;
        _value = value;
    }

    public static LpcValue type(LpcType type, LpcValue value)
    {
        if (value instanceof TypedValue)
        {
            TypedValue typed = (TypedValue) value;
            if (typed._type.equals(type))
            {
                return typed;
            }
        }
        if (MiscSupport.isDynamicType(type))
        {
            return value;
        }
        return new TypedValue(type, value);
    }

    public static LpcValue type(Kind kind, int arrayDepth, LpcValue value)
    {
        return type(Types.getType(kind, null, arrayDepth), value);
    }

    public LpcType getActualType()
    {
        if (MiscSupport.isZero(_value))
        {
            return Types.ZERO;
        }
        if (MiscSupport.isNil(_value))
        {
            return Types.NIL;
        }
        return _type;
    }

    public boolean asBoolean()
    {
        return _value.asBoolean();
    }

    public ByteSequence asBuffer()
    {
        return _value.asBuffer();
    }

    public Callable asCallable()
    {
        return _value.asCallable();
    }

    public ClassInstance asClass()
    {
        return _value.asClass();
    }

    public double asDouble()
    {
        return _value.asDouble();
    }

    public List<LpcValue> asList()
    {
        return _value.asList();
    }

    public long asLong()
    {
        return _value.asLong();
    }

    public Map<LpcValue, LpcValue> asMap()
    {
        return _value.asMap();
    }

    public ObjectInstance asObject()
    {
        return _value.asObject();
    }

    public String asString()
    {
        return _value.asString();
    }

    public String toString()
    {
        return "(" + _type + ")" + _value.toString();
    }

    public int hashCode()
    {
        return _value.hashCode();
    }

    public boolean equals(Object obj)
    {
        return _value.equals(obj);
    }

    public <T extends ExtensionValue> T asExtension(Class< ? extends T> type)
    {
        return _value.asExtension(type);
    }

    public CharSequence debugInfo()
    {
        return _value.debugInfo();
    }
}
