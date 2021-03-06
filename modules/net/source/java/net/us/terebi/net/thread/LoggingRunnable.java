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

import org.apache.log4j.Logger;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public class LoggingRunnable implements Runnable
{
    private final Logger LOG = Logger.getLogger(LoggingRunnable.class);

    private final Runnable _delegate;

    public LoggingRunnable(Runnable delegate)
    {
        _delegate = delegate;
    }

    public void run()
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Starting " + _delegate);
        }
        try
        {
            _delegate.run();
        }
        finally
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Finished " + _delegate);
            }
        }
    }

}
