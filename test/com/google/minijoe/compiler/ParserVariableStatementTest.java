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
import com.google.minijoe.compiler.ast.Identifier;
import com.google.minijoe.compiler.ast.NumberLiteral;
import com.google.minijoe.compiler.ast.VariableDeclaration;
import com.google.minijoe.compiler.ast.VariableStatement;

/**
 * @author Andy Hayward
 */
public class ParserVariableStatementTest extends AbstractParserTest {
  //
  // VariableStatement : 'var' VariableDeclaration+
  //
  // VariableDeclaration : Identifier [ '=' AssignmentExpression ]
  //

  public ParserVariableStatementTest() {
    super();
  }

  public ParserVariableStatementTest(String name) {
    super(name);
  }

  public void testVariableStatement() throws CompilerException {
    assertParserOutput(
        new VariableStatement(
            new VariableDeclaration[] {
                new VariableDeclaration(
                    new Identifier("foo"),
                    null
                )
            }
        ),
        "var foo;"
    );
    assertParserOutput(
        new VariableStatement(
            new VariableDeclaration[] {
                new VariableDeclaration(
                    new Identifier("foo"),
                    new NumberLiteral(1.0)
                )
            }
        ),
        "var foo = 1.0;"
    );
    assertParserOutput(
        new VariableStatement(
            new VariableDeclaration[] {
                new VariableDeclaration(
                    new Identifier("bar"),
                    new BinaryOperatorExpression(
                        new Identifier("x"),
                        new Identifier("baz"),
                        Token.KEYWORD_IN
                    )
                )
            }
        ),
        "var bar = x in baz;"
    );
    assertParserOutput(
        new VariableStatement(
            new VariableDeclaration[] {
                new VariableDeclaration(
                    new Identifier("foo"),
                    null
                ),
                new VariableDeclaration(
                    new Identifier("bar"),
                    null
                )
            }
        ),
        "var foo, bar;"
    );
    assertParserOutput(
        new VariableStatement(
            new VariableDeclaration[] {
                new VariableDeclaration(
                    new Identifier("foo"),
                    new NumberLiteral(1.0)
                ),
                new VariableDeclaration(
                    new Identifier("bar"),
                    null
                )
            }
        ),
        "var foo = 1.0, bar;"
    );
    assertParserOutput(
        new VariableStatement(
            new VariableDeclaration[] {
                new VariableDeclaration(
                    new Identifier("foo"),
                    new NumberLiteral(1.0)
                ),
                new VariableDeclaration(
                    new Identifier("bar"),
                    new BinaryOperatorExpression(
                        new Identifier("x"),
                        new Identifier("baz"),
                        Token.KEYWORD_IN
                    )
                )
            }
        ),
        "var foo = 1.0, bar = x in baz;"
    );
  }
}
