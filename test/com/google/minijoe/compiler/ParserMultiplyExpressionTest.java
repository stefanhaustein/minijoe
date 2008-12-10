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
import com.google.minijoe.compiler.ast.IncrementExpression;

/**
 * @author Andy Hayward
 */
public class ParserMultiplyExpressionTest extends AbstractParserTest {
  public ParserMultiplyExpressionTest() {
    super();
  }

  public ParserMultiplyExpressionTest(String name) {
    super(name);
  }

  public void testMultiplyExpression() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new BinaryOperatorExpression(
                new Identifier("foo"),
                new Identifier("bar"),
                Token.OPERATOR_MULTIPLY
            )
        ),
        "foo * bar;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new BinaryOperatorExpression(
                new BinaryOperatorExpression(
                    new Identifier("foo"),
                    new Identifier("bar"),
                    Token.OPERATOR_MULTIPLY
                ),
                new Identifier("baz"),
                Token.OPERATOR_MULTIPLY
            )
        ),
        "foo * bar * baz;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new BinaryOperatorExpression(
                new Identifier("foo"),
                new IncrementExpression(
                    new Identifier("bar"), 1, false
                ),
                Token.OPERATOR_MULTIPLY
            )
        ),
        "foo * ++ bar;"
    );
  }

  public void testDivideExpression() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new BinaryOperatorExpression(
                new Identifier("foo"),
                new Identifier("bar"),
                Token.OPERATOR_DIVIDE
            )
        ),
        "foo / bar;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new BinaryOperatorExpression(
                new BinaryOperatorExpression(
                    new Identifier("foo"),
                    new Identifier("bar"),
                    Token.OPERATOR_DIVIDE
                ),
                new Identifier("baz"),
                Token.OPERATOR_DIVIDE
            )
        ),
        "foo / bar / baz;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new BinaryOperatorExpression(
                new Identifier("foo"),
                new IncrementExpression(
                    new Identifier("bar"), 1, false
                ),
                Token.OPERATOR_DIVIDE
            )
        ),
        "foo / ++ bar;"
    );
  }

  public void testModuloExpression() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new BinaryOperatorExpression(
                new Identifier("foo"),
                new Identifier("bar"),
                Token.OPERATOR_MODULO
            )
        ),
        "foo % bar;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new BinaryOperatorExpression(
                new BinaryOperatorExpression(
                    new Identifier("foo"),
                    new Identifier("bar"),
                    Token.OPERATOR_MODULO
                ),
                new Identifier("baz"),
                Token.OPERATOR_MODULO
            )
        ),
        "foo % bar % baz;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new BinaryOperatorExpression(
                new Identifier("foo"),
                new IncrementExpression(
                    new Identifier("bar"), 1, false
                ),
                Token.OPERATOR_MODULO
            )
        ),
        "foo % ++ bar;"
    );
  }
}
