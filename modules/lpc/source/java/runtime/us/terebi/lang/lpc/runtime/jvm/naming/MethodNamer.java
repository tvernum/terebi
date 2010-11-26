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

package us.terebi.lang.lpc.runtime.jvm.naming;

/**
 * 
 */
public class MethodNamer
{
    private boolean _avoidStandardMethodNames;
    private boolean _avoidJavaReservedWords;

    public MethodNamer(boolean avoidStandardMethodNames, boolean avoidJavaReservedWords)
    {
        _avoidStandardMethodNames = avoidStandardMethodNames;
        _avoidJavaReservedWords = avoidJavaReservedWords;
    }

    public void setAvoidJavaReservedWords(boolean avoidJavaReservedWords)
    {
        _avoidJavaReservedWords = avoidJavaReservedWords;
    }
    
    public void setAvoidStandardMethodNames(boolean avoidStandardMethodNames)
    {
        _avoidStandardMethodNames = avoidStandardMethodNames;
    }
    
    public String getInternalName(String lpcName)
    {
        if (_avoidJavaReservedWords && ReservedWords.isReservedWord(lpcName))
        {
            return lpcName + "_$";
        }
        else if (_avoidStandardMethodNames && StandardMethods.isStandardMethod(lpcName))
        {
            return lpcName + "_$";
        }
        {
            return lpcName;
        }
    }
}
