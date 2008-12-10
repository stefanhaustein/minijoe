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
import com.google.minijoe.compiler.ast.LogicalAndExpression;
import com.google.minijoe.compiler.ast.LogicalOrExpression;
import com.google.minijoe.compiler.ast.NumberLiteral;

/**
 * @author Andy Hayward
 */
public class ParserBinaryLogicalExpressionTest extends AbstractParserTest {
  public ParserBinaryLogicalExpressionTest() {
    super();
  }

  public ParserBinaryLogicalExpressionTest(String name) {
    super(name);
  }

  public void testLogicalAndExpression() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new LogicalAndExpression(
                new Identifier("foo"),
                new Identifier("bar")
            )
        ),
        "foo && bar;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new LogicalAndExpression(
                new LogicalAndExpression(
                    new Identifier("foo"),
                    new Identifier("bar")
                ),
                new Identifier("baz")
            )
        ),
        "foo && bar && baz;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new LogicalAndExpression(
                new Identifier("foo"),
                new BinaryOperatorExpression(
                    new Identifier("bar"),
                    new NumberLiteral(1.0),
                    Token.OPERATOR_EQUALEQUAL
                )
            )
        ),
        "foo && bar == 1;"
    );
  }

  public void testLogicalOrExpression() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new LogicalOrExpression(
                new Identifier("foo"),
                new Identifier("bar")
            )
        ),
        "foo || bar;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new LogicalOrExpression(
                new LogicalOrExpression(
                    new Identifier("foo"),
                    new Identifier("bar")
                ),
                new Identifier("baz")
            )
        ),
        "foo || bar || baz;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new LogicalOrExpression(
                new Identifier("foo"),
                new BinaryOperatorExpression(
                    new Identifier("bar"),
                    new NumberLiteral(1.0),
                    Token.OPERATOR_EQUALEQUAL
                )
            )
        ),
        "foo || bar == 1;"
    );
  }
}
