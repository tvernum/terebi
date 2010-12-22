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

package us.terebi.plugins.privs.efun;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.LpcConstants;
import us.terebi.lang.lpc.runtime.jvm.efun.AbstractEfun;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.NilValue;
import us.terebi.lang.lpc.runtime.jvm.value.StringValue;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;
import us.terebi.plugins.privs.Privs;

/**
 * 
 */
public class QueryPrivsEfun extends AbstractEfun implements FunctionSignature, Callable
{
    private final Logger LOG = Logger.getLogger(QueryPrivsEfun.class);

    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        return Collections.singletonList(new ArgumentSpec("ob", Types.OBJECT));
    }

    public LpcType getReturnType()
    {
        return Types.STRING;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        checkArguments(arguments);
        ObjectInstance obj = arguments.get(0).asObject();
        String privs = Privs.get(obj);
        if (privs == null)
        {
            LOG.debug("No privs for " + obj);
            return NilValue.INSTANCE;
        }
        if (privs.length() == 0)
        {
            LOG.debug("Blank privs for " + obj);
            return LpcConstants.STRING.BLANK;
        }
        LOG.debug("Privs for " + obj + " = " + privs);
        return new StringValue(privs);
    }

}
