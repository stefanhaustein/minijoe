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

import java.util.Vector;

/**
 * @author Andy Hayward
 */
public class Parser {
  private Lexer lexer;
  private Token nextToken;
  private boolean seenNewline;

  public Parser(Lexer lexer) throws CompilerException {
    this.lexer = lexer;

    readToken();
  }

  //
  // program
  //

  public Program parseProgram() throws CompilerException {
    Vector statementVector = new Vector();

    statementVector.addElement(parseSourceElement());

    while (nextToken != Token.EOF) {
      statementVector.addElement(parseSourceElement());
    }

    return new Program(Util.vectorToStatementArray(statementVector));
  }

  //
  // Statements
  //

  public Statement parseSourceElement() throws CompilerException {
    if (nextToken == Token.KEYWORD_FUNCTION) {
      return parseFunctionDeclaration();
    } else {
      return parseStatement();
    }
  }

  private Statement parseFunctionDeclaration() throws CompilerException {
    return new FunctionDeclaration(parseFunctionLiteral(true));
  }

  public Statement parseStatement() throws CompilerException {
    Statement statement;
    int lineNumber;

    if (Config.LINENUMBER) {
      lineNumber = getLineNumber();
    }

    if (nextToken == Token.OPERATOR_SEMICOLON) {
      readToken(Token.OPERATOR_SEMICOLON);
      statement = new EmptyStatement();

    } else if (nextToken == Token.OPERATOR_OPENBRACE) {
      statement = parseBlockStatement();

    } else if (nextToken == Token.KEYWORD_BREAK) {
      statement = parseBreakStatement();

    } else if (nextToken == Token.KEYWORD_CONTINUE) {
      statement = parseContinueStatement();

    } else if (nextToken == Token.KEYWORD_DO) {
      statement = parseDoStatement();

    } else if (nextToken == Token.KEYWORD_FOR) {
      statement = parseForStatement();

    } else if (nextToken == Token.KEYWORD_IF) {
      statement = parseIfStatement();

    } else if (nextToken == Token.KEYWORD_RETURN) {
      statement = parseReturnStatement();

    } else if (nextToken == Token.KEYWORD_THROW) {
      statement = parseThrowStatement();

    } else if (nextToken == Token.KEYWORD_TRY) {
      statement = parseTryStatement();

    } else if (nextToken == Token.KEYWORD_SWITCH) {
      statement = parseSwitchStatement();

    } else if (nextToken == Token.KEYWORD_VAR) {
      statement = parseVariableStatement();

    } else if (nextToken == Token.KEYWORD_WHILE) {
      statement = parseWhileStatement();

    } else if (nextToken == Token.KEYWORD_WITH) {
      statement = parseWithStatement();

    } else {
      statement = parseExpressionStatement();

    }

    if (Config.LINENUMBER) {
      statement.setLineNumber(lineNumber);
    }

    return statement;
  }

  private Statement parseBlockStatement() throws CompilerException {
    readToken(Token.OPERATOR_OPENBRACE);

    Vector vector = new Vector();
    while (nextToken != Token.OPERATOR_CLOSEBRACE) {
      vector.addElement(parseStatement());
    }

    readToken(Token.OPERATOR_CLOSEBRACE);

    return new BlockStatement(Util.vectorToStatementArray(vector));
  }

  private Statement parseBreakStatement() throws CompilerException {
    Identifier identifier = null;

    readToken(Token.KEYWORD_BREAK);

    if (nextToken != Token.OPERATOR_SEMICOLON) {
      identifier = parseIdentifier();
    }

    readTokenSemicolon();

    return new BreakStatement(identifier);
  }

  private Statement parseContinueStatement() throws CompilerException {
    Identifier identifier = null;

    readToken(Token.KEYWORD_CONTINUE);

    if (nextToken != Token.OPERATOR_SEMICOLON) {
      identifier = parseIdentifier();
    }

    readTokenSemicolon();

    return new ContinueStatement(identifier);
  }

  private Statement parseDoStatement() throws CompilerException {
    Statement statement;
    Expression expression;

    readToken(Token.KEYWORD_DO);
    statement = parseStatement();
    readToken(Token.KEYWORD_WHILE);
    readToken(Token.OPERATOR_OPENPAREN);
    expression = parseExpression(true);
    readToken(Token.OPERATOR_CLOSEPAREN);

    return new DoStatement(statement, expression);
  }

  private Statement parseExpressionStatement() throws CompilerException {
    Expression expression = parseExpression(true);

    // TODO there are comments in the v8 code about wrapping a
    // labelled try statement in a block and applying the label to the outer
    // block. we should consider doing something similar here if handling a
    // labelled try block proves problematic.

    if (expression instanceof Identifier && nextToken == Token.OPERATOR_COLON) {
      readToken(Token.OPERATOR_COLON);
      return new LabelledStatement((Identifier) expression, parseStatement());
    } else {
      readTokenSemicolon();
      return new ExpressionStatement(expression);
    }
  }

  private Statement parseForStatement() throws CompilerException {
    Expression declaration = null;
    Expression initial = null;
    Expression condition = null;
    Expression increment = null;
    Expression variable = null;
    Expression expression = null;
    Statement statement = null;

    // 'for' statements can be one of the follow four productions:
    //
    // for ( ExpressionNoIn_opt; Expression_opt ; Expression_opt ) Statement
    // for ( var VariableDeclarationListNoIn; Expression_opt ; Expression_opt ) Statement
    // for ( LeftHandSideExpression in Expression ) Statement
    // for ( var VariableDeclarationNoIn in Expression ) Statement

    readToken(Token.KEYWORD_FOR);
    readToken(Token.OPERATOR_OPENPAREN);

    int state = 0;
    while (statement == null) {
      switch (state) {
        case 0:
          // initial state
          if (nextToken == Token.KEYWORD_VAR) {
            state = 1;
          } else if (nextToken != Token.OPERATOR_SEMICOLON) {
            state = 2;
          } else {
            state = 5;
          }
          break;

        case 1:
          // 'for' '(' 'var'
          readToken(Token.KEYWORD_VAR);
          declaration = parseVariableDeclaration(false);
          if (nextToken == Token.KEYWORD_IN) {
            variable = declaration;
            state = 3;
          } else {
            state = 4;
          }
          break;

        case 2:
          // 'for' '(' Expression
          initial = parseExpression(false);
          if (nextToken == Token.KEYWORD_IN) {
            variable = initial;
            state = 3;
          } else {
            state = 5;
          }
          break;

        case 3:
          // 'for' '(' ... 'in'
          readToken(Token.KEYWORD_IN);
          expression = parseExpression(true);
          readToken(Token.OPERATOR_CLOSEPAREN);

          // 'for' '(' ... 'in' ... ')' Statement
          statement = new ForInStatement(variable, expression, parseStatement());
          break;

        case 4:
          // 'for' '(' 'var' VariableDeclarationList
          Vector declarationVector = new Vector();

          declarationVector.addElement(declaration);
          while (nextToken == Token.OPERATOR_COMMA) {
            readToken(Token.OPERATOR_COMMA);
            declarationVector.addElement(parseVariableDeclaration(false));
          }
          initial = new VariableExpression(Util.vectorToDeclarationArray(declarationVector));

          // fall through

        case 5:
          // 'for' '(' ... ';'
          readToken(Token.OPERATOR_SEMICOLON);

          // 'for' '(' ... ';' ...
          if (nextToken != Token.OPERATOR_SEMICOLON) {
            condition = parseExpression(true);
          }

          // 'for' '(' ... ';' ... ';'
          readToken(Token.OPERATOR_SEMICOLON);

          // 'for' '(' ... ';' ... ';' ...
          if (nextToken != Token.OPERATOR_CLOSEPAREN) {
            increment = parseExpression(true);
          }

          // 'for' '(' ... ';' ... ';' ... ')'
          readToken(Token.OPERATOR_CLOSEPAREN);

          // 'for' '(' ... ';' ... ';' ... ')' Statement
          statement = new ForStatement(initial, condition, increment, parseStatement());
          break;
      }
    }

    return statement;
  }

  private Statement parseIfStatement() throws CompilerException {
    Expression expression = null;
    Statement trueStatement = null;
    Statement falseStatement = null;

    readToken(Token.KEYWORD_IF);
    readToken(Token.OPERATOR_OPENPAREN);
    expression = parseExpression(true);
    readToken(Token.OPERATOR_CLOSEPAREN);

    trueStatement = parseStatement();

    if (nextToken == Token.KEYWORD_ELSE) {
      readToken(Token.KEYWORD_ELSE);
      falseStatement = parseStatement();
    }

    return new IfStatement(expression, trueStatement, falseStatement);
  }

  private Statement parseReturnStatement() throws CompilerException {
    Expression result = null;

    readToken(Token.KEYWORD_RETURN);

    if (nextToken != Token.OPERATOR_SEMICOLON) {
      result = parseExpression(true);
    }

    readTokenSemicolon();

    return new ReturnStatement(result);
  }

  private Statement parseSwitchStatement() throws CompilerException {
    Vector caseClauseVector = new Vector();
    boolean defaultSeen = false;

    readToken(Token.KEYWORD_SWITCH);
    readToken(Token.OPERATOR_OPENPAREN);
    Expression switchExpression = parseExpression(true);
    readToken(Token.OPERATOR_CLOSEPAREN);

    readToken(Token.OPERATOR_OPENBRACE);

    while (nextToken != Token.OPERATOR_CLOSEBRACE) {
      Expression caseExpression = null;
      Vector caseStatements = new Vector();

      if (nextToken == Token.KEYWORD_CASE) {
        readToken(Token.KEYWORD_CASE);
        caseExpression = parseExpression(true);
        readToken(Token.OPERATOR_COLON);
      } else {
        if (defaultSeen == false) {
          defaultSeen = true;
        } else {
          throwCompilerException("duplication default clause in switch statement");
        }

        readToken(Token.KEYWORD_DEFAULT);
        caseExpression = null;
        readToken(Token.OPERATOR_COLON);
      }

      while (nextToken != Token.KEYWORD_CASE
          && nextToken != Token.KEYWORD_DEFAULT
          && nextToken != Token.OPERATOR_CLOSEBRACE) {
        caseStatements.addElement(parseStatement());
      }

      caseClauseVector.addElement(
          new CaseStatement(caseExpression, Util.vectorToStatementArray(caseStatements)));
    }

    readToken(Token.OPERATOR_CLOSEBRACE);

    CaseStatement[] caseClauseArray = new CaseStatement[caseClauseVector.size()];

    caseClauseVector.copyInto(caseClauseArray);

    return new SwitchStatement(switchExpression, caseClauseArray);
  }

  private Statement parseThrowStatement() throws CompilerException {
    readToken(Token.KEYWORD_THROW);

    return new ThrowStatement(parseExpression(true));
  }

  private Statement parseTryStatement() throws CompilerException {
    Statement tryBlock = null;
    Identifier catchIdentifier = null;
    Statement catchBlock = null;
    Statement finallyBlock = null;

    readToken(Token.KEYWORD_TRY);
    tryBlock = parseBlockStatement();

    if (nextToken != Token.KEYWORD_CATCH && nextToken != Token.KEYWORD_FINALLY) {
      throwCompilerException("catch or finally expected after try");
    }

    if (nextToken == Token.KEYWORD_CATCH) {
      readToken(Token.KEYWORD_CATCH);
      readToken(Token.OPERATOR_OPENPAREN);
      catchIdentifier = parseIdentifier();
      readToken(Token.OPERATOR_CLOSEPAREN);
      catchBlock = parseBlockStatement();
    }

    if (nextToken == Token.KEYWORD_FINALLY) {
      readToken(Token.KEYWORD_FINALLY);
      finallyBlock = parseBlockStatement();
    }

    return new TryStatement(tryBlock, catchIdentifier, catchBlock, finallyBlock);
  }

  private Statement parseVariableStatement() throws CompilerException {
    Vector declarationVector = new Vector();

    readToken(Token.KEYWORD_VAR);

    // there must be at least one variable declaration

    declarationVector.addElement(parseVariableDeclaration(true));
    while (nextToken == Token.OPERATOR_COMMA) {
      readToken(Token.OPERATOR_COMMA);
      declarationVector.addElement(parseVariableDeclaration(true));
    }

    readTokenSemicolon();

    return new VariableStatement(Util.vectorToDeclarationArray(declarationVector));
  }

  private Statement parseWhileStatement() throws CompilerException {
    Statement statement;
    Expression expression;

    readToken(Token.KEYWORD_WHILE);
    readToken(Token.OPERATOR_OPENPAREN);
    expression = parseExpression(true);
    readToken(Token.OPERATOR_CLOSEPAREN);
    statement = parseStatement();

    return new WhileStatement(expression, statement);
  }

  private Statement parseWithStatement() throws CompilerException {
    Statement statement;
    Expression expression;

    readToken(Token.KEYWORD_WITH);
    readToken(Token.OPERATOR_OPENPAREN);
    expression = parseExpression(true);
    readToken(Token.OPERATOR_CLOSEPAREN);
    statement = parseStatement();

    return new WithStatement(expression, statement);
  }

  //
  // Expressions
  //

  private VariableDeclaration parseVariableDeclaration(boolean inFlag) throws CompilerException {
    Identifier identifier = parseIdentifier();
    Expression initializer = null;

    if (nextToken == Token.OPERATOR_ASSIGNMENT) {
      readToken(Token.OPERATOR_ASSIGNMENT);
      initializer = parseAssignmentExpression(inFlag);
    }

    return new VariableDeclaration(identifier, initializer);
  }

  private Expression parseExpression(boolean inFlag) throws CompilerException {
    Expression left = parseAssignmentExpression(inFlag);

    // comma expressions are left associative
    while (true) {
      if (nextToken == Token.OPERATOR_COMMA) {
        readToken(Token.OPERATOR_COMMA);
        Expression right = parseAssignmentExpression(inFlag);
        left = new BinaryOperatorExpression(left, right, Token.OPERATOR_COMMA);
      } else {
        return left;
      }
    }
  }

  private Expression parseAssignmentExpression(boolean inFlag) throws CompilerException {
    Expression left = parseConditionalExpression(inFlag);

    // assignment expressions are right associative
    if (nextToken == Token.OPERATOR_ASSIGNMENT) {
      readToken();
      return new AssignmentExpression(left, parseAssignmentExpression(inFlag));
    } else if (nextToken == Token.OPERATOR_ASSIGNMENT
            || nextToken == Token.OPERATOR_MULTIPLYASSIGNMENT
            || nextToken == Token.OPERATOR_DIVIDEASSIGNMENT
            || nextToken == Token.OPERATOR_MODULOASSIGNMENT
            || nextToken == Token.OPERATOR_PLUSASSIGNMENT
            || nextToken == Token.OPERATOR_MINUSASSIGNMENT
            || nextToken == Token.OPERATOR_SHIFTLEFTASSIGNMENT
            || nextToken == Token.OPERATOR_SHIFTRIGHTASSIGNMENT
            || nextToken == Token.OPERATOR_SHIFTRIGHTUNSIGNEDASSIGNMENT
            || nextToken == Token.OPERATOR_BITWISEANDASSIGNMENT
            || nextToken == Token.OPERATOR_BITWISEORASSIGNMENT
            || nextToken == Token.OPERATOR_BITWISEXORASSIGNMENT) {
      Token op = nextToken;
      readToken();
      return new AssignmentOperatorExpression(left, parseAssignmentExpression(inFlag), op);
    } else {
      return left;
    }
  }

  private Expression parseConditionalExpression(boolean inFlag) throws CompilerException {
    Expression expression = parseLogicalOrExpression(inFlag);

    // conditional expressions are right associative
    if (nextToken == Token.OPERATOR_CONDITIONAL) {
      readToken(Token.OPERATOR_CONDITIONAL);
      Expression trueExpression = parseAssignmentExpression(inFlag);
      readToken(Token.OPERATOR_COLON);
      Expression falseExpression = parseAssignmentExpression(inFlag);
      return new ConditionalExpression(expression, trueExpression, falseExpression);
    } else {
      return expression;
    }
  }

  private Expression parseLogicalOrExpression(boolean inFlag) throws CompilerException {
    Expression left = parseLogicalAndExpression(inFlag);
    Expression right;

    // logical or expressions are left associative
    while (true) {
      if (nextToken == Token.OPERATOR_LOGICALOR) {
        readToken(Token.OPERATOR_LOGICALOR);
        right = parseLogicalAndExpression(inFlag);
        left = new LogicalOrExpression(left, right);
      } else {
        return left;
      }
    }
  }

  private Expression parseLogicalAndExpression(boolean inFlag) throws CompilerException {
    Expression left = parseBitwiseOrExpression(inFlag);
    Expression right;

    // logical and expressions are left associative
    while (true) {
      if (nextToken == Token.OPERATOR_LOGICALAND) {
        readToken(Token.OPERATOR_LOGICALAND);
        right = parseBitwiseOrExpression(inFlag);
        left = new LogicalAndExpression(left, right);
      } else {
        return left;
      }
    }
  }

  private Expression parseBitwiseOrExpression(boolean inFlag) throws CompilerException {
    Expression left = parseBitwiseXorExpression(inFlag);
    Expression right;

    // bitwise or expressions are left associative
    while (true) {
      if (nextToken == Token.OPERATOR_BITWISEOR) {
        readToken(Token.OPERATOR_BITWISEOR);
        right = parseBitwiseXorExpression(inFlag);
        left = new BinaryOperatorExpression(left, right, Token.OPERATOR_BITWISEOR);
      } else {
        return left;
      }
    }
  }

  private Expression parseBitwiseXorExpression(boolean inFlag) throws CompilerException {
    Expression left = parseBitwiseAndExpression(inFlag);
    Expression right;

    // bitwise xor expressions are left associative
    while (true) {
      if (nextToken == Token.OPERATOR_BITWISEXOR) {
        readToken(Token.OPERATOR_BITWISEXOR);
        right = parseBitwiseAndExpression(inFlag);
        left = new BinaryOperatorExpression(left, right, Token.OPERATOR_BITWISEXOR);
      } else {
        return left;
      }
    }
  }

  private Expression parseBitwiseAndExpression(boolean inFlag) throws CompilerException {
    Expression left = parseEqualityExpression(inFlag);
    Expression right;

    // bitwise and expressions are left associative
    while (true) {
      if (nextToken == Token.OPERATOR_BITWISEAND) {
        readToken(Token.OPERATOR_BITWISEAND);
        right = parseEqualityExpression(inFlag);
        left = new BinaryOperatorExpression(left, right, Token.OPERATOR_BITWISEAND);
      } else {
        return left;
      }
    }
  }

  private Expression parseEqualityExpression(boolean inFlag) throws CompilerException {
    Expression left = parseRelationalExpression(inFlag);
    Expression right;

    // equality expressions are left associative
    while (true) {
      if (nextToken == Token.OPERATOR_EQUALEQUAL) {
        readToken(Token.OPERATOR_EQUALEQUAL);
        right = parseRelationalExpression(inFlag);
        left = new BinaryOperatorExpression(left, right, Token.OPERATOR_EQUALEQUAL);
      } else if (nextToken == Token.OPERATOR_NOTEQUAL) {
        readToken(Token.OPERATOR_NOTEQUAL);
        right = parseRelationalExpression(inFlag);
        left = new BinaryOperatorExpression(left, right, Token.OPERATOR_NOTEQUAL);
      } else if (nextToken == Token.OPERATOR_EQUALEQUALEQUAL) {
        readToken(Token.OPERATOR_EQUALEQUALEQUAL);
        right = parseRelationalExpression(inFlag);
        left = new BinaryOperatorExpression(left, right, Token.OPERATOR_EQUALEQUALEQUAL);
      } else if (nextToken == Token.OPERATOR_NOTEQUALEQUAL) {
        readToken(Token.OPERATOR_NOTEQUALEQUAL);
        right = parseRelationalExpression(inFlag);
        left = new BinaryOperatorExpression(left, right, Token.OPERATOR_NOTEQUALEQUAL);
      } else {
        return left;
      }
    }
  }

  private Expression parseRelationalExpression(boolean inFlag) throws CompilerException {
    Expression left = parseShiftExpression();
    Expression right;

    // relational expressions are left associative
    while (true) {
      if (nextToken == Token.OPERATOR_LESSTHAN) {
        readToken(Token.OPERATOR_LESSTHAN);
        right = parseShiftExpression();
        left = new BinaryOperatorExpression(left, right, Token.OPERATOR_LESSTHAN);
      } else if (nextToken == Token.OPERATOR_GREATERTHAN) {
        readToken(Token.OPERATOR_GREATERTHAN);
        right = parseShiftExpression();
        left = new BinaryOperatorExpression(left, right, Token.OPERATOR_GREATERTHAN);
      } else if (nextToken == Token.OPERATOR_LESSTHANOREQUAL) {
        readToken(Token.OPERATOR_LESSTHANOREQUAL);
        right = parseShiftExpression();
        left = new BinaryOperatorExpression(left, right, Token.OPERATOR_LESSTHANOREQUAL);
      } else if (nextToken == Token.OPERATOR_GREATERTHANOREQUAL) {
        readToken(Token.OPERATOR_GREATERTHANOREQUAL);
        right = parseShiftExpression();
        left = new BinaryOperatorExpression(left, right, Token.OPERATOR_GREATERTHANOREQUAL);
      } else if (nextToken == Token.KEYWORD_INSTANCEOF) {
        readToken(Token.KEYWORD_INSTANCEOF);
        right = parseShiftExpression();
        left = new BinaryOperatorExpression(left, right, Token.KEYWORD_INSTANCEOF);
      } else if (inFlag && nextToken == Token.KEYWORD_IN) {
        readToken(Token.KEYWORD_IN);
        right = parseShiftExpression();
        left = new BinaryOperatorExpression(left, right, Token.KEYWORD_IN);
      } else {
        return left;
      }
    }
  }

  private Expression parseShiftExpression() throws CompilerException {
    Expression left = parseAdditionExpression();
    Expression right;

    // shift expressions are left associative
    while (true) {
      if (nextToken == Token.OPERATOR_SHIFTLEFT) {
        readToken(Token.OPERATOR_SHIFTLEFT);
        right = parseAdditionExpression();
        left = new BinaryOperatorExpression(left, right, Token.OPERATOR_SHIFTLEFT);
      } else if (nextToken == Token.OPERATOR_SHIFTRIGHT) {
        readToken(Token.OPERATOR_SHIFTRIGHT);
        right = parseAdditionExpression();
        left = new BinaryOperatorExpression(left, right, Token.OPERATOR_SHIFTRIGHT);
      } else if (nextToken == Token.OPERATOR_SHIFTRIGHTUNSIGNED) {
        readToken(Token.OPERATOR_SHIFTRIGHTUNSIGNED);
        right = parseAdditionExpression();
        left = new BinaryOperatorExpression(left, right, Token.OPERATOR_SHIFTRIGHTUNSIGNED);
      } else {
        return left;
      }
    }
  }

  private Expression parseAdditionExpression() throws CompilerException {
    Expression left = parseMultiplyExpression();
    Expression right;

    // addition expressions are left associative
    while (true) {
      if (nextToken == Token.OPERATOR_PLUS) {
        readToken(Token.OPERATOR_PLUS);
        right = parseMultiplyExpression();
        left = new BinaryOperatorExpression(left, right, Token.OPERATOR_PLUS);
      } else if (nextToken == Token.OPERATOR_MINUS) {
        readToken(Token.OPERATOR_MINUS);
        right = parseMultiplyExpression();
        left = new BinaryOperatorExpression(left, right, Token.OPERATOR_MINUS);
      } else {
        return left;
      }
    }
  }

  private Expression parseMultiplyExpression() throws CompilerException {
    Expression left = parseUnaryExpression();
    Expression right;

    // multiplicative expressions are left associative
    while (true) {
      if (nextToken == Token.OPERATOR_MULTIPLY) {
        readToken(Token.OPERATOR_MULTIPLY);
        right = parseUnaryExpression();
        left = new BinaryOperatorExpression(left, right, Token.OPERATOR_MULTIPLY);
      } else if (nextToken == Token.OPERATOR_DIVIDE) {
        readToken(Token.OPERATOR_DIVIDE);
        right = parseUnaryExpression();
        left = new BinaryOperatorExpression(left, right, Token.OPERATOR_DIVIDE);
      } else if (nextToken == Token.OPERATOR_MODULO) {
        readToken(Token.OPERATOR_MODULO);
        right = parseUnaryExpression();
        left = new BinaryOperatorExpression(left, right, Token.OPERATOR_MODULO);
      } else {
        return left;
      }
    }
  }

  private Expression parseUnaryExpression() throws CompilerException {
    // TODO parse '-' numeric literal directly into literals,
    // to ensure that -0 keeps its proper value.

    // unary expressions are right associative
    if (nextToken == Token.OPERATOR_PLUSPLUS) {
      readToken(Token.OPERATOR_PLUSPLUS);
      return new IncrementExpression(parseUnaryExpression(), 1, false);
    } else if (nextToken == Token.OPERATOR_MINUSMINUS) {
      readToken(Token.OPERATOR_MINUSMINUS);
      return new IncrementExpression(parseUnaryExpression(), -1, false);
    } else if (nextToken == Token.OPERATOR_PLUS
            || nextToken == Token.OPERATOR_MINUS
            || nextToken == Token.OPERATOR_BITWISENOT
            || nextToken == Token.OPERATOR_LOGICALNOT
            || nextToken == Token.KEYWORD_VOID
            || nextToken == Token.KEYWORD_TYPEOF) {
      Token token = nextToken;
      readToken();
      UnaryOperatorExpression result = new UnaryOperatorExpression(parseUnaryExpression(), token);
      return result;
    } else if (nextToken == Token.KEYWORD_DELETE) {
      readToken(Token.KEYWORD_DELETE);
      return new DeleteExpression(parseUnaryExpression());
    } else {
      return parsePostfixExpression();
    }
  }

  private Expression parsePostfixExpression() throws CompilerException {
    // TODO this can be merged with parseUnary().

    Expression expression = parseMemberExpression(false);

    // postfix expressions aren't associative
    if (nextToken == Token.OPERATOR_PLUSPLUS) {
      readToken(Token.OPERATOR_PLUSPLUS);
      return new IncrementExpression(expression, 1, true);
    } else if (nextToken == Token.OPERATOR_MINUSMINUS) {
      readToken(Token.OPERATOR_MINUSMINUS);
      return new IncrementExpression(expression, -1, true);
    } else {
      return expression;
    }
  }

  /**
   * The grammar for the 'new' keyword is a little complicated. The keyword
   * 'new' can occur in either a NewExpression (where its not followed by
   * an argument list) or in MemberExpresison (where it is followed by an
   * argument list). The intention seems to be that an argument list should
   * bind to any unmatched 'new' keyword to the left in the same expression
   * if possible, otherwise an argument list is taken as a call expression.
   *
   * Since the rest of the productions for NewExpressions and CallExpressions
   * are similar we roll these two into one routine with a parameter to
   * indicate whether we're currently parsing a 'new' expression or not.
   *
   * @param newFlag if we're currently parsing a 'new' expression
   * @return an expression
   */
  private Expression parseMemberExpression(boolean newFlag) throws CompilerException {
    Expression expression;

    // new expressions are right associative
    if (nextToken == Token.KEYWORD_NEW) {
      Expression name;

      readToken(Token.KEYWORD_NEW);
      name = parseMemberExpression(true);

      if (nextToken == Token.OPERATOR_OPENPAREN) {
        Expression[] arguments = parseArgumentList();
        expression = new NewExpression(name, arguments);
      } else {
        expression = new NewExpression(name, null);
      }

    } else if (nextToken == Token.KEYWORD_FUNCTION) {
      expression = parseFunctionLiteral(false);

    } else {
      expression = parsePrimaryExpression();
    }

    // call expressions are left associative
    while (true) {
      if (!newFlag && nextToken == Token.OPERATOR_OPENPAREN) {
        Expression[] arguments = parseArgumentList();
        expression = new CallExpression(expression, arguments);
      } else if (nextToken == Token.OPERATOR_OPENSQUARE) {
        readToken(Token.OPERATOR_OPENSQUARE);
        Expression property = parseExpression(true);
        readToken(Token.OPERATOR_CLOSESQUARE);
        expression = new PropertyExpression(expression, property);
      } else if (nextToken == Token.OPERATOR_DOT) {
        // transform x.bar to x["bar"]
        readToken(Token.OPERATOR_DOT);
        Identifier identifier = parseIdentifier();
        expression = new PropertyExpression(expression, new StringLiteral(identifier.string));
      } else {
        return expression;
      }
    }
  }

  private Expression[] parseArgumentList() throws CompilerException {
    Vector argumentVector = new Vector();

    readToken(Token.OPERATOR_OPENPAREN);

    if (nextToken != Token.OPERATOR_CLOSEPAREN) {
      argumentVector.addElement(parseAssignmentExpression(true));
      while (nextToken == Token.OPERATOR_COMMA) {
        readToken(Token.OPERATOR_COMMA);
        argumentVector.addElement(parseAssignmentExpression(true));
      }
    }

    readToken(Token.OPERATOR_CLOSEPAREN);

    return Util.vectorToExpressionArray(argumentVector);
  }

  private Expression parsePrimaryExpression() throws CompilerException {
    if (nextToken == Token.KEYWORD_THIS) {
      readToken(Token.KEYWORD_THIS);
      return new ThisLiteral();

    } else if (nextToken == Token.KEYWORD_NULL) {
      readToken(Token.KEYWORD_NULL);
      return new NullLiteral();

    } else if (nextToken == Token.KEYWORD_TRUE) {
      readToken(Token.KEYWORD_TRUE);
      return new BooleanLiteral(true);

    } else if (nextToken == Token.KEYWORD_FALSE) {
      readToken(Token.KEYWORD_FALSE);
      return new BooleanLiteral(false);

    } else if (nextToken == Token.OPERATOR_OPENPAREN) {
      readToken(Token.OPERATOR_OPENPAREN);
      Expression expression = parseExpression(true);
      readToken(Token.OPERATOR_CLOSEPAREN);
      return expression;

    } else if (nextToken == Token.OPERATOR_OPENBRACE) {
      return parseObjectLiteral();

    } else if (nextToken == Token.OPERATOR_OPENSQUARE) {
      return parseArrayLiteral();

    } else if (nextToken.isIdentifier()) {
      return parseIdentifier();

    } else if (nextToken.isStringLiteral()) {
      return parseStringLiteral();

    } else if (nextToken.isNumericLiteral()) {
      return parseNumericLiteral();

    } else {
      throwCompilerException("identifier or literal expected at token: " + nextToken);
      
    }
    
    return null;
  }

  private ArrayLiteral parseArrayLiteral() throws CompilerException {
    Vector arrayElements = new Vector();

    readToken(Token.OPERATOR_OPENSQUARE);

    while (nextToken != Token.OPERATOR_CLOSESQUARE) {
      if (nextToken == Token.OPERATOR_COMMA) {
        arrayElements.addElement(null);
      } else {
        arrayElements.addElement(parseAssignmentExpression(true));
      }

      if (nextToken != Token.OPERATOR_CLOSESQUARE) {
        readToken(Token.OPERATOR_COMMA);
      }
    }

    readToken(Token.OPERATOR_CLOSESQUARE);

    return new ArrayLiteral(Util.vectorToExpressionArray(arrayElements));
  }

  private FunctionLiteral parseFunctionLiteral(boolean nameFlag) throws CompilerException {
    Identifier name = null;
    Vector parameterVector = new Vector();
    Vector statementVector = new Vector();

    readToken(Token.KEYWORD_FUNCTION);

    if (nameFlag || nextToken != Token.OPERATOR_OPENPAREN) {
      name = parseIdentifier();
    }

    // function literals may have zero or more parameters.

    readToken(Token.OPERATOR_OPENPAREN);

    if (nextToken != Token.OPERATOR_CLOSEPAREN) {
      parameterVector.addElement(parseIdentifier());

      while (nextToken != Token.OPERATOR_CLOSEPAREN) {
        readToken(Token.OPERATOR_COMMA);
        parameterVector.addElement(parseIdentifier());
      }
    }

    readToken(Token.OPERATOR_CLOSEPAREN);

    // function literals are required the have at least one SourceElement.

    readToken(Token.OPERATOR_OPENBRACE);

    // statementVector.addElement(parseStatement());

    while (nextToken != Token.OPERATOR_CLOSEBRACE) {
      statementVector.addElement(parseSourceElement());
    }

    readToken(Token.OPERATOR_CLOSEBRACE);

    return new FunctionLiteral(name,
        Util.vectorToIdentifierArray(parameterVector),
        Util.vectorToStatementArray(statementVector)
    );
  }

  private ObjectLiteral parseObjectLiteral() throws CompilerException {
    Vector propertyVector = new Vector();

    readToken(Token.OPERATOR_OPENBRACE);

    while (nextToken != Token.OPERATOR_CLOSEBRACE) {
      propertyVector.addElement(parseObjectLiteralProperty());

      while (nextToken == Token.OPERATOR_COMMA) {
        readToken(Token.OPERATOR_COMMA);
        propertyVector.addElement(parseObjectLiteralProperty());
      }
    }

    readToken(Token.OPERATOR_CLOSEBRACE);

    ObjectLiteralProperty[] propertyArray = new ObjectLiteralProperty[propertyVector.size()];

    propertyVector.copyInto(propertyArray);

    return new ObjectLiteral(propertyArray);
  }

  private ObjectLiteralProperty parseObjectLiteralProperty() throws CompilerException {
    Expression propertyName = null;
    Expression propertyValue = null;

    if (nextToken.isIdentifier()) {
      propertyName = new StringLiteral(parseIdentifier().string);
    } else if (nextToken.isStringLiteral()) {
      propertyName = parseStringLiteral();
    } else if (nextToken.isNumericLiteral()) {
      propertyName = parseNumericLiteral();
    } else {
      throwCompilerException("identifier or numeric literal expected");
    }

    readToken(Token.OPERATOR_COLON);

    propertyValue = parseAssignmentExpression(true);

    return new ObjectLiteralProperty(propertyName, propertyValue);
  }

  private Identifier parseIdentifier() throws CompilerException {
    Identifier identifier = null;

    if (nextToken.getType() == Token.TYPE_IDENTIFIER) {
      identifier = new Identifier(nextToken.getValue());
    } else {
      throwCompilerException("identifier or numeric literal expected");
    }

    readToken();

    return identifier;
  }

  private StringLiteral parseStringLiteral() throws CompilerException {
    String string = null;

    if (nextToken.getType() == Token.TYPE_STRING) {
      string = nextToken.getValue();
    } else {
      throwCompilerException("string literal expected");
    }

    readToken();

    return new StringLiteral(string);
  }

  private NumberLiteral parseNumericLiteral() throws CompilerException {
    double value = 0.0;

    try {
      switch (nextToken.getType()) {
        case Token.TYPE_FLOAT:
          value = Double.parseDouble(nextToken.getValue());
          break;

        case Token.TYPE_DECIMAL:
          value = Long.parseLong(nextToken.getValue());
          break;

        case Token.TYPE_OCTAL:
          value = Long.parseLong(nextToken.getValue().substring(1), 8);
          break;

        case Token.TYPE_HEXADECIMAL:
          value = Long.parseLong(nextToken.getValue().substring(2), 16);
          break;

        default:
          throwCompilerException("numeric literal expected");
      }
    } catch (NumberFormatException e) {
      value = Double.NaN;
    }

    readToken();

    return new NumberLiteral(value);
  }

  //
  // Tokens
  //

  private int getLineNumber() {
    return lexer.getLineNumber();
  }

  private void readToken() throws CompilerException {
    seenNewline = false;

    do {
      nextToken = lexer.nextToken();

      if (nextToken.isEOF() || nextToken.isNewLine()) {
        seenNewline = true;
      }
    } while (nextToken.isWhitespace());
  }

  private void readToken(Token token) throws CompilerException {
    if (nextToken == token) {
      readToken();
    } else {
      throwCompilerException("expected '" + token.getValue() + "'");
    }
  }

  private void readTokenSemicolon() throws CompilerException {
    if (nextToken == Token.OPERATOR_SEMICOLON) {
      readToken();
    } else if (nextToken == Token.OPERATOR_CLOSEBRACE) {
      // semicolon insertion
    } else if (seenNewline) {
      // semicolon insertion
    } else {
      throwCompilerException("expected '" + Token.OPERATOR_SEMICOLON.getValue() + "'");
    }
  }
  
  private void throwCompilerException(String message) throws CompilerException {
    throw new CompilerException(message, getLineNumber());
  }
}
