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

package us.terebi.lang.lpc.compiler.bytecode;

import org.adjective.stout.operation.Expression;
import org.adjective.stout.operation.VM;

import us.terebi.lang.lpc.runtime.ClassDefinition;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.jvm.LpcObject;
import us.terebi.lang.lpc.runtime.jvm.LpcRuntimeSupport;

/**
 * 
 */
public class TypeCompiler
{
    /**
    *    @TODO Merge with {@link FieldCompiler#typeExpression(LpcType, us.terebi.lang.lpc.compiler.bytecode.FieldCompiler.EnclosingType)}
     */
    public static Expression getTypeExpression(LpcType type)
    {
        Expression typeExpression;
        if (type.getKind() == LpcType.Kind.CLASS)
        {
            typeExpression = VM.Expression.callStatic(//
                    LpcObject.class, VM.Method.find(LpcRuntimeSupport.class, "withType", ClassDefinition.class, Integer.TYPE), //
                    VM.Expression.callInherited(
                            //
                            VM.Method.find(LpcObject.class, "findClassDefinition", String.class),
                            VM.Expression.constant(type.getClassDefinition().getName())//
                    ), //
                    VM.Expression.constant(type.getArrayDepth()));
        }
        else
        {
            typeExpression = VM.Expression.callStatic(//
                    LpcObject.class, VM.Method.find(LpcRuntimeSupport.class, "withType", LpcType.Kind.class, Integer.TYPE), //
                    VM.Expression.getEnum(type.getKind()), //
                    VM.Expression.constant(type.getArrayDepth()) //
            );
        }
        return typeExpression;
    }

}
