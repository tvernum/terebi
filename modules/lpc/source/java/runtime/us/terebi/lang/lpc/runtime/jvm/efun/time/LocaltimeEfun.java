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

package us.terebi.lang.lpc.runtime.jvm.efun.time;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.efun.AbstractEfun;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.ArrayValue;
import us.terebi.lang.lpc.runtime.jvm.value.IntValue;
import us.terebi.lang.lpc.runtime.jvm.value.StringValue;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;

/**
 * 
 */
public class LocaltimeEfun extends AbstractEfun implements FunctionSignature, Callable
{
    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        return Collections.singletonList(new ArgumentSpec("clock", Types.INT));
    }

    public LpcType getReturnType()
    {
        return Types.MIXED_ARRAY;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        checkArguments(arguments);
        LpcValue clock = arguments.get(0);
        long time = clock.asLong() * 1000;

        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(time);

        List<LpcValue> array = new ArrayList<LpcValue>(10);
        array.add(new IntValue(calendar.get(Calendar.SECOND)));
        array.add(new IntValue(calendar.get(Calendar.MINUTE)));
        array.add(new IntValue(calendar.get(Calendar.HOUR)));
        array.add(new IntValue(calendar.get(Calendar.DAY_OF_MONTH)));
        array.add(new IntValue(calendar.get(Calendar.MONTH)));
        array.add(new IntValue(calendar.get(Calendar.YEAR) + 1900));
        array.add(new IntValue(calendar.get(Calendar.DAY_OF_WEEK)));
        array.add(new IntValue(calendar.get(Calendar.DAY_OF_YEAR)));
        TimeZone timeZone = calendar.getTimeZone();
        long offset = timeZone.getOffset(time) / 1000;
        array.add(new IntValue(offset));
        array.add(new StringValue(timeZone.getDisplayName()));
        return new ArrayValue(Types.MIXED_ARRAY, array);
    }
}
