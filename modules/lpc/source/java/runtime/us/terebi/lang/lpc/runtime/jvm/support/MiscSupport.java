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

package us.terebi.lang.lpc.runtime.jvm.support;

import us.terebi.lang.lpc.runtime.ClassDefinition;
import us.terebi.lang.lpc.runtime.ExtensionType;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.LpcType.Kind;
import us.terebi.lang.lpc.runtime.jvm.LpcConstants;
import us.terebi.lang.lpc.runtime.jvm.type.Types;

/**
 * 
 */
public class MiscSupport
{
    public static boolean isType(LpcType type, LpcValue value)
    {
        return type.equals(value.getActualType());
    }

    public static boolean isString(LpcValue value)
    {
        return isType(Types.STRING, value);
    }

    public static boolean isMapping(LpcValue value)
    {
        return isType(Types.MAPPING, value);
    }

    public static boolean isInt(LpcValue value)
    {
        return isType(Types.INT, value);
    }

    public static boolean isFloat(LpcValue value)
    {
        return isType(Types.FLOAT, value);
    }

    public static boolean isNumber(LpcValue value)
    {
        return isInt(value) || isFloat(value);
    }

    public static boolean isArray(LpcValue value)
    {
        return value.getActualType().getArrayDepth() > 0;
    }

    public static LpcValue getValue(boolean bool)
    {
        if (bool)
        {
            return LpcConstants.INT.TRUE;
        }
        else
        {
            return LpcConstants.INT.FALSE;
        }
    }

    public static LpcType commonType(LpcType... types)
    {
        if (types.length == 0)
        {
            return Types.MIXED;
        }
        if (types.length == 1)
        {
            return types[0];
        }

        LpcType.Kind kind = types[0].getKind();
        int depth = types[0].getArrayDepth();
        ClassDefinition cls = types[0].getClassDefinition();
        ExtensionType ext = types[0].getExtensionType();

        for (int i = 1; i < types.length; i++)
        {
            LpcType type = types[i];
            if (type.getArrayDepth() < depth)
            {
                depth = type.getArrayDepth();
                kind = LpcType.Kind.MIXED;
                continue;
            }
            if (type.getKind() != kind)
            {
                kind = LpcType.Kind.MIXED;
            }
            else if (kind == LpcType.Kind.CLASS && type.getClassDefinition() != cls)
            {
                kind = LpcType.Kind.MIXED;
            }
            else if (kind == LpcType.Kind.EXTENSION && type.getExtensionType() != ext)
            {
                kind = LpcType.Kind.MIXED;
            }
        }

        switch (kind)
        {
            case CLASS:
                return Types.classType(cls, depth);
            case EXTENSION:
                return Types.extensionType(ext, depth);
            default:
                return Types.getType(kind, null, depth);
        }
    }

    public static boolean isMoreSpecific(LpcType type, LpcType value)
    {
        if (value.getKind() != Kind.MIXED)
        {
            return false;
        }
        if (type.getArrayDepth() < value.getArrayDepth())
        {
            return false;
        }
        if (type.getKind() == Kind.MIXED)
        {
            return false;
        }
        return true;
    }

}
