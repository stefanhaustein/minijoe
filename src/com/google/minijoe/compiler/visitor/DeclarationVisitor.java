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
import com.google.minijoe.compiler.Token;
import com.google.minijoe.compiler.Util;
import com.google.minijoe.compiler.ast.AssignmentExpression;
import com.google.minijoe.compiler.ast.BinaryOperatorExpression;
import com.google.minijoe.compiler.ast.BlockStatement;
import com.google.minijoe.compiler.ast.EmptyStatement;
import com.google.minijoe.compiler.ast.Expression;
import com.google.minijoe.compiler.ast.ExpressionStatement;
import com.google.minijoe.compiler.ast.FunctionDeclaration;
import com.google.minijoe.compiler.ast.FunctionLiteral;
import com.google.minijoe.compiler.ast.Identifier;
import com.google.minijoe.compiler.ast.Program;
import com.google.minijoe.compiler.ast.Statement;
import com.google.minijoe.compiler.ast.VariableDeclaration;
import com.google.minijoe.compiler.ast.VariableExpression;
import com.google.minijoe.compiler.ast.VariableStatement;
import com.google.minijoe.compiler.ast.WithStatement;

import java.util.Vector;

/**
 * Process function and variable declarations.
 * 
 * @author Andy Hayward
 */
public class DeclarationVisitor extends TraversalVisitor {
  private Vector functionVector;
  private Vector variableVector;
  private boolean hasWithStatement = false;
  private boolean hasArgumentsVariable = false;
  private boolean hasFunctionLiteral = false;

  public DeclarationVisitor() {
    super();
    visitor = this;
  }

  private void addVariable(Identifier identifier) {
    if (variableVector.indexOf(identifier) == -1) {
      identifier.index = variableVector.size();
      variableVector.addElement(identifier);
    }
  }

  public Program visit(Program program) throws CompilerException {
    Vector oldFunctionVector = functionVector;

    functionVector = new Vector();

    program = super.visit(program);

    program.functions = Util.vectorToStatementArray(functionVector);

    functionVector = oldFunctionVector;

    return program;
  }

  public Expression visit(FunctionLiteral literal) throws CompilerException {
    Vector oldFunctionVector = functionVector;
    Vector oldVariableVector = variableVector;
    boolean oldHasWithStatement = hasWithStatement;
    boolean oldHasArgumentsVariable = hasArgumentsVariable;

    functionVector = new Vector();
    variableVector = new Vector();
    hasWithStatement = false;
    hasArgumentsVariable = false;
    hasFunctionLiteral = false;

    Identifier[] parameters = literal.parameters;
    for (int i = 0; i < parameters.length; i++) {
      addVariable(parameters[i]);
    }

    literal = (FunctionLiteral) super.visit(literal);

    literal.functions = Util.vectorToStatementArray(functionVector);
    literal.variables = Util.vectorToIdentifierArray(variableVector);

    // if this function literal:
    // * contains a function literal
    // * contains a 'with' statement
    // * contains a reference to 'arguments'
    //
    // then we need to disable the "access locals by index" optimisation for
    // this function literal.

    literal.enableLocalsOptimization =
        !(hasWithStatement | hasArgumentsVariable | hasFunctionLiteral);

    functionVector = oldFunctionVector;
    variableVector = oldVariableVector;
    hasWithStatement = oldHasWithStatement;
    hasArgumentsVariable = oldHasArgumentsVariable;
    hasFunctionLiteral = true;

    return literal;
  }

  public Statement visit(FunctionDeclaration functionDeclaration) throws CompilerException {
    functionDeclaration = (FunctionDeclaration) super.visit(functionDeclaration);

    functionVector.addElement(new ExpressionStatement(functionDeclaration.literal));

    return new EmptyStatement();
  }

  public Statement visit(WithStatement withStatement) throws CompilerException {
    withStatement = (WithStatement) super.visit(withStatement);

    hasWithStatement = true;

    return withStatement;
  }

  public Statement visit(VariableStatement variableStatement) throws CompilerException {
    Vector statements = new Vector(0);

    for (int i = 0; i < variableStatement.declarations.length; i++) {
      Expression expression = visitExpression(variableStatement.declarations[i]);

      if (expression != null) {
        Statement statement = new ExpressionStatement(expression);
        statement.setLineNumber(variableStatement.getLineNumber());
        statements.addElement(statement);
      }
    }

    if (statements.size() == 0) {
      return new EmptyStatement();
    } else if (statements.size() == 1) {
      return (ExpressionStatement) statements.elementAt(0);
    } else {
      return new BlockStatement(Util.vectorToStatementArray(statements));
    }
  }

  public Expression visit(VariableExpression variableExpression) throws CompilerException {
    Expression result = null;

    for (int i = 0; i < variableExpression.declarations.length; i++) {
      Expression expression = visitExpression(variableExpression.declarations[i]);

      if (expression != null) {
        if (result == null) {
          result = expression;
        } else {
          result = new BinaryOperatorExpression(result, expression, Token.OPERATOR_COMMA);
        }
      }
    }

    return result;
  }

  public Expression visit(VariableDeclaration declaration) throws CompilerException {
    Identifier identifier = visitIdentifier(declaration.identifier);
    Expression initializer = visitExpression(declaration.initializer);
    Expression result = null;

    if (variableVector != null) {
      addVariable(identifier);
    }

    if (initializer != null) {
      result = new AssignmentExpression(identifier, initializer);
    } else {
      result = identifier;
    }

    return result;
  }

  public Expression visit(Identifier identifier) throws CompilerException {
    identifier = (Identifier) super.visit(identifier);

    if (identifier.string.equals("arguments")) {
      hasArgumentsVariable = true;
    }

    return identifier;
  }
}
