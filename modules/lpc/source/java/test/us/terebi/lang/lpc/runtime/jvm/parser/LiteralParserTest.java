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

import java.util.Collections;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import us.terebi.lang.lpc.compiler.java.context.ClassLookup;
import us.terebi.lang.lpc.parser.jj.ParseException;
import us.terebi.lang.lpc.runtime.ClassInstance;
import us.terebi.lang.lpc.runtime.FieldDefinition;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.MemberDefinition.Modifier;
import us.terebi.lang.lpc.runtime.jvm.support.MiscSupport;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.FloatValue;
import us.terebi.lang.lpc.runtime.jvm.value.IntValue;
import us.terebi.lang.lpc.runtime.jvm.value.StringValue;
import us.terebi.lang.lpc.runtime.util.type.DynamicClassDefinition;
import us.terebi.lang.lpc.runtime.util.type.DynamicClassFactory;
import us.terebi.lang.lpc.runtime.util.type.DynamicField;

/**
 * 
 */
public class LiteralParserTest
{
    @Test
    public void parseInteger() throws Exception
    {
        LpcValue value = new LiteralParser(new ClassLookup()).parse("45");
        Assert.assertEquals(new IntValue(45), value);
    }

    @Test
    public void parseFloat() throws Exception
    {
        LpcValue value = new LiteralParser(new ClassLookup()).parse("6.7");
        Assert.assertEquals(new FloatValue(6.7), value);
    }

    @Test
    public void parseString() throws Exception
    {
        LpcValue value = new LiteralParser(new ClassLookup()).parse("\"don\\'t\"");
        Assert.assertEquals(new StringValue("don't"), value);
    }

    @Test
    public void parseArray() throws Exception
    {
        LpcValue value = new LiteralParser(new ClassLookup()).parse("({ 1, 1.1, \"1.2\" })");
        Assert.assertTrue(MiscSupport.isArray(value));
        Assert.assertEquals(Types.MIXED_ARRAY, value.getActualType());
        Assert.assertEquals(new IntValue(1), value.asList().get(0));
        Assert.assertEquals(new FloatValue(1.1), value.asList().get(1));
        Assert.assertEquals(new StringValue("1.2"), value.asList().get(2));
    }

    @Test
    public void parseMapping() throws Exception
    {
        LpcValue value = new LiteralParser(new ClassLookup()).parse("([ \"a\" : 1 , \"b\" : 2 ])");
        Assert.assertTrue(MiscSupport.isMapping(value));
        Assert.assertEquals(2, value.asMap().size());
        Assert.assertEquals(new IntValue(1), value.asMap().get(new StringValue("a")));
        Assert.assertEquals(new IntValue(2), value.asMap().get(new StringValue("b")));
    }

    @Test
    public void parseClass() throws Exception
    {
        ClassLookup lookup = getClassLookup();
        LpcValue value = new LiteralParser(lookup).parse("($ Foo : bar = 7 , baz = \"xyzzy\" $)");
        Assert.assertTrue(MiscSupport.isClass(value));
        ClassInstance cls = value.asClass();
        Map<FieldDefinition, LpcValue> fields = cls.getFieldValues();
        Assert.assertNotNull(fields);
        FieldDefinition bar = cls.getDefinition().getFields().get("bar");
        Assert.assertEquals(new IntValue(7), fields.get(bar));
        FieldDefinition baz = cls.getDefinition().getFields().get("baz");
        Assert.assertEquals(new StringValue("xyzzy"), fields.get(baz));
    }

    @Test
    public void DontParseExpression() throws Exception
    {
        try
        {
            new LiteralParser(new ClassLookup()).parse("({ 1+1 })");
            Assert.fail("Addition should not be parsed correctly by literal parser");
        }
        catch (ParseException e)
        {
            // Pass
        }
    }

    private ClassLookup getClassLookup()
    {
        ClassLookup lookup = new ClassLookup();
        DynamicClassDefinition definition = new DynamicClassDefinition("Foo", Collections.<Modifier> emptySet());
        definition.addField(new DynamicField(definition, "bar", Types.INT));
        definition.addField(new DynamicField(definition, "baz", Types.STRING));
        definition.setFactory(new DynamicClassFactory(definition));
        lookup.defineClass(definition);
        return lookup;
    }
}
