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

package us.terebi.lang.lpc.compiler.java.test;

import java.util.Collections;
import java.util.Map;

import lpc.secure.sefun.sefun;

import org.junit.Test;

import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.LpcConstants;
import us.terebi.lang.lpc.runtime.jvm.LpcField;
import us.terebi.lang.lpc.runtime.jvm.StandardEfuns;
import us.terebi.lang.lpc.runtime.jvm.context.Functions;
import us.terebi.lang.lpc.runtime.jvm.context.RuntimeContext;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.ArrayValue;
import us.terebi.lang.lpc.runtime.jvm.value.IntValue;
import us.terebi.lang.lpc.runtime.jvm.value.NilValue;
import us.terebi.lang.lpc.runtime.jvm.value.StringValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * 
 */
public class SefunTest
{
    private sefun makeSefunObject()
    {
        Functions functions = StandardEfuns.getImplementation();
        RuntimeContext.set(new RuntimeContext(functions));
        sefun sefun = new sefun();
        return sefun;
    }

    @Test
    public void abs() throws Exception
    {
        sefun sefun = makeSefunObject();
        assertEquals(LpcConstants.INT.ONE, sefun.abs_(LpcConstants.INT.MINUS_ONE));
    }

    @Test
    public void _f_Uncolor() throws Exception
    {
        sefun sefun = makeSefunObject();

        LpcField uncolorField = sefun._f_Uncolor;
        assertNotNull(uncolorField);
        assertEquals(Types.MAPPING, uncolorField.getType());

        LpcValue uncolorValue = uncolorField.get();
        assertEquals(Types.MAPPING, uncolorValue.getActualType());

        Map<LpcValue, LpcValue> uncolorMap = uncolorValue.asMap();
        assertNotNull(uncolorMap);
        assertEquals(24, uncolorMap.size());
        assertEquals(new StringValue(""), uncolorMap.get(new StringValue("RED")));
    }

    @Test
    public void true_() throws Exception
    {
        sefun sefun = makeSefunObject();

        ArrayValue arg = new ArrayValue(Types.MIXED_ARRAY, Collections.singleton(LpcConstants.INT.MINUS_ONE));
        assertEquals(LpcConstants.INT.ONE, sefun.true_(arg));

        arg = new ArrayValue(Types.MIXED_ARRAY, Collections.singleton(LpcConstants.INT.ZERO));
        assertEquals(LpcConstants.INT.ONE, sefun.true_(arg));

        arg = new ArrayValue(Types.MIXED_ARRAY, Collections.singleton(LpcConstants.STRING.BLANK));
        assertEquals(LpcConstants.INT.ONE, sefun.true_(arg));
    }

    @Test
    public void false_() throws Exception
    {
        sefun sefun = makeSefunObject();

        ArrayValue arg = new ArrayValue(Types.MIXED_ARRAY, Collections.singleton(LpcConstants.INT.MINUS_ONE));
        assertEquals(LpcConstants.INT.ZERO, sefun.false_(arg));

        arg = new ArrayValue(Types.MIXED_ARRAY, Collections.singleton(LpcConstants.INT.ZERO));
        assertEquals(LpcConstants.INT.ZERO, sefun.false_(arg));

        arg = new ArrayValue(Types.MIXED_ARRAY, Collections.singleton(LpcConstants.STRING.BLANK));
        assertEquals(LpcConstants.INT.ZERO, sefun.false_(arg));
    }

    @Test
    public void add_article() throws Exception
    {
        sefun sefun = makeSefunObject();

        LpcValue result = sefun.add_article_(new StringValue("frog"), LpcConstants.INT.FALSE);
        assertEquals(new StringValue("a frog"), result);

        result = sefun.add_article_(new StringValue("a frog"), LpcConstants.INT.TRUE);
        assertEquals(new StringValue("the frog"), result);

        result = sefun.add_article_(new StringValue("elephant"), LpcConstants.INT.FALSE);
        assertEquals(new StringValue("an elephant"), result);
    }

    @Test
    public void atoi() throws Exception
    {
        sefun sefun = makeSefunObject();

        LpcValue result = sefun.atoi_(new StringValue("1234"));
        assertEquals(new IntValue(1234), result);

        result = sefun.atoi_(new StringValue("xyz"));
        assertEquals(new IntValue(0), result);

        result = sefun.atoi_(NilValue.INSTANCE);
        assertEquals(new IntValue(0), result);

        result = sefun.atoi_(new StringValue("456z"));
        assertEquals(new IntValue(456), result);
    }

}
