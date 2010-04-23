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
public interface Callable
{
    public enum Kind
    {
        METHOD, FUNCTION, EFUN, SIMUL_EFUN;

        public String description()
        {
            return name().toLowerCase().replace('_', '-');
        }
    }

    public Kind getKind();
    
    public FunctionSignature getSignature();
    public ObjectInstance getOwner();
    
    /** @return The name of the callable, or null if it is unnamed (e.g. a function pointer) */
    public CharSequence getName();
    

    public LpcValue execute(List< ? extends LpcValue> arguments);
    public LpcValue execute(LpcValue... arguments);
}
