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

import com.google.minijoe.compiler.ast.AssignmentExpression;
import com.google.minijoe.compiler.ast.BlockStatement;
import com.google.minijoe.compiler.ast.ExpressionStatement;
import com.google.minijoe.compiler.ast.Identifier;
import com.google.minijoe.compiler.ast.NumberLiteral;
import com.google.minijoe.compiler.ast.Program;
import com.google.minijoe.compiler.ast.Statement;
import com.google.minijoe.compiler.ast.VariableDeclaration;
import com.google.minijoe.compiler.ast.VariableStatement;

/**
 * @author Andy Hayward
 */
public class ParserSemicolonInsertionTest extends AbstractParserTest {
  public ParserSemicolonInsertionTest() {
    super();
  }

  public ParserSemicolonInsertionTest(String name) {
    super(name);
  }

  public void testSemicolonInsertion() throws CompilerException {
    // example from the ECMA version 3 specification. the source:
    //
    //   { 1
    //     2 } 3
    //
    // gets turned into:
    //
    //   { 1 ;
    //     2 ; } 3 ;
    //

    assertParserOutput(
        new Program(
            new Statement[] {
                new BlockStatement(
                    new Statement[] {
                        new ExpressionStatement(
                            new NumberLiteral(1.0)
                        ),
                        new ExpressionStatement(
                            new NumberLiteral(2.0)
                        )
                    }
                ),
                new ExpressionStatement(
                    new NumberLiteral(3.0)
                )
            }
        ),
        "{ 1 \n 2 } 3 "
    );
    assertParserOutput(
        new Program(
            new Statement[] {
                new VariableStatement(
                    new VariableDeclaration[] {
                        new VariableDeclaration(
                            new Identifier("foo"),
                            null
                        )
                    }
                ),
                new ExpressionStatement(
                    new AssignmentExpression(
                        new Identifier("bar"),
                        new NumberLiteral(1.0)
                    )
                )
            }
        ),
        "var foo \n bar = 1.0"
    );
  }
}
