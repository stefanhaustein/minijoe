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

import com.google.minijoe.compiler.ast.AssignmentExpression;
import com.google.minijoe.compiler.ast.AssignmentOperatorExpression;
import com.google.minijoe.compiler.ast.BinaryOperatorExpression;
import com.google.minijoe.compiler.ast.ExpressionStatement;
import com.google.minijoe.compiler.ast.Identifier;
import com.google.minijoe.compiler.ast.NumberLiteral;

/**
 * @author Andy Hayward
 */
public class ParserAssignmentExpressionTest extends AbstractParserTest {
  public ParserAssignmentExpressionTest() {
    super();
  }

  public ParserAssignmentExpressionTest(String name) {
    super(name);
  }

  public void testAssignmentExpression() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new AssignmentExpression(
                new Identifier("foo"),
                new Identifier("bar")
            )
        ),
        "foo = bar;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new AssignmentExpression(
                new Identifier("foo"),
                new AssignmentExpression(
                    new Identifier("bar"),
                    new Identifier("baz")
                )
            )
        ),
        "foo = bar = baz;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new AssignmentExpression(
                new Identifier("foo"),
                new BinaryOperatorExpression(
                    new Identifier("bar"),
                    new NumberLiteral(1.0),
                    Token.OPERATOR_PLUS
                )
            )
        ),
        "foo = bar + 1;"
    );
  }

  public void testMultiplyAssignmentExpression() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new AssignmentOperatorExpression(
                new Identifier("foo"),
                new Identifier("bar"),
                Token.OPERATOR_MULTIPLYASSIGNMENT
            )
        ),
        "foo *= bar;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new AssignmentOperatorExpression(
                new Identifier("foo"),
                new AssignmentOperatorExpression(
                    new Identifier("bar"),
                    new Identifier("baz"),
                    Token.OPERATOR_MULTIPLYASSIGNMENT
                ),
                Token.OPERATOR_MULTIPLYASSIGNMENT
            )
        ),
        "foo *= bar *= baz;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new AssignmentOperatorExpression(
                new Identifier("foo"),
                new BinaryOperatorExpression(
                    new Identifier("bar"),
                    new NumberLiteral(1.0),
                    Token.OPERATOR_PLUS
                ),
                Token.OPERATOR_MULTIPLYASSIGNMENT
            )
        ),
        "foo *= bar + 1;"
    );
  }

  public void testDivideAssignmentExpression() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new AssignmentOperatorExpression(
                new Identifier("foo"),
                new Identifier("bar"),
                Token.OPERATOR_DIVIDEASSIGNMENT
            )
        ),
        "foo /= bar;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new AssignmentOperatorExpression(
                new Identifier("foo"),
                new AssignmentOperatorExpression(
                    new Identifier("bar"),
                    new Identifier("baz"),
                    Token.OPERATOR_DIVIDEASSIGNMENT
                ),
                Token.OPERATOR_DIVIDEASSIGNMENT
            )
        ),
        "foo /= bar /= baz;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new AssignmentOperatorExpression(
                new Identifier("foo"),
                new BinaryOperatorExpression(
                    new Identifier("bar"),
                    new NumberLiteral(1.0),
                    Token.OPERATOR_PLUS
                ),
                Token.OPERATOR_DIVIDEASSIGNMENT
            )
        ),
        "foo /= bar + 1;"
    );
  }

  public void testModuloAssignmentExpression() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new AssignmentOperatorExpression(
                new Identifier("foo"),
                new Identifier("bar"),
                Token.OPERATOR_MODULOASSIGNMENT
            )
        ),
        "foo %= bar;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new AssignmentOperatorExpression(
                new Identifier("foo"),
                new AssignmentOperatorExpression(
                    new Identifier("bar"),
                    new Identifier("baz"),
                    Token.OPERATOR_MODULOASSIGNMENT
                ),
                Token.OPERATOR_MODULOASSIGNMENT
            )
        ),
        "foo %= bar %= baz;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new AssignmentOperatorExpression(
                new Identifier("foo"),
                new BinaryOperatorExpression(
                    new Identifier("bar"),
                    new NumberLiteral(1.0),
                    Token.OPERATOR_PLUS
                ),
                Token.OPERATOR_MODULOASSIGNMENT
            )
        ),
        "foo %= bar + 1;"
    );
  }

  public void testAdditionAssignmentExpression() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new AssignmentOperatorExpression(
                new Identifier("foo"),
                new Identifier("bar"),
                Token.OPERATOR_PLUSASSIGNMENT
            )
        ),
        "foo += bar;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new AssignmentOperatorExpression(
                new Identifier("foo"),
                new AssignmentOperatorExpression(
                    new Identifier("bar"),
                    new Identifier("baz"),
                    Token.OPERATOR_PLUSASSIGNMENT
                ),
                Token.OPERATOR_PLUSASSIGNMENT
            )
        ),
        "foo += bar += baz;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new AssignmentOperatorExpression(
                new Identifier("foo"),
                new BinaryOperatorExpression(
                    new Identifier("bar"),
                    new NumberLiteral(1.0),
                    Token.OPERATOR_PLUS
                ),
                Token.OPERATOR_PLUSASSIGNMENT
            )
        ),
        "foo += bar + 1;"
    );
  }

  public void testSubtractionAssignmentExpression() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new AssignmentOperatorExpression(
                new Identifier("foo"),
                new Identifier("bar"),
                Token.OPERATOR_MINUSASSIGNMENT
            )
        ),
        "foo -= bar;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new AssignmentOperatorExpression(
                new Identifier("foo"),
                new AssignmentOperatorExpression(
                    new Identifier("bar"),
                    new Identifier("baz"),
                    Token.OPERATOR_MINUSASSIGNMENT
                ),
                Token.OPERATOR_MINUSASSIGNMENT
            )
        ),
        "foo -= bar -= baz;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new AssignmentOperatorExpression(
                new Identifier("foo"),
                new BinaryOperatorExpression(
                    new Identifier("bar"),
                    new NumberLiteral(1.0),
                    Token.OPERATOR_PLUS
                ),
                Token.OPERATOR_MINUSASSIGNMENT
            )
        ),
        "foo -= bar + 1;"
    );
  }

  public void testShiftLeftAssignmentExpression() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new AssignmentOperatorExpression(
                new Identifier("foo"),
                new Identifier("bar"),
                Token.OPERATOR_SHIFTLEFTASSIGNMENT
            )
        ),
        "foo <<= bar;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new AssignmentOperatorExpression(
                new Identifier("foo"),
                new AssignmentOperatorExpression(
                    new Identifier("bar"),
                    new Identifier("baz"),
                    Token.OPERATOR_SHIFTLEFTASSIGNMENT
                ),
                Token.OPERATOR_SHIFTLEFTASSIGNMENT
            )
        ),
        "foo <<= bar <<= baz;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new AssignmentOperatorExpression(
                new Identifier("foo"),
                new BinaryOperatorExpression(
                    new Identifier("bar"),
                    new NumberLiteral(1.0),
                    Token.OPERATOR_PLUS
                ),
                Token.OPERATOR_SHIFTLEFTASSIGNMENT
            )
        ),
        "foo <<= bar + 1;"
    );
  }

  public void testShiftRightAssignmentExpression() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new AssignmentOperatorExpression(
                new Identifier("foo"),
                new Identifier("bar"),
                Token.OPERATOR_SHIFTRIGHTASSIGNMENT
            )
        ),
        "foo >>= bar;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new AssignmentOperatorExpression(
                new Identifier("foo"),
                new AssignmentOperatorExpression(
                    new Identifier("bar"),
                    new Identifier("baz"),
                    Token.OPERATOR_SHIFTRIGHTASSIGNMENT
                ),
                Token.OPERATOR_SHIFTRIGHTASSIGNMENT
            )
        ),
        "foo >>= bar >>= baz;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new AssignmentOperatorExpression(
                new Identifier("foo"),
                new BinaryOperatorExpression(
                    new Identifier("bar"),
                    new NumberLiteral(1.0),
                    Token.OPERATOR_PLUS
                ),
                Token.OPERATOR_SHIFTRIGHTASSIGNMENT
            )
        ),
        "foo >>= bar + 1;"
    );
  }

  public void testShiftRightUnsignedExpression() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new AssignmentOperatorExpression(
                new Identifier("foo"),
                new Identifier("bar"),
                Token.OPERATOR_SHIFTRIGHTUNSIGNEDASSIGNMENT
            )
        ),
        "foo >>>= bar;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new AssignmentOperatorExpression(
                new Identifier("foo"),
                new AssignmentOperatorExpression(
                    new Identifier("bar"),
                    new Identifier("baz"),
                    Token.OPERATOR_SHIFTRIGHTUNSIGNEDASSIGNMENT

                ),
                Token.OPERATOR_SHIFTRIGHTUNSIGNEDASSIGNMENT
            )
        ),
        "foo >>>= bar >>>= baz;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new AssignmentOperatorExpression(
                new Identifier("foo"),
                new BinaryOperatorExpression(
                    new Identifier("bar"),
                    new NumberLiteral(1.0),
                    Token.OPERATOR_PLUS
                ),
                Token.OPERATOR_SHIFTRIGHTUNSIGNEDASSIGNMENT

            )
        ),
        "foo >>>= bar + 1;"
    );
  }

  public void testBitwiseAndAssignmentExpression() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new AssignmentOperatorExpression(
                new Identifier("foo"),
                new Identifier("bar"),
                Token.OPERATOR_BITWISEANDASSIGNMENT
            )
        ),
        "foo &= bar;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new AssignmentOperatorExpression(
                new Identifier("foo"),
                new AssignmentOperatorExpression(
                    new Identifier("bar"),
                    new Identifier("baz"),
                    Token.OPERATOR_BITWISEANDASSIGNMENT
                ),
                Token.OPERATOR_BITWISEANDASSIGNMENT

            )
        ),
        "foo &= bar &= baz;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new AssignmentOperatorExpression(
                new Identifier("foo"),
                new BinaryOperatorExpression(
                    new Identifier("bar"),
                    new NumberLiteral(1.0),
                    Token.OPERATOR_PLUS
                ),
                Token.OPERATOR_BITWISEANDASSIGNMENT

            )
        ),
        "foo &= bar + 1;"
    );
  }

  public void testBitwiseOrAssignmentExpression() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new AssignmentOperatorExpression(
                new Identifier("foo"),
                new Identifier("bar"),
                Token.OPERATOR_BITWISEORASSIGNMENT
            )
        ),
        "foo |= bar;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new AssignmentOperatorExpression(
                new Identifier("foo"),
                new AssignmentOperatorExpression(
                    new Identifier("bar"),
                    new Identifier("baz"),
                    Token.OPERATOR_BITWISEORASSIGNMENT
                ),
                Token.OPERATOR_BITWISEORASSIGNMENT
            )
        ),
        "foo |= bar |= baz;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new AssignmentOperatorExpression(
                new Identifier("foo"),
                new BinaryOperatorExpression(
                    new Identifier("bar"),
                    new NumberLiteral(1.0),
                    Token.OPERATOR_PLUS
                ),
                Token.OPERATOR_BITWISEORASSIGNMENT
            )
        ),
        "foo |= bar + 1;"
    );
  }

  public void testBitwiseXorAssignmentExpression() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new AssignmentOperatorExpression(
                new Identifier("foo"),
                new Identifier("bar"),
                Token.OPERATOR_BITWISEXORASSIGNMENT
            )
        ),
        "foo ^= bar;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new AssignmentOperatorExpression(
                new Identifier("foo"),
                new AssignmentOperatorExpression(
                    new Identifier("bar"),
                    new Identifier("baz"),
                    Token.OPERATOR_BITWISEXORASSIGNMENT
                ),
                Token.OPERATOR_BITWISEXORASSIGNMENT
            )
        ),
        "foo ^= bar ^= baz;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new AssignmentOperatorExpression(
                new Identifier("foo"),
                new BinaryOperatorExpression(
                    new Identifier("bar"),
                    new NumberLiteral(1.0),
                    Token.OPERATOR_PLUS
                ),
                Token.OPERATOR_BITWISEXORASSIGNMENT
            )
        ),
        "foo ^= bar + 1;"
    );
  }
}
