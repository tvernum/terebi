/* ------------------------------------------------------------------------
 * $Id$
 * Copyright 2008 Tim Vernum
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

package us.terebi.lang.lpc.runtime;

import java.util.List;


/**
 * @version $Revision$
 */
public interface MethodDefinition extends MemberDefinition
{
    public ObjectDefinition getDeclaringType();
    public FunctionSignature getSignature();
    
    public Callable getFunction(ObjectInstance instance); 
    public LpcValue execute(ObjectInstance instance, List<? extends LpcValue> arguments);
}
