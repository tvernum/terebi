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

package us.terebi.lang.lpc.runtime.jvm.support;

import java.text.SimpleDateFormat;
import java.util.Date;

import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;

/**
 * 
 */
public class ExecutionTimeTooHighException extends LpcRuntimeException
{
    public ExecutionTimeTooHighException(long start, long allowed)
    {
        super(getMessage(start, allowed));
    }

    private static String getMessage(long start, long allowed)
    {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss.SSS");
        return "Execution time too long [started="
                + format.format(new Date(start))
                + ";allowed="
                + allowed
                + "ms;now="
                + format.format(new Date())
                + ";elapsed="
                + (System.currentTimeMillis() - start)
                + "ms]";
    }

}
