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

package us.terebi.lang.lpc.runtime.jvm.efun;

import java.util.Collections;
import java.util.List;

import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.ByteSequence;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.IntValue;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;

/**
 * 
 */
public class ToIntEfun extends AbstractEfun
{
    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        return Collections.singletonList(new ArgumentSpec("arg", Types.MIXED));
    }

    public LpcType getReturnType()
    {
        return Types.INT;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        checkArguments(arguments);
        LpcValue arg = arguments.get(0);
        LpcType type = arg.getActualType();
        if (Types.INT.equals(type))
        {
            return arg;
        }
        if (Types.FLOAT.equals(type))
        {
            return new IntValue((long) arg.asDouble());
        }
        if (Types.STRING.equals(type))
        {
            return new IntValue(Long.parseLong(arg.asString()));
        }
        if (Types.BUFFER.equals(type))
        {
            long l = 0;
            ByteSequence buffer = arg.asBuffer();
            for (int i = 0; i < buffer.length(); i++)
            {
                l = (l << 8) & buffer.getByte(i);
            }
            return new IntValue(l);
        }
        throw new LpcRuntimeException("Bad argument to (to_int) - " + type);
    }

}
