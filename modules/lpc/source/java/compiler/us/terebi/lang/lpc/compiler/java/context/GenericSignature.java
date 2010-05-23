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

package us.terebi.lang.lpc.compiler.java.context;

import java.util.Collections;
import java.util.List;

import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.ArgumentSemantics;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;

/**
 * 
 */
public class GenericSignature implements FunctionSignature
{
    public static final GenericSignature INSTANCE = new GenericSignature();

    private final List<ArgumentSpec> _arguments;

    public GenericSignature()
    {
        _arguments = Collections.singletonList(new ArgumentSpec("args", Types.MIXED_ARRAY, true, ArgumentSemantics.BY_VALUE));
    }

    public boolean hasUnstructuredArguments()
    {
        return true;
    }

    public boolean hasVarArgsArgument()
    {
        return true;
    }

    public List< ? extends ArgumentDefinition> getArguments()
    {
        return _arguments;
    }

    public LpcType getReturnType()
    {
        return Types.MIXED;
    }

    public boolean acceptsLessArguments()
    {
        return true;
    }

}
