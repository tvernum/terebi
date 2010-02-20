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

package us.terebi.engine.objects;

import us.terebi.engine.config.Config;
import us.terebi.engine.config.ConfigNames;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public class BehaviourOptions
{
    private static final String[] DEFAULT_DISALLOW_EXTENSIONS = new String[] { ".c" };

    public class SaveBehaviour
    {
        public final String extension;
        public final boolean addExtension;
        public final boolean enforceExtension;
        public final String[] disallowedExtensions;

        public SaveBehaviour(String ext, boolean addExt, boolean enforceExt, String[] disallowedExt)
        {
            this.extension = ext;
            this.addExtension = addExt;
            this.enforceExtension = enforceExt;
            this.disallowedExtensions = disallowedExt;
        }
    }

    private final SaveBehaviour _save;

    public BehaviourOptions(Config config)
    {
        String extention = config.getString(ConfigNames.SAVE_EXTENSION, ".dat");
        boolean add = config.getBoolean(ConfigNames.SAVE_ADD_EXTENSION, true);
        boolean enforce = config.getBoolean(ConfigNames.SAVE_ENFORCE_EXTENSION, false);
        String[] disallow = config.hasKey(ConfigNames.SAVE_DISALLOW_EXTENSIONS) ? config.getStrings(ConfigNames.SAVE_DISALLOW_EXTENSIONS)
                : DEFAULT_DISALLOW_EXTENSIONS;
        _save = new SaveBehaviour(extention, add, enforce, disallow);
    }
    
    public SaveBehaviour getSaveBehaviour()
    {
        return _save;
    }
}
