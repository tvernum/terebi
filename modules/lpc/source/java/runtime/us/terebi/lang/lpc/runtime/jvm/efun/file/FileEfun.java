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

import java.io.IOException;

import us.terebi.lang.lpc.io.Resource;
import us.terebi.lang.lpc.runtime.jvm.efun.AbstractEfun;
import us.terebi.lang.lpc.runtime.jvm.efun.ThisObjectEfun;
import us.terebi.lang.lpc.runtime.jvm.value.StringValue;

/**
 * 
 */
public abstract class FileEfun extends AbstractEfun
{
    private final GameIO _io;
    private final StringValue _efunName;

    public FileEfun()
    {
        super();
        _io = GameIO.INSTANCE;
        _efunName = new StringValue(getName());
    }

    protected Resource getResource(String name) throws IOException
    {
        return _io.getResource(name, ThisObjectEfun.this_object(), getEfunNameAsLpcValue());
    }

    private StringValue getEfunNameAsLpcValue()
    {
        return _efunName;
    }

}
