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

package us.terebi.lang.lpc.compiler.java.context;

import java.util.List;

import org.adjective.stout.operation.Expression;

import us.terebi.lang.lpc.runtime.ArgumentDefinition;
import us.terebi.lang.lpc.runtime.ArgumentSemantics;
import us.terebi.lang.lpc.runtime.LpcType;
import us.terebi.lang.lpc.runtime.ObjectDefinition;

/**
 * 
 */
public interface VariableResolver
{
    public interface VariableResolution
    {
        public enum Kind
        {
            FIELD, PARAMETER, REF, LOCAL, INTERNAL, ENCLOSING;
        }
        
        public VariableResolution.Kind kind();
        public String lpcName();
        public String internalName();
        public LpcType type();
        public Expression access();
    }

    public void pushScope();
    public void popScope();

    public VariableResolution findVariable(String name);
    public VariableResolution getVariableInFrame(String name);

    public String allocateInternalVariableName();
    
    public void addInherit(String name, ObjectDefinition parent);
    
    public VariableResolution declareLocal(String name, LpcType type);

    public VariableResolution declareParameter(String name, ArgumentSemantics semantics, LpcType type);
    public VariableResolution[] declareParameters(List< ? extends ArgumentDefinition> args);

    public VariableResolution declareField(String name, LpcType type);
    public VariableResolution declareField(String name, String internalName, LpcType type);

    public VariableResolution declareEnclosing(String name, String internalName, LpcType type);
    public VariableResolution declareInternal(String name, LpcType type);

}
