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

package us.terebi.lang.lpc.compiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * 
 */
public class Output implements ObjectOutput
{
    private final String _package;
    private final String _class;
    private final File _file;

    public Output(String pkg, String cls, File file)
    {
        _package = pkg;
        _class = cls;
        _file = file;
    }

    public String getClassName()
    {
        return _class;
    }

    public String getPackageName()
    {
        return _package;
    }

    public OutputStream open() throws FileNotFoundException
    {
        return new FileOutputStream(_file);
    }

    public File getFile()
    {
        return _file;
    }

    public String toString()
    {
        return getClass().getSimpleName() + "{P=" + _package + ",C=" + _class + ",F=" + _file + "}";
    }
}
