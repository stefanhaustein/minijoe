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

import com.google.minijoe.compiler.ast.Expression;
import com.google.minijoe.compiler.ast.FunctionLiteral;
import com.google.minijoe.compiler.ast.Identifier;
import com.google.minijoe.compiler.ast.NumberLiteral;
import com.google.minijoe.compiler.ast.Statement;
import com.google.minijoe.compiler.ast.StringLiteral;
import com.google.minijoe.compiler.ast.VariableDeclaration;

import java.util.Vector;

/**
 * @author Andy Hayward
 */
public class Util {
  /**
   * Prevent instantiation.
   */
  private Util() {  
  }
  
  public static Statement[] vectorToStatementArray(Vector vector) {
    Statement[] statementArray = new Statement[vector.size()];

    vector.copyInto(statementArray);

    return statementArray;
  }

  public static Expression[] vectorToExpressionArray(Vector vector) {
    Expression[] expressionArray = new Expression[vector.size()];

    vector.copyInto(expressionArray);

    return expressionArray;
  }

  public static VariableDeclaration[] vectorToDeclarationArray(Vector vector) {
    VariableDeclaration[] declarationArray = new VariableDeclaration[vector.size()];

    vector.copyInto(declarationArray);

    return declarationArray;
  }

  public static Identifier[] vectorToIdentifierArray(Vector vector) {
    Identifier[] identifierArray = new Identifier[vector.size()];

    vector.copyInto(identifierArray);

    return identifierArray;
  }

  public static FunctionLiteral[] vectorToFunctionLiteralArray(Vector vector) {
    FunctionLiteral[] literalArray = new FunctionLiteral[vector.size()];

    vector.copyInto(literalArray);

    return literalArray;
  }

  public static NumberLiteral[] vectorToNumberLiteralArray(Vector vector) {
    NumberLiteral[] literalArray = new NumberLiteral[vector.size()];

    vector.copyInto(literalArray);

    return literalArray;
  }

  public static StringLiteral[] vectorToStringLiteralArray(Vector vector) {
    StringLiteral[] literalArray = new StringLiteral[vector.size()];

    vector.copyInto(literalArray);

    return literalArray;
  }
}
