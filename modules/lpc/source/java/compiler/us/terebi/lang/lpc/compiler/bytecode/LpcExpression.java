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

package us.terebi.lang.lpc.compiler.bytecode;

import org.adjective.stout.operation.Expression;

import us.terebi.lang.lpc.runtime.LpcType;

/**
 * 
 */
public class LpcExpression
{
    public final LpcType type;
    public final Expression expression;
    public final boolean reference;

    public LpcExpression(LpcType exprType, Expression expr)
    {
        this(exprType, expr, false);
    }

    public LpcExpression(LpcType exprType, Expression expr, boolean ref)
    {
        this.type = exprType;
        this.expression = expr;
        this.reference = ref;
    }
    
    public String toString()
    {
        return getClass().getSimpleName() + "{" + type + (reference ? " ref " : " val ") + expression + "}";
    }
}
