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

import com.google.minijoe.compiler.ast.ExpressionStatement;
import com.google.minijoe.compiler.ast.Identifier;
import com.google.minijoe.compiler.ast.IncrementExpression;
import com.google.minijoe.compiler.ast.PropertyExpression;
import com.google.minijoe.compiler.ast.StringLiteral;

/**
 * @author Andy Hayward
 */
public class ParserPostfixExpressionTest extends AbstractParserTest {
  public ParserPostfixExpressionTest() {
    super();
  }

  public ParserPostfixExpressionTest(String name) {
    super(name);
  }

  public void testPostfixIncrementExpression() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new IncrementExpression(
                new Identifier("foo"), +1, true
            )
        ),
        "foo++;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new IncrementExpression(
                new PropertyExpression(
                    new Identifier("foo"),
                    new StringLiteral("bar")
                ),
                1,
                true
            )
        ),
        "foo.bar++;"
    );
  }

  public void testPostfixDecrementExpression() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new IncrementExpression(
                new Identifier("foo"), -1, true
            )
        ),
        "foo--;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new IncrementExpression(
                new PropertyExpression(
                    new Identifier("foo"),
                    new StringLiteral("bar")
                ),
                -1,
                true
            )
        ),
        "foo.bar--;"
    );
  }
}
