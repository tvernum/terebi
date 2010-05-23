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

package us.terebi.lang.lpc.compiler;

import us.terebi.lang.lpc.parser.ParserException;
import us.terebi.lang.lpc.parser.ast.TokenNode;
import us.terebi.lang.lpc.parser.jj.Token;
import us.terebi.lang.lpc.parser.util.ASTUtil;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;

/**
 * 
 */
public class CompileException extends LpcRuntimeException
{
    private final Token _token;

    public CompileException(Token token, String message)
    {
        // @TODO better context
        super(token == null ? message : message + " [at node " + ASTUtil.describe(token) + "]");
        _token = token;
    }

    public CompileException(TokenNode node, String message)
    {
        super(getMessage(node, message));
        _token = (node == null ? null : node.jjtGetFirstToken());
    }

    public CompileException(TokenNode node, String message, Throwable cause)
    {
        super(getMessage(node, message), cause);
        _token = (node == null ? null : node.jjtGetFirstToken());
    }

    private static String getMessage(TokenNode node, String message)
    {
        return message + " [at node " + getImage(node) + "]";
    }

    private static CharSequence getImage(TokenNode node)
    {
        if (node == null)
        {
            return "<??>";
        }
        // @TODO smart truncation
        CharSequence image = ASTUtil.getCompleteImage(node);
        if (image.length() > 30)
        {
            return image.subSequence(0, 30);
        }
        return image;
    }

    public CompileException(String file, int line, CompileException ce)
    {
        super("At " + file + ":" + line + " " + ce.getMessage(), ce);
        _token = ce.getToken();
    }

    public CompileException(ParserException e)
    {
        super(e.getMessage(), e);
        _token = e.getToken();
    }

    public CompileException(String message, Throwable cause)
    {
        super(message, cause);
        _token = null;
    }


    public CompileException(CompileException cause, String location)
    {
        super(cause, location);
        _token = cause.getToken();
    }

    public Token getToken()
    {
        return _token;
    }

}
