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

import org.junit.Test;

import us.terebi.lang.lpc.runtime.jvm.efun.string.PluralizeEfun;

import static org.junit.Assert.assertEquals;


/**
 * 
 */
public class PluralizeEfunTest
{
    @Test
    public void wordsShouldPluralizeCorrectly() throws Exception
    {
        PluralizeEfun efun = new PluralizeEfun();
        assertEquals("frogs", efun.getPlural("frog"));
        assertEquals("foxes", efun.getPlural("fox"));
        assertEquals("data", efun.getPlural("datum"));
        assertEquals("chefs", efun.getPlural("chef"));
        assertEquals("zeros", efun.getPlural("zero"));
        assertEquals("swords of fate", efun.getPlural("sword of fate"));
        assertEquals("Green Lanterns", efun.getPlural("Green Lantern"));
        assertEquals("Dead Geese", efun.getPlural("Dead Goose"));
    }
}
