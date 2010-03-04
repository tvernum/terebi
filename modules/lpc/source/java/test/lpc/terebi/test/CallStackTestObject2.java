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

package lpc.terebi.test;

import java.util.ArrayList;
import java.util.List;

import us.terebi.lang.lpc.compiler.java.context.CompiledObjectDefinition;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.LpcType.Kind;
import us.terebi.lang.lpc.runtime.MemberDefinition.Modifier;
import us.terebi.lang.lpc.runtime.jvm.LpcMember;
import us.terebi.lang.lpc.runtime.jvm.LpcMemberType;
import us.terebi.lang.lpc.runtime.jvm.LpcObject;
import us.terebi.lang.lpc.runtime.jvm.context.CallStack;
import us.terebi.lang.lpc.runtime.jvm.context.RuntimeContext;
import us.terebi.lang.lpc.runtime.jvm.context.CallStack.DetailFrame;
import us.terebi.lang.lpc.runtime.jvm.context.CallStack.MajorFrame;
import us.terebi.lang.lpc.runtime.jvm.value.NilValue;

/**
 * 
 */
public class CallStackTestObject2 extends LpcObject implements ICallStackTestObject2
{
    @SuppressWarnings("unused")
    public CallStackTestObject2(ICallStackTestObject2 self, CompiledObjectDefinition definition)
    {
        super(definition);
    }

    private List<MajorFrame> _major;
    private List<DetailFrame> _detail;

    @LpcMember(modifiers = Modifier.PUBLIC, name = "topFunction")
    @LpcMemberType(kind = Kind.NIL, depth = 0)
    public LpcValue topFunction()
    {
        return secondFunction();
    }

    @LpcMember(modifiers = Modifier.PRIVATE, name = "secondFunction")
    @LpcMemberType(kind = Kind.NIL, depth = 0)
    public LpcValue secondFunction()
    {
        CallStack stack = RuntimeContext.obtain().callStack();
        _major = getList(stack.majorFrames());
        _detail = getList(stack.detailFrames());
        return NilValue.INSTANCE;
    }

    public List<MajorFrame> getMajor()
    {
        return _major;
    }

    public List<DetailFrame> getDetail()
    {
        return _detail;
    }

    private <T> List<T> getList(Iterable<T> iterable)
    {
        List<T> list = new ArrayList<T>();
        for (T t : iterable)
        {
            list.add(t);
        }
        return list;
    }
}
