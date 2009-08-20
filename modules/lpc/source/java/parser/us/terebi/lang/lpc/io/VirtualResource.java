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

package us.terebi.lang.lpc.io;

import java.io.IOException;

import us.terebi.lang.lpc.preprocessor.Source;
import us.terebi.lang.lpc.preprocessor.VirtualFile;

/**
 * 
 */
public class VirtualResource implements VirtualFile
{
    private final Resource _resource;

    public VirtualResource(Resource resource)
    {
        _resource = resource;
    }

    public VirtualFile getChildFile(String name)
    {
        return new VirtualResource(_resource.getChild(name));
    }

    public String getName()
    {
        return _resource.getName();
    }

    public VirtualFile getParentFile()
    {
        return new VirtualResource(_resource.getParent());
    }

    public String getPath()
    {
        return _resource.getPath();
    }

    public Source getSource() throws IOException
    {
        return new ResourceLexerSource(_resource);
    }

    public boolean isFile()
    {
        return _resource.isFile();
    }
    
    public String toString()
    {
        return _resource.toString();
    }

}
