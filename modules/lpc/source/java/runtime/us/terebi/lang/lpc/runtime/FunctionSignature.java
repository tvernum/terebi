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
 * 
 */
public interface FunctionSignature
{
    public LpcType getReturnType();

    /**
     * Used primarily in function literals, this indicates that the return value of {@link #getArguments()} should, in general, be ignored,
     * and all provided argument values should be passed directly to the {@link Callable#execute(LpcValue...) execute method} 
     * without being restructured, or checked for type compatability
     */
    public boolean hasUnstructuredArguments();

    public List< ? extends ArgumentDefinition> getArguments();

    /** Corresponds to the <code>varargs</code> keyword in LPC.
     * Indicates that this fuction can be called with less than the formal number of arguments
     */
    public boolean acceptsLessArguments();

    public boolean hasVarArgsArgument();

}
