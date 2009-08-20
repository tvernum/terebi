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

package us.terebi.lang.lpc.runtime.jvm.context;

import us.terebi.lang.lpc.runtime.AttributeMap;
import us.terebi.lang.lpc.runtime.util.Attributes;

/**
 * 
 */
public class ThreadContext
{
    private final CallStack _callStack;
    private final AttributeMap _attributes;
    private final SystemContext _system;

    public ThreadContext(SystemContext system)
    {
        _system = system;
        _callStack = new CallStack();
        _attributes = new Attributes();
    }

    public SystemContext system()
    {
        return _system;
    }

    public CallStack callStack()
    {
        return _callStack;
    }

    /**
     * Attributes are stored for the life of the execution thread only.
     * They are reset at the begining of each call stack.
     */
    public AttributeMap attributes()
    {
        return _attributes;
    }

    public void begin()
    {
        _callStack.begin();
        _attributes.clear();
    }
}
