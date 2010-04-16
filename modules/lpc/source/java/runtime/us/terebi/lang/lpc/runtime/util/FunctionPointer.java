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

package us.terebi.lang.lpc.runtime.util;

import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.context.CallStack.Origin;

/**
 * 
 */
public class FunctionPointer extends StackCall
{
    private final ObjectInstance _owner;

    public FunctionPointer(Callable delegate, ObjectInstance owner)
    {
        super(delegate, Origin.POINTER);
        _owner = owner;
    }

    public ObjectInstance getOwner()
    {
        return _owner;
    }
}
