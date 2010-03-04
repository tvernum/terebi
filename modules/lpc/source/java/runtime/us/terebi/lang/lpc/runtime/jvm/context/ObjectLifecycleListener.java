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

package us.terebi.lang.lpc.runtime.jvm.context;

import us.terebi.lang.lpc.compiler.java.context.CompiledObjectDefinition;
import us.terebi.lang.lpc.compiler.java.context.CompiledObjectInstance;
import us.terebi.lang.lpc.runtime.ObjectInstance;

/**
 * 
 */
public interface ObjectLifecycleListener
{
    public void objectCompiled(SystemContext context, ObjectManager manager, CompiledObjectDefinition definition);
    public void vitualObjectCompiled(SystemContext context, ObjectManager manager, CompiledObjectDefinition definition, ObjectInstance prototype);
    public void objectCreated(SystemContext context, ObjectManager manager, CompiledObjectInstance object);
    public void objectDestructed(SystemContext context, ObjectManager manager, ObjectInstance object);
}
