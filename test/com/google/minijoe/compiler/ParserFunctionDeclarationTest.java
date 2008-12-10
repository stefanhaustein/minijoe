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
import com.google.minijoe.compiler.ast.FunctionDeclaration;
import com.google.minijoe.compiler.ast.FunctionLiteral;
import com.google.minijoe.compiler.ast.Identifier;
import com.google.minijoe.compiler.ast.IfStatement;
import com.google.minijoe.compiler.ast.NumberLiteral;
import com.google.minijoe.compiler.ast.Statement;

/**
 * @author Andy Hayward
 */
public class ParserFunctionDeclarationTest extends AbstractParserTest {
  //
  // FunctionLiteral    : 'function' Identifier_opt '(' FunctionParameters ')' '{' FunctionBody '}'
  //
  // FunctionParameters : Identifier_opt ...
  //
  // FunctionBody       : SourceElement_opt ...
  //

  public ParserFunctionDeclarationTest() {
    super();
  }

  public ParserFunctionDeclarationTest(String name) {
    super(name);
  }

  public void testFunctionDeclaration() throws CompilerException {
    assertParserOutput(
        new FunctionDeclaration(
            new FunctionLiteral(
                new Identifier("foo"),
                new Identifier[] {
                },
                new Statement[] {
                }
            )
        ),
        "function foo() {};"
    );
    assertParserOutput(
        new FunctionDeclaration(
            new FunctionLiteral(
                new Identifier("foo"),
                new Identifier[] {
                  new Identifier("a"),
                },
                new Statement[] {
                }
            )
        ),
        "function foo(a) {}"
    );
    assertParserOutput(
        new FunctionDeclaration(
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
        "function foo(a, b, c) {};"
    );
    assertParserOutput(
        new FunctionDeclaration(
            new FunctionLiteral(
                new Identifier("a"),
                new Identifier[] {
                },
                new Statement[] {
                  new FunctionDeclaration(
                      new FunctionLiteral(
                          new Identifier("b"),
                          new Identifier[] {
                          },
                          new Statement[] {
                            new IfStatement(
                                new NumberLiteral(0.0),
                                new ExpressionStatement(
                                    new FunctionLiteral(
                                        new Identifier("c"),
                                        new Identifier[] {
                                        },
                                        new Statement[] {
                                        }
                                    )
                                ),
                                new ExpressionStatement(
                                    new FunctionLiteral(
                                        null,
                                        new Identifier[] {
                                        },
                                        new Statement[] {
                                        }
                                    )
                                )
                            )
                          }
                      )
                  )
                }
            )
        ),
        "function a() {function b() {if (0) function c() {}; else function () {};}}"
    );
  }
}
