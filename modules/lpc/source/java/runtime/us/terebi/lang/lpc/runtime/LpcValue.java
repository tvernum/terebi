/* ------------------------------------------------------------------------
 * $Id$
 * Copyright 2008 Tim Vernum
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

package us.terebi.lang.lpc.runtime;

import java.util.List;
import java.util.Map;

/**
 * @version $Revision$
 */
public interface LpcValue
{
    public LpcType getActualType();

    public long asLong();
    public double asDouble();
    public String asString();
    public ByteSequence asBuffer();
    public ObjectInstance asObject();
    public Callable asCallable();
    public List<LpcValue> asList();
    public Map<LpcValue, LpcValue> asMap();
    public ClassInstance asClass();
    public boolean asBoolean();

    public <T extends ExtensionValue> T asExtension(Class<? extends T> type);

    public CharSequence debugInfo(); 
}
