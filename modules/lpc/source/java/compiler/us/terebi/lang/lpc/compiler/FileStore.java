/* ------------------------------------------------------------------------
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

package us.terebi.lang.lpc.compiler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.objectweb.asm.ClassReader;

import org.adjective.stout.asm.AbstractASMVisitor;

import us.terebi.util.io.IOUtil;

/**
 * 
 */
public class FileStore implements ClassStore
{
    private final Logger LOG = Logger.getLogger(FileStore.class);

    private final File _directory;

    public FileStore(File directory)
    {
        _directory = directory;
        LOG.info("New " + getClass().getSimpleName() + " @" + directory);
    }

    public OutputStream open(ClassName className) throws FileNotFoundException
    {
        File classFile = getFile(className);
        classFile.getParentFile().mkdirs();
        return new FileOutputStream(classFile);
    }

    private File getFile(ClassName className)
    {
        File classFile = new File(_directory, className.fileName());
        return classFile;
    }

    public long getLastModified(ClassName className)
    {
        return getFile(className).lastModified();
    }
    
    public class HierarchyVisitor extends AbstractASMVisitor
    {
        private final Set<ClassName> _hierarchy;

        public HierarchyVisitor(Set<ClassName> hierarchy)
        {
            _hierarchy = hierarchy;
        }
    }


    public Iterable<ClassName> getDependencies(ClassName className)
    {
        File file = getFile(className);
        if (!file.exists())
        {
            return null;
        }
        
        Set<ClassName> hierarchy = new LinkedHashSet<ClassName>();
        FileInputStream input = null;
        try
        {
            input = new FileInputStream(file);
            ClassReader reader = new ClassReader(input);
            reader.accept(new HierarchyVisitor(hierarchy), ClassReader.SKIP_DEBUG | ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES );
        }
        catch (IOException e)
        {
            // Log & Ignore - fall through to return whatever we've loaded...
            LOG.warn("Failed to load class hierarchy for " + className, e);
        }
        finally
        {
            IOUtil.close(input);
        }
        return hierarchy;
    }

}
