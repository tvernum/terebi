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
import java.util.List;

import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.efun.AbstractEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.ThisObjectEfun;
import us.terebi.lang.lpc.runtime.jvm.support.MiscSupport;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.util.Apply;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;

/**
 * 
 */
public class PresentEfun extends AbstractEfun implements FunctionSignature, Callable
{
    private static final Apply ID = new Apply("id");

    //  object present( mixed str);
    //  object present( mixed str, object ob );
    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        ArrayList<ArgumentDefinition> list = new ArrayList<ArgumentDefinition>();
        list.add(new ArgumentSpec("id", Types.MIXED));
        list.add(new ArgumentSpec("location", Types.OBJECT));
        return list;
    }

    public boolean acceptsLessArguments()
    {
        return true;
    }

    public LpcType getReturnType()
    {
        return Types.OBJECT;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        checkArguments(arguments, 1);
        boolean present = present(arguments);
        return getValue(present);
    }

    private boolean present(List< ? extends LpcValue> arguments)
    {
        List<ObjectInstance> inventory;
        if (arguments.size() < 2)
        {
            ObjectInstance thisObject = ThisObjectEfun.this_object();
            inventory = new ArrayList<ObjectInstance>();
            inventory.addAll(Environment.getInventory(thisObject, false));
            inventory.addAll(Environment.getInventory(Environment.getEnvironment(thisObject), false));
        }
        else
        {
            ObjectInstance obj = arguments.get(1).asObject();
            inventory = Environment.getInventory(obj, false);
        }

        LpcValue arg1 = arguments.get(0);
        if (MiscSupport.isObject(arg1))
        {
            return inventory.contains(arg1.asObject());
        }

        for (ObjectInstance object : inventory)
        {
            if (ID.invoke(object, arg1).asBoolean())
            {
                return true;
            }
        }
        return false;
    }

}
