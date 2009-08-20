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

package us.terebi.lang.lpc.runtime.jvm.context;

import java.util.Map;

import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.FunctionSignature;

/**
 * 
 */
public interface Efuns
{
    public Callable getImplementation(String efun);
    public FunctionSignature getSignature(String efun);

    public Map<String, ? extends FunctionSignature> getSignatures();
    public Map<String, ? extends Callable> getImplementations();
    
    public void define(String efun, FunctionSignature signature, Callable implementation);
    public <E extends FunctionSignature & Callable> void define(String name, E efun);

    public void delete(String efun);
}
