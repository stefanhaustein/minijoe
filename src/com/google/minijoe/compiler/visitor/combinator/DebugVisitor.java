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

import com.google.minijoe.compiler.ast.Expression;
import com.google.minijoe.compiler.ast.Program;
import com.google.minijoe.compiler.ast.Statement;
import com.google.minijoe.compiler.visitor.TraversalVisitor;
import com.google.minijoe.compiler.visitor.Visitor;

/**
 * @author Andy Hayward
 */
public class DebugVisitor extends SequenceVisitor {
  class IncrementVisitor extends CommonVisitor {
    PrintVisitor visitor;

    public IncrementVisitor(PrintVisitor visitor) {
      this.visitor = visitor;
    }

    public Program visit(Program program) {
      visitor.incrementDepth();

      return program;
    }

    public Statement visit(Statement statement) {
      visitor.incrementDepth();

      return statement;
    }

    public Expression visit(Expression expression) {
      visitor.incrementDepth();

      return expression;
    }
  }

  class DecrementVisitor extends CommonVisitor {
    PrintVisitor visitor;

    public DecrementVisitor(PrintVisitor visitor) {
      this.visitor = visitor;
    }

    public Program visit(Program program) {
      visitor.decrementDepth();

      return program;
    }

    public Statement visit(Statement statement) {
      visitor.decrementDepth();

      return statement;
    }

    public Expression visit(Expression expression) {
      visitor.decrementDepth();

      return expression;
    }
  }

  public DebugVisitor() {
    final PrintVisitor visitor = new PrintVisitor();

    visitors = new Visitor[] {
        visitor,
        new IncrementVisitor(visitor),
        new TraversalVisitor(this),
        new DecrementVisitor(visitor),
    };
  }
}
