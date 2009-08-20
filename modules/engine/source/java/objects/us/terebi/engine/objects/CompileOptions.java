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

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public class CompileOptions
{
    private static final String CONFIG_AUTO_INCLUDE = "compile.include.auto";
    private static final String CONFIG_INCLUDE_DIRECTORIES = "compile.include.directories";
    private static final String CONFIG_COMPILE_OUTPUT = "compile.output";

    private final File _javaOutputDirectory;
    private final List<Pattern> _debugPatterns;

    private final File[] _includeDirectories;
    private final File[] _autoIncludeFiles;

    private final Map<String, String> _preprocessorDefinitions;

    public CompileOptions(Config config, MudlibSetup mudlib)
    {
        this( //
                config.getFile(CONFIG_COMPILE_OUTPUT, Config.FileType.EXISTING_DIRECTORY), //
                config.getPath(CONFIG_INCLUDE_DIRECTORIES, mudlib.root(), Config.FileType.EXISTING_DIRECTORY), //
                config.getPath(CONFIG_AUTO_INCLUDE, mudlib.root(), Config.FileType.EXISTING_FILE));

    }

    public CompileOptions(File javaOutputDirectory, File[] include, File[] autoInclude)
    {
        _javaOutputDirectory = javaOutputDirectory;
        _debugPatterns = new ArrayList<Pattern>();
        _includeDirectories = include;
        _autoIncludeFiles = autoInclude;
        _preprocessorDefinitions = new HashMap<String, String>();
    }

    public File compilerOutputDirectory()
    {
        return _javaOutputDirectory;
    }

    public boolean isDebugEnabled(String file)
    {
        for (Pattern pattern : _debugPatterns)
        {
            if (pattern.matcher(file).matches())
            {
                return true;
            }
        }
        return false;
    }

    public void enableDebug(Pattern pattern)
    {
        _debugPatterns.add(pattern);
    }

    public File[] includeDirectories()
    {
        return _includeDirectories;
    }

    public File[] autoIncludeFiles()
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

}
