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

package us.terebi.lang.lpc.runtime.jvm.parser;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import us.terebi.lang.lpc.compiler.java.context.ClassLookup;
import us.terebi.lang.lpc.compiler.util.ConstantHandler;
import us.terebi.lang.lpc.compiler.util.MathLength;
import us.terebi.lang.lpc.parser.ast.ASTArrayElement;
import us.terebi.lang.lpc.parser.ast.ASTArrayLiteral;
import us.terebi.lang.lpc.parser.ast.ASTClassElement;
import us.terebi.lang.lpc.parser.ast.ASTClassLiteral;
import us.terebi.lang.lpc.parser.ast.ASTConstant;
import us.terebi.lang.lpc.parser.ast.ASTIdentifier;
import us.terebi.lang.lpc.parser.ast.ASTLiteralValue;
import us.terebi.lang.lpc.parser.ast.ASTMappingElement;
import us.terebi.lang.lpc.parser.ast.ASTMappingLiteral;
import us.terebi.lang.lpc.parser.ast.Node;
import us.terebi.lang.lpc.parser.ast.SimpleNode;
import us.terebi.lang.lpc.parser.ast.TokenNode;
import us.terebi.lang.lpc.parser.jj.ParseException;
import us.terebi.lang.lpc.parser.jj.Parser;
import us.terebi.lang.lpc.parser.util.ASTUtil;
import us.terebi.lang.lpc.parser.util.BaseASTVisitor;
import us.terebi.lang.lpc.runtime.ClassDefinition;
import us.terebi.lang.lpc.runtime.ClassInstance;
import us.terebi.lang.lpc.runtime.FieldDefinition;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.support.MiscSupport;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.ArrayValue;
import us.terebi.lang.lpc.runtime.jvm.value.ClassValue;
import us.terebi.lang.lpc.runtime.jvm.value.FloatValue;
import us.terebi.lang.lpc.runtime.jvm.value.IntValue;
import us.terebi.lang.lpc.runtime.jvm.value.MappingValue;
import us.terebi.lang.lpc.runtime.jvm.value.StringValue;
import us.terebi.lang.lpc.runtime.jvm.value.ZeroValue;

/**
 * 
 */
public class LiteralParser extends BaseASTVisitor
{
    private final ObjectInstance _owner;
    private final ClassLookup _lookup;

    public LiteralParser(ObjectInstance owner)
    {
        this(owner, new ClassLookup(owner));
    }

    public LiteralParser(ObjectInstance owner, ClassLookup lookup)
    {
        _owner = owner;
        _lookup = lookup;
    }

    public LpcValue parse(String literal) throws ParseException
    {
        Parser parser = new Parser(new StringReader(literal));
        parser.setConstantsOnly(true);
        ASTLiteralValue node = parser.LiteralValue();
        return this.visit(node, null);
    }

    public LpcValue visit(ASTLiteralValue node, Object data)
    {
        return (LpcValue) node.jjtGetChild(0).jjtAccept(this, data);
    }

    public ArrayValue visit(ASTArrayLiteral node, Object data)
    {
        List<LpcValue> list = new ArrayList<LpcValue>(node.jjtGetNumChildren());
        for (TokenNode child : ASTUtil.children(node))
        {
            LpcValue value = (LpcValue) child.jjtAccept(this, data);
            list.add(value);
        }
        LpcType arrayType = Types.arrayOf(MiscSupport.commonType(list));
        return new ArrayValue(arrayType, list);
    }

    public LpcValue visit(ASTArrayElement node, Object data)
    {
        return (LpcValue) node.jjtGetChild(0).jjtAccept(this, data);
    }

    public ClassValue visit(ASTClassLiteral node, Object data)
    {
        ASTIdentifier ident = (ASTIdentifier) node.jjtGetChild(0);
        ClassDefinition definition = _lookup.findClass(ident);
        ClassInstance instance = definition.newInstance(_owner);
        for (int i = 1; i < node.jjtGetNumChildren(); i++)
        {
            Node child = node.jjtGetChild(i);
            child.jjtAccept(this, instance);
        }
        return new ClassValue(instance);
    }

    public Object visit(ASTClassElement node, Object data)
    {
        ClassInstance instance = (ClassInstance) data;
        String name = ASTUtil.getImage((SimpleNode) node.jjtGetChild(0));
        FieldDefinition field = instance.getDefinition().getFields().get(name);
        if (field != null)
        {
            Node child = node.jjtGetChild(1);
            LpcValue value = (LpcValue) child.jjtAccept(this, null);
            field.setValue(instance, value);
        }
        return null;
    }

    public Object visit(ASTConstant node, Object data)
    {
        Object constant = new ConstantHandler().getConstant(node, MathLength.MATH_64_BIT);
        if (constant instanceof Long)
        {
            Long num = (Long) constant;
            long l = num.longValue();
            if (l == 0)
            {
                return ZeroValue.INSTANCE;
            }
            return new IntValue(l);
        }
        if (constant instanceof Double)
        {
            Double num = (Double) constant;
            return new FloatValue(num.doubleValue());
        }
        if (constant instanceof Character)
        {
            Character ch = (Character) constant;
            return new IntValue(ch.charValue());
        }
        if (constant instanceof CharSequence)
        {
            CharSequence str = (CharSequence) constant;
            return new StringValue(str);
        }
        return null;
    }

    public MappingValue visit(ASTMappingLiteral node, Object data)
    {
        Map<LpcValue, LpcValue> map = new HashMap<LpcValue, LpcValue>(node.jjtGetNumChildren());
        for (TokenNode child : ASTUtil.children(node))
        {
            child.jjtAccept(this, map);
        }
        return new MappingValue(map);
    }

    @SuppressWarnings("unchecked")
    public Object visit(ASTMappingElement node, Object data)
    {
        Map<LpcValue, LpcValue> map = (Map) data;
        LpcValue key = (LpcValue) node.jjtGetChild(0).jjtAccept(this, null);
        LpcValue value = (LpcValue) node.jjtGetChild(1).jjtAccept(this, null);
        map.put(key, value);
        return null;
    }
}
