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

package us.terebi.lang.lpc.runtime.jvm.efun.file;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.log4j.Logger;

import us.terebi.lang.lpc.io.Resource;
import us.terebi.lang.lpc.io.ResourceFinder;
import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.LpcConstants;
import us.terebi.lang.lpc.runtime.jvm.support.ValueSupport;
import us.terebi.lang.lpc.runtime.jvm.type.Types;
import us.terebi.lang.lpc.runtime.jvm.value.ArrayValue;
import us.terebi.lang.lpc.runtime.jvm.value.IntValue;
import us.terebi.lang.lpc.runtime.jvm.value.StringValue;
import us.terebi.lang.lpc.runtime.util.ArgumentSpec;

/**
 * 
 */
public class GetDirectoryInfoEfun extends FileEfun implements FunctionSignature, Callable
{
    private final Logger LOG = Logger.getLogger(GetDirectoryInfoEfun.class);

    //    mixed array get_dir(string dir);
    //    mixed array get_dir(string dir, int flag);
    protected List< ? extends ArgumentDefinition> defineArguments()
    {
        ArrayList<ArgumentDefinition> list = new ArrayList<ArgumentDefinition>();
        list.add(new ArgumentSpec("dir", Types.STRING));
        list.add(new ArgumentSpec("flag", Types.INT));
        return list;
    }

    public boolean acceptsLessArguments()
    {
        return true;
    }

    public LpcType getReturnType()
    {
        return Types.MIXED_ARRAY;
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        checkArguments(arguments);
        String path = arguments.get(0).asString();
        long flag = arguments.get(1).asLong();

        path = path.replace("//", "/");
        try
        {
            List<LpcValue> info = getDirectoryInfo(this, path, flag);
            if (LOG.isDebugEnabled())
            {
                if (info.size() <= 3)
                {
                    LOG.debug("get_dir(" + path + ',' + flag + ") = " + info);
                }
                else
                {
                    LOG.debug("get_dir(" + path + ',' + flag + ") = " + info.get(0) + " ... " + info.get(info.size() - 1) + "  [*" + info.size() + "]");
                }
            }
            return ValueSupport.arrayValue(info);
        }
        catch (IOException e)
        {
            LOG.warn(e);
            return LpcConstants.ARRAY.EMPTY;
        }
    }

    public static List<LpcValue> getDirectoryInfo(ResourceFinder resourceFinder, String path, long flag) throws IOException
    {
        boolean listDirectory = path.endsWith("/");
        boolean longListing = (flag == -1);

        Resource[] files = resolveWildCard(resourceFinder, path);
        List<LpcValue> array = new ArrayList<LpcValue>(files.length);

        for (Resource resource : files)
        {
            if (listDirectory)
            {
                if (!resource.isDirectory())
                {
                    continue;
                }
                for (Resource child : resource.getChildren())
                {
                    array.add(getInfo(child, longListing));
                }
            }
            else
            {
                array.add(getInfo(resource, longListing));
            }
        }

        return array;
    }

    private static LpcValue getInfo(Resource resource, boolean longListing)
    {
        if (longListing)
        {
            return new ArrayValue(Types.MIXED_ARRAY, //
                    new StringValue(resource.getName()), new IntValue(resource.getSizeInBytes()), new IntValue(resource.lastModified()));
        }
        else
        {
            return new StringValue(resource.getName());
        }
    }

    protected static Resource[] resolveWildCard(ResourceFinder resourceFinder, String path) throws IOException
    {
        if (path.indexOf('*') == -1 && path.indexOf('?') == -1)
        {
            return new Resource[] { resourceFinder.getResource(path) };
        }

        List<Resource> match = new ArrayList<Resource>();

        int slash = path.lastIndexOf('/', path.length() - 1);
        String parentPath = path.substring(0, slash);
        String childPath = path.substring(slash + 1);

        Resource[] parents = resolveWildCard(resourceFinder, parentPath);
        for (Resource parent : parents)
        {
            Resource[] children = parent.getChildren();
            for (Resource child : children)
            {
                if (FilenameUtils.wildcardMatch(child.getName(), childPath, IOCase.INSENSITIVE))
                {
                    match.add(child);
                }
            }
        }

        return match.toArray(new Resource[match.size()]);
    }
}
