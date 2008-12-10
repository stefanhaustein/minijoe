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

import com.google.minijoe.compiler.ast.CaseStatement;
import com.google.minijoe.compiler.ast.ExpressionStatement;
import com.google.minijoe.compiler.ast.Identifier;
import com.google.minijoe.compiler.ast.NumberLiteral;
import com.google.minijoe.compiler.ast.Statement;
import com.google.minijoe.compiler.ast.SwitchStatement;

/**
 * @author Andy Hayward
 */
public class ParserSwitchStatementTest extends AbstractParserTest {
  //
  // SwitchStatement  : 'switch' '(' Expression ')' CaseBlock
  //
  // CaseBlock        : '{' [CaseStatement]* [DefaultStatement] [CaseStatement]* '}'
  //
  // CaseStatement    : 'case' Expression ':' [Statement]*
  //
  // DefaultStatement : 'default' ':' [Statement]*
  //

  public ParserSwitchStatementTest() {
    super();
  }

  public ParserSwitchStatementTest(String name) {
    super(name);
  }

  public void testSwitchStatement() throws CompilerException {
    assertParserOutput(
        new SwitchStatement(
            new Identifier("something"),
            new CaseStatement[] {
            }
        ),
        "switch (something) {}"
    );
    assertParserOutput(
        new SwitchStatement(
            new Identifier("something"),
            new CaseStatement[] {
              new CaseStatement(
                  null,
                  new Statement[] {
                      new ExpressionStatement(
                          new Identifier("foo")
                      )
                  }
              )
            }
        ),
        "switch (something) {default: foo;}"
    );
    assertParserOutput(
        new SwitchStatement(
            new Identifier("something"),
            new CaseStatement[] {
              new CaseStatement(
                  new NumberLiteral(0),
                  new Statement[] {
                    new ExpressionStatement(
                        new Identifier("bar")
                    )
                  }
              )
            }
        ),
        "switch (something) {case 0: bar;}"
    );
    assertParserOutput(
        new SwitchStatement(
            new Identifier("something"),
            new CaseStatement[] {
              new CaseStatement(
                  new NumberLiteral(0),
                  new Statement[] {
                    new ExpressionStatement(
                        new Identifier("bar")
                    )
                  }
              ),
              new CaseStatement(
                  new NumberLiteral(1),
                  new Statement[] {
                    new ExpressionStatement(
                        new Identifier("baz")
                    )
                  }
              )
            }
        ),
        "switch (something) {case 0: bar; case 1: baz;}"
    );
    assertParserOutput(
        new SwitchStatement(
            new Identifier("something"),
            new CaseStatement[] {
              new CaseStatement(
                  null,
                  new Statement[] {
                      new ExpressionStatement(
                          new Identifier("foo")
                      )
                  }
              ),
              new CaseStatement(
                  new NumberLiteral(0),
                  new Statement[] {
                    new ExpressionStatement(
                        new Identifier("bar")
                    )
                  }
              ),
              new CaseStatement(
                  new NumberLiteral(1),
                  new Statement[] {
                    new ExpressionStatement(
                        new Identifier("baz")
                    )
                  }
              )
            }
        ),
        "switch (something) {default: foo; case 0: bar; case 1: baz;}"
    );
    assertParserOutput(
        new SwitchStatement(
            new Identifier("something"),
            new CaseStatement[] {
              new CaseStatement(
                  new NumberLiteral(0),
                  new Statement[] {
                    new ExpressionStatement(
                        new Identifier("bar")
                    )
                  }
              ),
              new CaseStatement(
                  null,
                  new Statement[] {
                      new ExpressionStatement(
                          new Identifier("foo")
                      )
                  }
              ),
              new CaseStatement(
                  new NumberLiteral(1),
                  new Statement[] {
                    new ExpressionStatement(
                        new Identifier("baz")
                    )
                  }
              )
            }
        ),
        "switch (something) {case 0: bar; default: foo; case 1: baz;}"
    );
    assertParserOutput(
        new SwitchStatement(
            new Identifier("something"),
            new CaseStatement[] {
              new CaseStatement(
                  new NumberLiteral(0),
                  new Statement[] {
                    new ExpressionStatement(
                        new Identifier("bar")
                    )
                  }
              ),
              new CaseStatement(
                  new NumberLiteral(1),
                  new Statement[] {
                    new ExpressionStatement(
                        new Identifier("baz")
                    )
                  }
              ),
              new CaseStatement(
                  null,
                  new Statement[] {
                      new ExpressionStatement(
                          new Identifier("foo")
                      )
                  }
              )
            }
        ),
        "switch (something) {case 0: bar; case 1: baz; default: foo;}"
    );
  }
}
