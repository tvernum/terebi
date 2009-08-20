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

package us.terebi.net.core.impl;

import java.util.HashSet;
import java.util.Set;

import us.terebi.net.core.Component;
import us.terebi.net.core.NetException;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public abstract class AbstractComponent<C extends Component> implements Component
{
    private State _state;
    private Set<C> _children;
    private Component _parent;

    public AbstractComponent()
    {
        _state = State.NEW;
        _children = new HashSet<C>();
        _parent = null;
    }

    protected Set<C> getChildren()
    {
        return _children;
    }

    protected void addChild(C child)
    {
        if (child != null)
        {
            _children.add(child);
            child.attachedToParent(this);
        }
    }

    protected void removeChild(C child)
    {
        if (child != null)
        {
            _children.remove(child);
            child.attachedToParent(null);
        }
    }

    public void attachedToParent(Component parent)
    {
        _parent = parent;
    }

    public Component getParent()
    {
        return _parent;
    }

    protected <A extends Component> A findAncestor(Class< ? extends A> type)
    {
        return findAncestor(type, this);
    }

    protected <A extends Component> A findAncestor(Class< ? extends A> type, Component start)
    {
        Component p = start;
        while (p != null)
        {
            if (type.isInstance(p))
            {
                return type.cast(p);
            }
            p = p.getParent();
        }
        return null;
    }

    public void init() throws NetException
    {
        _state = State.INITIALISING;
        preInit();
        for (Component child : _children)
        {
            child.init();
        }
        postInit();
        _state = State.INITIALISED;
    }

    @SuppressWarnings("unused")
    protected void preInit() throws NetException
    {
        // Hook for derived classes to override
    }

    @SuppressWarnings("unused")
    protected void postInit() throws NetException
    {
        // Hook for derived classes to override
    }

    public void begin() throws NetException
    {
        _state = State.STARTING;
        preBegin();
        for (Component child : _children)
        {
            child.begin();
        }
        postBegin();
        _state = State.RUNNING;
    }

    @SuppressWarnings("unused")
    protected void preBegin() throws NetException
    {
        // Hook for derived classes to override
    }

    @SuppressWarnings("unused")
    protected void postBegin() throws NetException
    {
        // Hook for derived classes to override
    }

    public void suspend() throws NetException
    {
        _state = State.SUSPENDING;
        for (Component child : _children)
        {
            child.suspend();
        }
        _state = State.SUSPENDED;
    }

    public void resume() throws NetException
    {
        _state = State.RESUMING;
        for (Component child : _children)
        {
            child.resume();
        }
        _state = State.RUNNING;
    }

    public void destroy() throws NetException
    {
        _state = State.STOPPING;
        for (Component child : _children)
        {
            child.destroy();
        }
        _state = State.DESTROYED;
    }

    public State getState()
    {
        return _state;
    }

}
