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

import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.MemberDefinition.Modifier;
import us.terebi.lang.lpc.runtime.jvm.LpcClass;
import us.terebi.lang.lpc.runtime.jvm.LpcField;
import us.terebi.lang.lpc.runtime.jvm.LpcFunction;
import us.terebi.lang.lpc.runtime.jvm.LpcMember;
import us.terebi.lang.lpc.runtime.jvm.LpcMemberType;
import us.terebi.lang.lpc.runtime.jvm.LpcObject;
import us.terebi.lang.lpc.runtime.jvm.LpcParameter;
import us.terebi.lang.lpc.runtime.jvm.LpcReference;
import us.terebi.lang.lpc.runtime.jvm.LpcVariable;
import us.terebi.lang.lpc.runtime.jvm.support.CallableSupport;
import us.terebi.lang.lpc.runtime.jvm.support.ClassSupport;
import us.terebi.lang.lpc.runtime.jvm.support.ComparisonSupport;
import us.terebi.lang.lpc.runtime.jvm.support.MathSupport;

public class ClassTestObject extends LpcObject
{
    public @LpcMember(name = "ii", modifiers = {})
    @LpcMemberType(kind = us.terebi.lang.lpc.runtime.LpcType.Kind.INT, depth = 0)
    LpcValue ii_()
    {
        final LpcValue _lpc_v1 = makeValue(7);
        return _lpc_v1;
    }

    @LpcMember(name = "huh", modifiers = { Modifier.PRIVATE })
    public class cls_huh extends LpcClass
    {
        public cls_huh()
        {
            super(getObjectDefinition());
        }

        public final LpcField _f_i = new LpcField("i", withType(LpcType.Kind.INT, 0));
        {
            final LpcValue _lpc_v2 = ii_();
            _f_i.set(_lpc_v2);
        }
        public final LpcField _f_s = new LpcField("s", withType(LpcType.Kind.STRING, 0));
        public final LpcField _f_f = new LpcField("f", withType(LpcType.Kind.FUNCTION, 0));
    }

    public @LpcMember(name = "foo", modifiers = {})
    @LpcMemberType(kind = us.terebi.lang.lpc.runtime.LpcType.Kind.CLASS, depth = 0, className = "huh")
    LpcValue foo_()
    {
        final LpcValue _lpc_v3 = classReference(cls_huh.class);
        final LpcValue _lpc_v4 = efun("new").execute(_lpc_v3, makeArray());
        final LpcVariable _l_huh = new LpcVariable("huh", withType(classDefinition(cls_huh.class), 0), _lpc_v4);
        final LpcReference _lpc_v5 = ClassSupport.getField(_l_huh.get(), "i");
        final LpcValue _lpc_v6 = makeValue(7);
        _lpc_v5.set(_lpc_v6);
        final LpcReference _lpc_v7 = ClassSupport.getField(_l_huh.get(), "s");
        final LpcValue _lpc_v8 = makeValue("seven");
        _lpc_v7.set(_lpc_v8);
        final LpcReference _lpc_v9 = ClassSupport.getField(_l_huh.get(), "f");
        final LpcValue _lpc_v10 = new LpcFunction(getObjectInstance(), 0)
        {
            public LpcValue execute(LpcValue... args)
            {
                final LpcValue _lpc_v11 = makeValue(7);
                return _lpc_v11;
            } /* _lpc_v10.execute() */
        }; /* _lpc_v10 */
        _lpc_v9.set(_lpc_v10);
        return _l_huh.get();
    }

    public @LpcMember(name = "test", modifiers = {})
    @LpcMemberType(kind = us.terebi.lang.lpc.runtime.LpcType.Kind.VOID, depth = 0)
    LpcValue test_(
            @LpcParameter(kind = us.terebi.lang.lpc.runtime.LpcType.Kind.CLASS, depth = 0, className = "huh", name = "h", semantics = us.terebi.lang.lpc.runtime.ArgumentSemantics.BY_VALUE)
            LpcValue _p_h_v)
    {
        final LpcVariable _p_h = new LpcVariable("h", withType(classDefinition(cls_huh.class), 0), _p_h_v);
        final LpcReference _lpc_v12 = ClassSupport.getField(_p_h.get(), "f");
        final LpcValue _lpc_v13 = CallableSupport.asCallable(_lpc_v12.get()).execute();
        final LpcReference _lpc_v14 = ClassSupport.getField(_p_h.get(), "i");
        final LpcValue _lpc_v15 = ComparisonSupport.notEqual(_lpc_v13, _lpc_v14.get());
        if (_lpc_v15.asBoolean())
        {
            final LpcReference _lpc_v16 = ClassSupport.getField(_p_h.get(), "s");
            final LpcValue _lpc_v17 = makeValue(" failed");
            final LpcValue _lpc_v18 = MathSupport.add(_lpc_v16.get(), _lpc_v17);
            final LpcValue _lpc_v19 = efun("write").execute(_lpc_v18);
        } // (if _lpc_v15)
        /* 'if'@23.5 */

        return makeValue();
    }

}
