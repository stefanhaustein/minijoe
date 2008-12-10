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

import com.google.minijoe.compiler.ast.ArrayLiteral;
import com.google.minijoe.compiler.ast.EmptyStatement;
import com.google.minijoe.compiler.ast.Expression;
import com.google.minijoe.compiler.ast.ForInStatement;
import com.google.minijoe.compiler.ast.Identifier;
import com.google.minijoe.compiler.ast.NumberLiteral;
import com.google.minijoe.compiler.ast.PropertyExpression;
import com.google.minijoe.compiler.ast.StringLiteral;
import com.google.minijoe.compiler.ast.VariableDeclaration;

/**
 * @author Andy Hayward
 */
public class ParserForInStatementTest extends AbstractParserTest {
  //
  // ForStatement    : 'for' '(' Expression 'in' Expression ')' Statement
  // ForStatement    : 'for' '(' 'var' VariableDeclaration 'in' Expression ')' Statement
  //

  public ParserForInStatementTest() {
    super();
  }

  public ParserForInStatementTest(String name) {
    super(name);
  }

  public void testForInStatement() throws CompilerException {
    assertParserOutput(
        new ForInStatement(
            new Identifier("foo"),
            new ArrayLiteral(
                new Expression[] {
                    new NumberLiteral(1.0),
                    new NumberLiteral(2.0),
                    new NumberLiteral(3.0),
                    new NumberLiteral(4.0)
                }
            ),
            new EmptyStatement()
        ),
        "for (foo in [1, 2, 3, 4]);"
    );
    assertParserOutput(
        new ForInStatement(
            new PropertyExpression(
                new Identifier("foo"),
                new StringLiteral("bar")
            ),
            new ArrayLiteral(
                new Expression[] {
                    new NumberLiteral(1.0),
                    new NumberLiteral(2.0),
                    new NumberLiteral(3.0),
                    new NumberLiteral(4.0)
                }
            ),
            new EmptyStatement()
        ),
        "for (foo.bar in [1, 2, 3, 4]);"
    );
    assertParserOutput(
        new ForInStatement(
            new VariableDeclaration(
                new Identifier("foo"),
                null
            ),
            new ArrayLiteral(
                new Expression[] {
                    new NumberLiteral(1.0),
                    new NumberLiteral(2.0),
                    new NumberLiteral(3.0),
                    new NumberLiteral(4.0)
                }
            ),
            new EmptyStatement()
        ),
        "for (var foo in [1, 2, 3, 4]);"
    );
  }
}
