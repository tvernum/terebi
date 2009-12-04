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

package us.terebi.lang.lpc.compiler.util;

import us.terebi.lang.lpc.parser.ast.ASTIdentifier;
import us.terebi.lang.lpc.parser.util.ASTUtil;

/**
 * 
 */
public class Positional
{
    private final ASTIdentifier _identifier;
    private final int _index;

    public Positional(ASTIdentifier identifier)
    {
        _identifier = identifier;
        String image = ASTUtil.getImage(identifier);
        if (image.charAt(0) == '$')
        {
            _index = Integer.parseInt(image.substring(1));
        }
        else
        {
            _index = -1;
        }
    }

    public boolean isPositionalVariable()
    {
        return _index != -1;
    }

    public int getIndex()
    {
        return _index;
    }

    public String toString()
    {
        return getClass().getSimpleName() + "{" + _identifier + " (" + _index + ")}";
    }

}
