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

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public class ConfigNames
{
    public static final String MUDLIB_ROOT = "mudlib.root";
    public static final String MASTER_OBJECT = "mudlib.master";
    public static final String SEFUN_OBJECT = "mudlib.sefun";

    public static final String COMPILE_OUTPUT = "compile.output";
    public static final String COMPILE_INCLUDE_DIRECTORIES = "compile.include.directories";
    public static final String COMPILE_AUTO_INCLUDE = "compile.include.auto";
    public static final String COMPILE_DEBUG = "compile.debug";

    public static final String TELNET_PORT = "telnet.ports";

    public static final String MAX_EVAL_TIME_MILLIS = "max.eval.time";
    public static final String MAX_EVAL_TIME_INIT = "max.eval.time.init";

    public static final String SAVE_EXTENSION = "save.extension.default";
    public static final String SAVE_ADD_EXTENSION = "save.extension.add";
    public static final String SAVE_ENFORCE_EXTENSION = "save.extension.enforce";
    public static final String SAVE_DISALLOW_EXTENSIONS = "save.extension.disallow";
}
