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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isNil;

import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.efun.AbstractEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.ThisObjectEfun;
import us.terebi.lang.lpc.runtime.jvm.support.MiscSupport;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.ArrayValue;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;

/**
 * 
 */
public class InventoryEfun extends AbstractEfun
{
    private final boolean _deep;

    public InventoryEfun(boolean deep)
    {
        _deep = deep;
    }

    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        return Collections.singletonList(new ArgumentSpec("ob", Types.OBJECT));
    }

    public LpcType getReturnType()
    {
        return Types.OBJECT_ARRAY;
    }

    public boolean acceptsLessArguments()
    {
        return true;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        checkArguments(arguments);

        ObjectInstance obj;
        if (arguments.isEmpty() || isNil(arguments.get(0)))
        {
            obj = ThisObjectEfun.this_object();
        }
        else
        {
            obj = arguments.get(0).asObject();
        }

        List<ObjectInstance> inventory = Environment.getInventory(obj, _deep);
        List<LpcValue> array = new ArrayList<LpcValue>();
        for (ObjectInstance object : inventory)
        {
            array.add(object.asValue());
        }
        return new ArrayValue(Types.OBJECT_ARRAY, array);
    }

}
