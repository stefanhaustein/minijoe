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

import com.google.minijoe.compiler.ast.Program;
import com.google.minijoe.compiler.ast.Statement;
import com.google.minijoe.compiler.visitor.AssertVisitor;
import com.google.minijoe.compiler.visitor.RoundtripVisitor;

import j2meunit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;

/**
 * @author Andy Hayward
 */
public abstract class AbstractParserTest extends TestCase {
  public AbstractParserTest() {
    super();
  }

  public AbstractParserTest(String name) {
    super(name);
  }

  public void assertParserOutput(Program expected, String input) throws CompilerException {
    Lexer lexer = new Lexer(input);
    Parser parser = new Parser(lexer);
    Program program = parser.parseProgram();

    try {
      // ensure the RoundTripVisitor doesn't complain about this parse tree
      program.visitProgram(
          new RoundtripVisitor(
              new OutputStreamWriter(
                  new ByteArrayOutputStream()
              )
          )
      );

      // assert the expected and actual trees are equal
      expected.visitProgram(new AssertVisitor(program));

      // check that the DebugVisitor doesn't affect the tree
      expected.visitProgram(new AssertVisitor(program));

      // assert that we've reached the end of the input
      assertEquals(Token.EOF, lexer.nextToken());

    } catch (CompilerException e) {
      fail();
    }
  }

  public void assertParserOutput(Statement expected, String input) throws CompilerException {
    Lexer lexer = new Lexer(input);
    Parser parser = new Parser(lexer);
    Statement statement = parser.parseSourceElement();

    try {
      // ensure the RoundTripVisitor doesn't complain about this parse tree
      statement.visitStatement(
          new RoundtripVisitor(
              new OutputStreamWriter(
                  new ByteArrayOutputStream()
              )
          )
      );

      // assert the expected and actual trees are equal
      expected.visitStatement(new AssertVisitor(statement));

      // check that the DebugVisitor doesn't affect the tree
      expected.visitStatement(new AssertVisitor(statement));

      // assert that we've reached the end of the input
      assertEquals(Token.EOF, lexer.nextToken());

    } catch (CompilerException e) {
      fail();
    }
  }
}
