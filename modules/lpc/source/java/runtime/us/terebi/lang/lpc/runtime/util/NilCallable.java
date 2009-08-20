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

package us.terebi.lang.lpc.runtime.util;

import java.util.Collections;
import java.util.List;

import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.NilValue;

/**
 * 
 */
public class NilCallable implements Callable
{
    private final Kind _kind;
    private final ObjectInstance _owner;
    private final FunctionSignature _signature;

    public NilCallable(ObjectInstance instance, Kind kind)
    {
        this(instance, kind, new Signature(true, Types.MIXED, Collections.<ArgumentDefinition> emptyList()));
    }

    public NilCallable(ObjectInstance owner, Kind kind, FunctionSignature signature)
    {
        _owner = owner;
        _kind = kind;
        _signature = signature;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        return NilValue.INSTANCE;
    }

    public LpcValue execute(LpcValue... arguments)
    {
        return NilValue.INSTANCE;
    }

    public Kind getKind()
    {
        return _kind;
    }

    public ObjectInstance getOwner()
    {
        return _owner;
    }

    public FunctionSignature getSignature()
    {
        return _signature;
    }

}
