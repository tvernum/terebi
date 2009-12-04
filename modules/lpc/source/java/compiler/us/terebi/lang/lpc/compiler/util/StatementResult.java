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

package us.terebi.lang.lpc.compiler.util;


public class StatementResult
{
    public static final StatementResult NON_TERMINAL = new StatementResult(TerminationType.NOT_TERMINATED);

    public enum TerminationType
    {
        NOT_TERMINATED, CONTINUE, BREAK, RETURN
    }

    public final StatementResult.TerminationType termination;

    public StatementResult(StatementResult.TerminationType term)
    {
        this.termination = term;
    }

    public boolean isTerminated()
    {
        return this.termination != TerminationType.NOT_TERMINATED;
    }

    public String toString()
    {
        return getClass().getSimpleName() + ":" + this.termination.name();
    }
}
