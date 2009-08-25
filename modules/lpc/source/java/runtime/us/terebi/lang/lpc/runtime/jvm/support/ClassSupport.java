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

package us.terebi.lang.lpc.runtime.jvm.support;

import static us.terebi.lang.lpc.runtime.jvm.support.MiscSupport.isClass;

import us.terebi.lang.lpc.runtime.ClassInstance;
import us.terebi.lang.lpc.runtime.FieldDefinition;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.LpcReference;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;
import us.terebi.lang.lpc.runtime.jvm.value.NilValue;
import us.terebi.lang.lpc.runtime.jvm.value.ReferenceValue;

/**
 * 
 */
public class ClassSupport
{
    public static LpcReference getField(LpcValue value, String fieldName)
    {
        if (isClass(value))
        {
            ClassInstance cls = value.asClass();
            FieldDefinition field = cls.getDefinition().getFields().get(fieldName);
            if (field == null)
            {
                // @TODO What is the right sematics here ?
                return new ReferenceValue(NilValue.INSTANCE);
            }
            return field.getReference(cls);
        }
        else
        {
            throw new LpcRuntimeException("LHS of field reference (" + value + " -> " + fieldName + ") is not a class");
        }
    }

}
