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

import com.google.minijoe.compiler.ast.BooleanLiteral;
import com.google.minijoe.compiler.ast.ExpressionStatement;
import com.google.minijoe.compiler.ast.Identifier;
import com.google.minijoe.compiler.ast.IfStatement;

/**
 * @author Andy Hayward
 */
public class ParserIfStatementTest extends AbstractParserTest {
  public ParserIfStatementTest() {
    super();
  }

  public ParserIfStatementTest(String name) {
    super(name);
  }

  public void testIfStatement() throws CompilerException {
    assertParserOutput(
        new IfStatement(
            new BooleanLiteral(true),
            new ExpressionStatement(
                new Identifier("foo")
            ),
            null
        ),
        "if (true) foo;"
    );
    assertParserOutput(
        new IfStatement(
            new BooleanLiteral(true),
            new ExpressionStatement(
                new Identifier("foo")
            ),
            new ExpressionStatement(
                new Identifier("bar")
            )
        ),
        "if (true) foo; else bar;"
    );
    assertParserOutput(
        new IfStatement(
            new BooleanLiteral(true),
            new IfStatement(
                new BooleanLiteral(true),
                new ExpressionStatement(
                    new Identifier("foo")
                ),
                new ExpressionStatement(
                    new Identifier("bar")
                )
            ),
            null
        ),
        "if (true) if (true) foo; else bar;"
    );
    assertParserOutput(
        new IfStatement(
            new BooleanLiteral(true),
            new IfStatement(
                new BooleanLiteral(true),
                new ExpressionStatement(
                    new Identifier("foo")
                ),
                new ExpressionStatement(
                    new Identifier("bar")
                )
            ),
            new ExpressionStatement(
                new Identifier("baz")
            )
        ),
        "if (true) if (true) foo; else bar; else baz;"
    );
  }
}
