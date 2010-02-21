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

package us.terebi.lang.lpc.runtime.jvm.value;

import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.type.Types;

/**
 * 
 */
public class FunctionValue extends AbstractValue implements LpcValue
{
    private final Callable _function;

    public FunctionValue(Callable function)
    {
        _function = function;
    }

    protected CharSequence getDescription()
    {
        return "function @" + _function.getOwner().getCanonicalName();
    }

    protected boolean valueEquals(LpcValue other)
    {
        return _function.equals(other.asCallable());
    }

    protected int valueHashCode()
    {
        return _function.hashCode();
    }

    public CharSequence debugInfo()
    {
        return getDescription();
    }

    public LpcType getActualType()
    {
        return Types.FUNCTION;
    }
    
    public Callable asCallable()
    {
        return _function;
    }

}
