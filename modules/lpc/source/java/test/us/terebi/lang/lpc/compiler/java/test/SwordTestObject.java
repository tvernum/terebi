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
import us.terebi.lang.lpc.runtime.jvm.InheritedObject;
import us.terebi.lang.lpc.runtime.jvm.LpcInherited;
import us.terebi.lang.lpc.runtime.jvm.LpcMethod;
import us.terebi.lang.lpc.runtime.jvm.LpcObject;
import us.terebi.lang.lpc.runtime.jvm.LpcParameter;
import us.terebi.lang.lpc.runtime.jvm.LpcReturn;
import us.terebi.lang.lpc.runtime.jvm.value.VoidValue;

/**
 * 
 */
public class SwordTestObject extends LpcObject
{
    @LpcInherited(name = "object", lpc = "/std/lib/object.c", implementation = "us.terebi.lang.lpc.compiler.java.test.ObjectTestObject")
    public InheritedObject<ObjectTestObject> inherit_object;

    public @LpcMethod(name = "create", modifiers = { Modifier.PUBLIC })
    @LpcReturn(kind = Kind.VOID, depth = 0)
    LpcValue create()
    {
        return VoidValue.INSTANCE;
    }

    @LpcMethod(name = "set_wc", modifiers = { Modifier.PUBLIC })
    @LpcReturn(kind = Kind.VOID, depth = 0)
    public LpcValue set_wc(@LpcParameter(name = "wc", kind = Kind.INT, depth = 0, semantics=ArgumentSemantics.BY_VALUE)
    LpcValue wc_)
    {
        return VoidValue.INSTANCE;
    }

    @LpcMethod(name = "add_wc_bonus", modifiers = { Modifier.PUBLIC })
    @LpcReturn(kind = Kind.VOID, depth = 0)
    public LpcValue add_wc_bonus(@LpcParameter(name = "func", kind = Kind.FUNCTION, depth = 0, semantics=ArgumentSemantics.BY_VALUE)
    LpcValue func_)
    {
        return VoidValue.INSTANCE;
    }

}
