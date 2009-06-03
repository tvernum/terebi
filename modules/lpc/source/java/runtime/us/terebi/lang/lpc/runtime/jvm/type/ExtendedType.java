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

package us.terebi.lang.lpc.runtime.jvm.type;

import us.terebi.lang.lpc.runtime.ExtensionType;
import us.terebi.lang.lpc.runtime.LpcType;

/**
 * 
 */
class ExtendedType extends AbstractType implements LpcType
{
    private final ExtensionType _extension;
    private final int _depth;

    public ExtendedType(ExtensionType extension, int depth)
    {
        _extension = extension;
        _depth = depth;
    }

    public int getArrayDepth()
    {
        return _depth;
    }

    public ExtensionType getExtensionType()
    {
        return _extension;
    }

    public Kind getKind()
    {
        return Kind.EXTENSION;
    }
}
