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

package us.terebi.net.core.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import us.terebi.net.core.FeatureSet;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public class Features implements FeatureSet
{
    private final PropertyListener< ? super Boolean> _listener;
    private final Set<String> _enabled;
    private final Set<String> _disabled;

    public Features(PropertyListener< ? super Boolean> listener)
    {
        _listener = listener;
        _enabled = new HashSet<String>();
        _disabled = new HashSet<String>();
    }

    public void disableFeature(String feature)
    {
        boolean wasEnabled = _enabled.remove(feature);
        boolean wasChanged = _disabled.add(feature);
        if (wasChanged)
        {
            _listener.propertyChanged(feature, wasEnabled ? Boolean.TRUE : null, Boolean.FALSE);
        }
    }

    public void enableFeature(String feature)
    {
        boolean wasDisabled = _disabled.remove(feature);
        boolean wasChanged = _enabled.add(feature);
        if (wasChanged)
        {
            _listener.propertyChanged(feature, wasDisabled ? Boolean.FALSE : null, Boolean.TRUE);
        }
    }

    public void removeFeature(String feature)
    {
        boolean wasEnabled = _enabled.remove(feature);
        boolean wasDisabled = _disabled.remove(feature);
        if (wasEnabled)
        {
            _listener.propertyRemoved(feature, Boolean.TRUE);
        }
        if (wasDisabled)
        {
            _listener.propertyRemoved(feature, Boolean.FALSE);
        }
    }

    public void setFeature(String feature, Boolean value)
    {
        if (value == null)
        {
            removeFeature(feature);
        }
        else if (value.booleanValue())
        {
            enableFeature(feature);
        }
        else
        {
            disableFeature(feature);
        }
    }

    public Set<String> getDefinedFeatures()
    {
        HashSet<String> set = new HashSet<String>(_enabled);
        set.addAll(_disabled);
        return Collections.unmodifiableSet(set);
    }

    public Set<String> getEnabledFeatures()
    {
        return Collections.unmodifiableSet(_enabled);
    }

    public Boolean getFeature(String feature)
    {
        if (_enabled.contains(feature))
        {
            return Boolean.TRUE;
        }
        if (_disabled.contains(feature))
        {
            return Boolean.FALSE;
        }
        return null;
    }

    public boolean isEnabled(String feature)
    {
        return _enabled.contains(feature);
    }

    public boolean isDefined(String feature)
    {
        return _enabled.contains(feature) || _disabled.contains(feature);
    }

}
