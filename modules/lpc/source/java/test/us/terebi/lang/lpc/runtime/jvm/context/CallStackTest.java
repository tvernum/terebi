/* ------------------------------------------------------------------------
 * Copyright 2010 Tim Vernum
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

package us.terebi.lang.lpc.runtime.jvm.context;

import java.io.File;
import java.util.Collections;
import java.util.List;

import lpc.terebi.test.CallStackTestObject1;
import lpc.terebi.test.CallStackTestObject2;

import org.junit.Assert;
import org.junit.Test;

import us.terebi.lang.lpc.compiler.CompilerObjectManager;
import us.terebi.lang.lpc.compiler.ObjectBuilder;
import us.terebi.lang.lpc.compiler.ObjectBuilderFactory;
import us.terebi.lang.lpc.compiler.java.context.BasicScopeLookup;
import us.terebi.lang.lpc.compiler.java.context.CompiledObjectInstance;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.StandardEfuns;
import us.terebi.lang.lpc.runtime.jvm.context.CallStack.DetailFrame;
import us.terebi.lang.lpc.runtime.jvm.context.CallStack.MajorFrame;
import us.terebi.lang.lpc.runtime.jvm.context.CallStack.Origin;
import us.terebi.lang.lpc.runtime.jvm.object.CompiledDefinition;
import us.terebi.lang.lpc.runtime.jvm.value.ObjectValue;
import us.terebi.lang.lpc.runtime.util.InContext;
import us.terebi.lang.lpc.runtime.util.InContext.Exec;

/**
 * 
 */
public class CallStackTest
{
    @Test
    public void testCallStack() throws Exception
    {
        Efuns efuns = StandardEfuns.getImplementation();

        ObjectBuilderFactory factory = new ObjectBuilderFactory(efuns);
        ObjectBuilder builder = factory.createBuilder(new File("."));

        CompilerObjectManager manager = builder.getObjectManager();
        SystemContext context = new SystemContext(efuns, manager, builder.getSourceFinder());
        RuntimeContext.activate(context);

        synchronized (context.lock())
        {

            CompiledDefinition<CallStackTestObject1> def1 = new CompiledDefinition<CallStackTestObject1>( // 
                    manager, new BasicScopeLookup(manager), "/terebi/CallStackTestObject1", CallStackTestObject1.class);
            CompiledDefinition<CallStackTestObject2> def2 = new CompiledDefinition<CallStackTestObject2>( //
                    manager, new BasicScopeLookup(manager), "/terebi/CallStackTestObject2", CallStackTestObject2.class);

            final CompiledObjectInstance obj1 = def1.newInstance(Collections.<LpcValue> emptyList());
            final CompiledObjectInstance obj2 = def2.newInstance(Collections.<LpcValue> emptyList());

            final CallStackTestObject1 cs1 = (CallStackTestObject1) obj1.getImplementingObject();
            final CallStackTestObject2 cs2 = (CallStackTestObject2) obj2.getImplementingObject();

            final LpcValue val2 = new ObjectValue(obj2);

            InContext.execute(Origin.DRIVER, obj1, new Exec<Object>()
            {
                public Object execute()
                {
                    cs1.topFunction(val2);
                    return null;
                }
            });

            List<MajorFrame> major = cs2.getMajor();
            List<DetailFrame> detail = cs2.getDetail();

            Assert.assertEquals(2, major.size());
            Assert.assertEquals(Origin.CALL_OTHER, major.get(0).origin());
            Assert.assertEquals(Origin.DRIVER, major.get(1).origin());
            Assert.assertEquals(obj2, major.get(0).instance());
            Assert.assertEquals(obj1, major.get(1).instance());

            Assert.assertEquals(4, detail.size());
            Assert.assertEquals(Origin.CALL_OTHER, detail.get(0).origin());
            Assert.assertEquals(Origin.CALL_OTHER, detail.get(1).origin());
            Assert.assertEquals(Origin.DRIVER, detail.get(2).origin());
            Assert.assertEquals(Origin.DRIVER, detail.get(3).origin());
            Assert.assertEquals(obj2, detail.get(0).instance());
            Assert.assertEquals(obj2, detail.get(1).instance());
            Assert.assertEquals(obj1, detail.get(2).instance());
            Assert.assertEquals(obj1, detail.get(3).instance());
            Assert.assertEquals("secondFunction", detail.get(0).function());
            Assert.assertEquals("topFunction", detail.get(1).function());
            Assert.assertEquals("secondFunction", detail.get(2).function());
            Assert.assertEquals("topFunction", detail.get(3).function());
        }
    }
}
