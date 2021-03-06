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

package us.terebi.lang.lpc.runtime.util;

import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.util.Range;

/**
 * 
 */
public class FunctionUtil
{
    public static final int MAX_ARGS = 32;

    public static Range<Integer> getAllowedNumberOfArgument(FunctionSignature signature)
    {
        int count = 0;
        boolean noMax = false;
        for (ArgumentDefinition arg : signature.getArguments())
        {
            if (arg.isVarArgs())
            {
                noMax = true;
            }
            else
            {
                count++;
            }
        }
        int max = count, min = count;
        if (noMax)
        {
            max = MAX_ARGS;
        }
        if(signature.acceptsLessArguments()) {
            min = 0;
        }
            return new Range<Integer>(min, max);
    }

}
