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
import com.google.minijoe.compiler.ast.NumberLiteral;

/**
 * @author Andy Hayward
 */
public class ParserAdditionExpressionTest extends AbstractParserTest {
  public ParserAdditionExpressionTest() {
    super();
  }

  public ParserAdditionExpressionTest(String name) {
    super(name);
  }

  public void testAdditionExpression() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new BinaryOperatorExpression(
                new Identifier("foo"),
                new Identifier("bar"),
                Token.OPERATOR_PLUS
            )
        ),
        "foo + bar;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new BinaryOperatorExpression(
                new BinaryOperatorExpression(
                    new Identifier("foo"),
                    new Identifier("bar"),
                    Token.OPERATOR_PLUS
                ),
                new Identifier("baz"),
                Token.OPERATOR_PLUS
            )
        ),
        "foo + bar + baz;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new BinaryOperatorExpression(
                new Identifier("foo"),
                new BinaryOperatorExpression(
                    new Identifier("bar"),
                    new NumberLiteral(2.0),
                    Token.OPERATOR_MULTIPLY
                ),
                Token.OPERATOR_PLUS
            )
        ),
        "foo + bar * 2;"
    );
  }

  public void testSubtractionExpression() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new BinaryOperatorExpression(
                new Identifier("foo"),
                new Identifier("bar"),
                Token.OPERATOR_MINUS
            )
        ),
        "foo - bar;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new BinaryOperatorExpression(
                new BinaryOperatorExpression(
                    new Identifier("foo"),
                    new Identifier("bar"),
                    Token.OPERATOR_MINUS
                ),
                new Identifier("baz"),
                Token.OPERATOR_MINUS
            )
        ),
        "foo - bar - baz;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new BinaryOperatorExpression(
                new Identifier("foo"),
                new BinaryOperatorExpression(
                    new Identifier("bar"),
                    new NumberLiteral(2.0),
                    Token.OPERATOR_MULTIPLY
                ),
                Token.OPERATOR_MINUS
            )
        ),
        "foo - bar * 2;"
    );
  }
}
