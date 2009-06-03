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

package us.terebi.lang.lpc.runtime.jvm;


/**
 * 
 */
public class InheritedObject<T>
{
    private final Class< ? extends T> _type;
    private T _instance;

    public InheritedObject(Class< ? extends T> type) throws InstantiationException, IllegalAccessException
    {
        _type = type;
        _instance = _type.newInstance();
    }

    public T get()
    {
        return _instance;
    }

}
