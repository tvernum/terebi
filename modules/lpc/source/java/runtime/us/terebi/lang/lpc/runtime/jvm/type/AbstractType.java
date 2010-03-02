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

import us.terebi.lang.lpc.runtime.ClassDefinition;
import us.terebi.lang.lpc.runtime.ExtensionType;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.util.StringUtil;

import static us.terebi.util.Compare.equal;

/**
 * 
 */
public abstract class AbstractType implements LpcType
{
    public ClassDefinition getClassDefinition()
    {
        return null;
    }

    public ExtensionType getExtensionType()
    {
        return null;
    }

    public boolean isClass()
    {
        return getKind() == Kind.CLASS && getArrayDepth() == 0;
    }

    public boolean isArray()
    {
        return getArrayDepth() > 0;
    }
    
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        else if (obj == null)
        {
            return false;
        }
        else if (!(obj instanceof LpcType))
        {
            return false;
        }
        LpcType other = (LpcType) obj;
        return (this.getArrayDepth() == other.getArrayDepth()
                && this.getKind() == other.getKind()
                && equal(this.getClassDefinition(), other.getClassDefinition()) && equal(this.getExtensionType(),
                other.getExtensionType()));
    }

    public int hashCode()
    {
        switch (getKind())
        {
            case CLASS:
                return getClassDefinition().hashCode() ^ (0x100 << getArrayDepth());
            case EXTENSION:
                return getExtensionType().hashCode() ^ (0x200 << getArrayDepth());
            default:
                return getKind().hashCode() ^ (0x300 << getArrayDepth());
        }
    }

    public String toString()
    {
        switch (getKind())
        {
            case CLASS:
                return "class " + getClassDefinition().getName() + StringUtil.repeat("*", getArrayDepth());
            case EXTENSION:
                return "extension " + getExtensionType().getName() + StringUtil.repeat("*", getArrayDepth());
            default:
                return getKind().toString().toLowerCase() + StringUtil.repeat("*", getArrayDepth());
        }
    }

}
