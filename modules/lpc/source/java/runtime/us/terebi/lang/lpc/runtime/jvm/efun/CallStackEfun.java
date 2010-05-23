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

package us.terebi.lang.lpc.runtime.jvm.efun;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.context.RuntimeContext;
import us.terebi.lang.lpc.runtime.jvm.context.ThreadContext;
import us.terebi.lang.lpc.runtime.jvm.context.CallStack.DetailFrame;
import us.terebi.lang.lpc.runtime.jvm.exception.LpcRuntimeException;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.ArrayValue;
import us.terebi.lang.lpc.runtime.jvm.value.StringValue;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;

/**
 * 
 */
public class CallStackEfun extends AbstractEfun implements FunctionSignature, Callable
{
    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        return Collections.singletonList(new ArgumentSpec("flag", Types.INT));
    }

    public LpcType getReturnType()
    {
        return Types.MIXED_ARRAY;
    }

    public boolean acceptsLessArguments()
    {
        return true;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        checkArguments(arguments);
        if (arguments.size() == 0)
        {
            return execute(0);
        }
        else
        {
            LpcValue arg = arguments.get(0);
            return execute((int) arg.asLong());
        }
    }

    /*
     * > eval return call_stack()
    Result = ({ "/secure/tmp/zod_CMD_EVAL_TMP_FILE.c", "/secure/cmds/creators/eval.c", "/lib/command.c", "/<driver>" })

    > eval return call_stack(1)
    Result = ({ OBJ(/secure/tmp/zod_CMD_EVAL_TMP_FILE), OBJ(/secure/cmds/creators/eval), OBJ(zod /secure/save/creators/z/zod), OBJ(zod
    /secure/save/creators/z/zod) })

    > eval return call_stack(2)
    Result = ({ "eval", "cmd", "cmdAll", "<function>" })

    > eval return call_stack(3)
    Result = ({ "call_other", "call_other", "local", "function pointer" })

    > eval return call_stack(4)
    ---
    2009.12.24-14.40,24
    *First argument of call_stack() must be 0, 1, 2, or 3.
    Object: /secure/tmp/zod_CMD_EVAL_TMP_FILE at line 21

     */
    private LpcValue execute(int flag)
    {
        List<LpcValue> result = new ArrayList<LpcValue>();

        ThreadContext context = RuntimeContext.obtain();
        List<DetailFrame> frames = context.callStack().detailFrames();

        LpcType type = (flag == 1) ? Types.OBJECT_ARRAY : Types.STRING_ARRAY;
        for (DetailFrame frame : frames)
        {
            if (flag == 1)
            {
                result.add(frame.instance().asValue());
                continue;
            }
            String str;
            switch (flag)
            {
                case 0:
                    str = frame.instance().getDefinition().getName();
                    break;
                case 2:
                    str = frame.function();
                    break;
                case 3:
                    str = frame.origin().lpcName();
                    break;
                default:
                    throw new LpcRuntimeException("Bad flag (" + flag + ") to " + getName());
            }
            result.add(new StringValue(str));
        }

        return new ArrayValue(type, result);
    }

}
