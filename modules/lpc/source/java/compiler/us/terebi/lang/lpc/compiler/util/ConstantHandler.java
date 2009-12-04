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

package us.terebi.lang.lpc.compiler.util;

import us.terebi.lang.lpc.compiler.CompileException;
import us.terebi.lang.lpc.parser.ast.ASTConstant;
import us.terebi.lang.lpc.parser.ast.Node;
import us.terebi.lang.lpc.parser.ast.ParserVisitor;
import us.terebi.lang.lpc.parser.ast.TokenNode;
import us.terebi.lang.lpc.parser.jj.ParserConstants;
import us.terebi.lang.lpc.parser.jj.Token;
import us.terebi.lang.lpc.parser.util.ASTUtil;
import us.terebi.lang.lpc.parser.util.BaseASTVisitor;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;
import us.terebi.util.io.CharStream;

/**
 * 
 */
public class ConstantHandler extends BaseASTVisitor implements ParserVisitor
{
    public Object visit(ASTConstant node, Object data)
    {
        Object value = null;
        Token token = node.jjtGetFirstToken();
        String text = token.image;
        MathLength math = (MathLength) data;
        switch (token.kind)
        {
            case ParserConstants.DECIMAL_LITERAL:
                {
                    // Java treats "0123" as octal, but LPC treats it as decimal
                    while (text.length() > 1 && text.charAt(0) == '0')
                    {
                        text = text.substring(1);
                    }
                    value = parseInteger(text, 10, math);
                    break;
                }
            case ParserConstants.HEXADECIMAL_LITERAL:
                {
                    value = parseInteger(text.substring(2), 16, math);
                    break;
                }
            case ParserConstants.FLOAT_LITERAL:
                {
                    value = new Double(text);
                    break;
                }
            case ParserConstants.OCTAL_LITERAL:
                {
                    // Java treats "0123" as octal, but LPC uses 0o123
                    value = parseInteger(text.substring(2), 8, math);
                    break;
                }
            case ParserConstants.BINARY_LITERAL:
                {
                    value = parseInteger(text.substring(2), 2, math);
                    break;
                }
            case ParserConstants.CHAR_LITERAL:
                {
                    value = new Character(readLpcChar(new CharStream(text, 1)));
                    break;
                }
            case ParserConstants.STRING_LITERAL:
                {
                    value = readLpcString(node);
                }
        }
        return value;
    }

    private Object parseInteger(String text, int radix, MathLength math)
    {
        try
        {
            long l = Long.parseLong(text, radix);
            if (math == MathLength.MATH_32_BIT)
            {
                return new Integer((int) l);
            }
            else
            {
                return l;
            }
        }
        catch (NumberFormatException e)
        {
            throw new CompileException("Constant " + text + " isn't a base " + radix + " number (math = " + math + ")", e);
        }
    }

    private CharSequence readLpcString(ASTConstant node)
    {
        StringBuilder buf = new StringBuilder();
        for (Token stringToken : ASTUtil.getTokens(node))
        {
            assert (stringToken.kind == ParserConstants.STRING_LITERAL);
            CharStream stream = new CharStream(stringToken.image, 1, stringToken.image.length() - 1);
            while (!stream.eof())
            {
                buf.append(readLpcChar(stream));
            }
        }
        return buf;
    }

    public static CharSequence readLpcString(String str)
    {
        StringBuilder buf = new StringBuilder();
        CharStream stream = new CharStream(str);
        while (!stream.eof())
        {
            buf.append(readLpcChar(stream));
        }
        return buf;
    }

    private static char readLpcChar(CharStream text)
    {
        int ch = readChar(text);

        if (ch != '\\')
        {
            return (char) ch;
        }

        ch = readChar(text);

        switch (ch)
        {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
                {
                    StringBuilder seq = new StringBuilder();
                    int maxlen = (ch > '3' ? 2 : 3);
                    seq.append(ch);
                    for (int i = 1; i < maxlen; i++)
                    {
                        ch = readChar(text);
                        if (ch >= '0' && ch <= '7')
                        {
                            seq.append(ch);
                        }
                    }
                    return (char) Short.parseShort(seq.toString());
                }

            case '\\':
            case '\'':
            case '\"':
                return (char) ch;

            case 'n':
                return '\n';
            case 'r':
                return '\r';
            case 'f':
                return '\f';
            case 't':
                return '\t';
            case 'b':
                return '\b';
            case 'a':
                return '\007';
            case 'e':
                return '\033';

            default:
                // Unnecessary escape...
                // @TODO warn?
                return (char) ch;
        }
    }

    private static char readChar(CharStream stream)
    {
        if (stream.eof())
        {
            throw new LpcRuntimeException("No characters available in reader " + stream);
        }
        return stream.read();
    }

    public CharSequence getString(Iterable< ? extends TokenNode> children)
    {
        StringBuilder builder = new StringBuilder();
        for (TokenNode simpleNode : children)
        {
            builder.append(simpleNode.jjtAccept(this, null));
        }
        return builder;
    }

    public Object getConstant(Node node, MathLength math)
    {
        return node.jjtAccept(this, math);
    }

}
