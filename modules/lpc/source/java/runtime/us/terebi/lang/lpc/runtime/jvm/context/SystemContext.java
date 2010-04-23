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

package us.terebi.lang.lpc.runtime.jvm.context;

import us.terebi.lang.lpc.io.ResourceFinder;

/**
 * 
 */
public class SystemContext
{
    private final Efuns _efuns;
    private final ObjectManager _objectManager;
    private final ResourceFinder _resourceFinder;
    private final Object _lock;
    private final CallOutManager _callout;
    private final ObjectMap _attachments;

    public SystemContext(Efuns efuns, ObjectManager objectManager, ResourceFinder resourceFinder)
    {
        _efuns = efuns;
        _objectManager = objectManager;
        _resourceFinder = resourceFinder;
        _lock = new Object();
        _callout = new CallOutManager(this);
        _attachments = new ObjectMap();
        _attachments.put(StartTime.class, new StartTime());
    }

    public ObjectMap attachments()
    {
        return _attachments;
    }

    public Efuns efuns()
    {
        return _efuns;
    }

    public ObjectManager objectManager()
    {
        return _objectManager;
    }

    public Object lock()
    {
        return _lock;
    }

    public String toString()
    {
        return getClass().getSimpleName() + '@' + System.identityHashCode(this) + "{" + _objectManager + "}";
    }

    public ResourceFinder resourceFinder()
    {
        return _resourceFinder;
    }

    public CallOutManager callout()
    {
        return _callout;
    }

}
