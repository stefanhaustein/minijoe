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
public class ParserRelationalExpressionTest extends AbstractParserTest {
  public ParserRelationalExpressionTest() {
    super();
  }

  public ParserRelationalExpressionTest(String name) {
    super(name);
  }

  public void testLessThanExpression() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new BinaryOperatorExpression(
                new Identifier("foo"),
                new Identifier("bar"),
                Token.OPERATOR_LESSTHAN
            )
        ),
        "foo < bar;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new BinaryOperatorExpression(
                new BinaryOperatorExpression(
                    new Identifier("foo"),
                    new Identifier("bar"),
                    Token.OPERATOR_LESSTHAN
                ),
                new Identifier("baz"),
                Token.OPERATOR_LESSTHAN
            )
        ),
        "foo < bar < baz;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new BinaryOperatorExpression(
                new Identifier("foo"),
                new BinaryOperatorExpression(
                    new Identifier("bar"),
                    new NumberLiteral(1.0),
                    Token.OPERATOR_PLUS
                ),
                Token.OPERATOR_LESSTHAN
            )
        ),
        "foo < bar + 1;"
    );
  }

  public void testLessThanOrEqualExpression() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new BinaryOperatorExpression(
                new Identifier("foo"),
                new Identifier("bar"),
                Token.OPERATOR_LESSTHANOREQUAL
            )
        ),
        "foo <= bar;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new BinaryOperatorExpression(
                new BinaryOperatorExpression(
                    new Identifier("foo"),
                    new Identifier("bar"),
                    Token.OPERATOR_LESSTHANOREQUAL
                ),
                new Identifier("baz"),
                Token.OPERATOR_LESSTHANOREQUAL
            )
        ),
        "foo <= bar <= baz;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new BinaryOperatorExpression(
                new Identifier("foo"),
                new BinaryOperatorExpression(
                    new Identifier("bar"),
                    new NumberLiteral(1.0),
                    Token.OPERATOR_PLUS
                ),
                Token.OPERATOR_LESSTHANOREQUAL
            )
        ),
        "foo <= bar + 1;"
    );
  }

  public void testGreaterThanExpression() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new BinaryOperatorExpression(
                new Identifier("foo"),
                new Identifier("bar"),
                Token.OPERATOR_GREATERTHAN
            )
        ),
        "foo > bar;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new BinaryOperatorExpression(
                new BinaryOperatorExpression(
                    new Identifier("foo"),
                    new Identifier("bar"),
                    Token.OPERATOR_GREATERTHAN
                ),
                new Identifier("baz"),
                Token.OPERATOR_GREATERTHAN
            )
        ),
        "foo > bar > baz;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new BinaryOperatorExpression(
                new Identifier("foo"),
                new BinaryOperatorExpression(
                    new Identifier("bar"),
                    new NumberLiteral(1.0),
                    Token.OPERATOR_PLUS
                ),
                Token.OPERATOR_GREATERTHAN
            )
        ),
        "foo > bar + 1;"
    );
  }

  public void testGreaterThanOrEqualExpression() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new BinaryOperatorExpression(
                new Identifier("foo"),
                new Identifier("bar"),
                Token.OPERATOR_GREATERTHANOREQUAL
            )
        ),
        "foo >= bar;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new BinaryOperatorExpression(
                new BinaryOperatorExpression(
                    new Identifier("foo"),
                    new Identifier("bar"),
                    Token.OPERATOR_GREATERTHANOREQUAL
                ),
                new Identifier("baz"),
                Token.OPERATOR_GREATERTHANOREQUAL
            )
        ),
        "foo >= bar >= baz;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new BinaryOperatorExpression(
                new Identifier("foo"),
                new BinaryOperatorExpression(
                    new Identifier("bar"),
                    new NumberLiteral(1.0),
                    Token.OPERATOR_PLUS
                ),
                Token.OPERATOR_GREATERTHANOREQUAL
            )
        ),
        "foo >= bar + 1;"
    );
  }

  public void testInstanceOfExpression() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new BinaryOperatorExpression(
                new Identifier("foo"),
                new Identifier("Object"),
                Token.KEYWORD_INSTANCEOF
            )
        ),
        "foo instanceof Object;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new BinaryOperatorExpression(
                new BinaryOperatorExpression(
                    new Identifier("foo"),
                    new Identifier("bar"),
                    Token.KEYWORD_INSTANCEOF
                ),
                new Identifier("baz"),
                Token.KEYWORD_INSTANCEOF
            )
        ),
        "foo instanceof bar instanceof baz;"
    );
  }

  public void testInExpression() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new BinaryOperatorExpression(
                new Identifier("foo"),
                new Identifier("bar"),
                Token.KEYWORD_IN
            )
        ),
        "foo in bar;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new BinaryOperatorExpression(
                new BinaryOperatorExpression(
                    new Identifier("foo"),
                    new Identifier("bar"),
                    Token.KEYWORD_IN
                ),
                new Identifier("baz"),
                Token.KEYWORD_IN
            )
        ),
        "foo in bar in baz;"
    );
  }
}
