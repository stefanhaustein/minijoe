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

import com.google.minijoe.compiler.ast.BlockStatement;
import com.google.minijoe.compiler.ast.BooleanLiteral;
import com.google.minijoe.compiler.ast.ExpressionStatement;
import com.google.minijoe.compiler.ast.Identifier;
import com.google.minijoe.compiler.ast.Statement;
import com.google.minijoe.compiler.ast.WhileStatement;

/**
 * @author Andy Hayward
 */
public class ParserWhileStatementTest extends AbstractParserTest {
  public ParserWhileStatementTest() {
    super();
  }

  public ParserWhileStatementTest(String name) {
    super(name);
  }

  public void testWhileStatement() throws CompilerException {
    assertParserOutput(
        new WhileStatement(
            new BooleanLiteral(true),
            new ExpressionStatement(
                new Identifier("something")
            )
        ),
        "while (true) something;"
    );
    assertParserOutput(
        new WhileStatement(
            new BooleanLiteral(true),
            new BlockStatement(
                new Statement[] {
                    new ExpressionStatement(
                        new Identifier("something")
                    )
                }
            )
        ),
        "while (true) {something;}"
    );
  }
}
