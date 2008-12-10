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

import com.google.minijoe.compiler.ast.BooleanLiteral;
import com.google.minijoe.compiler.ast.Expression;
import com.google.minijoe.compiler.ast.LogicalAndExpression;
import com.google.minijoe.compiler.ast.LogicalOrExpression;

/**
 * @author Andy Hayward
 */
public class AlgebraicIdentityVisitor extends IdentityVisitor {
  // Currently disabled, to avoid problems with expressions
  // like hello " + (1 * "World".
  //  
  // public Expression visit(BinaryOperatorExpression boe) {
  //   Token op = boe.operator;
  //
  //   if (boe.leftExpression instanceof NumberLiteral
  //       && boe.rightExpression instanceof NumberLiteral) {      
  //     double leftValue = ((NumberLiteral) boe.leftExpression).value;
  //     double rightValue = ((NumberLiteral) boe.rightExpression).value;
  //
  //     if ((op == Token.OPERATOR_PLUS && leftValue == 0.0)
  //         || (op == Token.OPERATOR_MULTIPLY && leftValue == 1.0)) {
  //       return boe.rightExpression;
  //     }
  //
  //     if((op == Token.OPERATOR_PLUS && rightValue == 0.0)
  //         || (op == Token.OPERATOR_MULTIPLY && rightValue == 1.0)
  //         || (op == Token.OPERATOR_DIVIDE && rightValue == 1.0))
  //       return boe.leftExpression;
  //   }
  //
  //   return boe;
  // }

  public Expression visit(LogicalAndExpression expression) {
    if (expression.leftExpression instanceof BooleanLiteral) {
      if (((BooleanLiteral) expression.leftExpression).value == false) {
        return expression.leftExpression;
      } else if (((BooleanLiteral) expression.leftExpression).value == true) {
        return expression.rightExpression;
      }
    }

    if (expression.rightExpression instanceof BooleanLiteral) {
      if (((BooleanLiteral) expression.rightExpression).value == false) {
        return expression.rightExpression;
      } else if (((BooleanLiteral) expression.rightExpression).value == true) {
        return expression.leftExpression;
      }
    }

    return expression;
  }

  public Expression visit(LogicalOrExpression expression) {
    if (expression.leftExpression instanceof BooleanLiteral) {
      if (((BooleanLiteral) expression.leftExpression).value == true) {
        return expression.leftExpression;
      } else  if (((BooleanLiteral) expression.leftExpression).value == false) {
        return expression.rightExpression;
      }
    }

    if (expression.rightExpression instanceof BooleanLiteral) {
      if (((BooleanLiteral) expression.rightExpression).value == true) {
        return expression.rightExpression;
      } else  if (((BooleanLiteral) expression.rightExpression).value == false) {
        return expression.leftExpression;
      }
    }

    return expression;
  }
}
