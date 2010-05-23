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

package us.terebi.engine.plugin;

import java.util.Properties;

import us.terebi.engine.config.Config;
import us.terebi.lang.lpc.runtime.jvm.context.SystemContext;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public abstract class AbstractPlugin implements Plugin
{
    public AbstractPlugin()
    {
        super();
    }

    public void load(Config config, SystemContext context, Properties properties)
    {
        // No-op
    }

    public void init(SystemContext context)
    {
        // No-op
    }

    public void epilog(SystemContext context)
    {
        // No-op
    }

    public void start(SystemContext context)
    {
        // No-op
    }

    public void run(SystemContext context)
    {
        // No-op
    }


}
