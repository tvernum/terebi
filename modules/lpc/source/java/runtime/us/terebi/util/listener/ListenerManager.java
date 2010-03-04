/* ------------------------------------------------------------------------
 * Copyright 2010 Tim Vernum
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

package us.terebi.util.listener;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 */
public class ListenerManager<T> implements InvocationHandler
{
    private final Set<T> _listeners;
    private final T _proxy;

    public ListenerManager(Class<T> type)
    {
        _listeners = new HashSet<T>();
        _proxy = type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class[] { type }, this));
    }

    public T dispatch()
    {
        return _proxy;
    }

    public void addListener(T listener)
    {
        _listeners.add(listener);
    }

    public void removeListener(T listener)
    {
        _listeners.remove(listener);
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        for (T listener : _listeners)
        {
            method.invoke(listener, args);
        }
        return null;
    }
}
