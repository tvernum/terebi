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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import us.terebi.lang.lpc.compiler.CompileException;
import us.terebi.lang.lpc.compiler.util.MathLength;
import us.terebi.lang.lpc.parser.ast.PragmaNode;
import us.terebi.lang.lpc.parser.jj.Token;

/**
 * 
 */
public class CompileOptions
{
    private static final Pattern MATH = Pattern.compile("^math\\s*=\\s*(\\d+)$");

    private MathLength _math = MathLength.MATH_64_BIT;

    public MathLength getMath()
    {
        return _math;
    }

    public void setMath(MathLength type)
    {
        _math = type;
    }

    public void pragma(Token token, String pragma)
    {
        Matcher math = MATH.matcher(pragma);
        if (math.matches())
        {
            int bits = Integer.parseInt(math.group(1));
            if (bits == 32)
            {
                this.setMath(MathLength.MATH_32_BIT);
            }
            else if (bits == 64)
            {
                this.setMath(MathLength.MATH_64_BIT);
            }
            else
            {
                throw new CompileException(token, "Cannot support " + bits + " bit math");
            }
        }
    }

    public void processPragmas(PragmaNode node)
    {
        for (List< ? extends Token> list : node.getPragmas())
        {
            for (Token token : list)
            {
                this.pragma(token, token.image.trim());
            }
        }

    }

}
