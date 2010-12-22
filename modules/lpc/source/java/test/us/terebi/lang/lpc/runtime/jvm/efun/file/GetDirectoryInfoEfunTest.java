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

package us.terebi.lang.lpc.runtime.jvm.efun.file;

import java.io.File;
import java.util.List;

import org.junit.Test;

import us.terebi.lang.lpc.io.FileResource;
import us.terebi.lang.lpc.io.Resource;
import us.terebi.lang.lpc.io.ResourceFinder;
import us.terebi.lang.lpc.runtime.LpcValue;

import static org.junit.Assert.assertEquals;

/**
 * 
 */
public class GetDirectoryInfoEfunTest
{
    @Test
    public void testWildCard_StarDotC() throws Exception
    {
        final Resource resource = new FileResource(new File("source/lpc/test/" + getClass().getName().replace('.', '/')));
        ResourceFinder resourceFinder = new ResourceFinder()
        {
            public Resource getResource(String path) 
            {
                if (path.equals("/foo/bar"))
                {
                    return resource;
                }
                else
                {
                    throw new IllegalArgumentException("Unexpected path: " + path);
                }
            }
        };
        List<LpcValue> info = GetDirectoryInfoEfun.getDirectoryInfo(resourceFinder, "/foo/bar/*.c", 0);
        assertEquals(3, info.size());
    }
}
