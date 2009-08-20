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

import us.terebi.lang.lpc.parser.ast.ASTObjectDefinition;

/**
 * 
 */
public class Source implements ObjectSource
{
    private final String _filename;
    private final ASTObjectDefinition _tree;

    public Source(String filename, ASTObjectDefinition tree)
    {
        _filename = filename;
        _tree = tree;
    }

    public String getFilename()
    {
        return _filename;
    }

    public ASTObjectDefinition getSyntaxTree()
    {
        return _tree;
    }

    public String toString()
    {
        return getClass().getSimpleName() + "{" + _filename + " : " + _tree + "}";
    }

}
