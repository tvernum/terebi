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

package us.terebi.lang.lpc.runtime.jvm.efun.environment;

import java.util.Collections;
import java.util.List;

import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.context.ObjectManager;
import us.terebi.lang.lpc.runtime.jvm.context.RuntimeContext;
import us.terebi.lang.lpc.runtime.jvm.context.SystemContext;
import us.terebi.lang.lpc.runtime.jvm.efun.AbstractEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.ThisObjectEfun;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;
import us.terebi.lang.lpc.runtime.jvm.support.MiscSupport;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.VoidValue;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;

/**
 * 
 */
public class MoveObjectEfun extends AbstractEfun
{
    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        return Collections.singletonList(new ArgumentSpec("dest", Types.MIXED));
    }

    public LpcType getReturnType()
    {
        return Types.VOID;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        checkArguments(arguments);
        LpcValue arg1 = arguments.get(0);

        ObjectInstance destination;
        if (MiscSupport.isString(arg1))
        {
            SystemContext system = RuntimeContext.obtain().system();
            ObjectManager objectManager = system.objectManager();
            destination = objectManager.findObject(arg1.asString(), true).getMasterInstance();
        }
        else
        {
            destination = arguments.get(0).asObject();
        }

        if (destination == null)
        {
            throw new LpcRuntimeException("No destination");
        }

        ObjectInstance object = ThisObjectEfun.this_object();
        if (object == null)
        {
            throw new LpcRuntimeException("No current object");
        }
        Environment.move(object, destination);

        return VoidValue.INSTANCE;
    }

}
