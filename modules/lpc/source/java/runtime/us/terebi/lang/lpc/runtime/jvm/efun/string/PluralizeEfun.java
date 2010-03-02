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

package us.terebi.lang.lpc.runtime.jvm.efun.string;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import us.terebi.lang.lpc.runtime.Callable;
import us.terebi.lang.lpc.runtime.FunctionSignature;
import us.terebi.lang.lpc.runtime.LpcValue;
import us.terebi.lang.lpc.runtime.jvm.efun.AbstractStringEfun;
import us.terebi.lang.lpc.runtime.jvm.value.StringValue;
import us.terebi.lang.lpc.runtime.util.SystemLog;
import us.terebi.util.io.IOUtil;
import us.terebi.util.io.RuntimeIOException;

/**
 * 
 */
public class PluralizeEfun extends AbstractStringEfun implements FunctionSignature, Callable
{
    private static final String RULE_PATTERN = "^\\s*(\\w+)\\s+(\\w+)\\s*$";
    private static final Pattern X_OF_Y = Pattern.compile("^(\\w+)(\\s+of\\s+\\w+)$");

    private static final String PLURALS_TEXT = "plurals.text";

    private final Map<String, String> _specialCases;

    public PluralizeEfun()
    {
        _specialCases = readExceptionsFile();
    }

    private Map<String, String> readExceptionsFile()
    {
        Map<String, String> exceptions = new HashMap<String, String>();

        URL plurals = getClass().getResource("/" + PLURALS_TEXT);
        if (plurals == null)
        {
            configNotFound(null);
            return exceptions;
        }

        InputStream stream = null;
        try
        {
            stream = plurals.openStream();
        }
        catch (IOException e)
        {
            configNotFound(e);
            return exceptions;
        }

        try
        {
            Pattern pattern = Pattern.compile(RULE_PATTERN);
            for (String line : IOUtil.lines(stream))
            {
                Matcher matcher = pattern.matcher(line);
                if (!matcher.matches())
                {
                    error("Invalid plural rule " + line + " - should match " + RULE_PATTERN, null);
                }
                String single = matcher.group(1);
                String plural = matcher.group(2);
                exceptions.put(single, plural);
            }
        }
        catch (RuntimeIOException e)
        {
            error("Cannot read " + PLURALS_TEXT, e);
        }
        finally
        {
            IOUtil.close(stream);
        }

        return exceptions;
    }

    private void configNotFound(Throwable cause)
    {
        error("Cannot find a " + PLURALS_TEXT + " resource file - no exceptions will be loaded", cause);
    }

    private void error(String msg, Throwable cause)
    {
        SystemLog.message(msg, cause);
    }

    public LpcValue execute(List< ? extends LpcValue> arguments)
    {
        checkArguments(arguments);
        LpcValue str = arguments.get(0);
        return new StringValue(getPlural(str.asString()));
    }

    public String getPlural(String singular)
    {
        singular = singular.trim();

        /* if the word is of the form 'X of Y', pluralize the 'X' part */
        Matcher matcher = X_OF_Y.matcher(singular);
        if (matcher.matches())
        {
            String x = matcher.group(1);
            String of_y = matcher.group(2);
            return getPlural(x) + of_y;
        }

        /* get rid of indefinite articles ("a", "an"), but not definite articles ("the") */
        if (singular.startsWith("a "))
        {
            singular = singular.substring(2);
        }
        else if (singular.startsWith("an "))
        {
            singular = singular.substring(3);
        }

        /* Pluralize the last word (assume preceding words are adjectives) */
        String word, prefix;
        int space = singular.lastIndexOf(' ');
        if (space == -1)
        {
            prefix = "";
            word = singular;
        }
        else
        {
            prefix = singular.substring(0, space + 1);
            word = singular.substring(space + 1);
        }

        /* Check special cases */
        String exception = _specialCases.get(word.toLowerCase());
        if (exception != null)
        {
            // @TODO re-capitalize the word if required.
            if (Character.isUpperCase(word.charAt(0)))
            {
                return reconstruct(prefix, Character.toUpperCase(exception.charAt(0)) + exception.substring(1));
            }
            else
            {
                return reconstruct(prefix, exception);
            }
        }

        String[] singularEndings = { "x", "s", "ch", "sh", "fe", "ef", "f", "y", "us", "man", "is", "o" };
        String[] pluralEndings = { "xes", "ses", "ches", "shes", "ves", "efs", "ves", "ies", "i", "men", "es", "os" };
        if (singularEndings.length != pluralEndings.length)
        {
            throw new IllegalStateException("Suffix arrays don't match");
        }
        for (int i = 0; i < singularEndings.length; i++)
        {
            if (word.endsWith(singularEndings[i]))
            {
                return reconstruct(prefix, replaceSuffix(word, singularEndings[i], pluralEndings[i]));
            }
        }
        return reconstruct(prefix, word + "s");
    }

    private String replaceSuffix(String word, String originalSuffix, String newSuffix)
    {
        int end = word.length() - originalSuffix.length();
        return word.substring(0, end) + newSuffix;
    }

    private String reconstruct(String prefix, String exception)
    {
        return prefix + exception;
    }
}
