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

import us.terebi.lang.lpc.runtime.ArgumentSemantics;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.LpcType.Kind;
import us.terebi.lang.lpc.runtime.MemberDefinition.Modifier;
import us.terebi.lang.lpc.runtime.jvm.LpcMember;
import us.terebi.lang.lpc.runtime.jvm.LpcObject;
import us.terebi.lang.lpc.runtime.jvm.LpcParameter;
import us.terebi.lang.lpc.runtime.jvm.LpcMemberType;
import us.terebi.lang.lpc.runtime.jvm.value.VoidValue;

/**
 * 
 */
public class ObjectTestObject extends LpcObject
{
    @LpcMember(name="set_name", modifiers = { Modifier.PUBLIC })
    @LpcMemberType(kind = Kind.VOID, depth = 0)
    public LpcValue set_name(@LpcParameter(name = "name", kind = Kind.STRING, depth = 0, semantics=ArgumentSemantics.BY_VALUE)
    LpcValue name)
    {
        return VoidValue.INSTANCE;
    }

    @LpcMember(name="add_alias", modifiers = { Modifier.PUBLIC })
    @LpcMemberType(kind = Kind.VOID, depth = 0)
    public LpcValue add_alias(@LpcParameter(name = "alias", kind = Kind.STRING, depth = 0, semantics=ArgumentSemantics.BY_VALUE)
    LpcValue alias)
    {
        return VoidValue.INSTANCE;
    }

    @LpcMember(name="set_short", modifiers = { Modifier.PUBLIC })
    @LpcMemberType(kind = Kind.VOID, depth = 0)
    public LpcValue set_short(@LpcParameter(name = "short", kind = Kind.STRING, depth = 0, semantics=ArgumentSemantics.BY_VALUE)
    LpcValue short_)
    {
        return VoidValue.INSTANCE;
    }

    @LpcMember(name="set_long", modifiers = { Modifier.PUBLIC })
    @LpcMemberType(kind = Kind.VOID, depth = 0)
    public LpcValue set_long(@LpcParameter(name = "long", kind = Kind.STRING, depth = 0, semantics=ArgumentSemantics.BY_VALUE)
    LpcValue long_)
    {
        return VoidValue.INSTANCE;
    }
    
    @LpcMember(name="set_weight", modifiers = { Modifier.PUBLIC })
    @LpcMemberType(kind = Kind.VOID, depth = 0)
    public LpcValue set_weight(@LpcParameter(name = "weight", kind = Kind.INT, depth = 0, semantics=ArgumentSemantics.BY_VALUE)
            LpcValue weight_)
    {
        return VoidValue.INSTANCE;
    }

}
