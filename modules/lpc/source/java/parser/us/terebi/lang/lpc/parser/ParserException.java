/* ------------------------------------------------------------------------
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

package us.terebi.lang.lpc.parser;

import us.terebi.lang.lpc.parser.jj.ParseException;
import us.terebi.lang.lpc.parser.jj.Token;

/**
 * 
 */
public class ParserException extends Exception
{
    private final String _file;
    private final int _line;
    private final Token _token;

    public ParserException(String message)
    {
        super(message);
        _file = null;
        _line = 0;
        _token = null;
    }

    public ParserException(String message, Throwable cause)
    {
        super(message, cause);
        _file = null;
        _line = 0;
        _token = null;
    }

    public ParserException(String file, int line, ParseException cause)
    {
        super("At " + file + " : " + line + " " + cause.getMessage(), cause);
        _file = file;
        _line = line;
        _token = cause.currentToken;
    }
    
    public String getFile()
    {
        return _file;
    }
    
    public int getLine()
    {
        return _line;
    }
    
    public Token getToken()
    {
        return _token;
    }
    
}
