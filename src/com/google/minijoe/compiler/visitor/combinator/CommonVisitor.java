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
public abstract class CommonVisitor implements Visitor {
  //
  // nodes
  //

  public abstract Statement visit(Statement statement) throws CompilerException;
  public abstract Expression visit(Expression expression) throws CompilerException;

  //
  // statements
  //

  public Statement visit(FunctionDeclaration declaration) throws CompilerException {
    return visit((Statement) declaration);
  }

  public Statement visit(BlockStatement statement) throws CompilerException {
    return visit((Statement) statement);
  }

  public Statement visit(BreakStatement statement) throws CompilerException {
    return visit((Statement) statement);
  }

  public Statement visit(CaseStatement statement) throws CompilerException {
    return visit((Statement) statement);
  }

  public Statement visit(ContinueStatement statement) throws CompilerException {
    return visit((Statement) statement);
  }

  public Statement visit(DoStatement statement) throws CompilerException {
    return visit((Statement) statement);
  }

  public Statement visit(EmptyStatement statement) throws CompilerException {
    return visit((Statement) statement);
  }

  public Statement visit(ExpressionStatement statement) throws CompilerException {
    return visit((Statement) statement);
  }

  public Statement visit(ForStatement statement) throws CompilerException {
    return visit((Statement) statement);
  }

  public Statement visit(ForInStatement statement) throws CompilerException {
    return visit((Statement) statement);
  }

  public Statement visit(IfStatement statement) throws CompilerException {
    return visit((Statement) statement);
  }

  public Statement visit(LabelledStatement statement) throws CompilerException {
    return visit((Statement) statement);
  }

  public Statement visit(ReturnStatement statement) throws CompilerException {
    return visit((Statement) statement);
  }

  public Statement visit(SwitchStatement statement) throws CompilerException {
    return visit((Statement) statement);
  }

  public Statement visit(ThrowStatement statement) throws CompilerException {
    return visit((Statement) statement);
  }

  public Statement visit(TryStatement statement) throws CompilerException {
    return visit((Statement) statement);
  }

  public Statement visit(VariableStatement statement) throws CompilerException {
    return visit((Statement) statement);
  }

  public Statement visit(WhileStatement statement) throws CompilerException {
    return visit((Statement) statement);
  }

  public Statement visit(WithStatement statement) throws CompilerException {
    return visit((Statement) statement);
  }

  //
  // expressions
  //

  public Expression visit(AssignmentExpression expression) throws CompilerException {
    return visit((Expression) expression);
  }

  public Expression visit(AssignmentOperatorExpression expression) throws CompilerException {
    return visit((Expression) expression);
  }

  public Expression visit(BinaryOperatorExpression expression) throws CompilerException {
    return visit((Expression) expression);
  }

  public Expression visit(CallExpression expression) throws CompilerException {
    return visit((Expression) expression);
  }

  public Expression visit(ConditionalExpression expression) throws CompilerException {
    return visit((Expression) expression);
  }

  public Expression visit(DeleteExpression expression) throws CompilerException {
    return visit((Expression) expression);
  }

  public Expression visit(LogicalAndExpression expression) throws CompilerException {
    return visit((Expression) expression);
  }

  public Expression visit(LogicalOrExpression expression) throws CompilerException {
    return visit((Expression) expression);
  }

  public Expression visit(NewExpression expression) throws CompilerException {
    return visit((Expression) expression);
  }

  public Expression visit(IncrementExpression expression) throws CompilerException {
    return visit((Expression) expression);
  }

  public Expression visit(PropertyExpression expression) throws CompilerException {
    return visit((Expression) expression);
  }

  public Expression visit(UnaryOperatorExpression expression) throws CompilerException {
    return visit((Expression) expression);
  }

  public Expression visit(VariableExpression expression) throws CompilerException {
    return visit((Expression) expression);
  }

  public Expression visit(VariableDeclaration declaration) throws CompilerException {
    return visit((Expression) declaration);
  }

  //
  // literals
  //

  public Expression visit(Identifier literal) throws CompilerException {
    return visit((Expression) literal);
  }

  public Expression visit(ThisLiteral literal) throws CompilerException {
    return visit((Expression) literal);
  }

  public Expression visit(NullLiteral literal) throws CompilerException {
    return visit((Expression) literal);
  }

  public Expression visit(BooleanLiteral literal) throws CompilerException {
    return visit((Expression) literal);
  }

  public Expression visit(NumberLiteral literal) throws CompilerException {
    return visit((Expression) literal);
  }

  public Expression visit(StringLiteral literal) throws CompilerException {
    return visit((Expression) literal);
  }

  public Expression visit(ArrayLiteral literal) throws CompilerException {
    return visit((Expression) literal);
  }

  public Expression visit(FunctionLiteral literal) throws CompilerException {
    return visit((Expression) literal);
  }

  public Expression visit(ObjectLiteral literal) throws CompilerException {
    return visit((Expression) literal);
  }

  public Expression visit(ObjectLiteralProperty property) throws CompilerException {
    return visit((Expression) property);
  }
}
