/* ------------------------------------------------------------------------
 * Copyright 2009,2010 Tim Vernum
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

package us.terebi.lang.lpc.runtime.jvm.efun.file;

import java.io.IOException;

import us.terebi.lang.lpc.io.Resource;
import us.terebi.lang.lpc.io.ResourceFinder;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.context.RuntimeContext;
import us.terebi.lang.lpc.runtime.jvm.context.SystemContext;
import us.terebi.lang.lpc.runtime.jvm.value.StringValue;

/**
 * 
 */
public class GameIO
{
    public static final GameIO INSTANCE = new GameIO();

    public Resource getResource(String name, ObjectInstance object, StringValue efun) throws IOException
    {
        SystemContext system = RuntimeContext.obtain().system();
        ResourceFinder resourceFinder = system.resourceFinder();
        Resource resource = resourceFinder.getResource(name);
        return new GameResource(resource, object, efun);
    }
}
