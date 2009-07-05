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

package us.terebi.lang.lpc.compiler.java.context;

/**
 * 
 */
public class ObjectId
{
    private final CompiledObjectDefinition _definition;
    private final long _id;

    public ObjectId(CompiledObjectInstance object)
    {
        this(object.getDefinition(), object.getId());
    }

    public ObjectId(CompiledObjectDefinition definition, long id)
    {
        _definition = definition;
        _id = id;
    }

    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_definition == null) ? 0 : _definition.hashCode());
        result = prime * result + (int) (_id ^ (_id >>> 32));
        return result;
    }

    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final ObjectId other = (ObjectId) obj;
        return (this._definition == other._definition && this._id == other._id);
    }

}
