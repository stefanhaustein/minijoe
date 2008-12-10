// Copyright 2008 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.minijoe.compiler;

import com.google.minijoe.compiler.ast.BinaryOperatorExpression;
import com.google.minijoe.compiler.ast.ConditionalExpression;
import com.google.minijoe.compiler.ast.ExpressionStatement;
import com.google.minijoe.compiler.ast.Identifier;
import com.google.minijoe.compiler.ast.IncrementExpression;
import com.google.minijoe.compiler.ast.NumberLiteral;

/**
 * @author Andy Hayward
 */
public class ParserConditionalExpressionTest extends AbstractParserTest {
  public ParserConditionalExpressionTest() {
    super();
  }

  public ParserConditionalExpressionTest(String name) {
    super(name);
  }

  public void testConditionalExpression() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new ConditionalExpression(
                new BinaryOperatorExpression(
                    new NumberLiteral(1.0),
                    new NumberLiteral(0.0),
                    Token.OPERATOR_EQUALEQUAL
                ),
                new IncrementExpression(
                    new Identifier("foo"), 1, true
                ),
                new IncrementExpression(
                    new Identifier("bar"), 1, true
                )
            )
        ),
        "1 == 0 ? foo ++ : bar ++;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new ConditionalExpression(
                new Identifier("foo"),
                new ConditionalExpression(
                    new Identifier("a"),
                    new Identifier("b"),
                    new Identifier("c")
                ),
                new ConditionalExpression(
                    new Identifier("x"),
                    new Identifier("y"),
                    new Identifier("z")
                )
            )
        ),
        "foo ? a ? b : c : x ? y : z;"
    );
  }
}
