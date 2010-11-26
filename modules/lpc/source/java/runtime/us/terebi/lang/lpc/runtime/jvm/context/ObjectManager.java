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

package us.terebi.lang.lpc.runtime.jvm.context;

import us.terebi.lang.lpc.compiler.java.context.ObjectId;
import us.terebi.lang.lpc.runtime.ObjectDefinition;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;

/**
 * 
 */
public interface ObjectManager
{
    public static class ObjectNotFoundException extends LpcRuntimeException
    {
        public ObjectNotFoundException(String name)
        {
            super("Object " + name + " not found");
        }
    }
    
    public ObjectDefinition defineMasterObject(String name);
    public ObjectInstance getMasterObject();

    public ObjectDefinition defineSimulatedEfunObject(String name);
    public ObjectInstance getSimulatedEfunObject();

    /**
     * @throws ObjectNotFoundException If <code>load</code> is <code>true</code>, but object cannot be found
     * @return The loaded object, or <code>null</code> if <code>load</code> is <code>false</code> and the object is not already loaded
     */
    public ObjectDefinition findObject(String name, boolean load);
    public ObjectInstance findObject(ObjectId id);
    
    public int objectCount();
    public Iterable< ? extends ObjectInstance> objects();
    public Iterable<? extends ObjectInstance> objects(String name);
    
    public void addListener(ObjectLifecycleListener listener);
    public void removeListener(ObjectLifecycleListener listener);
}
