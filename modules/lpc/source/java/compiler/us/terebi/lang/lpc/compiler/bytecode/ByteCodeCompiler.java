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

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.regex.Pattern;

import org.adjective.stout.builder.ClassSpec;
import org.adjective.stout.core.ClassDescriptor;
import org.adjective.stout.core.ElementModifier;
import org.adjective.stout.writer.ByteCodeWriter;

import us.terebi.lang.lpc.compiler.ClassName;
import us.terebi.lang.lpc.compiler.ClassStore;
import us.terebi.lang.lpc.compiler.Compiler;
import us.terebi.lang.lpc.compiler.CompilerObjectManager;
import us.terebi.lang.lpc.compiler.ObjectSource;
import us.terebi.lang.lpc.compiler.java.context.BasicScopeLookup;
import us.terebi.lang.lpc.compiler.java.context.ScopeLookup;
import us.terebi.lang.lpc.parser.LineMapping;
import us.terebi.lang.lpc.parser.ast.ASTObjectDefinition;
import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.LpcObject;
import us.terebi.lang.lpc.runtime.jvm.context.Efuns;

/**
 * 
 */
public class ByteCodeCompiler implements Compiler
{
    private final CompilerObjectManager _objectManager;
    private final Efuns _efuns;
    private final List<Pattern> _debugPatterns;
    private final boolean _insertTimeCheck;

    public ByteCodeCompiler(CompilerObjectManager objectManager, Efuns efuns, List<Pattern> debugPatterns, boolean insertTimeCheck)
    {
        _objectManager = objectManager;
        _efuns = efuns;
        _debugPatterns = debugPatterns;
        _insertTimeCheck = insertTimeCheck;
    }

    public void compile(ObjectSource source, ClassName name, ClassStore store, LineMapping lineMapping) throws IOException
    {
        ClassSpec spec = ClassSpec.newClass(name.packageName, name.className).withSuperClass(LpcObject.class).withModifiers(ElementModifier.PUBLIC);
        spec.withSourceCode(source.getFilename());
        ASTObjectDefinition ast = source.getSyntaxTree();
        CompileContext context = new CompileContext(store, new CompileOptions(), ast, spec, lineMapping, _debugPatterns, _insertTimeCheck);

        compile(context, spec);
        store(spec, context);
    }

    private void compile(CompileContext context, ClassSpec spec)
    {
        ScopeLookup scope = new BasicScopeLookup(_objectManager);
        scope.functions().addEfuns(_efuns);
        ObjectInstance sefun = _objectManager.getSimulatedEfunObject();
        if (sefun != null)
        {
            scope.functions().addSimulEfuns(sefun.getDefinition().getMethods().values());
        }
        new ClassBuilder(context, scope, spec).compile();
    }

    public static ClassDescriptor store(ClassSpec spec, CompileContext context) throws IOException
    {
        return store(spec, context.store(), context.getDebugPatterns(), context.isTimeCheckEnabled());
    }

    public static ClassDescriptor store(ClassSpec spec, ClassStore store, List<Pattern> debugPatterns, boolean execTimeCheck) throws IOException
    {
        ClassDescriptor cls = spec.create();
        ByteCodeWriter writer = new LpcByteCodeWriter(debugPatterns, execTimeCheck);
        byte[] bytes = writer.write(cls);
        OutputStream stream = store.open(cls.getPackage(), cls.getName());
        stream.write(bytes);
        stream.close();
        return cls;
    }
}
