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

package us.terebi.lang.lpc.parser;


/**
 */
public class ParserState extends Object
{
    private static final ThreadLocal<ParserState> STATE = new ThreadLocal<ParserState>();

    private LpcParser _parser;
    private LineMapping _lineMapping;

    public static ParserState getState()
    {
        return STATE.get();
    }

    public ParserState(LpcParser parser, LineMapping mapping)
    {
        _parser = parser;
        _lineMapping = mapping;
        STATE.set(this);
    }

    public LpcParser getParser()
    {
        return _parser;
    }

    public LineMapping getLineMapping()
    {
        return _lineMapping;
    }
}
