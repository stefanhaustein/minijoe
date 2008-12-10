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

import com.google.minijoe.compiler.ast.CallExpression;
import com.google.minijoe.compiler.ast.Expression;
import com.google.minijoe.compiler.ast.ExpressionStatement;
import com.google.minijoe.compiler.ast.Identifier;
import com.google.minijoe.compiler.ast.NewExpression;
import com.google.minijoe.compiler.ast.NumberLiteral;
import com.google.minijoe.compiler.ast.PropertyExpression;
import com.google.minijoe.compiler.ast.StringLiteral;

/**
 * @author Andy Hayward
 */
public class ParserMemberExpressionTest extends AbstractParserTest {
  public ParserMemberExpressionTest() {
    super();
  }

  public ParserMemberExpressionTest(String name) {
    super(name);
  }

  public void testPropertyExpression() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new PropertyExpression(
                new Identifier("foo"),
                new StringLiteral("bar")
            )
        ),
        "foo.bar;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new PropertyExpression(
                new Identifier("foo"),
                new StringLiteral("bar")
            )
        ),
        "foo[\"bar\"];"
    );
  }

  public void testNewExpression() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new NewExpression(
                new Identifier("Object"),
                new Expression[] {
                }
            )
        ),
        "new Object();"
    );
    assertParserOutput(
        new ExpressionStatement(
            new NewExpression(
                new Identifier("Object"),
                new Expression[] {
                  new NumberLiteral(1.0)
                }
            )
        ),
        "new Object(1.0);"
    );
    assertParserOutput(
        new ExpressionStatement(
            new NewExpression(
                new Identifier("Object"),
                new Expression[] {
                  new NumberLiteral(1.0),
                  new StringLiteral("hatstand")
                }
            )
        ),
        "new Object(1.0, 'hatstand');"
    );
  }

  public void testCallExpression() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new CallExpression(
                new Identifier("thing"),
                new Expression[] {
                }
            )
        ),
        "thing();"
    );
    assertParserOutput(
        new ExpressionStatement(
            new CallExpression(
                new Identifier("thing"),
                new Expression[] {
                  new NumberLiteral(1.0)
                }
            )
        ),
        "thing(1.0);"
    );
    assertParserOutput(
        new ExpressionStatement(
            new CallExpression(
                new Identifier("thing"),
                new Expression[] {
                  new NumberLiteral(1.0),
                  new StringLiteral("hatstand")
                }
            )
        ),
        "thing(1.0, 'hatstand');"
    );
  }
}
