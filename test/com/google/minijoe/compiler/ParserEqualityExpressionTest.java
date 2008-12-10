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
public class ParserEqualityExpressionTest extends AbstractParserTest {
  public ParserEqualityExpressionTest() {
    super();
  }

  public ParserEqualityExpressionTest(String name) {
    super(name);
  }

  public void testEqualsExpression() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new BinaryOperatorExpression(
                new Identifier("foo"),
                new Identifier("bar"),
                Token.OPERATOR_EQUALEQUAL
            )
        ),
        "foo == bar;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new BinaryOperatorExpression(
                new BinaryOperatorExpression(
                    new Identifier("foo"),
                    new Identifier("bar"),
                    Token.OPERATOR_EQUALEQUAL
                ),
                new Identifier("baz"),
                Token.OPERATOR_EQUALEQUAL
            )
        ),
        "foo == bar == baz;"
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
                Token.OPERATOR_EQUALEQUAL
            )
        ),
        "foo == bar + 1;"
    );
  }

  public void testNotEqualsExpression() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new BinaryOperatorExpression(
                new Identifier("foo"),
                new Identifier("bar"),
                Token.OPERATOR_NOTEQUAL
            )
        ),
        "foo != bar;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new BinaryOperatorExpression(
                new BinaryOperatorExpression(
                    new Identifier("foo"),
                    new Identifier("bar"),
                    Token.OPERATOR_NOTEQUAL
                ),
                new Identifier("baz"),
                Token.OPERATOR_NOTEQUAL
            )
        ),
        "foo != bar != baz;"
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
                Token.OPERATOR_NOTEQUAL
            )
        ),
        "foo != bar + 1;"
    );
  }

  public void testStrictEqualsExpression() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new BinaryOperatorExpression(
                new Identifier("foo"),
                new Identifier("bar"),
                Token.OPERATOR_EQUALEQUALEQUAL
            )
        ),
        "foo === bar;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new BinaryOperatorExpression(
                new BinaryOperatorExpression(
                    new Identifier("foo"),
                    new Identifier("bar"),
                    Token.OPERATOR_EQUALEQUALEQUAL
                ),
                new Identifier("baz"),
                Token.OPERATOR_EQUALEQUALEQUAL
            )
        ),
        "foo === bar === baz;"
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
                Token.OPERATOR_EQUALEQUALEQUAL
            )
        ),
        "foo === bar + 1;"
    );
  }

  public void testStrictNotEqualsExpression() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new BinaryOperatorExpression(
                new Identifier("foo"),
                new Identifier("bar"),
                Token.OPERATOR_NOTEQUALEQUAL
            )
        ),
        "foo !== bar;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new BinaryOperatorExpression(
                new BinaryOperatorExpression(
                    new Identifier("foo"),
                    new Identifier("bar"),
                    Token.OPERATOR_NOTEQUALEQUAL
                ),
                new Identifier("baz"),
                Token.OPERATOR_NOTEQUALEQUAL
            )
        ),
        "foo !== bar !== baz;"
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
                Token.OPERATOR_NOTEQUALEQUAL
            )
        ),
        "foo !== bar + 1;"
    );
  }
}
