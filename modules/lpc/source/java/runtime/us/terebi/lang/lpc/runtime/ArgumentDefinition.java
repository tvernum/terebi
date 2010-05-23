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

/**
 * 
 */
public interface ArgumentDefinition
{
    /**
     * @return The name of the argument. This name may be invented if the argument was dynamically generated (such as in a function literal)
     */
    public String getName();

    /**
     * @return The type of the argument (if the argument type is unknown, then <code>mixed</code> will be returned). 
     * For {@link #isVarArgs() varargs} arguments, the type will be the array type, rather than the element
     *  - thus it will reflect the java runtime view of the function argument (i.e. What type needs to be passed in to the {@link Callable}) and the LPC declaration view 
     * (i.e. <code><b>string array</b> args...</code>), but does not reflect the way that the method/function is invoked in LPC.
     */
    public LpcType getType();

    /**
     * @return How this argument should be treated semanticly
     */
    public ArgumentSemantics getSemantics();

    /**
     * @return Is this argument 'varargs' ? (e.g. It was declared in LPC as <code>string array args <b>...</b></code>)
     * @see #getType()
     */
    public boolean isVarArgs();
}
