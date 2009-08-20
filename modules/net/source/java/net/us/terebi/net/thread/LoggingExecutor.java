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

package us.terebi.net.thread;

import java.util.concurrent.Executor;

import org.apache.log4j.Logger;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public class LoggingExecutor implements Executor
{
    private final Logger LOG = Logger.getLogger(LoggingExecutor.class);

    private final Executor _delegate;

    public LoggingExecutor(Executor delegate)
    {
        _delegate = delegate;
    }

    public void execute(Runnable command)
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Adding " + command + " to run queue " + _delegate);
        }
        _delegate.execute(new LoggingRunnable(command));
    }
}
