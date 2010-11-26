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
import us.terebi.lang.lpc.runtime.LpcType.Kind;
import us.terebi.lang.lpc.runtime.jvm.value.ClassReference;

/**
 * 
 */
public class Types
{
    public static final LpcType VOID = new SimpleType(Kind.VOID);
    public static final LpcType NIL = new SimpleType(Kind.NIL);
    public static final LpcType ZERO = new SimpleType(Kind.ZERO);

    public static final LpcType MIXED = new SimpleType(Kind.MIXED);
    public static final LpcType MIXED_ARRAY = new SimpleType(Kind.MIXED, 1);
    public static final LpcType MIXED_ARRAY_ARRAY = new SimpleType(Kind.MIXED, 2);

    public static final LpcType STRING = new SimpleType(Kind.STRING);
    public static final LpcType STRING_ARRAY = new SimpleType(Kind.STRING, 1);
    public static final LpcType STRING_ARRAY_ARRAY = new SimpleType(Kind.STRING, 2);

    public static final LpcType OBJECT = new SimpleType(Kind.OBJECT);
    public static final LpcType OBJECT_ARRAY = new SimpleType(Kind.OBJECT, 1);

    public static final LpcType INT = new SimpleType(Kind.INT);
    public static final LpcType FUNCTION = new SimpleType(Kind.FUNCTION);
    public static final LpcType FLOAT = new SimpleType(Kind.FLOAT);
    public static final LpcType MAPPING = new SimpleType(Kind.MAPPING);
    public static final LpcType BUFFER = new SimpleType(Kind.BUFFER);

    public static final LpcType CLASS_REFERENCE = extensionType(ClassReference.TYPE, 0);

    public static LpcType classType(ClassDefinition definition)
    {
        return classType(definition, 0);
    }

    public static LpcType classType(final ClassDefinition definition, final int depth)
    {
        return new ClassType(depth, definition);
    }

    public static LpcType getType(Kind kind, ClassDefinition classDefinition, int depth)
    {
        if (kind == Kind.CLASS && classDefinition == null)
        {
            throw new IllegalArgumentException("Cannot create a class type without a class-definiton");
        }
        if (depth == 0)
        {
            switch (kind)
            {
                case MIXED:
                    return MIXED;
                case STRING:
                    return STRING;
                case OBJECT:
                    return OBJECT;
                case VOID:
                    return VOID;
            }
        }
        if (depth == 1)
        {
            switch (kind)
            {
                case MIXED:
                    return MIXED_ARRAY;
                case STRING:
                    return STRING_ARRAY;
                case OBJECT:
                    return OBJECT_ARRAY;
            }
        }
        if (classDefinition == null)
        {
            return new SimpleType(kind, depth);
        }
        else
        {
            return classType(classDefinition, depth);
        }
    }

    public static LpcType arrayOf(LpcType elementType)
    {
        return new ArrayType(elementType, 1);
    }

    public static LpcType extensionType(ExtensionType ext, int depth)
    {
        return new ExtendedType(ext, depth);
    }

    public static LpcType elementOf(LpcType type)
    {
        if (type.getArrayDepth() == 0)
        {
            throw new IllegalArgumentException("Type " + type + " is not an array");
        }
        if (type.getKind() == LpcType.Kind.EXTENSION)
        {
            return extensionType(type.getExtensionType(), type.getArrayDepth() - 1);
        }
        else
        {
            return getType(type.getKind(), type.getClassDefinition(), type.getArrayDepth() - 1);
        }
    }

}
