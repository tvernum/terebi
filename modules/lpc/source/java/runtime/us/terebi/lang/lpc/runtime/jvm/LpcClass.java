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

package us.terebi.lang.lpc.runtime.jvm;

import java.lang.reflect.Field;

import us.terebi.lang.lpc.runtime.ClassDefinition;
import us.terebi.lang.lpc.runtime.ObjectDefinition;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;
import us.terebi.lang.lpc.runtime.jvm.object.CompiledField;
import us.terebi.lang.lpc.runtime.util.type.DynamicClassDefinition;

/**
 * 
 */
public class LpcClass
{
    private final ObjectDefinition _declaring;
    private ClassDefinition _definition;

    public LpcClass(ObjectDefinition declaring)
    {
        _declaring = declaring;
        _definition = null;
    }

    public ClassDefinition getClassDefinition()
    {
        synchronized (this)
        {
            if (_definition == null)
            {
                _definition = loadDefinition();
            }
        }
        return _definition;
    }

    private ClassDefinition loadDefinition()
    {
        Class< ? extends LpcClass> cls = getClass();
        ObjectDefinition declaring = _declaring;
        return getClassDefinition(cls, declaring);
    }

    public static ClassDefinition getClassDefinition(Class< ? extends LpcClass> cls, ObjectDefinition declaring)
    {
        // @TODO Cache these by class (weak hash map ?)
        LpcMember annotation = cls.getAnnotation(LpcMember.class);
        if (annotation == null)
        {
            throw new LpcRuntimeException("Class object " + cls + " is not annotated with " + LpcMember.class.getSimpleName());
        }
        DynamicClassDefinition definition = new DynamicClassDefinition(annotation.name(), annotation.modifiers(), declaring);

        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields)
        {
            if (LpcField.class.isAssignableFrom(field.getType()))
            {
                definition.addField(new CompiledField(definition, field));
            }
        }
        return definition;
    }
}
