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

package us.terebi.lang.lpc.compiler.classloader;

import java.net.URL;
import java.net.URLClassLoader;

import org.apache.log4j.Logger;

import us.terebi.lang.lpc.compiler.ClassNameMapper;
import us.terebi.lang.lpc.compiler.CompileException;
import us.terebi.lang.lpc.compiler.ObjectCompiler;

/**
 * 
 */
public class AutoCompilingClassLoader extends URLClassLoader
{
    private final Logger LOG = Logger.getLogger(AutoCompilingClassLoader.class);

    private final ObjectCompiler _compiler;

    public AutoCompilingClassLoader(URL[] urls, ClassLoader parent, ObjectCompiler compiler)
    {
        super(urls, parent);
        _compiler = compiler;
    }

    public Class< ? > findClass(String name) throws ClassNotFoundException
    {
        compile(name);
        return super.findClass(name);
    }

    private void compile(String name)
    {
        String lpc = ClassNameMapper.getLpcName(name);
        if (lpc == null)
        {
            return;
        }
        try
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Attempt to compile " + lpc);
            }
            _compiler.precompile(lpc);
        }
        catch (CompileException e)
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Cannot compile " + lpc + "for " + name + " - " + e);
            }
        }
    }
}
