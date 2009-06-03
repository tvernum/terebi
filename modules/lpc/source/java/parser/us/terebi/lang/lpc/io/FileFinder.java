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

import java.io.File;

public final class FileFinder implements ResourceFinder
{
    private final File _rootDirectory;

    public FileFinder(File rootDirectory)
    {
        _rootDirectory = rootDirectory;
    }

    public Resource getResource(String path)
    {
        File file = findFile(path);
        return new FileResource(file, path);
    }

    private File findFile(String path)
    {
        File file = new File(_rootDirectory, path);
        return file;
    }
}
