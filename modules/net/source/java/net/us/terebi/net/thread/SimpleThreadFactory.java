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

import java.util.concurrent.ThreadFactory;

import org.apache.log4j.Logger;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public class SimpleThreadFactory implements ThreadFactory
{
    private final Logger LOG = Logger.getLogger(SimpleThreadFactory.class);

    private String _prefix;
    private int _id;
    private int _priority;
    private boolean _daemon;

    public SimpleThreadFactory()
    {
        _prefix = getClass().getSimpleName() + "(" + System.identityHashCode(this) + ")";
        _id = 0;
        _priority = Thread.NORM_PRIORITY;
        _daemon = false;
    }

    public void setPrefix(String prefix)
    {
        _prefix = prefix;
    }

    public void setPriority(int priority)
    {
        _priority = priority;
    }

    public Thread newThread(Runnable r)
    {
        String name = _prefix + '-' + nextId();
        Thread thread = new Thread(r, name);
        thread.setPriority(_priority);
        thread.setDaemon(_daemon);
        if (LOG.isDebugEnabled())
        {
            LOG.info("New thread " + thread);
        }
        return thread;
    }

    private synchronized int nextId()
    {
        return ++_id;
    }

    public void setDaemon(boolean daemon)
    {
        _daemon = daemon;
    }

}
