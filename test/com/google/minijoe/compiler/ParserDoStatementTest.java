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
import com.google.minijoe.compiler.ast.DoStatement;
import com.google.minijoe.compiler.ast.ExpressionStatement;
import com.google.minijoe.compiler.ast.Identifier;
import com.google.minijoe.compiler.ast.Statement;

/**
 * @author Andy Hayward
 */
public class ParserDoStatementTest extends AbstractParserTest {
  public ParserDoStatementTest() {
    super();
  }

  public ParserDoStatementTest(String name) {
    super(name);
  }

  public void testDoStatement() throws CompilerException {
    assertParserOutput(
        new DoStatement(
            new ExpressionStatement(
                new Identifier("something")
            ),
            new BooleanLiteral(true)
        ),
        "do something; while (true);"
    );
    assertParserOutput(
        new DoStatement(
            new BlockStatement(
                new Statement[] {
                    new ExpressionStatement(
                        new Identifier("something")
                    )
                }
            ),
            new BooleanLiteral(true)
        ),
        "do {something;} while (true);"
    );
  }
}
