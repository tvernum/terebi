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

package us.terebi.lang.lpc.runtime.jvm.context;

import java.util.List;

import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.util.collection.ArrayStack;
import us.terebi.util.collection.Stack;

/**
 * 
 */
public class CallStack
{
    public enum Origin
    {
        APPLY, HEART_BEAT, CALL_OUT, CALL_OTHER, EFUN, POINTER,
    }

    public static class MajorFrame
    {
        public final Origin origin;
        public final ObjectInstance instance;
        public final int javaStackSize;

        public MajorFrame(Origin orgn, ObjectInstance inst, int javaStackSz)
        {
            this.origin = orgn;
            this.instance = inst;
            this.javaStackSize = javaStackSz;
        }

        public String toString()
        {
            return "<" + origin + ":" + instance + ">";
        }
    }

    public static class DetailFrame extends MajorFrame
    {
        public DetailFrame(Origin orgn, ObjectInstance inst, int javaStackSz)
        {
            super(orgn, inst, javaStackSz);
            //@TODO
        }

//        public final String function;
    }

    private final Stack<MajorFrame> _frames;

    public CallStack()
    {
        _frames = new ArrayStack<MajorFrame>();
    }

    public void pushFrame(MajorFrame frame)
    {
        _frames.push(frame);
    }

    public void pushFrame(Origin origin, ObjectInstance instance)
    {
        int stackIndex = mostRecentUserFrameIndex();
        pushFrame(new MajorFrame(origin, instance, stackIndex));
    }

    private int mostRecentUserFrameIndex()
    {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        int stackIndex = stackTrace.length - 2;
        for (; stackIndex > 0; stackIndex--)
        {
            if (isUserCode(stackTrace[stackIndex]))
            {
                break;
            }
        }
        return stackIndex;
    }

    private boolean isUserCode(StackTraceElement element)
    {
        // @TODO This isn't really a guarantee...
        return element.getClassName().startsWith("lpc.");
    }

    public void popFrame()
    {
        _frames.pop();
    }

    public MajorFrame peekFrame(int offset)
    {
        return _frames.peek(offset);
    }

    /**
     * @return An {@link Iterable} over the set of {@link MajorFrame frames}, starting with the bottom-most (newest) frame.
     */
    public Iterable<MajorFrame> majorFrames()
    {
        return _frames;
    }

    public List<DetailFrame> detailFrames()
    {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        int stackIndex = stackTrace.length - 2;
        for (; stackIndex > 0; stackIndex--)
        {
            if (isUserCode(stackTrace[stackIndex]))
            {
                break;
            }
        }
//        return stackIndex;
        // @TODO
        return null;
    }

    public int size()
    {
        return _frames.size();
    }

    public void begin()
    {
        _frames.clear();
    }

    public MajorFrame topFrame()
    {
        return _frames.top();
    }

    public String toString()
    {
        return getClass().getSimpleName() + "{" + _frames + "}";
    }
}
