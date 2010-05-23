/* ------------------------------------------------------------------------
 * Copyright 2010 Tim Vernum
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

package us.terebi.lang.lpc.runtime.jvm.value;

import us.terebi.lang.lpc.runtime.ByteSequence;

/**
 * 
 */
public class NoBytes implements ByteSequence
{
    public static final ByteSequence INSTANCE = new NoBytes();

    public byte getByte(int index)
    {
        return 0;
    }

    public int length()
    {
        return 0;
    }

    public void setByte(int index, byte value)
    {
        // Do nothing
    }

    public int compareTo(ByteSequence o)
    {
        if (o.length() == 0)
        {
            return 0;
        }
        else
        {
            return -1;
        }
    }

}
