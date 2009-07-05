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

package us.terebi.lang.lpc.runtime.util;

import java.util.Arrays;
import java.util.List;

import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.MethodDefinition;
import us.terebi.lang.lpc.runtime.ObjectInstance;

/**
 * 
 */
public class BoundMethod implements Callable
{
    private final MethodDefinition _method;
    private final ObjectInstance _instance;

    public BoundMethod(MethodDefinition method, ObjectInstance instance)
    {
        _method = method;
        _instance = instance;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        return _method.execute(_instance, arguments);
    }

    public LpcValue execute(LpcValue... arguments)
    {
        return _method.execute(_instance, Arrays.asList(arguments));
    }

    public Kind getKind()
    {
        return Kind.METHOD;
    }
    
    public ObjectInstance getOwner()
    {
        return _instance;
    }

    public FunctionSignature getSignature()
    {
        return _method.getSignature();
    }

}
