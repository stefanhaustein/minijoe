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
import com.google.minijoe.compiler.ast.FunctionLiteral;
import com.google.minijoe.compiler.ast.Identifier;
import com.google.minijoe.compiler.ast.NumberLiteral;
import com.google.minijoe.compiler.ast.Statement;
import com.google.minijoe.compiler.ast.VariableDeclaration;
import com.google.minijoe.compiler.ast.VariableStatement;

/**
 * @author Andy Hayward
 */
public class ParserFunctionLiteralTest extends AbstractParserTest {
  //
  // FunctionLiteral    : 'function' Identifier_opt '(' FunctionParameters ')' '{' FunctionBody '}'
  //
  // FunctionParameters : Identifier_opt ...
  //
  // FunctionBody       : SourceElement_opt ...
  //

  public ParserFunctionLiteralTest() {
    super();
  }

  public ParserFunctionLiteralTest(String name) {
    super(name);
  }

  public void testFunctionLiteral() throws CompilerException {

    // we're need to put the function literals in parenthesis to ensure that
    // they're parsed as function expressions and not function declarations.

    assertParserOutput(
        new ExpressionStatement(
            new FunctionLiteral(
                null,
                new Identifier[] {
                },
                new Statement[] {
                }
            )
        ),
        "(function () {});"
    );
    assertParserOutput(
        new ExpressionStatement(
            new FunctionLiteral(
                new Identifier("foo"),
                new Identifier[] {
                },
                new Statement[] {
                }
            )
        ),
        "(function foo() {});"
    );
    assertParserOutput(
        new ExpressionStatement(
            new FunctionLiteral(
                new Identifier("foo"),
                new Identifier[] {
                  new Identifier("a"),
                },
                new Statement[] {
                }
            )
        ),
        "(function foo(a) {});"
    );
    assertParserOutput(
        new ExpressionStatement(
            new FunctionLiteral(
                new Identifier("foo"),
                new Identifier[] {
                  new Identifier("a"),
                  new Identifier("b"),
                  new Identifier("c"),
                },
                new Statement[] {
                }
            )
        ),
        "(function foo(a, b, c) {});"
    );
    assertParserOutput(
        new ExpressionStatement(
            new FunctionLiteral(
                new Identifier("foo"),
                new Identifier[] {
                    new Identifier("a"),
                    new Identifier("b"),
                    new Identifier("c"),
                },
                new Statement[] {
                    new VariableStatement(
                        new VariableDeclaration[] {
                            new VariableDeclaration(
                                new Identifier("bar"),
                                new NumberLiteral(0.0)
                            )
                        }
                    )
                }
            )
        ),
        "(function foo(a, b, c) {var bar = 0;});"
    );
  }
}
