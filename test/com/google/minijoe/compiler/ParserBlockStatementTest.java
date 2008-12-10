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
import com.google.minijoe.compiler.ast.BlockStatement;
import com.google.minijoe.compiler.ast.ExpressionStatement;
import com.google.minijoe.compiler.ast.Identifier;
import com.google.minijoe.compiler.ast.NumberLiteral;
import com.google.minijoe.compiler.ast.Statement;

/**
 * @author Andy Hayward
 */
public class ParserBlockStatementTest extends AbstractParserTest {
  public ParserBlockStatementTest() {
    super();
  }

  public ParserBlockStatementTest(String name) {
    super(name);
  }

  public void testBlockStatement() throws CompilerException {
    assertParserOutput(
        new BlockStatement(
            new Statement[] {}
        ),
        "{};"
    );
    assertParserOutput(
        new BlockStatement(
            new Statement[] {
                new ExpressionStatement(
                    new AssignmentExpression(
                        new Identifier("foo"),
                        new NumberLiteral(1.0)
                    )
                )
            }
        ),
        "{foo = 1.0;};"
    );
    assertParserOutput(
        new BlockStatement(
            new Statement[] {
                new ExpressionStatement(
                    new AssignmentExpression(
                        new Identifier("foo"),
                        new NumberLiteral(1.0)
                    )
                ),
                new ExpressionStatement(
                    new AssignmentExpression(
                        new Identifier("bar"),
                        new NumberLiteral(2.0)
                    )
                )
            }
        ),
        "{foo = 1.0; bar = 2.0;};"
    );
  }
}
