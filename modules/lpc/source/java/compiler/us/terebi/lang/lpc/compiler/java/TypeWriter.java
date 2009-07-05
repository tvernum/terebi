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

package us.terebi.lang.lpc.compiler.java;

import java.io.PrintWriter;

import us.terebi.lang.lpc.compiler.CompileException;
import us.terebi.lang.lpc.compiler.java.context.CompileContext;
import us.terebi.lang.lpc.compiler.java.context.LookupException;
import us.terebi.lang.lpc.parser.ast.ASTArrayStar;
import us.terebi.lang.lpc.parser.ast.ASTFullType;
import us.terebi.lang.lpc.parser.ast.ASTType;
import us.terebi.lang.lpc.parser.ast.ASTUtil;
import us.terebi.lang.lpc.parser.jj.ParserConstants;
import us.terebi.lang.lpc.parser.jj.Token;
import us.terebi.lang.lpc.runtime.ClassDefinition;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.jvm.type.Types;

/**
 * Nothing to do with keyboards and inkstrips :)
 */
public class TypeWriter
{
    private final CompileContext _context;

    public TypeWriter(CompileContext context)
    {
        _context = context;
    }

    public LpcType getType(ASTType type, boolean array)
    {
        LpcType.Kind kind = LpcType.Kind.MIXED;
        int depth = (array ? 1 : 0);
        String className = null;

        for (Token token : ASTUtil.getTokens(type))
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
                classDefinition = _context.classes().findClass(className);
            }
            catch (LookupException e)
            {
                throw new CompileException(type, e.getMessage());
            }
        }
        return Types.getType(kind, classDefinition, depth);
    }

    public LpcType getType(ASTFullType type)
    {
        ASTType simpleType = ASTUtil.getChild(ASTType.class, type);
        boolean array = ASTUtil.hasChildType(ASTArrayStar.class, type);
        return getType(simpleType, array);
    }

    public void printType(ASTType type, boolean array)
    {
        printType(getType(type, array));
    }

    public void printType(ASTFullType type)
    {
        printType(getType(type));
    }

    public void printType(LpcType type)
    {
        PrintWriter writer = _context.writer();

        writer.print("withType( ");
        if (type.getKind() == LpcType.Kind.CLASS)
        {
            // @TODO Is this reliable ??? Is the class always in scope in the java code?
            // What about inherited classes?
            writer.print( new ClassWriter(_context).getInternalName(type.getClassDefinition()));
            writer.print(".class");
        }
        else
        {
            writer.print(fullyQualifiedName(type.getKind()));
        }
        writer.print(", ");
        writer.print(Integer.toString(type.getArrayDepth()));
        writer.print(") ");
    }

    public static String fullyQualifiedName(Enum< ? > e)
    {
        return e.getClass().getCanonicalName() + '.' + e.name();
    }

}
