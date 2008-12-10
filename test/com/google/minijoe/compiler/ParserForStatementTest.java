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
import com.google.minijoe.compiler.ast.BinaryOperatorExpression;
import com.google.minijoe.compiler.ast.EmptyStatement;
import com.google.minijoe.compiler.ast.ForStatement;
import com.google.minijoe.compiler.ast.Identifier;
import com.google.minijoe.compiler.ast.IncrementExpression;
import com.google.minijoe.compiler.ast.NumberLiteral;
import com.google.minijoe.compiler.ast.VariableDeclaration;
import com.google.minijoe.compiler.ast.VariableExpression;

/**
 * @author Andy Hayward
 */
public class ParserForStatementTest extends AbstractParserTest {
  //
  // ForStatement
  //   : 'for' '(' Expression_opt ';' Expression_opt ';' Expression_opt ')' Statement
  //   : 'for' '(' 'var' VariableDeclarationList ';' Expression_opt ';' Expression_opt ')' Statement
  //

  public ParserForStatementTest() {
    super();
  }

  public ParserForStatementTest(String name) {
    super(name);
  }

  public void testForStatement() throws CompilerException {

    assertParserOutput(
        new ForStatement(
            null,
            null,
            null,
            new EmptyStatement()
        ),
        "for (;;);"
    );
    assertParserOutput(
        new ForStatement(
            null,
            null,
            new IncrementExpression(
                new Identifier("x"),
                +1,
                true
            ),
            new EmptyStatement()
        ),
        "for (;; x++);"
    );
    assertParserOutput(
        new ForStatement(
            null,
            new BinaryOperatorExpression(
                new Identifier("x"),
                new NumberLiteral(4.0),
                Token.OPERATOR_LESSTHAN
            ),
            null,
            new EmptyStatement()
        ),
        "for (; x < 4;);"
    );
    assertParserOutput(
        new ForStatement(
            null,
            new BinaryOperatorExpression(
                new Identifier("x"),
                new NumberLiteral(4.0),
                Token.OPERATOR_LESSTHAN
            ),
            new IncrementExpression(
                new Identifier("x"), +1, true
            ),
            new EmptyStatement()
        ),
        "for (; x < 4; x++);"
    );
    assertParserOutput(
        new ForStatement(
            new AssignmentExpression(
                new Identifier("x"),
                new NumberLiteral(0.0)
            ),
            new BinaryOperatorExpression(
                new Identifier("x"),
                new NumberLiteral(4.0),
                Token.OPERATOR_LESSTHAN
            ),
            new IncrementExpression(
                new Identifier("x"), +1, true
            ),
            new EmptyStatement()
        ),
        "for (x = 0; x < 4; x++);"
    );
    assertParserOutput(
        new ForStatement(
            new VariableExpression(
                new VariableDeclaration[] {
                    new VariableDeclaration(
                        new Identifier("x"),
                        new NumberLiteral(0.0)
                    ),
                }
            ),
            new BinaryOperatorExpression(
                new Identifier("x"),
                new NumberLiteral(4.0),
                Token.OPERATOR_LESSTHAN
            ),
            new IncrementExpression(
                new Identifier("x"), +1, true
            ),
            new EmptyStatement()
        ),
        "for (var x = 0; x < 4; x++);"
    );
    assertParserOutput(
        new ForStatement(
            new VariableExpression(
                new VariableDeclaration[] {
                    new VariableDeclaration(
                        new Identifier("x"),
                        new NumberLiteral(0.0)
                    ),
                    new VariableDeclaration(
                        new Identifier("y"),
                        new NumberLiteral(8.0)
                    )
                }
            ),
            new BinaryOperatorExpression(
                new Identifier("x"),
                new NumberLiteral(4.0),
                Token.OPERATOR_LESSTHAN
            ),
            new BinaryOperatorExpression(
                new IncrementExpression(
                    new Identifier("x"), +1, true
                ),
                new IncrementExpression(
                    new Identifier("y"), -1, true
                ),
                Token.OPERATOR_COMMA
            ),
            new EmptyStatement()
        ),
        "for (var x = 0, y = 8; x < 4; x++, y--);"
    );
  }
}
