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

package us.terebi.lang.lpc.runtime.jvm.value;

import us.terebi.lang.lpc.runtime.ClassDefinition;
import us.terebi.lang.lpc.runtime.ExtensionType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectInstance;

/**
 * 
 */
public class ClassReference extends AbstractExtension
{
    public static final ExtensionType TYPE = new ExtensionType()
    {
        public String getName()
        {
            return "class";
        }
    };

    private final ClassDefinition _definition;
    private final ObjectInstance _owner;

    public ClassReference(ClassDefinition definition, ObjectInstance owner)
    {
        super(TYPE);
        _definition = definition;
        _owner = owner;
    }

    protected CharSequence getDescription()
    {
        return "class " + _definition.getName();
    }

    protected boolean valueEquals(LpcValue other)
    {
        if (other instanceof ClassReference)
        {
            return this._definition.equals(((ClassReference) other)._definition);
        }
        else
        {
            return false;
        }
    }

    protected int valueHashCode()
    {
        return _definition.hashCode();
    }

    public ClassDefinition getClassDefinition()
    {
        return _definition;
    }

    public ObjectInstance getOwner()
    {
        return _owner;
    }

    public CharSequence debugInfo()
    {
        return getDescription();
    }
}
