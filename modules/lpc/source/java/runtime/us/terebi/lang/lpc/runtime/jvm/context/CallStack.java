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

import java.util.ArrayList;
import java.util.List;

import us.terebi.lang.lpc.runtime.ObjectInstance;
import us.terebi.lang.lpc.runtime.jvm.exception.InternalError;
import us.terebi.util.collection.ArrayStack;
import us.terebi.util.collection.Stack;

/**
 * 
 */
public class CallStack
{
    public enum Origin
    {
        APPLY, DRIVER, CALL_OUT, CALL_OTHER, EFUN, POINTER, SIMUL;

        public String lpcName()
        {
            return name().toLowerCase();
        }
    }

    public interface Frame
    {
        public Origin origin();

        public ObjectInstance instance();
    }

    public static class MajorFrame implements Frame
    {
        private final Origin _origin;
        private final ObjectInstance _instance;
        private final int _javaStackSize;

        public MajorFrame(Origin origin, ObjectInstance instance, int javaStackSize)
        {
            if (instance == null)
            {
                throw new InternalError("Attempt to push frame (" + origin + ") with no object");
            }
            this._origin = origin;
            this._instance = instance;
            this._javaStackSize = javaStackSize;
        }

        public String toString()
        {
            return "<" + _origin + ":" + _instance + ">";
        }

        public ObjectInstance instance()
        {
            return _instance;
        }

        public Origin origin()
        {
            return _origin;
        }

        public int stackIndex()
        {
            return _javaStackSize;
        }
    }

    public static class DetailFrame implements Frame
    {
        private final Origin _origin;
        private final ObjectInstance _instance;
        private final String _function;

        public DetailFrame(Origin origin, ObjectInstance instance, String function)
        {
            _origin = origin;
            _instance = instance;
            _function = function;
        }

        public Origin origin()
        {
            return _origin;
        }

        public ObjectInstance instance()
        {
            return _instance;
        }

        public String function()
        {
            return _function;
        }

        public String toString()
        {
            return "<" + _origin + ":" + _instance + "->" + _function + ">";
        }
    }

    private final Stack<MajorFrame> _frames;

    public CallStack()
    {
        _frames = new ArrayStack<MajorFrame>();
    }

    private void pushFrame(MajorFrame frame)
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
        int stackIndex = 2;
        for (; stackIndex < stackTrace.length; stackIndex++)
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
        List<DetailFrame> frames = new ArrayList<DetailFrame>(stackTrace.length / 2);

        int frameIndex = 0;
        for (int stackIndex = 2; stackIndex < stackTrace.length; stackIndex++)
        {
            if (frameIndex >= _frames.size())
            {
                break;
            }
            MajorFrame major = _frames.peek(frameIndex);
            if (isUserCode(stackTrace[stackIndex]))
            {
                DetailFrame frame = new DetailFrame(major.origin(), major.instance(), stackTrace[stackIndex].getMethodName());
                frames.add(frame);
            }
            if (stackIndex == major.stackIndex())
            {
                frameIndex++;
            }
        }

        return frames;
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
