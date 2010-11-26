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

package us.terebi.lang.lpc.compiler.bytecode;

/**
 * 
 */
public class DebugPoint
{
    public static void breakpoint(String fullQualifiedClass, String method, int count)
    {
        System.err.println("Breakpoint: " + fullQualifiedClass + " " + method + " " + count);
    }

    public static void breakpoint(Object object, String method, int count)
    {
        System.err.println("Breakpoint: " + object.getClass() + " (@" + System.identityHashCode(object) + ") " + method + " " + count);
    }
}
