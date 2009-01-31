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

package us.terebi.lang.lpc.parser;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 */
public class LineMapping
{
    private Map<Integer, String> _files;
    private Map<Integer, Integer> _offsets;

    public LineMapping(String initialFilename)
    {
        _files = new LinkedHashMap<Integer, String>();
        _offsets = new LinkedHashMap<Integer, Integer>();
        mapFile(0, initialFilename);
    }

    public void mapLine(int input, int mapTo)
    {
        _offsets.put(input, input - mapTo);
    }

    public void mapFile(int input, String mapTo)
    {
        _files.put(input, mapTo);
    }

    public String getFile(int inputLine)
    {
        String file = findMatch(inputLine, _files);
        if (file.startsWith("\"") && file.endsWith("\""))
        {
            return file.substring(1, file.length() - 1);
        }
        return file;
    }

    public int getLine(int inputLine)
    {
        Integer offset = findMatch(inputLine, _offsets);
        if (offset == null)
        {
            return inputLine;
        }
        return inputLine - offset;
    }

    private <T> T findMatch(int inputLine, Map<Integer, T> map)
    {
        T match = null;
        for (Map.Entry<Integer, T> entry : map.entrySet())
        {
            if (entry.getKey() > inputLine)
            {
                break;
            }
            match = entry.getValue();
        }
        return match;
    }
}
