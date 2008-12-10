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

package com.google.minijoe.compiler.visitor.combinator;

import com.google.minijoe.compiler.CompilerException;
import com.google.minijoe.compiler.ast.Expression;
import com.google.minijoe.compiler.ast.Program;
import com.google.minijoe.compiler.ast.Statement;
import com.google.minijoe.compiler.visitor.Visitor;

/**
 * Applies a number of visitors, one after the other.
 *
 * @author Andy Hayward
 */
public class SequenceVisitor extends CommonVisitor {
  Visitor[] visitors;

  public SequenceVisitor() {
  }

  public SequenceVisitor(Visitor[] visitors) {
    this.visitors = visitors;
  }

  public Program visit(Program node) throws CompilerException {
    for (int i = 0; i < visitors.length; i++) {
      node = node.visitProgram(visitors[i]);
    }

    return node;
  }

  public Statement visit(Statement node) throws CompilerException {
    for (int i = 0; i < visitors.length; i++) {
      node = node.visitStatement(visitors[i]);
    }

    return node;
  }

  public Expression visit(Expression node) throws CompilerException {
    for (int i = 0; i < visitors.length; i++) {
      node = node.visitExpression(visitors[i]);
    }

    return node;
  }
}
