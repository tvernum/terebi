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
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 
 */
public interface Resource
{
    public Resource getParent();
    public Resource getChild(String name);
    public Resource[] getChildren();

    public String getName();
    public String getPath();
    public String getParentName();
    public boolean isFile();
    public boolean isDirectory();
    public boolean exists();
    public long getSizeInBytes();
    public InputStream read() throws IOException;
    public OutputStream write() throws IOException;
    public OutputStream append() throws IOException;
    public void mkdir() throws IOException;
    public void delete() throws IOException;
    public void rename(Resource to) throws IOException;
    
    public boolean newerThan(long mod);
    public long lastModified();
}
