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

package us.terebi.plugins.action.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import us.terebi.plugins.action.handler.ActionHandler.Action;

public class ActionSet
{
    private final Logger LOG = Logger.getLogger(ActionSet.class);

    private final List<Action> _actions;

    public ActionSet()
    {
        _actions = new ArrayList<Action>(30);
    }

    public void add(Action action)
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Adding " + action + " to " + this);
        }
        _actions.add(action);
    }

    public Iterable<Action> find(String verb)
    {
        if (verb == null)
        {
            return Collections.emptyList();
        }
        List<Action> actions = new ArrayList<Action>(5);
        for (Action action : _actions)
        {
            if (isMatch(action, verb))
            {
                actions.add(action);
            }
        }
        return actions;
    }

    private boolean isMatch(Action action, String verb)
    {
        if (action.verb.length() == 0)
        {
            return true;
        }
        switch (action.type)
        {
            case REGULAR:
                return (verb.equals(action.verb));
            default:
                return verb.startsWith(action.verb);
        }
    }
}
