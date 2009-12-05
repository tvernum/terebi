/* ------------------------------------------------------------------------
 * The Terebi (LPC) Game Engine
 * Copyright 2009 Tim Vernum
 * ------------------------------------------------------------------------
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ------------------------------------------------------------------------
 */

package us.terebi.engine.config;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public class PropertiesConfigTest
{
    @Test
    public void testReadingProperties() throws Exception
    {
        File file = new File("source/resource/test/test.properties");
        Assert.assertTrue(file.exists());

        PropertiesConfig config = new PropertiesConfig(file);
        Assert.assertEquals("abc", config.getString("test.1"));
        Assert.assertEquals("xyzzy", config.getString("test.2", "foo"));
        Assert.assertEquals("foo", config.getString("test.2x", "foo"));
        Assert.assertEquals(false, config.getBoolean("test.3"));
        Assert.assertEquals(true, config.getBoolean("test.4", false));
        Assert.assertEquals(false, config.getBoolean("test.4x", false));
        Assert.assertEquals(4, config.getLongs("test.5").length);
    }
}
