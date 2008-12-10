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
import com.google.minijoe.compiler.ast.AssignmentExpression;
import com.google.minijoe.compiler.ast.BooleanLiteral;
import com.google.minijoe.compiler.ast.Expression;
import com.google.minijoe.compiler.ast.ExpressionStatement;
import com.google.minijoe.compiler.ast.Identifier;
import com.google.minijoe.compiler.ast.NullLiteral;
import com.google.minijoe.compiler.ast.NumberLiteral;
import com.google.minijoe.compiler.ast.ObjectLiteral;
import com.google.minijoe.compiler.ast.ObjectLiteralProperty;
import com.google.minijoe.compiler.ast.StringLiteral;
import com.google.minijoe.compiler.ast.ThisLiteral;

/**
 * @author Andy Hayward
 */
public class ParserPrimaryExpressionTest extends AbstractParserTest {
  public ParserPrimaryExpressionTest() {
    super();
  }

  public ParserPrimaryExpressionTest(String name) {
    super(name);
  }

  public void testIdentifier() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new Identifier("hatstand")
        ),
        "hatstand;"
    );
  }

  public void testThisExpression() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new ThisLiteral()
        ),
        "this;"
    );
  }

  public void testNullLiteral() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new NullLiteral()
        ),
        "null;"
    );
  }

  public void testBooleanLiteral() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new BooleanLiteral(true)
        ),
        "true;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new BooleanLiteral(false)
        ),
        "false;"
    );
  }

  public void testNumericLiteral() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new NumberLiteral(Double.MIN_VALUE)
        ),
        Double.toString(Double.MIN_VALUE) + ";"
    );
    assertParserOutput(
        new ExpressionStatement(
            new NumberLiteral(0.1)
        ),
        "0.1;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new NumberLiteral(0.0)
        ),
        "0.0;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new NumberLiteral(1.0)
        ),
        "1.0;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new NumberLiteral(Integer.MAX_VALUE)
        ),
        Double.toString(Integer.MAX_VALUE) + ";"
    );
    assertParserOutput(
        new ExpressionStatement(
            new NumberLiteral(Float.MAX_VALUE)
        ),
        Double.toString(Float.MAX_VALUE) + ";"
    );
    assertParserOutput(
        new ExpressionStatement(
            new NumberLiteral(Double.MAX_VALUE)
        ),
        Double.toString(Double.MAX_VALUE) + ";"
    );
  }

  public void testStringLiteral() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new StringLiteral("foo")
        ),
        "\"foo\";"
    );
    assertParserOutput(
        new ExpressionStatement(
            new StringLiteral("bar")
        ),
        "\'bar\';"
    );
  }

  public void testArrayLiteral() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new ArrayLiteral(
                new Expression[] {
                }
            )
        ),
        "[];"
    );
    assertParserOutput(
        new ExpressionStatement(
            new ArrayLiteral(
                new Expression[] {
                    null
                }
            )
        ),
        "[,];"
    );
    assertParserOutput(
        new ExpressionStatement(
            new ArrayLiteral(
                new Expression[] {
                    new NumberLiteral(0.0)
                }
            )
        ),
        "[0.0];"
    );
    assertParserOutput(
        new ExpressionStatement(
            new ArrayLiteral(
                new Expression[] {
                    null,
                    new NumberLiteral(0.0)
                }
            )
        ),
        "[,0.0];"
    );
    assertParserOutput(
        new ExpressionStatement(
            new ArrayLiteral(
                new Expression[] {
                    new NumberLiteral(0.0)
                }
            )
        ),
        "[0.0,];"
    );
    assertParserOutput(
        new ExpressionStatement(
            new ArrayLiteral(
                new Expression[] {
                    new NumberLiteral(0.0),
                    null
                }
            )
        ),
        "[0.0,,];"
    );
    assertParserOutput(
        new ExpressionStatement(
            new ArrayLiteral(
                new Expression[] {
                    new NumberLiteral(0.0),
                    null,
                    new NumberLiteral(1.0)
                }
            )
        ),
        "[0.0,,1.0];"
    );
    assertParserOutput(
        new ExpressionStatement(
            new ArrayLiteral(
                new Expression[] {
                    new NumberLiteral(0.0),
                    null,
                    new NumberLiteral(1.0)
                }
            )
        ),
        "[0.0,,1.0,];"
    );
    assertParserOutput(
        new ExpressionStatement(
            new ArrayLiteral(
                new Expression[] {
                    new NumberLiteral(0.0),
                    null,
                    new NumberLiteral(1.0),
                    null
                }
            )
        ),
        "[0.0,,1.0,,];"
    );
  }

  public void testObjectLiteral() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new AssignmentExpression(
                new Identifier("foo"),
                new ObjectLiteral(
                    new ObjectLiteralProperty[] {}
                )
            )
        ),
        "foo = {};"
    );
    assertParserOutput(
        new ExpressionStatement(
            new AssignmentExpression(
                new Identifier("foo"),
                new ObjectLiteral(
                    new ObjectLiteralProperty[] {
                        new ObjectLiteralProperty(
                            new StringLiteral("name"),
                            new StringLiteral("wibble")
                        )
                    }
                )
            )
        ),
        "foo = {name: \"wibble\"};"
    );
    assertParserOutput(
        new ExpressionStatement(
            new AssignmentExpression(
                new Identifier("foo"),
                new ObjectLiteral(
                    new ObjectLiteralProperty[] {
                        new ObjectLiteralProperty(
                            new StringLiteral("name"),
                            new StringLiteral("wibble")
                        ),
                        new ObjectLiteralProperty(
                            new StringLiteral("value"),
                            new NumberLiteral(1.0)
                        )
                    }
                )
            )
        ),
        "foo = {name: \"wibble\", value: 1.0};"
    );
  }
}
