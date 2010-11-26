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

package us.terebi.lang.lpc.runtime.jvm.efun;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;
import us.terebi.lang.lpc.runtime.util.reflect.ObjectIntrospector;

/**
 * 
 */
public class FetchVariableEfun extends AbstractEfun implements Efun
{
    private final Logger LOG = Logger.getLogger(FetchVariableEfun.class);
    
    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        return Collections.singletonList(new ArgumentSpec("name", Types.STRING));
    }

    public LpcType getReturnType()
    {
        return Types.MIXED;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        checkArguments(arguments);

        ObjectInstance thisObject = ThisObjectEfun.this_object();
        String name = arguments.get(0).asString();

        LpcValue value = new ObjectIntrospector(thisObject).getField(name);
        LOG.debug("Field " + thisObject  + "->" + name + " = " + value);
        return value;
    }

}
