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

package us.terebi.lang.lpc.runtime.jvm;

import java.util.Collections;

import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.ArrayValue;
import us.terebi.lang.lpc.runtime.jvm.value.IntValue;
import us.terebi.lang.lpc.runtime.jvm.value.NilValue;
import us.terebi.lang.lpc.runtime.jvm.value.StringValue;

/**
 * 
 */
public class LpcConstants
{
    public static final class INT
    {
        public static final IntValue ZERO = new IntValue(0);
        public static final IntValue ONE = new IntValue(1);
        public static final IntValue TWO = new IntValue(2);
        public static final IntValue MINUS_ONE = new IntValue(-1);

        public static final IntValue TRUE = ONE;
        public static final IntValue FALSE = ZERO;
    }

    public static final class STRING
    {
        public static final StringValue BLANK = new StringValue("");
    }

    public static final class ARRAY
    {
        public static final ArrayValue EMPTY = new ArrayValue(Types.MIXED_ARRAY, Collections.<LpcValue> emptyList());
    }
    
    public static final NilValue NIL = NilValue.INSTANCE;

}
