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
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;

/**
 * 
 */
public abstract class AbstractValue implements LpcValue
{
    public AbstractValue()
    {
        super();
    }

    protected abstract CharSequence getDescription();

    protected LpcRuntimeException isNot(String kind)
    {
        return new LpcRuntimeException(getActualType().toString() + ' ' + getDescription() + " is not " + kind);
    }

    public boolean asBoolean()
    {
        return true;
    }

    public ByteSequence asBuffer()
    {
        throw isNot("a buffer");
    }

    public Callable asCallable()
    {
        throw isNot("a function");
    }

    public ClassInstance asClass()
    {
        throw isNot("a class");
    }

    public double asDouble()
    {
        throw isNot("a float");
    }

    public List<LpcValue> asList()
    {
        throw isNot("an array");
    }

    public long asLong()
    {
        throw isNot("an int");
    }

    public Map<LpcValue, LpcValue> asMap()
    {
        throw isNot("a mapping");
    }

    public ObjectInstance asObject()
    {
        throw isNot("an object");
    }

    public String asString()
    {
        throw isNot("a string");
    }

    public int hashCode()
    {
        return getActualType().hashCode() ^ valueHashCode();
    }

    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (obj instanceof LpcValue)
        {
            LpcValue other = (LpcValue) obj;
            return equalsOther(other);
        }
        return false;
    }

    protected boolean equalsOther(LpcValue other)
    {
        if (this.getActualType().equals(other.getActualType()))
        {
            return valueEquals(other);
        }
        return false;
    }

    protected abstract boolean valueEquals(LpcValue other);

    protected abstract int valueHashCode();

    public String toString()
    {
        return getDescription().toString();
    }

    public <T extends ExtensionValue> T asExtension(Class< ? extends T> type)
    {
        throw isNot("an extension");
    }
}
