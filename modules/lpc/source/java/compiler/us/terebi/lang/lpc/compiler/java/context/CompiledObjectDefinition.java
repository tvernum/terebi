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

package us.terebi.lang.lpc.compiler.java.context;

import java.util.List;
import java.util.Map;

import us.terebi.lang.lpc.runtime.CompiledMethodDefinition;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectDefinition;
import us.terebi.lang.lpc.runtime.ObjectInstance;

/**
 * 
 */
public interface CompiledObjectDefinition extends ObjectDefinition, ObjectInstance.DestructListener
{
    public Class<? extends CompiledImplementation> getImplementationClass(); 
    public CompiledObjectInstance getMasterInstance();
    public CompiledObjectInstance newInstance(List<? extends LpcValue> arguments);
    public CompiledObjectInstance getInheritableInstance(ObjectInstance forInstance);
    public CompiledObjectInstance getPrototypeInstance(ObjectInstance forInstance);
    public Map<String, ? extends CompiledMethodDefinition> getMethods();
    public String getBaseName();
}
