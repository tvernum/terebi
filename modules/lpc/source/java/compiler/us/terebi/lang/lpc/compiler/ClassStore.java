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

package us.terebi.lang.lpc.compiler;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 
 */
public interface ClassStore
{
    /**
     * Opens a class to be written to
     */
    public OutputStream open(ClassName name) throws IOException;

    /**
     * @returns The {@link System#currentTimeMillis() timestamp} that the class was last written to, or <code>0</code> if the class does not exist
     */
    public long getLastModified(ClassName className);
    
    /**
     * @return All "user" (lpc) classes that are directly used by '<code>className</code>', or null if '<code>className</code>' does not exist.
     */
    public Iterable<ClassName> getDependencies(ClassName className);
}
