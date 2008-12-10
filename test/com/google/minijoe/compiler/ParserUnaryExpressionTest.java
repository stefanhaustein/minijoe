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

import com.google.minijoe.compiler.ast.DeleteExpression;
import com.google.minijoe.compiler.ast.ExpressionStatement;
import com.google.minijoe.compiler.ast.Identifier;
import com.google.minijoe.compiler.ast.IncrementExpression;
import com.google.minijoe.compiler.ast.UnaryOperatorExpression;

/**
 * @author Andy Hayward
 */
public class ParserUnaryExpressionTest extends AbstractParserTest {
  public ParserUnaryExpressionTest() {
    super();
  }

  public ParserUnaryExpressionTest(String name) {
    super(name);
  }

  public void testDeleteExpression() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new DeleteExpression(
                new Identifier("foo")
            )
        ),
        "delete foo;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new UnaryOperatorExpression(
                new DeleteExpression(
                    new Identifier("foo")
                ),
                Token.KEYWORD_VOID
            )
        ),
        "void delete foo;"
    );
  }

  public void testTypeOfExpression() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new UnaryOperatorExpression(
                new Identifier("foo"),
                Token.KEYWORD_TYPEOF
            )
        ),
        "typeof foo;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new UnaryOperatorExpression(
                new UnaryOperatorExpression(
                    new Identifier("foo"),
                    Token.KEYWORD_TYPEOF
                ),
                Token.KEYWORD_VOID
            )
        ),
        "void typeof foo;"
    );
  }

  public void testVoidExpression() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new UnaryOperatorExpression(
                new Identifier("foo"),
                Token.KEYWORD_VOID
            )
        ),
        "void foo;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new UnaryOperatorExpression(
                new UnaryOperatorExpression(
                    new Identifier("foo"),
                    Token.KEYWORD_VOID
                ),
                Token.KEYWORD_VOID
            )
        ),
        "void void foo;"
    );
  }

  public void testPrefixIncrementExpression() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new IncrementExpression(
                new Identifier("foo"), 1, false
            )
        ),
        "++foo;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new IncrementExpression(
                new IncrementExpression(
                    new Identifier("foo"), 1, true
                ),
                1, false
            )
        ),
        "++foo++;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new IncrementExpression(
                new IncrementExpression(
                    new Identifier("foo"), 1, false
                ), 1, false
            )
        ),
        "++++foo;"
    );
  }

  public void testPrefixDecrementExpression() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new IncrementExpression(
                new Identifier("foo"), -1, false
            )
        ),
        "--foo;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new IncrementExpression(
                new IncrementExpression(
                    new Identifier("foo"), -1, true
                ), -1, false
            )
        ),
        "--foo--;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new IncrementExpression(
                new IncrementExpression(
                    new Identifier("foo"), -1, false
                ), -1, false
            )
        ),
        "----foo;"
    );
  }

  public void testUnaryPlusExpression() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new UnaryOperatorExpression(
                new Identifier("foo"),
                Token.OPERATOR_PLUS
            )
        ),
        "+foo;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new UnaryOperatorExpression(
                new IncrementExpression(
                    new Identifier("foo"), 1, true
                ),
                Token.OPERATOR_PLUS
            )
        ),
        "+foo++;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new IncrementExpression(
                new UnaryOperatorExpression(
                    new Identifier("foo"),
                    Token.OPERATOR_PLUS
                ), 1, false
            )
        ),
        "+++foo;"
    );
  }

  public void testUnaryMinsExpression() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new UnaryOperatorExpression(
                new Identifier("foo"),
                Token.OPERATOR_MINUS

            )
        ),
        "-foo;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new UnaryOperatorExpression(
                new IncrementExpression(
                    new Identifier("foo"),
                    -1, true
                ), Token.OPERATOR_MINUS
            )
        ),
        "-foo--;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new IncrementExpression(
                new UnaryOperatorExpression(
                    new Identifier("foo"),
                    Token.OPERATOR_MINUS
                ), -1, false
            )
        ),
        "---foo;"
    );
  }

  public void testBitwiseNotExpression() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new UnaryOperatorExpression(
                new Identifier("foo"),
                Token.OPERATOR_BITWISENOT
            )
        ),
        "~foo;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new UnaryOperatorExpression(
                new UnaryOperatorExpression(
                    new Identifier("foo"),
                    Token.OPERATOR_BITWISENOT
                ),
                Token.OPERATOR_BITWISENOT
            )
        ),
        "~~foo;"
    );
  }

  public void testLogicalNotExpression() throws CompilerException {
    assertParserOutput(
        new ExpressionStatement(
            new UnaryOperatorExpression(
                new Identifier("foo"),
                Token.OPERATOR_LOGICALNOT
            )
        ),
        "!foo;"
    );
    assertParserOutput(
        new ExpressionStatement(
            new UnaryOperatorExpression(
                new UnaryOperatorExpression(
                    new Identifier("foo"),
                    Token.OPERATOR_LOGICALNOT
                ),
                Token.OPERATOR_LOGICALNOT
            )
        ),
        "!!foo;"
    );
  }
}
