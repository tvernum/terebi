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

package us.terebi.lang.lpc.compiler.bytecode.stout;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

import org.adjective.stout.core.ExecutionStack;
import org.adjective.stout.core.InstructionCollector;
import org.adjective.stout.core.UnresolvedType;
import org.adjective.stout.instruction.GenericInstruction;
import org.adjective.stout.instruction.LabelInstruction;
import org.adjective.stout.loop.ExpressionCondition;
import org.adjective.stout.operation.DuplicateStackExpression;
import org.adjective.stout.operation.Expression;

import us.terebi.lang.lpc.compiler.bytecode.ByteCodeConstants;
import us.terebi.lang.lpc.compiler.bytecode.ExpressionCompiler;

/**
 * 
 */
public class LogicalOrExpression implements Expression
{
    private final Expression[] _branches;

    public LogicalOrExpression(Expression[] branches)
    {
        _branches = branches;
    }

    public UnresolvedType getExpressionType(ExecutionStack stack)
    {
        return ByteCodeConstants.LPC_VALUE;
    }

    public void getInstructions(ExecutionStack stack, InstructionCollector collector)
    {
        Label end = new Label();
        DuplicateStackExpression dup = new DuplicateStackExpression();
        GenericInstruction pop = new GenericInstruction(Opcodes.POP);

        for (Expression branch : _branches)
        {
            branch.getInstructions(stack, collector);
            new ExpressionCondition(ExpressionCompiler.toBoolean(dup)).jumpWhenTrue(end).getInstructions(stack, collector);
            pop.getInstructions(stack, collector);
        }
        
        ByteCodeConstants.NIL.getInstructions(stack, collector);
        new LabelInstruction(end).getInstructions(stack, collector);
    }

}
