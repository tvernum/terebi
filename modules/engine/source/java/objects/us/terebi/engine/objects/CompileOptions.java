/* ------------------------------------------------------------------------
 * Copyright 2009 Tim Vernum
 * ------------------------------------------------------------------------
 * Licensed under the GNU Affero General Public License (AGPL) 
 * ------------------------------------------------------------------------
 */

package us.terebi.engine.objects;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import us.terebi.engine.config.Config;
import us.terebi.engine.config.ConfigNames;
import us.terebi.lang.lpc.io.Resource;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public class CompileOptions
{
    private final File _javaOutputDirectory;
    private final List<Pattern> _debugPatterns;

    private final Resource[] _includeDirectories;
    private final Resource[] _autoIncludeFiles;

    private final Map<String, String> _preprocessorDefinitions;

    public CompileOptions(Config config, MudlibSetup mudlib)
    {
        this( //
                config.getFile(ConfigNames.COMPILE_OUTPUT, Config.FileType.EXISTING_DIRECTORY), //
                config.getResourcePath(ConfigNames.COMPILE_INCLUDE_DIRECTORIES, mudlib.root(), Config.FileType.EXISTING_DIRECTORY), //
                config.getResourcePath(ConfigNames.COMPILE_AUTO_INCLUDE, mudlib.root(), Config.FileType.EXISTING_FILE), //
                config.getStrings(ConfigNames.COMPILE_DEBUG) //        
        );

    }

    public CompileOptions(File javaOutputDirectory, Resource[] include, Resource[] autoInclude, String[] debugPatterns)
    {
        _javaOutputDirectory = javaOutputDirectory;
        _includeDirectories = include;
        _autoIncludeFiles = autoInclude;
        _debugPatterns = new ArrayList<Pattern>(debugPatterns.length);
        for (String pattern : debugPatterns)
        {
            _debugPatterns.add(Pattern.compile(pattern));
        }
        _preprocessorDefinitions = new HashMap<String, String>();
    }

    public File compilerOutputDirectory()
    {
        return _javaOutputDirectory;
    }

    public Resource[] includeDirectories()
    {
        return _includeDirectories;
    }

    public Resource[] autoIncludeFiles()
    {
        return _autoIncludeFiles;
    }

    public Map<String, String> getPreprocessorDefinitions()
    {
        return Collections.unmodifiableMap(_preprocessorDefinitions);
    }

    public void defineString(String word, String string)
    {
        if (_preprocessorDefinitions.containsKey(word))
        {
            throw new IllegalArgumentException("Word '" + word + "' is already defined as " + _preprocessorDefinitions.get(word));
        }
        string = string.replace("\\", "\\\\");
        string = string.replace("\"", "\\\"");
        string = "\"" + string + "\"";
        _preprocessorDefinitions.put(word, string);
    }

    public void defineLong(String word, long value)
    {
        if (_preprocessorDefinitions.containsKey(word))
        {
            throw new IllegalArgumentException("Word '" + word + "' is already defined as " + _preprocessorDefinitions.get(word));
        }
        _preprocessorDefinitions.put(word, Long.toString(value));
    }

    public void defineTrue(String word)
    {
        defineLong(word, 1);
    }
    
    public List<Pattern> getDebugPatterns()
    {
        return _debugPatterns;
    }

}
