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
import com.google.minijoe.compiler.ast.ExpressionStatement;
import com.google.minijoe.compiler.ast.Identifier;
import com.google.minijoe.compiler.ast.Statement;
import com.google.minijoe.compiler.ast.TryStatement;

/**
 * @author Andy Hayward
 */
public class ParserTryStatementTest extends AbstractParserTest {
  //
  // TryStatement    : 'try' BlockStatement CatchStatement
  //                 | 'try' BlockStatement FinallyStatement
  //                 | 'try' BlockStatement CatchStatement FinallyStatement
  //
  // BlockStatement  : '{' [Statement_opt]* '}'
  //
  // CatchStatement  : 'catch' '(' Identifier ')' BlockStatement
  //
  // FinallyStatement: 'finally' BlockStatement
  //

  public ParserTryStatementTest() {
    super();
  }

  public ParserTryStatementTest(String name) {
    super(name);
  }

  public void testTryCatchStatement() throws CompilerException {
    assertParserOutput(
        new TryStatement(
            new BlockStatement(
                new Statement[] {
                    new ExpressionStatement(
                        new Identifier("something")
                    )
                }
            ),
            new Identifier("foo"),
            new BlockStatement(
                new Statement[] {
                    new ExpressionStatement(
                        new Identifier("bar")
                    )
                }
            ),
            null
        ),
        "try {something;} catch (foo) {bar;}"
    );
    assertParserOutput(
        new TryStatement(
            new BlockStatement(
                new Statement[] {
                    new ExpressionStatement(
                        new Identifier("something")
                    )
                }
            ),
            null,
            null,
            new BlockStatement(
                new Statement[] {
                    new ExpressionStatement(
                        new Identifier("baz")
                    )
                }
            )
        ),
        "try {something;} finally {baz;}"
    );
    assertParserOutput(
        new TryStatement(
            new BlockStatement(
                new Statement[] {
                    new ExpressionStatement(
                        new Identifier("something")
                    )
                }
            ),
            new Identifier("foo"),
            new BlockStatement(
                new Statement[] {
                    new ExpressionStatement(
                        new Identifier("bar")
                    )
                }
            ),
            new BlockStatement(
                new Statement[] {
                    new ExpressionStatement(
                        new Identifier("baz")
                    )
                }
            )
        ),
        "try {something;} catch (foo) {bar;} finally {baz;}"
    );
  }
}
