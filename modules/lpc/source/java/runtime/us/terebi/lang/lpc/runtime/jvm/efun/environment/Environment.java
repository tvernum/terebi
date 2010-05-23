/* ------------------------------------------------------------------------
 * Copyright 2010 Tim Vernum
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

import us.terebi.lang.lpc.runtime.ObjectInstance;

/**
 * 
 */
public class Environment
{
    public static final String ENVIRONMENT = "terebi.environment.environment";
    public static final String INVENTORY = "terebi.environment.inventory";

    public static ObjectInstance getEnvironment(ObjectInstance obj)
    {
        return (ObjectInstance) obj.getAttributes().get(ENVIRONMENT);
    }

    @SuppressWarnings("unchecked")
    private static List<ObjectInstance> inventoryOf(ObjectInstance obj, boolean create)
    {
        List<ObjectInstance> inventory = (List<ObjectInstance>) obj.getAttributes().get(INVENTORY);
        if (inventory == null)
        {
            if (!create)
            {
                return Collections.emptyList();
            }
            inventory = new ArrayList<ObjectInstance>();
            obj.getAttributes().set(INVENTORY, inventory);
        }
        return inventory;
    }

    public static List<ObjectInstance> getInventory(ObjectInstance obj, boolean deep)
    {
        List<ObjectInstance> inventory = inventoryOf(obj, false);

        if (deep)
        {
            List<ObjectInstance> all = new ArrayList<ObjectInstance>();
            getDeepInventory(inventory, all);
            return all;
        }
        return inventory;
    }

    private static void getDeepInventory(Iterable<ObjectInstance> inventory, List<ObjectInstance> collection)
    {
        for (ObjectInstance object : inventory)
        {
            collection.add(object);
            getDeepInventory(inventoryOf(object, false), collection);
        }
    }

    public static void move(ObjectInstance object, ObjectInstance destination)
    {
        ObjectInstance from = getEnvironment(object);
        if (from != null)
        {
            inventoryOf(from, false).remove(object);
        }
        inventoryOf(destination, true).add(object);
        object.getAttributes().set(ENVIRONMENT, destination);
    }
}
