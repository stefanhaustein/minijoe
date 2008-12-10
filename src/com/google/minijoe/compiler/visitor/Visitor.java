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

/**
 * @author Andy Hayward
 */
public interface Visitor {
  //
  // nodes
  //

  public abstract Program visit(Program program) throws CompilerException;

  //
  // statements
  //

  public abstract Statement visit(FunctionDeclaration statement) throws CompilerException;
  public abstract Statement visit(BlockStatement statement) throws CompilerException;
  public abstract Statement visit(BreakStatement statement) throws CompilerException;
  public abstract Statement visit(CaseStatement statement ) throws CompilerException;
  public abstract Statement visit(ContinueStatement statement) throws CompilerException;
  public abstract Statement visit(DoStatement statement) throws CompilerException;
  public abstract Statement visit(EmptyStatement statement) throws CompilerException;
  public abstract Statement visit(ExpressionStatement statement) throws CompilerException;
  public abstract Statement visit(ForStatement statement) throws CompilerException;
  public abstract Statement visit(ForInStatement statement) throws CompilerException;
  public abstract Statement visit(IfStatement statement) throws CompilerException;
  public abstract Statement visit(LabelledStatement statement) throws CompilerException;
  public abstract Statement visit(ReturnStatement statement) throws CompilerException;
  public abstract Statement visit(SwitchStatement statement) throws CompilerException;
  public abstract Statement visit(ThrowStatement statement) throws CompilerException;
  public abstract Statement visit(TryStatement statement) throws CompilerException;
  public abstract Statement visit(VariableStatement statement) throws CompilerException;
  public abstract Statement visit(WhileStatement statement) throws CompilerException;
  public abstract Statement visit(WithStatement statement) throws CompilerException;

  //
  // expressions
  //

  public abstract Expression visit(AssignmentExpression expression) throws CompilerException;
  public abstract Expression visit(AssignmentOperatorExpression expression) throws CompilerException;
  public abstract Expression visit(BinaryOperatorExpression expression) throws CompilerException;
  public abstract Expression visit(CallExpression expression) throws CompilerException;
  public abstract Expression visit(ConditionalExpression expression) throws CompilerException;
  public abstract Expression visit(DeleteExpression expression) throws CompilerException;
  public abstract Expression visit(IncrementExpression expression) throws CompilerException;
  public abstract Expression visit(LogicalAndExpression expression) throws CompilerException;
  public abstract Expression visit(LogicalOrExpression expression) throws CompilerException;
  public abstract Expression visit(NewExpression expression) throws CompilerException;
  public abstract Expression visit(PropertyExpression expression) throws CompilerException;
  public abstract Expression visit(UnaryOperatorExpression expression) throws CompilerException;
  public abstract Expression visit(VariableExpression expression) throws CompilerException;
  public abstract Expression visit(VariableDeclaration declaration) throws CompilerException;

  //
  // literals
  //

  public abstract Expression visit(Identifier identifier) throws CompilerException;
  public abstract Expression visit(ThisLiteral literal) throws CompilerException;
  public abstract Expression visit(NullLiteral literal) throws CompilerException;
  public abstract Expression visit(BooleanLiteral literal) throws CompilerException;
  public abstract Expression visit(NumberLiteral literal) throws CompilerException;
  public abstract Expression visit(StringLiteral literal) throws CompilerException;
  public abstract Expression visit(ArrayLiteral literal) throws CompilerException;
  public abstract Expression visit(FunctionLiteral literal) throws CompilerException;
  public abstract Expression visit(ObjectLiteral literal) throws CompilerException;
  public abstract Expression visit(ObjectLiteralProperty property) throws CompilerException;
}
