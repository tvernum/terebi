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

package us.terebi.lang.lpc.runtime.jvm.value;

import us.terebi.lang.lpc.runtime.ByteSequence;

/**
 * 
 */
public class ByteArraySequence implements ByteSequence
{
    private final byte[] _bytes;

    public ByteArraySequence(byte[] bytes)
    {
        _bytes = bytes;
    }

    public byte getByte(int index)
    {
        return _bytes[index];
    }

    public int length()
    {
        return _bytes.length;
    }

    public void setByte(int index, byte value)
    {
        _bytes[index] = value;
    }

    public int compareTo(ByteSequence other)
    {
        int i = 0;
        for (i = 0; i < _bytes.length; i++)
        {
            if (other.length() <= i)
            {
                return 1;
            }
            byte b1 = this.getByte(i);
            byte b2 = other.getByte(i);
            if (b1 > b2)
            {
                return 1;
            }
            if (b1 < b2)
            {
                return -1;
            }
        }
        if (i < other.length())
        {
            return -1;
        }
        return 0;
    }

}
