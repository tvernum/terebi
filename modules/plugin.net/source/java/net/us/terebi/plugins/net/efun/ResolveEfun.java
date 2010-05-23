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

package us.terebi.plugins.net.efun;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.LpcConstants;
import us.terebi.lang.lpc.runtime.jvm.context.RuntimeContext;
import us.terebi.lang.lpc.runtime.jvm.context.SystemContext;
import us.terebi.lang.lpc.runtime.jvm.context.CallStack.Origin;
import us.terebi.lang.lpc.runtime.jvm.efun.AbstractEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.Efun;
import us.terebi.lang.lpc.runtime.jvm.support.ValueSupport;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.StringValue;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;
import us.terebi.lang.lpc.runtime.util.InContext;
import us.terebi.lang.lpc.runtime.util.InContext.Exec;
import us.terebi.plugins.net.NetworkResolver;
import us.terebi.plugins.net.NetworkResolver.Token;

/**
 */
public class ResolveEfun extends AbstractEfun implements Efun
{
    static final Logger LOG = Logger.getLogger(ResolveEfun.class);

    public static class Handler implements NetworkResolver.Callback
    {
        private final Callable _callable;
        private final SystemContext _context;

        public Handler(Callable callable, SystemContext context)
        {
            _callable = callable;
            _context = context;
        }

        public void addressNotResolved(String address, Token token)
        {
            execute(address, null, token);
        }

        public void addressResolved(String ip, String hostName, Token token)
        {
            execute(hostName, ip, token);
        }

        private void execute(String address, String ip, Token token)
        {
            final LpcValue ipValue = ip == null ? LpcConstants.NIL : new StringValue(ip);
            final StringValue addressValue = new StringValue(address);
            final LpcValue tokenValue = ValueSupport.intValue(token.id);
            final Callable callable = _callable;

            RuntimeContext.activate(_context);
            InContext.execute(Origin.APPLY, callable.getOwner(), new Exec<LpcValue>()
            {
                public LpcValue execute()
                {
                    LOG.info("Callback " + callable + " - " + addressValue + " , " + ipValue + " , " + tokenValue);
                    return callable.execute(addressValue, ipValue, tokenValue);
                }
            });
        }
    }

    public List< ? extends ArgumentDefinition> defineArguments()
    {
        return Arrays.asList( //
                new ArgumentSpec("address", Types.MIXED), //
                new ArgumentSpec("callback", Types.MIXED));
    }

    public boolean acceptsLessArguments()
    {
        return false;
    }

    public LpcType getReturnType()
    {
        return Types.INT;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        checkArguments(arguments);
        String address = arguments.get(0).asString();
        Callable callable = getFunctionReference(arguments.get(1));

        NetworkResolver resolver = NetworkResolver.getResolver();
        Token token = resolver.resolve(address, new Handler(callable, RuntimeContext.obtain().system()));
        return ValueSupport.intValue(token.id);
    }

}
