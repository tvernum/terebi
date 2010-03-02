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

import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.efun.string.ExplodeEfun;

/**
 * 
 */
public class ExplodeEfunTest
{
    @Test
    public void explode() throws Exception
    {
        //        eval return explode( "///AAA//BB/CCC///DDD/EE/", "/" );
        //        Result = ({ "", "", "AAA", "", "BB", "CCC", "", "", "DDD", "EE" })

        List<LpcValue> explode = ExplodeEfun.explode("///AAA//BB/CCC///DDD/EE/", "/");
        assertEquals(10, explode.size());
        assertEquals("", explode.get(0).asString());
        assertEquals("", explode.get(1).asString());
        assertEquals("AAA", explode.get(2).asString());
        assertEquals("", explode.get(3).asString());
        assertEquals("BB", explode.get(4).asString());
        assertEquals("CCC", explode.get(5).asString());
        assertEquals("", explode.get(6).asString());
        assertEquals("", explode.get(7).asString());
        assertEquals("DDD", explode.get(8).asString());
        assertEquals("EE", explode.get(9).asString());

        //        eval return explode("/A/B/C/////", "/");
        //        Result = ({ "A", "B", "C", "", "", "", "" })
        explode = ExplodeEfun.explode("/A/B/C/////", "/");
        assertEquals(7, explode.size());
        assertEquals("A", explode.get(0).asString());
        assertEquals("B", explode.get(1).asString());
        assertEquals("C", explode.get(2).asString());
        assertEquals("", explode.get(3).asString());
        assertEquals("", explode.get(4).asString());
        assertEquals("", explode.get(5).asString());
        assertEquals("", explode.get(6).asString());

        //        eval return explode("//A/B//" , "/" );
        //        Result = ({ "", "A", "B", "" })
        explode = ExplodeEfun.explode("//A/B//", "/");
        assertEquals(4, explode.size());
        assertEquals("", explode.get(0).asString());
        assertEquals("A", explode.get(1).asString());
        assertEquals("B", explode.get(2).asString());
        assertEquals("", explode.get(3).asString());
    }
}
