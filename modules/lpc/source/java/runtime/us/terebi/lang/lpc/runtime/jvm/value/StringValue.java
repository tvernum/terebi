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

import java.util.ArrayList;
import java.util.List;

import us.terebi.lang.lpc.runtime.ByteSequence;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.type.Types;

/**
 * 
 */
public class StringValue extends AbstractValue implements LpcValue
{
    private final String _value;

    public StringValue(String value)
    {
        _value = value;
    }

    public StringValue(CharSequence charSequence)
    {
        this(charSequence.toString());
    }

    public boolean asBoolean()
    {
        return _value != null;
    }

    public ByteSequence asBuffer()
    {
        return new ByteArraySequence(_value.getBytes());
    }

    public List<LpcValue> asList()
    {
        ArrayList<LpcValue> list = new ArrayList<LpcValue>(_value.length());
        for (int i = 0; i < _value.length(); i++)
        {
            list.add(new IntValue(_value.charAt(i)));
        }
        return list;
    }

    protected CharSequence getDescription()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("string \"");
        builder.append(_value);
        builder.append("\"");
        return builder;
    }

    public String asString()
    {
        return _value;
    }

    public LpcType getActualType()
    {
        return Types.STRING;
    }

    protected boolean valueEquals(LpcValue other)
    {
        return this.asString().equals(other.asString());
    }

    protected int valueHashCode()
    {
        return _value.hashCode();
    }

    public String toString()
    {
        return _value;
    }

    public CharSequence debugInfo()
    {
        return encodeString(_value);
    }

    public static CharSequence encodeString(String value)
    {
        StringBuilder builder = new StringBuilder();
        builder.append('"');
        for (int i = 0; i < value.length(); i++)
        {
            char ch = value.charAt(i);
            switch (ch)
            {
                case '\r':
                    builder.append("\\r");
                    break;
                case '\n':
                    builder.append("\\n");
                    break;
                case '\t':
                    builder.append("\\t");
                    break;
                case '\\':
                    builder.append("\\\\");
                    break;
                case '\"':
                    builder.append("\\\"");
                    break;
                case '\b':
                    builder.append("\\b");
                    break;
                case 7:
                    builder.append("\\a");
                    break;
                case 27:
                    builder.append("\\e");
                    break;
                default:
                    if (ch < 32 || ch > 126)
                    {
                        builder.append("\\");
                        builder.append(Integer.toOctalString(ch));
                    }
                    else
                    {
                        builder.append(ch);
                    }
                    break;
            }
        }
        builder.append('"');
        return builder;
    }

}
