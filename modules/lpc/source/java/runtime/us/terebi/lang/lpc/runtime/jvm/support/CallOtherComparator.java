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

package us.terebi.lang.lpc.runtime.jvm.support;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.efun.CallOtherEfun;
import us.terebi.lang.lpc.runtime.jvm.value.NilValue;

/**
 * 
 */
public class CallOtherComparator implements Comparator<LpcValue>
{
    private final ObjectInstance _object;
    private final String _func;
    private final List<LpcValue> _args;

    public CallOtherComparator(ObjectInstance object, String func, List<LpcValue> args)
    {
        _object = object;
        _func = func;
        _args = new ArrayList<LpcValue>(args.size() + 2);
        _args.add(NilValue.INSTANCE);
        _args.add(NilValue.INSTANCE);
        _args.addAll(args);
    }

    public int compare(LpcValue o1, LpcValue o2)
    {
        _args.set(0, o1);
        _args.set(1, o2);
        LpcValue cmp = CallOtherEfun.callOther(_object, _func, _args);
        if (MiscSupport.isInt(cmp))
        {
            long l = cmp.asLong();
            return l < 0 ? -1 : l > 0 ? +1 : 0;
        }
        return 0;
    }

}
