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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;
import java.util.Map;

import lpc.secure.sefun.sefun;

import org.junit.Test;

import us.terebi.lang.lpc.compiler.java.context.BasicScopeLookup;
import us.terebi.lang.lpc.compiler.java.context.LpcCompilerObjectManager;
import us.terebi.lang.lpc.compiler.java.context.ScopeLookup;
import us.terebi.lang.lpc.io.FileFinder;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.LpcConstants;
import us.terebi.lang.lpc.runtime.jvm.LpcField;
import us.terebi.lang.lpc.runtime.jvm.StandardEfuns;
import us.terebi.lang.lpc.runtime.jvm.context.CallStack;
import us.terebi.lang.lpc.runtime.jvm.context.Efuns;
import us.terebi.lang.lpc.runtime.jvm.context.RuntimeContext;
import us.terebi.lang.lpc.runtime.jvm.context.SystemContext;
import us.terebi.lang.lpc.runtime.jvm.object.CompiledDefinition;
import us.terebi.lang.lpc.runtime.jvm.object.CompiledObject;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.ArrayValue;
import us.terebi.lang.lpc.runtime.jvm.value.IntValue;
import us.terebi.lang.lpc.runtime.jvm.value.NilValue;
import us.terebi.lang.lpc.runtime.jvm.value.StringValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

/**
 * 
 */
public class SefunTest
{
    private static CompiledDefinition<sefun> _definition;
    private static LpcCompilerObjectManager _manager;

    private sefun makeSefunObject()
    {
        if (_definition == null)
        {
            _manager = new LpcCompilerObjectManager();
            ScopeLookup lookup = new BasicScopeLookup(_manager);
            _definition = new CompiledDefinition<sefun>(_manager, lookup, "sefun", sefun.class);
        }
        Efuns functions = StandardEfuns.getImplementation();
        RuntimeContext.activate(new SystemContext(functions, _manager, new FileFinder(new File("/"))));
        synchronized (RuntimeContext.lock())
        {
            CompiledObject<sefun> instance = _definition.newInstance(Collections.<LpcValue> emptyList());
            RuntimeContext.obtain().callStack().pushFrame(CallStack.Origin.APPLY, instance);

            // printMemoryUsage("*");

            return instance.getImplementingObject();
        }
    }

    @SuppressWarnings("unused")
    private void printMemoryUsage(String prefix)
    {
        System.gc();
        Runtime runtime = Runtime.getRuntime();
        new Formatter(System.err).format("%s Memory: %6dk / %6dk / %6dk\n", prefix,
                (runtime.totalMemory() - runtime.freeMemory()) / 1024, runtime.totalMemory() / 1024, runtime.maxMemory() / 1024);
    }

    @Test
    public void abs() throws Exception
    {
        sefun sefun = makeSefunObject();
        synchronized (RuntimeContext.lock())
        {
            assertEquals(LpcConstants.INT.ONE, sefun.abs_(LpcConstants.INT.MINUS_ONE));
        }
    }

    @Test
    public void _f_Uncolor() throws Exception
    {
        sefun sefun = makeSefunObject();
        synchronized (RuntimeContext.lock())
        {
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
    }

    @Test
    public void true_() throws Exception
    {
        sefun sefun = makeSefunObject();

        synchronized (RuntimeContext.lock())
        {
            ArrayValue arg = new ArrayValue(Types.MIXED_ARRAY, Collections.singleton(LpcConstants.INT.MINUS_ONE));
            assertEquals(LpcConstants.INT.ONE, sefun.true_(arg));

            arg = new ArrayValue(Types.MIXED_ARRAY, Collections.singleton(LpcConstants.INT.ZERO));
            assertEquals(LpcConstants.INT.ONE, sefun.true_(arg));

            arg = new ArrayValue(Types.MIXED_ARRAY, Collections.singleton(LpcConstants.STRING.BLANK));
            assertEquals(LpcConstants.INT.ONE, sefun.true_(arg));
        }
    }

    @Test
    public void false_() throws Exception
    {
        sefun sefun = makeSefunObject();
        synchronized (RuntimeContext.lock())
        {

            ArrayValue arg = new ArrayValue(Types.MIXED_ARRAY, Collections.singleton(LpcConstants.INT.MINUS_ONE));
            assertEquals(LpcConstants.INT.ZERO, sefun.false_(arg));

            arg = new ArrayValue(Types.MIXED_ARRAY, Collections.singleton(LpcConstants.INT.ZERO));
            assertEquals(LpcConstants.INT.ZERO, sefun.false_(arg));

            arg = new ArrayValue(Types.MIXED_ARRAY, Collections.singleton(LpcConstants.STRING.BLANK));
            assertEquals(LpcConstants.INT.ZERO, sefun.false_(arg));
        }
    }

    @Test
    public void add_article() throws Exception
    {
        sefun sefun = makeSefunObject();
        synchronized (RuntimeContext.lock())
        {

            LpcValue result = sefun.add_article_(new StringValue("frog"), LpcConstants.INT.FALSE);
            assertEquals(new StringValue("a frog"), result);

            result = sefun.add_article_(new StringValue("a frog"), LpcConstants.INT.TRUE);
            assertEquals(new StringValue("the frog"), result);

            result = sefun.add_article_(new StringValue("elephant"), LpcConstants.INT.FALSE);
            assertEquals(new StringValue("an elephant"), result);
        }
    }

    @Test
    public void atoi() throws Exception
    {
        sefun sefun = makeSefunObject();
        synchronized (RuntimeContext.lock())
        {

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

    @Test
    public void atomize_array() throws Exception
    {
        sefun sefun = makeSefunObject();
        synchronized (RuntimeContext.lock())
        {
            LpcValue[] elements = new LpcValue[] { new StringValue("chicken"), new StringValue("chick"),
                    new StringValue("chook"), new StringValue("hen") };
            LpcValue array = new ArrayValue(Types.STRING_ARRAY, Arrays.asList(elements));
            LpcValue result = sefun.atomize_array_(array);

            assertNotNull(result);
            assertEquals(1, result.getActualType().getArrayDepth());
            List<LpcValue> list = result.asList();
            assertNotNull(list);
            // cHICKEN , chOOK, hEN
            assertEquals(6 + 3 + 2, list.size());
            assertEquals(1, result.getActualType().getArrayDepth());
            for (LpcValue element : list)
            {
                assertEquals(Types.STRING, element.getActualType());
            }
        }
    }

    @Test
    public void absolute_path() throws Exception
    {
        sefun sefun = makeSefunObject();
        synchronized (RuntimeContext.lock())
        {
            assertEquals("/foo/", sefun.absolute_path_(new StringValue("/foo/"), new StringValue(".")).asString());
            assertEquals("/domains/zoop/whig/zah.c", sefun.absolute_path_(new StringValue("/foo/"),
                    new StringValue("^zoop/whig/zah.c")).asString());
            assertEquals("/foo/xyz/./123",
                    sefun.absolute_path_(new StringValue("/foo/bar"), new StringValue("../xyz/./123/")).asString());
        }
    }

    @Test
    public void compare_array() throws Exception
    {
        sefun sefun = makeSefunObject();
        synchronized (RuntimeContext.lock())
        {
            List<LpcValue> list1 = new ArrayList<LpcValue>();
            List<LpcValue> list2 = new ArrayList<LpcValue>();
            list1.add(new StringValue("abc"));
            list1.add(new StringValue("xyz"));
            list2.addAll(list1);

            ArrayValue arr1 = new ArrayValue(Types.STRING_ARRAY, list1);
            ArrayValue arr2 = new ArrayValue(Types.MIXED_ARRAY, list2);
            assertEquals(LpcConstants.INT.ONE, sefun.compare_array_(arr1, arr2));

            list1.add(new StringValue("123"));
            assertEquals(LpcConstants.INT.ZERO, sefun.compare_array_(arr1, arr2));

            list2.add(list1.get(list1.size() - 1));
            assertEquals(LpcConstants.INT.ONE, sefun.compare_array_(arr1, arr2));

            list2.add(LpcConstants.INT.MINUS_ONE);
            assertEquals(LpcConstants.INT.ZERO, sefun.compare_array_(arr1, arr2));
        }

    }

    @Test
    public void scramble_array() throws Exception
    {
        sefun sefun = makeSefunObject();
        synchronized (RuntimeContext.lock())
        {
            List<LpcValue> list = new ArrayList<LpcValue>();
            list.add(new IntValue(0));
            list.add(new IntValue(2));
            list.add(new IntValue(4));
            list.add(new IntValue(1));
            list.add(new IntValue(3));
            ArrayValue array = new ArrayValue(Types.MIXED_ARRAY, list);

            LpcValue result = sefun.scramble_array_(array);

            // Value passed in should not change...
            assertSame(list, array.asList());
            assertEquals(0, list.get(0).asLong());
            assertEquals(2, list.get(1).asLong());
            assertEquals(4, list.get(2).asLong());
            assertEquals(1, list.get(3).asLong());
            assertEquals(3, list.get(4).asLong());

            assertEquals(Types.MIXED_ARRAY, result.getActualType());
            List<LpcValue> resultList = result.asList();
            assertEquals(list.size(), resultList.size());
        }
    }

    @Test
    public void sefun_exists() throws Exception
    {
        sefun sefun = makeSefunObject();
        synchronized (RuntimeContext.lock())
        {
            assertEquals(LpcConstants.INT.ONE, sefun.sefun_exists_(new StringValue("sefun_exists")));
            assertEquals(LpcConstants.INT.ZERO, sefun.sefun_exists_(new StringValue("xyzzy")));
        }
    }
}
