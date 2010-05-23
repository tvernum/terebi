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

package us.terebi.lang.lpc.runtime.jvm.efun.string;

import java.util.ArrayList;
import java.util.List;

import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.ArgumentSemantics;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.LpcReference;
import us.terebi.lang.lpc.runtime.jvm.efun.AbstractEfun;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.FloatValue;
import us.terebi.lang.lpc.runtime.jvm.value.IntValue;
import us.terebi.lang.lpc.runtime.jvm.value.StringValue;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;

/**
 * 
 */
public class SscanfEfun extends AbstractEfun implements FunctionSignature, Callable
{
    public class Scanner
    {
        private final String _format;
        private final String _str;
        private int _strIndex;
        private int _fmtIndex;
        private List<LpcValue> _vars;

        public Scanner(String format, String str)
        {
            _format = format;
            _str = str;
            _strIndex = 0;
            _fmtIndex = 0;
            _vars = new ArrayList<LpcValue>();
        }

        public List<LpcValue> scan()
        {
            boolean more;
            do
            {
                more = _scan();
            } while (more);

            return _vars;
        }

        private boolean _scan()
        {
            if (_fmtIndex >= _format.length())
            {
                return false;
            }

            char ch = nextFormat();
            if (ch == '%')
            {
                boolean store = true;
                ch = nextFormat();
                if (ch == '*')
                {
                    store = false;
                    ch = nextFormat();
                }
                switch (ch)
                {
                    case '%':
                        return checkCharacter(ch);
                    case 'x':
                        return readHex(store);
                    case 'd':
                        return readInt(store);
                    case 'f':
                        return readFloat(store);
                    case '(':
                        return readRegexp(store);
                    case 's':
                        return readString(store);
                    default:
                        throw new LpcRuntimeException("Bad format character '" + ch + "' to sscanf");
                }
            }
            else
            {
                boolean match = checkCharacter(ch);
                return match;
            }
        }

        private boolean readString(boolean store)
        {
            if (_format.length() == _fmtIndex)
            {
                _vars.add(new StringValue(_str.substring(_strIndex)));
                return false;
            }
            int endFormat;
            for (endFormat = _fmtIndex; endFormat < _format.length(); endFormat++)
            {
                if (_format.charAt(endFormat) == '%')
                {
                    break;
                }
            }

            String search = _format.substring(_fmtIndex, endFormat);
            if (search.length() == 0)
            {
                throw new UnsupportedOperationException("sscanf - sequential formats not implemented (" + _format + ")");
            }

            int index = _str.indexOf(search, _strIndex);
            if (index == -1)
            {
                return false;
            }

            if (store)
            {
                _vars.add(new StringValue(_str.substring(_strIndex, index)));
            }
            _strIndex = index + search.length();
            _fmtIndex = endFormat;
            return true;
        }

        private boolean readRegexp(@SuppressWarnings("unused") boolean store)
        {
            throw new UnsupportedOperationException("sscanf(regexp) - Not implemented");
        }

        private boolean readInt(boolean store)
        {
            int start = _strIndex;
            while (Character.isDigit(peekStr()))
            {
                _strIndex++;
            }
            return readInt(start, 10, store);
        }

        private int peekStr()
        {
            if (_strIndex >= _str.length())
            {
                return -1;
            }
            return _str.charAt(_strIndex);
        }

        private boolean readHex(boolean store)
        {
            int start = _strIndex;
            while (isHexDigit(peekStr()))
            {
                _strIndex++;
            }
            return readInt(start, 16, store);
        }

        private boolean isHexDigit(int i)
        {
            return "0123456789ABCDEFabcdef".indexOf(i) != -1;
        }

        private boolean readInt(int start, int radix, boolean store)
        {
            if (_strIndex == start)
            {
                return false;
            }
            String sub = _str.substring(start, _strIndex);
            if (store)
            {
                _vars.add(new IntValue(Long.valueOf(sub, radix)));
            }
            return true;
        }

        private boolean readFloat(boolean store)
        {
            int start = _strIndex;
            while (isFloat(peekStr()))
            {
                _strIndex++;
            }
            if (_strIndex == start)
            {
                return false;
            }
            String sub = _str.substring(start, _strIndex);
            if (store)
            {
                _vars.add(new FloatValue(Double.valueOf(sub)));
            }
            return true;
        }

        private boolean isFloat(int i)
        {
            return Character.isDigit(i) || i == '.';
        }

        private boolean checkCharacter(char ch)
        {
            int next = nextString();
            return (next == ch);
        }

        private int nextString()
        {
            if (_strIndex >= _str.length())
            {
                return -1;
            }
            return _str.charAt(_strIndex++);
        }

        private char nextFormat()
        {
            return _format.charAt(_fmtIndex++);
        }

    }

    // int sscanf( string str, string fmt, mixed var1, mixed var2, ... );
    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        ArrayList<ArgumentDefinition> list = new ArrayList<ArgumentDefinition>();
        list.add(new ArgumentSpec("str", Types.STRING));
        list.add(new ArgumentSpec("format", Types.STRING));
        list.add(new ArgumentSpec("vars", Types.MIXED_ARRAY, true, ArgumentSemantics.IMPLICIT_REFERENCE));
        return list;
    }

    public LpcType getReturnType()
    {
        return Types.INT;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        checkArguments(arguments);
        String str = arguments.get(0).asString();
        String format = arguments.get(1).asString();
        List<LpcValue> variables = arguments.get(2).asList();

        List<LpcValue> values = new Scanner(format, str).scan();

        int index = 0;
        for (LpcValue value : values)
        {
            if (index >= variables.size())
            {
                break;
            }
            LpcValue argument = variables.get(index);
            checkSemantics(index, ArgumentSemantics.IMPLICIT_REFERENCE, argument);
            ((LpcReference) argument).set(value);
            index++;
        }

        return new IntValue(values.size());
    }

}
