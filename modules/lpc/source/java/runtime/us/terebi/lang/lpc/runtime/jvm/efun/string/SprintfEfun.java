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

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.ListIterator;

import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.ArgumentSemantics;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.efun.AbstractEfun;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.StringValue;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;

import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isInt;
import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isObject;
import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isString;

/**
 * 
 */
public class SprintfEfun extends AbstractEfun implements FunctionSignature, Callable
{
    // string sprintf( string format, ... );
    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        ArrayList<ArgumentDefinition> list = new ArrayList<ArgumentDefinition>();
        list.add(new ArgumentSpec("format", Types.STRING));
        list.add(new ArgumentSpec("vars", Types.MIXED_ARRAY, true, ArgumentSemantics.BY_VALUE));
        return list;
    }

    public LpcType getReturnType()
    {
        return Types.STRING;
    }

    private enum Alignment
    {
        LEFT, RIGHT, CENTRE;
    }

    private enum StringMode
    {
        STANDARD, COLUMN, TABLE;
    }

    private enum SignMode
    {
        NONE, SPACE, PLUS;
    }

    class Format
    {
        public char type = '?';
        public CharSequence padding = " ";
        public Alignment alignment = Alignment.RIGHT;
        public StringMode mode = StringMode.STANDARD;
        public SignMode sign = SignMode.NONE;
        public int size = -1;
        public int precision = -1;
        public boolean array = false;
        public int startPosition;
        public int endPosition;
    }

    private class Output
    {
        public StringBuilder result;
        public StringBuilder line;
        public List<StringBuilder> additionalLines;

        public Output()
        {
            result = new StringBuilder();
            line = new StringBuilder();
            additionalLines = new ArrayList<StringBuilder>();
        }

        public void newLine()
        {
            advance(true);
        }

        private void advance(boolean nl)
        {
            result.append(line);
            if (nl)
            {
                result.append('\n');
            }
            for (StringBuilder l : additionalLines)
            {
                result.append(l);
                if (nl)
                {
                    result.append('\n');
                }
            }
            line = new StringBuilder();
            additionalLines.clear();
        }

        public void append(char ch)
        {
            line.append(ch);
        }

        public void append(CharSequence string)
        {
            line.append(string);
        }

        public LpcValue asValue()
        {
            if (line.length() > 0)
            {
                this.advance(false);
            }
            return new StringValue(this.result);
        }

        public void append(int lineNumber, CharSequence text)
        {
            if (lineNumber == 0)
            {
                append(text);
                return;
            }
            while (additionalLines.size() < lineNumber)
            {
                additionalLines.add(new StringBuilder());
            }
            StringBuilder buffer = additionalLines.get(lineNumber - 1);
            while (buffer.length() < this.line.length())
            {
                buffer.append(' ');
            }
            buffer.append(text);
        }
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        String format = arguments.get(0).asString();
        Output output = new Output();

        ListIterator< ? extends LpcValue> iterator = arguments.get(1).asList().listIterator();

        int formatPosn = 0;
        while (formatPosn < format.length())
        {
            char ch = format.charAt(formatPosn);
            if (ch == '\n')
            {
                output.newLine();
            }
            else if (ch == '%')
            {
                Format defn = readFormat(format, formatPosn);
                applyFormat(output, defn, iterator);
                formatPosn = defn.endPosition;
            }
            else
            {
                output.append(ch);
            }
            formatPosn++;
        }
        return output.asValue();
    }

    private void applyFormat(Output output, Format format, ListIterator< ? extends LpcValue> iterator)
    {
        if (format.type == '%')
        {
            output.append('%');
            return;
        }

        if (format.size == -2)
        {
            format.size = (int) iterator.next().asLong();
        }

        if (format.array)
        {
            List<LpcValue> list = iterator.next().asList();
            for (LpcValue value : list)
            {
                applyFormat(output, format, value);
            }
        }
        else
        {
            LpcValue value = iterator.next();
            applyFormat(output, format, value);
        }
    }

    private void applyFormat(Output output, Format format, LpcValue value)
    {
        switch (format.type)
        {
            case 'd':
            case 'i':
                formatInteger(output, format, value.asLong(), 10, false);
                return;
            case 'o':
                formatInteger(output, format, value.asLong(), 8, false);
                return;
            case 'x':
                formatInteger(output, format, value.asLong(), 16, false);
                return;
            case 'X':
                formatInteger(output, format, value.asLong(), 16, true);
                return;
            case 'f':
                formatFloat(output, format, value.asDouble());
                return;
            case 's':
                formatString(output, format, value.asString());
                return;
            case 'O':
                formatValue(output, format, value);
                return;
            case 'c':
                if (isString(value))
                {
                    formatChar(output, format, value.asString().charAt(0));
                }
                else
                {
                    formatChar(output, format, value.asLong());
                }
                return;
            default:
                throw new UnsupportedOperationException("sprintf( %" + format.type + " ) - Not implemented");
        }
    }

    private void formatValue(Output output, Format format, LpcValue value)
    {
        if (isInt(value))
        {
            formatInteger(output, format, value.asLong(), 10, false);
            return;
        }
        if (isString(value))
        {
            formatString(output, format, value.asString());
            return;
        }
        if (isObject(value))
        {
            formatString(output, format, value.asObject().getCanonicalName());
            return;
        }
        formatString(output, format, value.debugInfo());
    }

    private void formatString(Output output, Format format, CharSequence string)
    {
        StringBuilder builder = new StringBuilder();
        if (format.mode == StringMode.COLUMN)
        {
            if (format.size < 1)
            {
                throw new LpcRuntimeException(getName() + " - column mode ('=') requires the field size to be set");
            }
            int nParts = (string.length() + format.size - 1) / format.size;
            if (nParts > 1)
            {
                for (int i = nParts - 1; i >= 0; i--)
                {
                    int start = i * format.size;
                    int end = start + format.size;
                    if (end > string.length())
                    {
                        end = string.length();
                    }
                    CharSequence part = string.subSequence(start, end);
                    builder.append(part);
                    output.append(i, align(builder, format));
                    builder = new StringBuilder();
                }
                return;
            }
            builder.append(string);
        }
        if (format.precision != -1 && string.length() > format.precision)
        {
            builder.append(string.subSequence(0, format.precision));
        }
        else
        {
            builder.append(string);
        }
        output.append(align(builder, format));
    }

    private void formatFloat(Output output, Format format, double value)
    {
        if (format.precision == -1)
        {
            format.precision = 6;
        }

        StringBuilder builder = new StringBuilder();
        Formatter formatter = new Formatter(builder);
        formatter.format("%." + format.precision + "f", value);
        output.append(align(builder, format));
    }

    private void formatInteger(Output output, Format format, long value, int radix, boolean upperCase)
    {
        // Some of this behaviour is a little strange, but it's here for compatability with FluffOS
        String str = Long.toString(value, radix);
        if (upperCase)
        {
            str = str.toUpperCase();
        }
        StringBuilder builder = new StringBuilder(str);
        switch (format.sign)
        {
            case NONE:
                break;
            case PLUS:
                builder.insert(0, '+');
                break;
            case SPACE:
                builder.insert(0, ' ');
                break;
        }
        output.append(align(builder, format));
    }

    private void formatChar(Output output, Format format, long value)
    {
        if (value < 0 || value > Character.MAX_VALUE)
        {
            throw new LpcRuntimeException("Invalid integer value " + value + " for character output");
        }
        char ch = (char) value;
        formatString(output, format, Character.toString(ch));
    }

    private CharSequence align(StringBuilder builder, Format format)
    {
        int prePad = 0, postPad = 0;
        int len = format.size - builder.length();
        switch (format.alignment)
        {
            case LEFT:
                prePad = 0;
                postPad = len;
                break;

            case RIGHT:
                prePad = len;
                postPad = 0;
                break;

            case CENTRE:
                postPad = len / 2;
                prePad = len - postPad;
                break;
        }

        long padCount = prePad / format.padding.length();
        int padExtra = prePad % format.padding.length();
        int insert = 0;
        for (int i = 0; i < padCount; i++)
        {
            builder.insert(insert, format.padding);
            insert += format.padding.length();
        }
        if (padExtra > 0)
        {
            builder.insert(insert, format.padding.subSequence(0, padExtra));
        }

        padCount = postPad / format.padding.length();
        padExtra = postPad % format.padding.length();
        for (int i = 0; i < padCount; i++)
        {
            builder.append(format.padding);
        }
        if (padExtra > 0)
        {
            builder.append(format.padding.subSequence(0, padExtra));
        }

        return builder;
    }

    private Format readFormat(String spec, int start)
    {
        Format format = new Format();
        format.startPosition = start;
        CharacterIterator iterator = new StringCharacterIterator(spec, start);
        while (iterator.next() != CharacterIterator.DONE)
        {
            char ch = iterator.current();
            switch (ch)
            {
                case '%':
                case 'O':
                case 's':
                case 'd':
                case 'i':
                case 'c':
                case 'o':
                case 'x':
                case 'X':
                case 'f':
                    format.type = ch;
                    format.endPosition = iterator.getIndex();
                    return format;

                case ' ':
                    format.sign = SignMode.PLUS;
                    break;
                case '+':
                    format.sign = SignMode.PLUS;
                    break;

                case '-':
                    format.alignment = Alignment.LEFT;
                    break;
                case '|':
                    format.alignment = Alignment.CENTRE;
                    break;

                case '=':
                    format.mode = StringMode.COLUMN;
                    break;
                case '#':
                    throw new LpcRuntimeException(getName() + " - Table mode ('#') not supported");
                    // format.mode = StringMode.TABLE;
                    // break;

                case '*':
                    format.size = -2;
                    break;

                case '0':
                    format.padding = "0";
                    break;

                case '\'':
                    format.padding = readPaddingString(iterator);
                    break;

                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    format.size = readNumber(iterator);
                    break;

                case '.':
                    iterator.next();
                    format.precision = readNumber(iterator);
                    break;

                case ':':
                    if (iterator.next() == '0')
                    {
                        format.padding = "0";
                    }
                    if (iterator.current() == '-')
                    {
                        format.alignment = Alignment.LEFT;
                        iterator.next();
                    }
                    format.precision = readNumber(iterator);
                    format.size = format.precision;
                    break;

                case '@':
                    format.array = true;
                    break;
            }
        }
        format.endPosition = iterator.getIndex();
        return format;
    }

    private CharSequence readPaddingString(CharacterIterator iterator)
    {
        StringBuilder builder = new StringBuilder();

        while (true)
        {
            iterator.next();
            char ch = iterator.current();
            if (ch == '\'' || ch == CharacterIterator.DONE)
            {
                break;
            }
            builder.append(ch);
        }
        return builder;
    }

    private int readNumber(CharacterIterator iterator)
    {
        StringBuilder builder = new StringBuilder();

        while (true)
        {
            char ch = iterator.current();
            if (Character.isDigit(ch))
            {
                builder.append(ch);
                iterator.next();
            }
            else
            {
                iterator.previous();
                break;
            }
        }
        return Integer.parseInt(builder.toString());
    }

}
