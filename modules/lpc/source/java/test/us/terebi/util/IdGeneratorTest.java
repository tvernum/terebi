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

package us.terebi.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * 
 */
public class IdGeneratorTest
{
    @Test
    public void testWrapAround() throws Exception
    {
        IdGenerator gen = new IdGenerator(3, 10);
        assertEquals(3, gen.next());
        assertEquals(4, gen.next());
        assertEquals(5, gen.next());
        assertEquals(6, gen.next());
        assertEquals(7, gen.next());
        assertEquals(8, gen.next());
        assertEquals(9, gen.next());
        assertEquals(10, gen.next());
        assertEquals(3, gen.next());
        assertEquals(4, gen.next());
        assertEquals(5, gen.next());
        assertEquals(6, gen.next());
        assertEquals(7, gen.next());
        assertEquals(8, gen.next());
        assertEquals(9, gen.next());
        assertEquals(10, gen.next());
        assertEquals(3, gen.next());
    }
}
