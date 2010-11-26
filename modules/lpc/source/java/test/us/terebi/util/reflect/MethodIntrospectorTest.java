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

package us.terebi.util.reflect;

import java.util.Set;

import org.junit.Test;

import junit.framework.Assert;

/**
 * 
 */
public class MethodIntrospectorTest
{
    @Test
    public void testIntrospectObjectMethodNames() throws Exception
    {
        MethodIntrospector introspector = new MethodIntrospector(Object.class);
        Assert.assertEquals(0, introspector.getAllDeclaredMethodNames(false).size());
        Set<String> names = introspector.getAllDeclaredMethodNames(true);
        Assert.assertEquals(10, names.size());
    }

    @Test
    public void testIntrospectStringMethodNames() throws Exception
    {
        MethodIntrospector introspector = new MethodIntrospector(String.class);
        Assert.assertEquals(38, introspector.getAllDeclaredMethodNames(false).size());
        Set<String> names = introspector.getAllDeclaredMethodNames(true);
        Assert.assertEquals(45, names.size());
    }
}
