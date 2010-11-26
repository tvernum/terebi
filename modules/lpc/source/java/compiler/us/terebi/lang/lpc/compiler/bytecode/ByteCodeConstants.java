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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.adjective.stout.core.ConstructorSignature;
import org.adjective.stout.core.ExtendedType;
import org.adjective.stout.core.MethodSignature;
import org.adjective.stout.core.ParameterisedClass;
import org.adjective.stout.impl.ParameterisedClassImpl;
import org.adjective.stout.operation.Expression;
import org.adjective.stout.operation.VM;

import us.terebi.lang.lpc.compiler.java.context.CompiledInstance;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.UserTypeDefinition;
import us.terebi.lang.lpc.runtime.jvm.InheritedObject;
import us.terebi.lang.lpc.runtime.jvm.LpcClass;
import us.terebi.lang.lpc.runtime.jvm.LpcField;
import us.terebi.lang.lpc.runtime.jvm.LpcFunction;
import us.terebi.lang.lpc.runtime.jvm.LpcObject;
import us.terebi.lang.lpc.runtime.jvm.LpcReference;
import us.terebi.lang.lpc.runtime.jvm.LpcRuntimeSupport;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;
import us.terebi.lang.lpc.runtime.jvm.support.ClassSupport;
import us.terebi.lang.lpc.runtime.jvm.support.ComparisonSupport;
import us.terebi.lang.lpc.runtime.jvm.support.IndexSupport;
import us.terebi.lang.lpc.runtime.jvm.support.ValueSupport;
import us.terebi.lang.lpc.runtime.jvm.value.NilValue;
import us.terebi.lang.lpc.runtime.jvm.value.StringValue;
import us.terebi.lang.lpc.runtime.jvm.value.VoidValue;
import us.terebi.lang.lpc.runtime.util.LogCatch;

/**
 * 
 */
public class ByteCodeConstants
{
    public static final ParameterisedClass LPC_VALUE = new ParameterisedClassImpl(LpcValue.class);
    public static final ParameterisedClass LPC_REFERENCE = new ParameterisedClassImpl(LpcReference.class);
    public static final ParameterisedClass LPC_RUNTIME_EXCEPTION = new ParameterisedClassImpl(LpcRuntimeException.class);
    public static final ParameterisedClass LPC_FIELD = new ParameterisedClassImpl(LpcField.class);
    public static final ExtendedType LPC_VALUE_ARRAY = new ParameterisedClassImpl(LpcValue[].class);
    
    public static final Expression[] NO_ARGUMENTS = new Expression[0];

    public static final MethodSignature VALUE_AS_LIST = VM.Method.find(LpcValue.class, "asList");
    public static final MethodSignature VALUE_AS_MAP = VM.Method.find(LpcValue.class, "asMap");

    public static final MethodSignature REFERENCE_SET = VM.Method.find(LpcReference.class, "set", LpcValue.class);
    public static final MethodSignature REFERENCE_GET = VM.Method.find(LpcReference.class, "get");
    public static final MethodSignature MAP_ENTRY_SET = VM.Method.find(Map.class, "entrySet");
    public static final MethodSignature MAP_ENTRY_GET_KEY = VM.Method.find(Map.Entry.class, "getKey");
    public static final MethodSignature MAP_ENTRY_GET_VALUE = VM.Method.find(Map.Entry.class, "getValue");

    public static final MethodSignature EQUALS = VM.Method.find(LpcObject.class, "equals", Object.class);

    public static final Expression NIL = VM.Expression.getStaticField(NilValue.class, "INSTANCE", NilValue.class);
    public static final Expression VOID = VM.Expression.getStaticField(VoidValue.class, "INSTANCE", VoidValue.class);

    public static final ParameterisedClass JAVA_VOID_TYPE = new ParameterisedClassImpl(Void.TYPE);

    public static final ParameterisedClass INHERITED_OBJECT_TYPE = new ParameterisedClassImpl(InheritedObject.class);
    public static final MethodSignature INHERITED_OBJECT_GET = VM.Method.find(InheritedObject.class, "get");

    public static final MethodSignature MAKE_ARRAY = VM.Method.find(LpcObject.class, "makeArray", LpcValue[].class);
    public static final MethodSignature MAKE_MAPPING = VM.Method.find(LpcObject.class, "makeMapping", LpcValue[].class);

    public static final MethodSignature CALL_WITH_COLLECTIONS = VM.Method.find(LpcObject.class, "call", Callable.class, Collection[].class);
    public static final MethodSignature CALLABLE_EXECUTE = VM.Method.find(Callable.class, "execute", LpcValue[].class);
    public static final MethodSignature GET_OBJECT_INSTANCE = VM.Method.find(LpcObject.class, "getObjectInstance");
    public static final MethodSignature GET_OBJECT_DEFINITION = VM.Method.find(LpcObject.class, "getObjectDefinition");
    public static final MethodSignature GET_INSTANCE_DEFINITION = VM.Method.find(ObjectInstance.class, "getDefinition");

    public static final MethodSignature SINGLETON_LIST = VM.Method.find(Collections.class, "singletonList", Object.class);

    public static final MethodSignature INDEX_REFERENCE_1 = VM.Method.find(IndexSupport.class, "index", LpcReference.class, LpcValue.class,
            Boolean.TYPE);
    public static final MethodSignature INDEX_REFERENCE_2 = VM.Method.find(IndexSupport.class, "index", LpcReference.class, LpcValue.class,
            Boolean.TYPE, LpcValue.class, Boolean.TYPE);
    public static final MethodSignature INDEX_VALUE_1 = VM.Method.find(IndexSupport.class, "index", LpcValue.class, LpcValue.class, Boolean.TYPE);
    public static final MethodSignature INDEX_VALUE_2 = VM.Method.find(IndexSupport.class, "index", LpcValue.class, LpcValue.class, Boolean.TYPE,
            LpcValue.class, Boolean.TYPE);

    public static final MethodSignature CLASS_GET_FIELD = VM.Method.find(ClassSupport.class, "getField", LpcValue.class, String.class);

    public static final MethodSignature WITH_TYPE_4 = VM.Method.find(LpcRuntimeSupport.class, "withType", UserTypeDefinition.class,
            LpcType.Kind.class, String.class, Integer.TYPE);
    public static final MethodSignature CLASS_DECLARING_OBJECT = VM.Method.find(LpcClass.class, "getDeclaringObject");
    public static final MethodSignature FUNCTION_OWNER_DEFINITION = VM.Method.find(LpcFunction.class, "getOwnerDefinition");
    public static final MethodSignature FUNCTION_OWNER= VM.Method.find(LpcFunction.class, "getOwner");
    public static final MethodSignature EXCEPTION_GET_LPC_MESSAGE = VM.Method.find(LpcRuntimeException.class, "getLpcMessage");
    public static final MethodSignature GET_IMPLEMENTING_OBJECT = VM.Method.find(CompiledInstance.class, "getImplementingObject");

    public static final ConstructorSignature STRING_VALUE_CONSTRUCTOR = VM.Method.constructor(StringValue.class, String.class);

    public static final ParameterisedClass LOG_CATCH_TYPE = new ParameterisedClassImpl(LogCatch.class);
    public static final MethodSignature LOG_CATCH_METHOD = VM.Method.find(LogCatch.class, "log", Exception.class);

    public static final MethodSignature IS_IN_RANGE = VM.Method.find(ComparisonSupport.class, "isInRange", LpcValue.class, LpcValue.class, LpcValue.class);

    public static final MethodSignature INT_VALUE = VM.Method.find(ValueSupport.class, "intValue", Long.TYPE);
    public static final MethodSignature FLOAT_VALUE = VM.Method.find(ValueSupport.class, "floatValue", Double.TYPE);

    public static final MethodSignature AS_LONG = VM.Method.find(LpcValue.class, "asLong");
    public static final MethodSignature AS_DOUBLE = VM.Method.find(LpcValue.class, "asDouble");
    public static final MethodSignature AS_LIST = VM.Method.find(LpcValue.class, "asList");

    public static final ParameterisedClass COLLECTIONS = new ParameterisedClassImpl(Collections.class);
    public static final ParameterisedClass LIST = new ParameterisedClassImpl(List.class);
    public static final ParameterisedClass COLLECTION = new ParameterisedClassImpl(Collection.class);
    public static final MethodSignature COLLECTION_SIZE = VM.Method.find(Collection.class, "size");
}
