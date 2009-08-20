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

package us.terebi.lang.lpc.runtime.jvm.efun;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectDefinition;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.ArrayValue;
import us.terebi.lang.lpc.runtime.jvm.value.StringValue;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;

/**
 * 
 */
public class InheritListEfun extends AbstractEfun
{
    private final boolean _deep;

    public InheritListEfun(boolean deep)
    {
        _deep = deep;
    }

    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        return Collections.singletonList(new ArgumentSpec("obj", Types.OBJECT));
    }

    public LpcType getReturnType()
    {
        return Types.STRING_ARRAY;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        checkArguments(arguments);
        ObjectInstance obj = arguments.get(0).asObject();
        ObjectDefinition definition = obj.getDefinition();
        List<LpcValue> inherit = null;
        if (_deep)
        {
            inherit = deepInheritList(definition);
        }
        else
        {
            inherit = shallowInheritList(definition);
        }
        return new ArrayValue(Types.STRING_ARRAY, inherit);
    }

    private List<LpcValue> deepInheritList(ObjectDefinition definition)
    {
        List<LpcValue> list = new ArrayList<LpcValue>();
        deepInheritList(definition, list);
        return list;
    }

    private void deepInheritList(ObjectDefinition definition, List<LpcValue> list)
    {
        Collection< ? extends ObjectDefinition> inherited = definition.getInheritedObjects().values();
        for (ObjectDefinition object : inherited)
        {
            list.add(new StringValue(object.getName()));
            deepInheritList(object, list);
        }
    }

    private List<LpcValue> shallowInheritList(ObjectDefinition definition)
    {
        Collection< ? extends ObjectDefinition> inherited = definition.getInheritedObjects().values();
        List<LpcValue> list = new ArrayList<LpcValue>(inherited.size());
        for (ObjectDefinition object : inherited)
        {
            list.add(new StringValue(object.getName()));
        }
        return list;
    }

}
