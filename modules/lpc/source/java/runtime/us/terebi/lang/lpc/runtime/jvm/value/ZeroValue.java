/* ------------------------------------------------------------------------
 * Copyright 2010 Tim Vernum
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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import us.terebi.lang.lpc.compiler.java.context.GenericSignature;
import us.terebi.lang.lpc.runtime.ByteSequence;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.ClassInstance;
import us.terebi.lang.lpc.runtime.ExtensionValue;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.LpcConstants;
import us.terebi.lang.lpc.runtime.jvm.type.Types;

/**
 * 
 */
public class ZeroValue extends AbstractValue implements LpcValue
{
    private static final Callable CALLABLE = new Callable()
    {
        public FunctionSignature getSignature()
        {
            return GenericSignature.INSTANCE;
        }

        public ObjectInstance getOwner()
        {
            return null;
        }

        public CharSequence getName()
        {
            return "0";
        }

        public Kind getKind()
        {
            return Kind.FUNCTION;
        }

        public LpcValue execute(LpcValue... arguments)
        {
            return NilValue.INSTANCE;
        }

        public LpcValue execute(List< ? extends LpcValue> arguments)
        {
            return NilValue.INSTANCE;
        }
    };
    
    public static final ZeroValue INSTANCE = new ZeroValue();

    public boolean asBoolean()
    {
        return false;
    }

    public ByteSequence asBuffer()
    {
        return NoBytes.INSTANCE;
    }

    public Callable asCallable()
    {
        return CALLABLE;
    }

    public ClassInstance asClass()
    {
        throw isNot("a class");
    }

    public double asDouble()
    {
        return 0;
    }

    public <T extends ExtensionValue> T asExtension(Class< ? extends T> type)
    {
        throw isNot("extension " + type.getSimpleName());
    }

    public List<LpcValue> asList()
    {
        return Collections.<LpcValue> emptyList();
    }

    public long asLong()
    {
        return 0;
    }

    public Map<LpcValue, LpcValue> asMap()
    {
        return Collections.<LpcValue, LpcValue> emptyMap();
    }

    public ObjectInstance asObject()
    {
        throw isNot("an object");
    }

    public String asString()
    {
        return "0";
    }

    public CharSequence debugInfo()
    {
        return "0";
    }

    public LpcType getActualType()
    {
        return Types.ZERO;
    }

    protected CharSequence getDescription()
    {
        return "0";
    }

    protected boolean valueEquals(LpcValue other)
    {
        if (other instanceof ZeroValue)
        {
            return true;
        }
        LpcType type = other.getActualType();
        if (type.getArrayDepth() > 0)
        {
            return other.asList().size() == 0;
        }
        switch (type.getKind())
        {
            case CLASS:
            case EXTENSION:
            case FUNCTION:
            case MIXED:
            case OBJECT:
                return false;
            case NIL:
            case VOID:
                return true;
            case BUFFER:
                return other.asBuffer().length() == 0;
            case FLOAT:
                return other.asDouble() == 0.0;
            case INT:
                return other.asLong() == 0;
            case MAPPING:
                return other.asMap().isEmpty();
            case STRING:
                return other.asString().length() == 0;
            default:
                return false;
        }
    }

    protected int valueHashCode()
    {
        return LpcConstants.INT.ZERO.hashCode();
    }

}
