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

package us.terebi.plugins.persist.efun;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.LpcConstants;
import us.terebi.lang.lpc.runtime.jvm.context.RuntimeContext;
import us.terebi.lang.lpc.runtime.jvm.context.ThreadContext;
import us.terebi.lang.lpc.runtime.jvm.efun.AbstractEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.ThisObjectEfun;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;
import us.terebi.plugins.persist.support.ObjectSerializer;

/**
 * @version $Revision$
 */
public class RestoreObjectEfun extends AbstractEfun
{
    private final static Logger LOG = Logger.getLogger(RestoreObjectEfun.class);

    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        return Arrays.asList( //
                new ArgumentSpec("file", Types.STRING), //
                new ArgumentSpec("flag", Types.INT) //
        );
    }

    public boolean acceptsLessArguments()
    {
        return true;
    }

    public LpcType getReturnType()
    {
        return Types.INT;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        checkArguments(arguments, 1);
        String file = arguments.get(0).asString();
        long flag = (arguments.size() >= 2 ? arguments.get(1).asLong() : 0);
        ThreadContext threadContext = RuntimeContext.obtain();
        ObjectInstance object = ThisObjectEfun.this_object(threadContext);
        try
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Restore " + object + " from " + file + " with flag " + flag);
            }
            boolean restored = new ObjectSerializer(file).restore(object, flag != 1);
            return getValue(restored);
        }
        catch (IOException e)
        {
            LOG.warn("I/O error during restore_object(" + arguments + ")", e);
            return LpcConstants.INT.FALSE;
        }
    }
}
