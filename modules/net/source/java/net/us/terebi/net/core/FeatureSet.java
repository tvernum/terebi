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

package us.terebi.net.core;

import java.util.Set;

/**
 * @author <a href="http://blog.adjective.org/">Tim Vernum</a>
 */
public interface FeatureSet
{
    public boolean isEnabled(String feature);
    public void enableFeature(String feature);
    public void disableFeature(String feature);
    public void removeFeature(String feature);

    public Set<String> getEnabledFeatures();
    public Set<String> getDefinedFeatures();
    
    public boolean isDefined(String feature);
    public Boolean getFeature(String feature);
    public void setFeature(String feature, Boolean value);
}
