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

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import org.adjective.stout.core.MethodDescriptor;
import org.adjective.stout.instruction.MethodInstruction;
import org.adjective.stout.operation.VM;
import org.adjective.stout.writer.ByteCodeWriter;

import us.terebi.lang.lpc.runtime.jvm.support.ExecutionTimeCheck;

/**
 * 
 */
public class LpcByteCodeWriter extends ByteCodeWriter
{
    protected static final class LpcMethodVisitor implements MethodVisitor
    {
        private final MethodVisitor _delegate;

        public LpcMethodVisitor(MethodVisitor delegate)
        {
            _delegate = delegate;
        }

        public void visitCode()
        {
            _delegate.visitCode();
            insertTimeCheck();
        }

        public void visitLabel(Label label)
        {
            _delegate.visitLabel(label);
            insertTimeCheck();
        }

        private void insertTimeCheck()
        {
            MethodInstruction instruction = new MethodInstruction(Opcodes.INVOKESTATIC, Type.getInternalName(ExecutionTimeCheck.class), VM.Method.find(ExecutionTimeCheck.class, "check"));
            instruction.accept(_delegate);
        }

        public AnnotationVisitor visitAnnotation(String desc, boolean visible)
        {
            return _delegate.visitAnnotation(desc, visible);
        }

        public AnnotationVisitor visitAnnotationDefault()
        {
            return _delegate.visitAnnotationDefault();
        }

        public void visitAttribute(Attribute attr)
        {
            _delegate.visitAttribute(attr);
        }

        public void visitEnd()
        {
            _delegate.visitEnd();
        }

        public void visitFieldInsn(int opcode, String owner, String name, String desc)
        {
            _delegate.visitFieldInsn(opcode, owner, name, desc);
        }

        public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack)
        {
            _delegate.visitFrame(type, nLocal, local, nStack, stack);
        }

        public void visitIincInsn(int var, int increment)
        {
            _delegate.visitIincInsn(var, increment);
        }

        public void visitInsn(int opcode)
        {
            _delegate.visitInsn(opcode);
        }

        public void visitIntInsn(int opcode, int operand)
        {
            _delegate.visitIntInsn(opcode, operand);
        }

        public void visitJumpInsn(int opcode, Label label)
        {
            _delegate.visitJumpInsn(opcode, label);
        }

        public void visitLdcInsn(Object cst)
        {
            _delegate.visitLdcInsn(cst);
        }

        public void visitLineNumber(int line, Label start)
        {
            _delegate.visitLineNumber(line, start);
        }

        public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index)
        {
            _delegate.visitLocalVariable(name, desc, signature, start, end, index);
        }

        public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels)
        {
            _delegate.visitLookupSwitchInsn(dflt, keys, labels);
        }

        public void visitMaxs(int maxStack, int maxLocals)
        {
            _delegate.visitMaxs(maxStack, maxLocals);
        }

        public void visitMethodInsn(int opcode, String owner, String name, String desc)
        {
            _delegate.visitMethodInsn(opcode, owner, name, desc);
        }

        public void visitMultiANewArrayInsn(String desc, int dims)
        {
            _delegate.visitMultiANewArrayInsn(desc, dims);
        }

        public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible)
        {
            return _delegate.visitParameterAnnotation(parameter, desc, visible);
        }

        public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels)
        {
            _delegate.visitTableSwitchInsn(min, max, dflt, labels);
        }

        public void visitTryCatchBlock(Label start, Label end, Label handler, String type)
        {
            _delegate.visitTryCatchBlock(start, end, handler, type);
        }

        public void visitTypeInsn(int opcode, String type)
        {
            _delegate.visitTypeInsn(opcode, type);
        }

        public void visitVarInsn(int opcode, int var)
        {
            _delegate.visitVarInsn(opcode, var);
        }

    }

    protected MethodVisitor visitMethod(ClassVisitor cv, MethodDescriptor method, String[] exceptions, String signature)
    {
        return new LpcMethodVisitor(super.visitMethod(cv, method, exceptions, signature));
    }
}
