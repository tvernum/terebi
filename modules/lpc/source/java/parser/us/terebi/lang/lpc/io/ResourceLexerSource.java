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

package us.terebi.lang.lpc.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import us.terebi.lang.lpc.preprocessor.LexerSource;

/**
 * 
 */
public class ResourceLexerSource extends LexerSource
{
    private final Resource _resource;

    public ResourceLexerSource(Resource resource) throws IOException
    {
        super(new BufferedReader(new InputStreamReader(resource.open())), true);
        _resource = resource;
    }

    protected String getPath()
    {
        return _resource.getPath();
    }

    protected String getName()
    {
        return _resource.getName();
    }

    public String toString()
    {
        return getClass().getSimpleName() + "{" + _resource + "}";
    }
}
