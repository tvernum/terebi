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

import us.terebi.lang.lpc.runtime.jvm.LpcReference;



/**
 * @version $Revision$
 */
public interface FieldDefinition extends MemberDefinition
{
    public LpcType getType();
    
    public LpcReference getReference(UserTypeInstance instance);
    public LpcValue getValue(UserTypeInstance instance);
    public void setValue(UserTypeInstance instance, LpcValue value);
    public void initialise(UserTypeInstance instance);
}
