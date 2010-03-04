/* ------------------------------------------------------------------------
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

package us.terebi.plugins.privs;

import us.terebi.lang.lpc.compiler.java.context.CompiledObjectDefinition;
import us.terebi.lang.lpc.compiler.java.context.CompiledObjectInstance;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.context.ObjectLifecycleListener;
import us.terebi.lang.lpc.runtime.jvm.context.ObjectManager;
import us.terebi.lang.lpc.runtime.jvm.context.SystemContext;
import us.terebi.lang.lpc.runtime.jvm.support.MiscSupport;
import us.terebi.lang.lpc.runtime.jvm.value.StringValue;
import us.terebi.lang.lpc.runtime.util.Apply;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public class PrivsListener implements ObjectLifecycleListener
{
    private static final Apply LOAD_PRIVS = new Apply("privs_file");

    public void objectCompiled(SystemContext context, ObjectManager manager, CompiledObjectDefinition definition)
    {
        loadPrivs(manager, definition);
    }

    public void vitualObjectCompiled(SystemContext context, ObjectManager manager, CompiledObjectDefinition definition, ObjectInstance prototype)
    {
        loadPrivs(manager, definition);
    }

    private void loadPrivs(ObjectManager manager, CompiledObjectDefinition definition)
    {
        ObjectInstance master = manager.getMasterObject();
        if (master == null)
        {
            return;
        }
        LpcValue privs = LOAD_PRIVS.invoke(master, new StringValue(definition.getName()));
        if (MiscSupport.isString(privs))
        {
            Privs.set(definition, privs.asString());
        }
    }

    public void objectCreated(SystemContext context, ObjectManager manager, CompiledObjectInstance object)
    {
        // Nothing to do
    }

    public void objectDestructed(SystemContext context, ObjectManager manager, ObjectInstance object)
    {
        // Nothing to do
    }

}
