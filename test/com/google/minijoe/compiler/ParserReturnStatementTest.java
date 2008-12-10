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
import com.google.minijoe.compiler.ast.NumberLiteral;
import com.google.minijoe.compiler.ast.ReturnStatement;

/**
 * @author Andy Hayward
 */
public class ParserReturnStatementTest extends AbstractParserTest {
  public ParserReturnStatementTest() {
    super();
  }

  public ParserReturnStatementTest(String name) {
    super(name);
  }

  public void testReturnStatement() throws CompilerException {
    assertParserOutput(
        new ReturnStatement(
            null
        ),
        "return;"
    );
    assertParserOutput(
        new ReturnStatement(
            new NumberLiteral(0)
        ),
        "return 0;"
    );
    assertParserOutput(
        new ReturnStatement(
            new BooleanLiteral(true)
        ),
        "return true;"
    );
  }
}
