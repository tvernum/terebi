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

package us.terebi.lang.lpc.compiler.util;

import java.util.ArrayList;
import java.util.List;

import us.terebi.lang.lpc.compiler.java.context.ScopeLookup;
import us.terebi.lang.lpc.parser.ast.ASTArrayStar;
import us.terebi.lang.lpc.parser.ast.ASTIdentifier;
import us.terebi.lang.lpc.parser.ast.ASTType;
import us.terebi.lang.lpc.parser.ast.ASTVariable;
import us.terebi.lang.lpc.parser.ast.ASTVariableAssignment;
import us.terebi.lang.lpc.parser.ast.ASTVariableDeclaration;
import us.terebi.lang.lpc.parser.ast.Node;
import us.terebi.lang.lpc.parser.util.ASTUtil;
import us.terebi.lang.lpc.runtime.LpcType;

/**
 * 
 */
public class VariableSupport
{
    public class VariableDeclaration
    {
        private final ScopeLookup _scope;
        private final TypeSupport _type;
        private final String _name;
        private final ASTVariableAssignment _assignment;
        private final boolean _internal;

        public VariableDeclaration(ScopeLookup scope, ASTType typeNode, ASTVariable varNode, boolean internal)
        {
            _scope = scope;
            boolean array = ASTUtil.hasChildType(ASTArrayStar.class, varNode);
            _type = new TypeSupport(scope, typeNode, array);

            ASTIdentifier identifier = ASTUtil.getChild(ASTIdentifier.class, varNode);
            _name = identifier.jjtGetFirstToken().image;

            _assignment = ASTUtil.getChild(ASTVariableAssignment.class, varNode);
            _internal = internal;
        }

        public TypeSupport getTypeSupport()
        {
            return _type;
        }

        public String getName()
        {
            return _name;
        }

        public ASTVariableAssignment getAssignment()
        {
            return _assignment;
        }

        public boolean isInternal()
        {
            return _internal;
        }

        public void declare()
        {
            if (_internal)
            {

                _scope.variables().declareInternal(_name, getType());
            }
            else
            {
                _scope.variables().declareLocal(_name, getType());
            }
        }

        public LpcType getType()
        {
            return _type.getType();
        }
    }

    private final List<VariableDeclaration> _variables;

    public VariableSupport(ScopeLookup scope, ASTVariableDeclaration declaration)
    {
        _variables = new ArrayList<VariableDeclaration>();

        ASTType typeNode = ASTUtil.getChild(ASTType.class, declaration);
        assert typeNode != null;

        for (int i = 1; i < declaration.jjtGetNumChildren(); i++)
        {
            Node child = declaration.jjtGetChild(i);
            assert child instanceof ASTVariable;
            _variables.add(new VariableDeclaration(scope, typeNode, (ASTVariable) child, declaration.isInternal()));
        }
    }

    public Iterable<VariableDeclaration> getVariables()
    {
        return _variables;
    }

}
