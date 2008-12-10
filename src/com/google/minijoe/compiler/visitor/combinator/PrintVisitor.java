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
import com.google.minijoe.compiler.visitor.Visitor;

/**
 * @author Andy Hayward
 */
public class PrintVisitor implements Visitor {
  int depth = 0;

  public PrintVisitor() {
  }

  //
  // utilities
  //

  public void incrementDepth() {
    depth++;
  }

  public void decrementDepth() {
    depth--;
  }

  private void write(String string) {
    for (int i = 0; i < depth; i++) {
      System.out.print("  ");
    }

    System.out.println(string);
  }

  //
  // nodes
  //

  public Program visit(Program node) {
    write("program");

    return node;
  }

  //
  // statements
  //

  public Statement visit(FunctionDeclaration declaration) {
    write("function declaration");

    return declaration;
  }

  public Statement visit(BlockStatement statement) {
    write("block (line = " + statement.getLineNumber() + ")");

    return statement;
  }

  public Statement visit(BreakStatement statement) {
    write("break (line = " + statement.getLineNumber() + ")");

    return statement;
  }

  public Statement visit(CaseStatement statement) {
    write("case (line = " + statement.getLineNumber() + ")");

    return statement;
  }

  public Statement visit(ContinueStatement statement) {
    write("continue (line = " + statement.getLineNumber() + ")");

    return statement;
  }

  public Statement visit(DoStatement statement) {
    write("do (line = " + statement.getLineNumber() + ")");

    return statement;
  }

  public Statement visit(EmptyStatement statement) {
    write("empty (line = " + statement.getLineNumber() + ")");

    return statement;
  }

  public Statement visit(ExpressionStatement statement) {
    write("expression statement (line = " + statement.getLineNumber() + ")");

    return statement;
  }

  public Statement visit(ForStatement statement) {
    write("for (line = " + statement.getLineNumber() + ")");

    return statement;
  }

  public Statement visit(ForInStatement statement) {
    write("for in (line = " + statement.getLineNumber() + ")");

    return statement;
  }

  public Statement visit(IfStatement statement) {
    write("if (line = " + statement.getLineNumber() + ")");

    return statement;
  }

  public Statement visit(LabelledStatement statement) {
    write("label (line = " + statement.getLineNumber() + ")");

    return statement;
  }

  public Statement visit(ReturnStatement statement) {
    write("return (line = " + statement.getLineNumber() + ")");

    return statement;
  }

  public Statement visit(SwitchStatement statement) {
    write("switch (line = " + statement.getLineNumber() + ")");

    return statement;
  }

  public Statement visit(ThrowStatement statement) {
    write("throw (line = " + statement.getLineNumber() + ")");

    return statement;
  }

  public Statement visit(TryStatement statement) {
    write("try (line = " + statement.getLineNumber() + ")");

    return statement;
  }

  public Statement visit(VariableStatement statement) {
    write("var (line = " + statement.getLineNumber() + ")");

    return statement;
  }

  public Statement visit(WhileStatement statement) {
    write("while (line = " + statement.getLineNumber() + ")");

    return statement;
  }

  public Statement visit(WithStatement statement) {
    write("with (line = " + statement.getLineNumber() + ")");

    return statement;
  }

  //
  // expressions
  //

  public Expression visit(AssignmentExpression expression) {
    write("assignment expression");

    return expression;
  }

  public Expression visit(AssignmentOperatorExpression expression) {
    write(expression.type.toString());

    return expression;
  }

  public Expression visit(BinaryOperatorExpression expression) {
    write("binary operator " + expression.operator);

    return expression;
  }

  public Expression visit(CallExpression expression) {
    write("call");

    return expression;
  }

  public Expression visit(ConditionalExpression expression) {
    write("?");

    return expression;
  }

  public Expression visit(DeleteExpression expression) {
    write("delete");

    return expression;
  }

  public Expression visit(IncrementExpression expression) {
    write((expression.post ? "postfix " : "prefix ") + expression.value);

    return expression;
  }

  public Expression visit(LogicalAndExpression expression) {
    write("&&");

    return expression;
  }

  public Expression visit(LogicalOrExpression expression) {
    write("||");

    return expression;
  }

  public Expression visit(NewExpression expression) {
    write("new");

    return expression;
  }

  public Expression visit(PropertyExpression expression) {
    write("property");

    return expression;
  }

  public Expression visit(UnaryOperatorExpression expression) {
    write("unary operator " + expression.operator);

    return expression;
  }

  public Expression visit(VariableExpression expression) {
    write("variable expression");

    return expression;
  }

  public Expression visit(VariableDeclaration declaration) {
    write("variable declaration");

    return declaration;
  }

  //
  // literals
  //

  public Expression visit(Identifier identifier) {
    write("identifier = \"" + identifier.string + "\"");

    return identifier;
  }

  public Expression visit(ThisLiteral literal) {
    write("this literal");

    return literal;
  }

  public Expression visit(NullLiteral literal) {
    write("null literal");

    return literal;
  }

  public Expression visit(BooleanLiteral literal) {
    write("boolean literal = " + literal.value);

    return literal;
  }

  public Expression visit(NumberLiteral literal) {
    write("numeric literal = " + literal.value);

    return literal;
  }

  public Expression visit(StringLiteral literal) {
    write("string literal = \"" + literal.string + "\"");

    return literal;
  }

  public Expression visit(ArrayLiteral literal) {
    write("array literal");

    return literal;
  }

  public Expression visit(FunctionLiteral literal) {
    write("function literal");

    return literal;
  }

  public Expression visit(ObjectLiteral literal) {
    write("object literal");

    return literal;
  }

  public Expression visit(ObjectLiteralProperty property) {
    write("object literal property");

    return property;
  }
}
