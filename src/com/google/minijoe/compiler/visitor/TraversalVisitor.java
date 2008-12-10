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
import com.google.minijoe.compiler.ast.BinaryExpression;
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
import com.google.minijoe.compiler.ast.UnaryExpression;
import com.google.minijoe.compiler.ast.UnaryOperatorExpression;
import com.google.minijoe.compiler.ast.VariableDeclaration;
import com.google.minijoe.compiler.ast.VariableExpression;
import com.google.minijoe.compiler.ast.VariableStatement;
import com.google.minijoe.compiler.ast.WhileStatement;
import com.google.minijoe.compiler.ast.WithStatement;

/**
 * @author Andy Hayward
 */
public class TraversalVisitor implements Visitor {
  Visitor visitor;

  public TraversalVisitor() {
  }

  public TraversalVisitor(Visitor visitor) {
    this.visitor = visitor;
  }

  //
  // utilities
  //

  protected Statement[] visitStatementArray(Statement[] statements) throws CompilerException {
    if (statements != null) {
      for (int i = 0; i < statements.length; i++) {
        statements[i] = visitStatement(statements[i]);
      }
    }

    return statements;
  }

  protected Statement visitStatement(Statement statement) throws CompilerException {
    if (statement != null) {
      statement = statement.visitStatement(visitor);
    }

    return statement;
  }

  protected Expression[] visitExpressionArray(Expression[] expressions) throws CompilerException {
    if (expressions != null) {
      for (int i = 0; i < expressions.length; i++) {
        expressions[i] = visitExpression(expressions[i]);
      }
    }

    return expressions;
  }

  protected Expression visitExpression(Expression expression) throws CompilerException {
    if (expression != null) {
      expression = expression.visitExpression(visitor);
    }

    return expression;
  }

  protected Expression visitBinaryExpression(BinaryExpression expression) throws CompilerException {
    expression.leftExpression = visitExpression(expression.leftExpression);
    expression.rightExpression = visitExpression(expression.rightExpression);

    return expression;
  }

  protected Expression visitUnaryExpression(UnaryExpression expression) throws CompilerException {
    expression.subExpression = visitExpression(expression.subExpression);

    return expression;
  }


  protected Identifier[] visitIdentifierArray(Identifier[] identifiers) throws CompilerException {
    if (identifiers != null) {
      for (int i = 0; i < identifiers.length; i++) {
        identifiers[i] = visitIdentifier(identifiers[i]);
      }
    }

    return identifiers;
  }

  protected Identifier visitIdentifier(Identifier identifier) throws CompilerException {
    if (identifier != null) {
      identifier = (Identifier) identifier.visitExpression(visitor);
    }

    return identifier;
  }

  //
  // nodes
  //

  public Program visit(Program program) throws CompilerException {
    program.functions = visitStatementArray(program.functions);
    program.statements = visitStatementArray(program.statements);

    return program;
  }

  //
  // statements
  //

  public Statement visit(FunctionDeclaration functionDeclaration) throws CompilerException {
    functionDeclaration.literal = (FunctionLiteral) visitExpression(functionDeclaration.literal);

    return functionDeclaration;
  }

  public Statement visit(BlockStatement blockStatement) throws CompilerException {
    blockStatement.statements = visitStatementArray(blockStatement.statements);

    return blockStatement;
  }

  public Statement visit(BreakStatement breakStatement) throws CompilerException {
    breakStatement.identifier = visitIdentifier(breakStatement.identifier);

    return breakStatement;
  }

  public Statement visit(CaseStatement caseStatement) throws CompilerException {
    caseStatement.expression = visitExpression(caseStatement.expression);
    caseStatement.statements = visitStatementArray(caseStatement.statements);

    return caseStatement;
  }


  public Statement visit(ContinueStatement continueStatement) throws CompilerException {
    continueStatement.identifier = visitIdentifier(continueStatement.identifier);

    return continueStatement;
  }

  public Statement visit(DoStatement doStatement) throws CompilerException {
    doStatement.statement = visitStatement(doStatement.statement);
    doStatement.expression = visitExpression(doStatement.expression);

    return doStatement;
  }

  public Statement visit(EmptyStatement emptyStatement) throws CompilerException {
    return emptyStatement;
  }

  public Statement visit(ExpressionStatement expressionStatement) throws CompilerException {
    expressionStatement.expression = visitExpression(expressionStatement.expression);

    return expressionStatement;
  }

  public Statement visit(ForStatement forStatement) throws CompilerException {
    forStatement.initial = visitExpression(forStatement.initial);
    forStatement.condition = visitExpression(forStatement.condition);
    forStatement.increment = visitExpression(forStatement.increment);
    forStatement.statement = visitStatement(forStatement.statement);

    return forStatement;
  }

  public Statement visit(ForInStatement forInStatement) throws CompilerException {
    forInStatement.variable = visitExpression(forInStatement.variable);
    forInStatement.expression = visitExpression(forInStatement.expression);
    forInStatement.statement = visitStatement(forInStatement.statement);

    return forInStatement;
  }

  public Statement visit(IfStatement ifStatement) throws CompilerException {
    ifStatement.expression = visitExpression(ifStatement.expression);
    ifStatement.trueStatement = visitStatement(ifStatement.trueStatement);
    ifStatement.falseStatement = visitStatement(ifStatement.falseStatement);

    return ifStatement;
  }

  public Statement visit(LabelledStatement labelledStatement) throws CompilerException {
    labelledStatement.identifier = visitIdentifier(labelledStatement.identifier);
    labelledStatement.statement = visitStatement(labelledStatement.statement);

    return labelledStatement;
  }

  public Statement visit(ReturnStatement returnStatement) throws CompilerException {
    returnStatement.expression = visitExpression(returnStatement.expression);

    return returnStatement;
  }

  public Statement visit(SwitchStatement switchStatement) throws CompilerException {
    switchStatement.expression = visitExpression(switchStatement.expression);
    switchStatement.clauses = (CaseStatement[]) visitStatementArray(switchStatement.clauses);

    return switchStatement;
  }

  public Statement visit(ThrowStatement throwStatement) throws CompilerException {
    throwStatement.expression = visitExpression(throwStatement.expression);

    return throwStatement;
  }

  public Statement visit(TryStatement tryStatement) throws CompilerException {
    tryStatement.tryBlock = visitStatement(tryStatement.tryBlock);
    tryStatement.catchIdentifier = visitIdentifier(tryStatement.catchIdentifier);
    tryStatement.catchBlock = visitStatement(tryStatement.catchBlock);
    tryStatement.finallyBlock = visitStatement(tryStatement.finallyBlock);

    return tryStatement;
  }

  public Statement visit(VariableStatement variableStatement) throws CompilerException {
    variableStatement.declarations =
        (VariableDeclaration[]) visitExpressionArray(variableStatement.declarations);

    return variableStatement;
  }

  public Statement visit(WhileStatement whileStatement) throws CompilerException {
    whileStatement.expression = visitExpression(whileStatement.expression);
    whileStatement.statement = visitStatement(whileStatement.statement);

    return whileStatement;
  }

  public Statement visit(WithStatement withStatement) throws CompilerException {
    withStatement.expression = visitExpression(withStatement.expression);
    withStatement.statement = visitStatement(withStatement.statement);

    return withStatement;
  }

  //
  // Expressions
  //

  public Expression visit(AssignmentExpression expression) throws CompilerException {
    return visitBinaryExpression(expression);
  }

  public Expression visit(AssignmentOperatorExpression expression) throws CompilerException {
    return visitBinaryExpression(expression);
  }

  public Expression visit(BinaryOperatorExpression expression) throws CompilerException {
    return visitBinaryExpression(expression);
  }

  public Expression visit(CallExpression callExpression) throws CompilerException {
    callExpression.function = visitExpression(callExpression.function);
    callExpression.arguments = visitExpressionArray(callExpression.arguments);

    return callExpression;
  }

  public Expression visit(ConditionalExpression conditionalExpression) throws CompilerException {
    conditionalExpression.expression = visitExpression(conditionalExpression.expression);
    conditionalExpression.trueExpression = visitExpression(conditionalExpression.trueExpression);
    conditionalExpression.falseExpression = visitExpression(conditionalExpression.falseExpression);

    return conditionalExpression;
  }

  public Expression visit(DeleteExpression expression) throws CompilerException {
    return visitUnaryExpression(expression);
  }

  public Expression visit(IncrementExpression expression) throws CompilerException {
    return visitUnaryExpression(expression);
  }

  public Expression visit(LogicalAndExpression expression) throws CompilerException {
    return visitBinaryExpression(expression);
  }

  public Expression visit(LogicalOrExpression expression) throws CompilerException {
    return visitBinaryExpression(expression);
  }

  public Expression visit(NewExpression newExpression) throws CompilerException {
    newExpression.function = visitExpression(newExpression.function);
    newExpression.arguments = visitExpressionArray(newExpression.arguments);

    return newExpression;
  }

  public Expression visit(PropertyExpression expression) throws CompilerException {
    return visitBinaryExpression(expression);
  }

  public Expression visit(UnaryOperatorExpression expression) throws CompilerException {
    return visitUnaryExpression(expression);
  }

  public Expression visit(VariableExpression variableExpression) throws CompilerException {
    variableExpression.declarations =
        (VariableDeclaration[]) visitExpressionArray(variableExpression.declarations);

    return variableExpression;
  }

  public Expression visit(VariableDeclaration variableDeclaration) throws CompilerException {
    variableDeclaration.identifier = visitIdentifier(variableDeclaration.identifier);
    variableDeclaration.initializer = visitExpression(variableDeclaration.initializer);

    return variableDeclaration;
  }

  //
  // literals
  //

  public Expression visit(Identifier identifier) throws CompilerException {
    return identifier;
  }

  public Expression visit(ThisLiteral thisLiteral) throws CompilerException {
    return thisLiteral;
  }

  public Expression visit(NullLiteral nullLiteral) throws CompilerException {
    return nullLiteral;
  }

  public Expression visit(BooleanLiteral booleanLiteral) throws CompilerException {
    return booleanLiteral;
  }

  public Expression visit(NumberLiteral numberLiteral) throws CompilerException {
    return numberLiteral;
  }

  public Expression visit(StringLiteral stringLiteral) throws CompilerException {
    return stringLiteral;
  }

  public Expression visit(ArrayLiteral arrayLiteral) throws CompilerException {
    arrayLiteral.elements = visitExpressionArray(arrayLiteral.elements);

    return arrayLiteral;
  }

  public Expression visit(FunctionLiteral functionLiteral) throws CompilerException {
    functionLiteral.name = visitIdentifier(functionLiteral.name);
    functionLiteral.parameters = visitIdentifierArray(functionLiteral.parameters);
    functionLiteral.variables = visitIdentifierArray(functionLiteral.variables);
    functionLiteral.functions = visitStatementArray(functionLiteral.functions);
    functionLiteral.statements = visitStatementArray(functionLiteral.statements);

    return functionLiteral;
  }

  public Expression visit(ObjectLiteral objectLiteral) throws CompilerException {
    objectLiteral.properties =
        (ObjectLiteralProperty[]) visitExpressionArray(objectLiteral.properties);

    return objectLiteral;
  }

  public Expression visit(ObjectLiteralProperty objectLiteralProperty) throws CompilerException {
    objectLiteralProperty.name = visitExpression(objectLiteralProperty.name);
    objectLiteralProperty.value = visitExpression(objectLiteralProperty.value);

    return objectLiteralProperty;
  }
}
