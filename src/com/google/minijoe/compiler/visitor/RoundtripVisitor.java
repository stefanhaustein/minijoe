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

package com.google.minijoe.compiler.visitor;

import com.google.minijoe.compiler.CompilerException;
import com.google.minijoe.compiler.ast.ArrayLiteral;
import com.google.minijoe.compiler.ast.AssignmentExpression;
import com.google.minijoe.compiler.ast.AssignmentOperatorExpression;
import com.google.minijoe.compiler.ast.BinaryOperatorExpression;
import com.google.minijoe.compiler.ast.BlockStatement;
import com.google.minijoe.compiler.ast.BooleanLiteral;
import com.google.minijoe.compiler.ast.BreakStatement;
import com.google.minijoe.compiler.ast.CallExpression;
import com.google.minijoe.compiler.ast.CaseStatement;
import com.google.minijoe.compiler.ast.ConditionalExpression;
import com.google.minijoe.compiler.ast.ContinueStatement;
import com.google.minijoe.compiler.ast.DeleteExpression;
import com.google.minijoe.compiler.ast.DoStatement;
import com.google.minijoe.compiler.ast.EmptyStatement;
import com.google.minijoe.compiler.ast.Expression;
import com.google.minijoe.compiler.ast.ExpressionStatement;
import com.google.minijoe.compiler.ast.ForInStatement;
import com.google.minijoe.compiler.ast.ForStatement;
import com.google.minijoe.compiler.ast.FunctionDeclaration;
import com.google.minijoe.compiler.ast.FunctionLiteral;
import com.google.minijoe.compiler.ast.Identifier;
import com.google.minijoe.compiler.ast.IfStatement;
import com.google.minijoe.compiler.ast.IncrementExpression;
import com.google.minijoe.compiler.ast.LabelledStatement;
import com.google.minijoe.compiler.ast.LogicalAndExpression;
import com.google.minijoe.compiler.ast.LogicalOrExpression;
import com.google.minijoe.compiler.ast.NewExpression;
import com.google.minijoe.compiler.ast.NullLiteral;
import com.google.minijoe.compiler.ast.NumberLiteral;
import com.google.minijoe.compiler.ast.ObjectLiteral;
import com.google.minijoe.compiler.ast.ObjectLiteralProperty;
import com.google.minijoe.compiler.ast.Program;
import com.google.minijoe.compiler.ast.PropertyExpression;
import com.google.minijoe.compiler.ast.ReturnStatement;
import com.google.minijoe.compiler.ast.Statement;
import com.google.minijoe.compiler.ast.StringLiteral;
import com.google.minijoe.compiler.ast.SwitchStatement;
import com.google.minijoe.compiler.ast.ThisLiteral;
import com.google.minijoe.compiler.ast.ThrowStatement;
import com.google.minijoe.compiler.ast.TryStatement;
import com.google.minijoe.compiler.ast.UnaryOperatorExpression;
import com.google.minijoe.compiler.ast.VariableDeclaration;
import com.google.minijoe.compiler.ast.VariableExpression;
import com.google.minijoe.compiler.ast.VariableStatement;
import com.google.minijoe.compiler.ast.WhileStatement;
import com.google.minijoe.compiler.ast.WithStatement;

import java.io.IOException;
import java.io.Writer;

/**
 * @author Andy Hayward
 */
public class RoundtripVisitor implements Visitor {
  Writer w;
  int indent = 0;

  public RoundtripVisitor(Writer w) {
    this.w = w;
  }

  //
  // utilities
  //

  private void increaseIndent() {
    indent += 2;
  }

  private void decreaseIndent() {
    indent -= 2;
  }

  private void visitStatementArray(Statement[] statements) throws CompilerException {
    if (statements != null) {
      for (int i = 0; i < statements.length; i++) {
        visitStatement(statements[i]);
      }
    }
  }

  private void visitStatement(Statement statement) throws CompilerException {
    if (statement != null) {
      statement.visitStatement(this);
    }
  }

  private void visitExpression(Expression expression) throws CompilerException {
    if (expression != null) {
      expression.visitExpression(this);
    }
  }

  private void write(String string) throws CompilerException {
    try {
      w.write(string);
    } catch (IOException e) {
      throw new CompilerException(e);
    }
  }

  private void write(char c) throws CompilerException {
    try {
      w.write(c);
    } catch (IOException e) {
      throw new CompilerException(e);
    }
  }

  private void writeIndent() throws CompilerException {
    try {
      w.write('\n');
      for (int i = 0; i < indent; i++) {
        w.write(' ');
      }
    } catch (IOException e) {
      throw new CompilerException(e);
    }
  }

  //
  // nodes
  //

  public Program visit(Program program) throws CompilerException {
    visitStatementArray(program.statements);
    write("\n\n");
    return program;
  }

  //
  // statements
  //

  public Statement visit(FunctionDeclaration declaration) throws CompilerException {
    writeIndent();
    declaration.literal.visitExpression(this);
    write(';');
    return declaration;
  }

  public Statement visit(BlockStatement statement) throws CompilerException {
    writeIndent();
    write('{');
    increaseIndent();
    visitStatementArray(statement.statements);
    decreaseIndent();
    writeIndent();
    write('}');
    return statement;
  }

  public Statement visit(BreakStatement statement) throws CompilerException {
    writeIndent();
    if (statement.identifier != null) {
      write("break " + statement.identifier + ";");
    } else {
      write("break");
    }
    return statement;
  }

  public Statement visit(CaseStatement statement) throws CompilerException {
    writeIndent();
    if (statement.expression == null) {
      write("default:");
    } else {
      write("case ");
      statement.expression.visitExpression(this);
      write(": ");
    }
    visitStatementArray(statement.statements);
    return statement;
  }

  public Statement visit(ContinueStatement statement) throws CompilerException {
    writeIndent();
    if (statement.identifier != null) {
      write("continue " + statement.identifier + ";");
    } else {
      write("continue;");
    }
    return statement;
  }

  public Statement visit(DoStatement statement) throws CompilerException {
    writeIndent();
    write("do ");
    increaseIndent();
    statement.statement.visitStatement(this);
    decreaseIndent();
    writeIndent();
    write("while (");
    statement.expression.visitExpression(this);
    write(')');
    return statement;
  }

  public Statement visit(EmptyStatement statement) {
    return statement;
  }

  public Statement visit(ExpressionStatement statement) throws CompilerException {
    writeIndent();
    statement.expression.visitExpression(this);
    write(';');
    return statement;
  }

  public Statement visit(ForStatement statement) throws CompilerException {
    writeIndent();
    write("for (");
    visitExpression(statement.initial);
    write("; ");
    visitExpression(statement.condition);
    write("; ");
    visitExpression(statement.increment);
    write(")");
    increaseIndent();
    statement.statement.visitStatement(this);
    decreaseIndent();
    return statement;
  }

  public Statement visit(ForInStatement statement) throws CompilerException {
    writeIndent();
    write("for (");
    statement.variable.visitExpression(this);
    write(" in ");
    statement.expression.visitExpression(this);
    write(")");
    statement.statement.visitStatement(this);
    return statement;
  }

  public Statement visit(IfStatement statement) throws CompilerException {
    writeIndent();
    write("if (");
    statement.expression.visitExpression(this);
    write(") ");
    statement.trueStatement.visitStatement(this);
    if (statement.falseStatement != null) {
      writeIndent();
      write("else ");
      statement.falseStatement.visitStatement(this);
    }
    return statement;
  }

  public Statement visit(LabelledStatement statement) throws CompilerException {
    writeIndent();
    statement.identifier.visitExpression(this);
    write(": ");
    statement.statement.visitStatement(this);
    return statement;
  }

  public Statement visit(ReturnStatement statement) throws CompilerException {
    writeIndent();
    if (statement.expression != null) {
      write("return ");
      statement.expression.visitExpression(this);
      write(";");
    } else {
      write("return;");
    }
    return statement;
  }

  public Statement visit(SwitchStatement statement) throws CompilerException {
    write("switch (");
    statement.expression.visitExpression(this);
    write(") {");
    visitStatementArray(statement.clauses);
    write("}");
    return statement;
  }

  public Statement visit(ThrowStatement statement) throws CompilerException {
    writeIndent();
    return null;
  }

  public Statement visit(TryStatement statement) throws CompilerException {
    writeIndent();
    write("try");
    statement.tryBlock.visitStatement(this);
    if (statement.catchBlock != null) {
      writeIndent();
      write("catch (");
      statement.catchIdentifier.visitExpression(this);
      write(")");
      statement.catchBlock.visitStatement(this);
    }
    if (statement.finallyBlock != null) {
      writeIndent();
      write("finally ");
      statement.finallyBlock.visitStatement(this);
    }
    return statement;
  }

  public Statement visit(VariableStatement statement) throws CompilerException {
    for (int i = 0; i < statement.declarations.length; i++) {
      statement.declarations[i].visitExpression(this);
    }
    return statement;
  }

  public Statement visit(WhileStatement statement) throws CompilerException {
    writeIndent();
    write("while (");
    statement.expression.visitExpression(this);
    write(")");
    statement.statement.visitStatement(this);
    return statement;
  }

  public Statement visit(WithStatement statement) throws CompilerException {
    writeIndent();
    write("width (");
    statement.expression.visitExpression(this);
    write(")");
    statement.statement.visitStatement(this);
    return statement;
  }

  //
  // expressions
  //

  public Expression visit(AssignmentExpression expression) throws CompilerException {
    expression.leftExpression.visitExpression(this);
    write(" = ");
    expression.rightExpression.visitExpression(this);
    return expression;
  }

  public Expression visit(AssignmentOperatorExpression expression) throws CompilerException {
    expression.leftExpression.visitExpression(this);
    write(' ');
    write(expression.type.getValue());
    write(' ');
    expression.rightExpression.visitExpression(this);
    return expression;
  }

  public Expression visit(BinaryOperatorExpression expression) throws CompilerException {
    write('(');
    expression.leftExpression.visitExpression(this);
    write(' ');
    write(expression.operator.getValue());
    write(' ');
    expression.rightExpression.visitExpression(this);
    write(')');

    return expression;
  }

  public Expression visit(CallExpression expression) throws CompilerException {
    expression.function.visitExpression(this);
    write('(');
    for (int i = 0; i < expression.arguments.length; i++) {
      if (i > 0) {
        write(", ");
      }
      expression.arguments[i].visitExpression(this);
    }
    write(')');
    return null;
  }

  public Expression visit(ConditionalExpression expression) throws CompilerException {
    write("(");
    expression.expression.visitExpression(this);
    write(" ? ");
    expression.trueExpression.visitExpression(this);
    write(" : ");
    expression.falseExpression.visitExpression(this);
    write(")");
    return expression;
  }

  public Expression visit(DeleteExpression expression) throws CompilerException {
    write("delete ");
    expression.subExpression.visitExpression(this);
    return expression;
  }

  public Expression visit(IncrementExpression expression) throws CompilerException {
    if (expression.post) {
      expression.subExpression.visitExpression(this);
    }
    switch (expression.value) {
      case -1:
        write("--");
        break;
      case 1:
        write("++");
        break;
      default:
        throw new IllegalArgumentException();
    }
    if (!expression.post) {
      expression.subExpression.visitExpression(this);
    }
    return expression;
  }

  public Expression visit(LogicalAndExpression expression) throws CompilerException {
    write('(');
    expression.leftExpression.visitExpression(this);
    write(" && ");
    expression.rightExpression.visitExpression(this);
    write(')');

    return expression;
  }

  public Expression visit(LogicalOrExpression expression) throws CompilerException {
    write('(');
    expression.leftExpression.visitExpression(this);
    write(" && ");
    expression.rightExpression.visitExpression(this);
    write(')');

    return expression;
  }

  public Expression visit(NewExpression expression) throws CompilerException {
    write("new ");
    expression.function.visitExpression(this);
    write("( ");
    for (int i = 0; i < expression.arguments.length; i++) {
      if (i != 0) {
        write(", ");
      }
    }
    write(")");
    return expression;
  }

  public Expression visit(PropertyExpression expression) throws CompilerException {
    expression.leftExpression.visitExpression(this);
    write("[");
    expression.rightExpression.visitExpression(this);
    write("]");
    return expression;
  }

  public Expression visit(UnaryOperatorExpression expression) throws CompilerException {
    write('(');
    write(expression.operator.getValue());
    write(' ');
    expression.subExpression.visitExpression(this);
    write(')');

    return expression;
  }

  public Expression visit(VariableExpression expression) throws CompilerException {
    write("var ");

    for (int i = 0; i < expression.declarations.length; i++) {
      if (i != 0) {
        write(", ");
      }
      expression.declarations[i].visitExpression(this);
    }

    return expression;
  }

  public Expression visit(VariableDeclaration declaration) throws CompilerException {
    declaration.identifier.visitExpression(this);
    if (declaration.initializer != null) {
      write(" = ");
      declaration.initializer.visitExpression(this);
    }
    return declaration;
  }

  //
  // literals
  //

  public Expression visit(Identifier identifier) throws CompilerException {
    write(identifier.string);
    return identifier;
  }

  public Expression visit(ThisLiteral literal) throws CompilerException {
    write("this");
    return literal;
  }

  public Expression visit(NullLiteral literal) throws CompilerException {
    write("null");
    return literal;
  }

  public Expression visit(BooleanLiteral literal) throws CompilerException {
    write("" + literal.value);
    return literal;
  }

  public Expression visit(NumberLiteral literal) throws CompilerException {
    write("" + literal.value);
    return literal;
  }

  public Expression visit(StringLiteral literal) throws CompilerException {
    write("\"" + literal.string + "\"");
    return literal;
  }

  public Expression visit(ArrayLiteral literal) throws CompilerException {
    write("[");
    for (int i = 0; i < literal.elements.length; i++) {
      if (i != 0) {
        write(", ");
      }
      if (literal.elements[i] != null) {
        literal.elements[i].visitExpression(this);
      }
    }
    return null;
  }

  public Expression visit(FunctionLiteral literal) throws CompilerException {
    write("function ");
    if (literal.name != null) {
      literal.name.visitExpression(this);
    }
    write('(');
    for (int i = 0; i < literal.parameters.length; i++) {
      if (i > 0) {
        write(", ");
      }
      literal.parameters[i].visitExpression(this);
    }
    write(") {");
    increaseIndent();
    visitStatementArray(literal.statements);
    decreaseIndent();
    writeIndent();
    write("}");

    return literal;
  }

  public Expression visit(ObjectLiteral literal) throws CompilerException {
    write("{");
    for  (int i = 0; i < literal.properties.length; i++) {
      if (i > 0) {
        write(", ");
      }
      literal.properties[i].visitExpression(this);
    }
    write("}");
    return literal;
  }

  public Expression visit(ObjectLiteralProperty property) throws CompilerException {
    property.name.visitExpression(this);
    write(": ");
    property.value.visitExpression(this);
    return property;
  }
}
