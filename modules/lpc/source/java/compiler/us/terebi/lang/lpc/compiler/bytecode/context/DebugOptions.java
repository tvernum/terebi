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

package us.terebi.lang.lpc.compiler.bytecode.context;

import java.util.Collections;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import org.adjective.stout.core.ClassDescriptor;
import org.adjective.stout.core.MethodDescriptor;

/**
 * 
 */
public class DebugOptions
{
    private final Logger LOG = Logger.getLogger(DebugOptions.class);

    private final Iterable<Pattern> _patterns;

    public DebugOptions(Iterable<Pattern> patterns)
    {
        _patterns = (patterns == null ? Collections.<Pattern> emptyList() : patterns);
    }

    public boolean isDebugEnabled(ClassDescriptor cls, MethodDescriptor method)
    {
        String spec = cls.getInternalName().substring(3) + ":" + method.getName();
        for (Pattern pattern : _patterns)
        {
            if (pattern.matcher(spec).matches())
            {
                LOG.info("Enabling debug for " + cls + " " + method + " [pattern:" + pattern + "]");
                return true;
            }
        }
        return false;
    }
}
