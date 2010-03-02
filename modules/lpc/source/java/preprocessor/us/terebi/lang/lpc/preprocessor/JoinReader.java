/*
 * Anarres C Preprocessor
 * Copyright (c) 2007-2008, Shevek
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */

package us.terebi.lang.lpc.preprocessor;

import java.io.IOException;
import java.io.Reader;

/* pp */class JoinReader 
{
    private Reader _in;

    private LexerSource _source;
    private boolean _trigraphs;
    private boolean _warnings;

    private int _newlines;
    private boolean _flushnl;
    private int[] _unget;
    private int _uptr;

    public JoinReader(Reader in, boolean trigraphs)
    {
        this._in = in;
        this._trigraphs = trigraphs;
        this._newlines = 0;
        this._flushnl = false;
        this._unget = new int[2];
        this._uptr = 0;
    }

    public JoinReader(Reader in)
    {
        this(in, false);
    }

    public void setTrigraphs(boolean enable, boolean warnings)
    {
        this._trigraphs = enable;
        this._warnings = warnings;
    }

    /* pp */void init(Preprocessor pp, LexerSource s)
    {
        this._source = s;
        setTrigraphs(pp.getFeature(Feature.TRIGRAPHS), pp.getWarning(Warning.TRIGRAPHS));
    }

    private int __read() throws IOException
    {
        if (_uptr > 0)
            return _unget[--_uptr];
        return _in.read();
    }

    private void _unread(int c)
    {
        if (c != -1)
            _unget[_uptr++] = c;
    }

    protected void warning(String msg) throws LexerException
    {
        if (_source != null)
            _source.warning(msg);
        else
            throw new LexerException(msg);
    }

    private char trigraph(char raw, char repl) throws LexerException
    {
        if (_trigraphs)
        {
            if (_warnings)
                warning("trigraph ??" + raw + " converted to " + repl);
            return repl;
        }
        else
        {
            if (_warnings)
                warning("trigraph ??" + raw + " ignored");
            _unread(raw);
            _unread('?');
            return '?';
        }
    }

    private int _read() throws IOException, LexerException
    {
        int c = __read();
        if (c == '?' && (_trigraphs || _warnings))
        {
            int d = __read();
            if (d == '?')
            {
                int e = __read();
                switch (e)
                {
                    case '(':
                        return trigraph('(', '[');
                    case ')':
                        return trigraph(')', ']');
                    case '<':
                        return trigraph('<', '{');
                    case '>':
                        return trigraph('>', '}');
                    case '=':
                        return trigraph('=', '#');
                    case '/':
                        return trigraph('/', '\\');
                    case '\'':
                        return trigraph('\'', '^');
                    case '!':
                        return trigraph('!', '|');
                    case '-':
                        return trigraph('-', '~');
                }
                _unread(e);
            }
            _unread(d);
        }
        return c;
    }

    public int read() throws IOException, LexerException
    {
        if (_flushnl)
        {
            if (_newlines > 0)
            {
                _newlines--;
                return '\n';
            }
            _flushnl = false;
        }

        for (;;)
        {
            int c = _read();
            switch (c)
            {
                case '\\':
                    int d = _read();
                    switch (d)
                    {
                        case '\n':
                            _newlines++;
                            continue;
                        case '\r':
                            _newlines++;
                            int e = _read();
                            if (e != '\n')
                                _unread(e);
                            continue;
                        default:
                            _unread(d);
                            return c;
                    }
                case '\r':
                case '\n':
                case '\u2028':
                case '\u2029':
                case '\u000B':
                case '\u000C':
                case '\u0085':
                    _flushnl = true;
                    return c;
                case -1:
                    if (_newlines > 0)
                    {
                        _newlines--;
                        return '\n';
                    }
                default:
                    return c;
            }
        }
    }

    public int read(char cbuf[], int off, int len) throws IOException, LexerException
    {
        for (int i = 0; i < len; i++)
        {
            int ch = read();
            if (ch == -1)
                return i;
            cbuf[off + i] = (char) ch;
        }
        return len;
    }

    public void close() throws IOException
    {
        _in.close();
    }

    public String toString()
    {
        return "JoinReader(nl=" + _newlines + ")";
    }

    /*
    	public static void main(String[] args) throws IOException {
    		FileReader		f = new FileReader(new File(args[0]));
    		BufferedReader	b = new BufferedReader(f);
    		JoinReader		r = new JoinReader(b);
    		BufferedWriter	w = new BufferedWriter(
    				new java.io.OutputStreamWriter(System.out)
    					);
    		int				c;
    		while ((c = r.read()) != -1) {
    			w.write((char)c);
    		}
    		w.close();
    	}
    */

}
