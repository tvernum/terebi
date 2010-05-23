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

package us.terebi.plugins.parser;

import java.util.Properties;

import us.terebi.engine.config.Config;
import us.terebi.engine.plugin.AbstractPlugin;
import us.terebi.engine.plugin.Plugin;
import us.terebi.lang.lpc.runtime.jvm.LpcConstants;
import us.terebi.lang.lpc.runtime.jvm.context.Efuns;
import us.terebi.lang.lpc.runtime.jvm.context.SystemContext;
import us.terebi.lang.lpc.runtime.jvm.efun.NoOpEfun;
import us.terebi.plugins.parser.efun.ParseAddRuleEfun;
import us.terebi.plugins.parser.efun.ParseAddSynonymEfun;
import us.terebi.plugins.parser.efun.ParseMyRulesEfun;
import us.terebi.plugins.parser.efun.ParseRemoveEfun;
import us.terebi.plugins.parser.efun.ParseSentenceEfun;
import us.terebi.plugins.parser.efun.VoidEfun;

/**
 * @version $Revision$
 */
public class ParserPlugin extends AbstractPlugin implements Plugin
{
    public void load(Config config, SystemContext context, Properties properties)
    {
        Efuns efuns = context.efuns();
        efuns.define("parse_init", new VoidEfun());
        efuns.define("parse_refresh", new VoidEfun());
        efuns.define("parse_sentence", new ParseSentenceEfun());
        efuns.define("parse_add_rule", new ParseAddRuleEfun());
        efuns.define("parse_remove", new ParseRemoveEfun());
        efuns.define("parse_add_synonym", new ParseAddSynonymEfun());
        efuns.define("parse_dump", new NoOpEfun(LpcConstants.STRING.BLANK));
        efuns.define("parse_my_rules", new ParseMyRulesEfun());
    }
}
