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

/**
 * 
 */
public class ExecutionTimeCheck
{
    public static final int DEFAULT_EVAL_TIME_MILLIS = 500;

    private static final ThreadLocal<ExecutionTimeCheck> CHECK = new ThreadLocal<ExecutionTimeCheck>();

    private static long _defaultEvalTimeMilliSeconds = DEFAULT_EVAL_TIME_MILLIS;

    private long _maxTimeMillis;
    private long _startTime;
    private long _endTime;
    private int _count;

    public static void check()
    {
        ExecutionTimeCheck evalCheck = CHECK.get();
        if (evalCheck == null)
        {
            throw new IllegalStateException("No " + ExecutionTimeCheck.class.getSimpleName() + " associated with current thread");
        }
        if (System.currentTimeMillis() > evalCheck._endTime)
        {
            throw new ExecutionTimeTooHighException(evalCheck._startTime, evalCheck.getAllowedTime());
        }
    }

    public ExecutionTimeCheck(long maxTimeMilliSeconds)
    {
        _maxTimeMillis = maxTimeMilliSeconds;
        _count = 0;
    }

    public void begin()
    {
        if (_count == 0)
        {
            CHECK.set(this);
            _startTime = System.currentTimeMillis();
            reset();
        }
        _count++;
    }

    public void reset()
    {
        _endTime = System.currentTimeMillis() + _maxTimeMillis;
    }

    public void end()
    {
        _count--;
        if (_count == 0)
        {
            CHECK.set(null);
        }
    }

    public long getMaximumTime()
    {
        return _maxTimeMillis;
    }

    public static ExecutionTimeCheck get()
    {
        ExecutionTimeCheck eval = CHECK.get();
        if (eval == null)
        {
            eval = new ExecutionTimeCheck(_defaultEvalTimeMilliSeconds);
            CHECK.set(eval);
        }
        return eval;
    }

    public static void setDefaultEvalTime(long defaultEvalTime)
    {
        _defaultEvalTimeMilliSeconds = defaultEvalTime;
    }

    public static long getDefaultEvalTime()
    {
        return _defaultEvalTimeMilliSeconds;
    }

    public void setMaximumTime(long l)
    {
        _maxTimeMillis = l;
        _endTime = _startTime + l;
    }

    public long getRemainingTime()
    {
        return _endTime - System.currentTimeMillis();
    }

    public long getElapsedTime()
    {
        return System.currentTimeMillis() - _startTime;
    }

    public long getAllowedTime()
    {
        return _endTime - _startTime;
    }

}
