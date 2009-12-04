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

import us.terebi.lang.lpc.compiler.CompileException;
import us.terebi.lang.lpc.compiler.java.context.LookupException;
import us.terebi.lang.lpc.compiler.java.context.ScopeLookup;
import us.terebi.lang.lpc.parser.ast.ASTArrayStar;
import us.terebi.lang.lpc.parser.ast.ASTFullType;
import us.terebi.lang.lpc.parser.ast.ASTType;
import us.terebi.lang.lpc.parser.ast.Node;
import us.terebi.lang.lpc.parser.ast.SimpleNode;
import us.terebi.lang.lpc.parser.jj.ParserConstants;
import us.terebi.lang.lpc.parser.jj.Token;
import us.terebi.lang.lpc.parser.util.ASTUtil;
import us.terebi.lang.lpc.runtime.ClassDefinition;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.jvm.type.Types;

/**
 * 
 */
public class TypeSupport
{
    private final LpcType _type;

    public TypeSupport(ScopeLookup scope, ASTFullType type)
    {
        this(scope, ASTUtil.getChild(ASTType.class, type), ASTUtil.hasChildType(ASTArrayStar.class, type));
    }

    public TypeSupport(ScopeLookup scope, ASTType node, boolean array)
    {
        LpcType.Kind kind = LpcType.Kind.MIXED;
        int depth = (array ? 1 : 0);
        String className = null;

        for (Token token : ASTUtil.getTokens(node))
        {
            switch (token.kind)
            {
                case ParserConstants.INT:
                    kind = LpcType.Kind.INT;
                    break;
                case ParserConstants.FLOAT:
                    kind = LpcType.Kind.FLOAT;
                    break;
                case ParserConstants.STRING:
                    kind = LpcType.Kind.STRING;
                    break;
                case ParserConstants.MIXED:
                    kind = LpcType.Kind.MIXED;
                    break;
                case ParserConstants.MAPPING:
                    kind = LpcType.Kind.MAPPING;
                    break;
                case ParserConstants.OBJECT:
                    kind = LpcType.Kind.OBJECT;
                    break;
                case ParserConstants.FUNCTION:
                    kind = LpcType.Kind.FUNCTION;
                    break;
                case ParserConstants.BUFFER:
                    kind = LpcType.Kind.BUFFER;
                    break;
                case ParserConstants.VOID:
                    kind = LpcType.Kind.VOID;
                    break;
                case ParserConstants.CLASS:
                    kind = LpcType.Kind.CLASS;
                    break;
                case ParserConstants.IDENTIFIER:
                    className = token.image;
                    break;
                case ParserConstants.ARRAY:
                    depth++;
                    break;
            }
        }

        ClassDefinition classDefinition = null;
        if (className != null)
        {
            try
            {
                classDefinition = scope.classes().findClass(className);
            }
            catch (LookupException e)
            {
                throw new CompileException(node, e.getMessage());
            }
        }
        _type = Types.getType(kind, classDefinition, depth);
    }


    public LpcType getType()
    {
        return _type;
    }

    public static boolean checkType(Node node, LpcType actual, LpcType... allowedTypes)
    {
        if (Types.MIXED.equals(actual))
        {
            return true;
        }
    
        LpcType type = actual;
        for (LpcType allowed : allowedTypes)
        {
            if (Types.MIXED.equals(allowed))
            {
                return true;
            }
            if (allowed.equals(type))
            {
                return true;
            }
            if (type.getKind() == LpcType.Kind.MIXED && type.getArrayDepth() >= allowed.getArrayDepth())
            {
                return true;
            }
            if (allowed.getKind() == LpcType.Kind.MIXED && type.getArrayDepth() >= allowed.getArrayDepth())
            {
                return true;
            }
            if (Types.FLOAT.equals(allowed) && Types.INT.equals(type))
            {
                return true;
            }
        }
        StringBuilder expected = new StringBuilder();
        for (LpcType lpcType : allowedTypes)
        {
            expected.append(lpcType);
            expected.append("|");
        }
    
        expected.deleteCharAt(expected.length() - 1);
        throw new CompileException((SimpleNode) node, "Type mismatch - expected " + expected + " but was " + type);
    }

}
