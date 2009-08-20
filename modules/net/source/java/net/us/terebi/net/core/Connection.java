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

package us.terebi.net.core;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public interface Connection extends Closeable
{
    public OutputStream getOutputStream();
    public PrintWriter getWriter();
    public ConnectionInfo getInfo();

    public FeatureSet getFeatures();
    public AttributeSet getAttributes();

    public void bind(Shell shell);
    public Shell boundTo();
    public void close() throws IOException;
}
