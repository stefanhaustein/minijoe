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
import com.google.minijoe.compiler.ast.ExpressionStatement;
import com.google.minijoe.compiler.ast.Identifier;

/**
 * @author Andy Hayward
 */
public class ParserCommaExpressionTest extends AbstractParserTest {
  public ParserCommaExpressionTest() {
    super();
  }

  public ParserCommaExpressionTest(String name) {
    super(name);
  }

  public void testCommaExpression() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new BinaryOperatorExpression(
                new BinaryOperatorExpression(
                    new BinaryOperatorExpression(
                        new BinaryOperatorExpression(
                            new Identifier("foo"),
                            new Identifier("bar"),
                            Token.OPERATOR_MULTIPLY
                        ),
                        new BinaryOperatorExpression(
                            new Identifier("foo"),
                            new Identifier("bar"),
                            Token.OPERATOR_DIVIDE
                        ),
                        Token.OPERATOR_COMMA
                    ),
                    new BinaryOperatorExpression(
                        new Identifier("foo"),
                        new Identifier("bar"),
                        Token.OPERATOR_PLUS
                    ),
                    Token.OPERATOR_COMMA
                ),
                new BinaryOperatorExpression(
                    new Identifier("foo"),
                    new Identifier("bar"),
                    Token.OPERATOR_MINUS
                ),
                Token.OPERATOR_COMMA
            )
        ),
        "foo * bar, foo / bar, foo + bar, foo - bar;"
    );
  }
}
