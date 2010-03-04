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

import us.terebi.lang.lpc.compiler.java.context.CompiledObjectDefinition;
import us.terebi.lang.lpc.runtime.ArgumentSemantics;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.LpcType.Kind;
import us.terebi.lang.lpc.runtime.MemberDefinition.Modifier;
import us.terebi.lang.lpc.runtime.jvm.LpcConstants;
import us.terebi.lang.lpc.runtime.jvm.LpcMember;
import us.terebi.lang.lpc.runtime.jvm.LpcMemberType;
import us.terebi.lang.lpc.runtime.jvm.LpcObject;
import us.terebi.lang.lpc.runtime.jvm.LpcParameter;
import us.terebi.lang.lpc.runtime.jvm.value.StringValue;

/**
 * 
 */
public class CallStackTestObject1 extends LpcObject implements ICallStackTestObject1
{
    @SuppressWarnings("unused")
    public CallStackTestObject1(ICallStackTestObject1 self, CompiledObjectDefinition definition)
    {
        super(definition);
    }

    @LpcMember(modifiers = Modifier.PUBLIC, name = "topFunction")
    @LpcMemberType(kind = Kind.NIL, depth = 0)
    public LpcValue topFunction(@LpcParameter(depth = 0, kind = Kind.OBJECT, semantics = ArgumentSemantics.BY_VALUE, name = "other") LpcValue other)
    {
        return secondFunction(other);
    }

    @LpcMember(modifiers = Modifier.PRIVATE, name = "secondFunction")
    @LpcMemberType(kind = Kind.NIL, depth = 0)
    public LpcValue secondFunction(@LpcParameter(depth = 0, kind = Kind.OBJECT, semantics = ArgumentSemantics.BY_VALUE, name = "other") LpcValue other)
    {
        return efun("call_other").execute(other, new StringValue("topFunction"), LpcConstants.ARRAY.EMPTY);
    }
}
