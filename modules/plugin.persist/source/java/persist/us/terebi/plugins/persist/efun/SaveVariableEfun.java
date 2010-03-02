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

import java.util.Collections;
import java.util.List;

import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.efun.AbstractEfun;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.NilValue;
import us.terebi.lang.lpc.runtime.jvm.value.StringValue;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;
import us.terebi.plugins.persist.support.ObjectSerializer;

/**
 * @version $Revision$
 */
public class SaveVariableEfun extends AbstractEfun
{
    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        return Collections.singletonList(new ArgumentSpec("value", Types.MIXED));
    }

    public boolean acceptsLessArguments()
    {
        return false;
    }

    public LpcType getReturnType()
    {
        return Types.STRING;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        checkArguments(arguments);
        String save = ObjectSerializer.save(arguments.get(0));
        if (save == null)
        {
            return new NilValue(Types.STRING);
        }
        return new StringValue(save);
    }
}
