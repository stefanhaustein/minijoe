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

import java.util.Hashtable;

/**
 * Lexer is a lexical analyzer for MiniJoe, a JavaScript runtime for J2ME.
 *
 * <p>This lexical analyzer handles all parts of the ECMAScript v3
 * specification, except for:
 * <ul>
 * <li>no support for regular expressions
 * <li>limited Unicode character support
 * </ul>
 *
 * @see <a href="http://wiki.corp.google.com/twiki/bin/view/Main/MiniJoe">MiniJoe</a>
 * @see <a href="http://www.mozilla.org/js/language/E262-3.pdf">ECMAScript v3 Specification</a>
 *
 * @author Andy Hayward
 */
public class Lexer {
  private static final int BASE_HEXADECIMAL = 16;
  private static final int BASE_OCTAL = 8;

  private static final int TOKENIZENUMERIC_ENTRY_POINT          =  0;
  private static final int TOKENIZENUMERIC_LEADING_ZERO         =  1;
  private static final int TOKENIZENUMERIC_LEADING_DECIMAL      =  2;
  private static final int TOKENIZENUMERIC_OCTAL_LITERAL        =  3;
  private static final int TOKENIZENUMERIC_LEADING_OX           =  4;
  private static final int TOKENIZENUMERIC_HEXADECIMAL_LITERAL  =  5;
  private static final int TOKENIZENUMERIC_DECIMAL_LITERAL      =  6;
  private static final int TOKENIZENUMERIC_DECIMAL_POINT        =  7;
  private static final int TOKENIZENUMERIC_FRACTIONAL_PART      =  8;
  private static final int TOKENIZENUMERIC_EXPONENT_SYMBOL      =  9;
  private static final int TOKENIZENUMERIC_EXPONENT_SIGN        = 10;
  private static final int TOKENIZENUMERIC_EXPONENT_PART        = 11;
  private static final int TOKENIZENUMERIC_UNREAD_TWO           = 12;
  private static final int TOKENIZENUMERIC_UNREAD_ONE           = 13;
  private static final int TOKENIZENUMERIC_RETURN_FLOAT         = 14;
  private static final int TOKENIZENUMERIC_RETURN_DECIMAL       = 15;
  private static final int TOKENIZENUMERIC_RETURN_OCTAL         = 16;
  private static final int TOKENIZENUMERIC_RETURN_HEXADECIMAL   = 17;
  private static final int TOKENIZENUMERIC_RETURN_OPERATOR_DOT  = 18;

  private Hashtable keywords = null;
  private String input = null;
  int lineNumber = 1;
  int maxPosition;
  int curPosition;
  int oldPosition;
  int c;

  /**
   * Creates a Lexer for the specified source string.
   *
   * @param input the string to tokenize
   */
  public Lexer(String input) {
    if (input == null) {
      throw new IllegalArgumentException();
    }

    // initialization

    this.input = input;

    initKeywords();

    // prime the main loop

    maxPosition = input.length();
    oldPosition = 0;
    curPosition = 0;

    c = curPosition < maxPosition ? input.charAt(curPosition) : -1;
  }

  /**
   * Adds a keyword token to the keyword hashtable.
   */
  private void addKeyword(Token keyword) {
    keywords.put(keyword.getValue(), keyword);
  }

  /**
   * Gets the line number of the most recently returned token.
   */
  public int getLineNumber() {
    return lineNumber;
  }

  /**
   * Initializes the keyword map.
   */
  private void initKeywords() {
    keywords = new Hashtable();

    // keywords

    addKeyword(Token.KEYWORD_BREAK);
    addKeyword(Token.KEYWORD_CASE);
    addKeyword(Token.KEYWORD_CATCH);
    addKeyword(Token.KEYWORD_CONTINUE);
    addKeyword(Token.KEYWORD_DEFAULT);
    addKeyword(Token.KEYWORD_DELETE);
    addKeyword(Token.KEYWORD_DO);
    addKeyword(Token.KEYWORD_ELSE);
    addKeyword(Token.KEYWORD_FALSE);
    addKeyword(Token.KEYWORD_FINALLY);
    addKeyword(Token.KEYWORD_FOR);
    addKeyword(Token.KEYWORD_FUNCTION);
    addKeyword(Token.KEYWORD_IF);
    addKeyword(Token.KEYWORD_IN);
    addKeyword(Token.KEYWORD_INSTANCEOF);
    addKeyword(Token.KEYWORD_NEW);
    addKeyword(Token.KEYWORD_NULL);
    addKeyword(Token.KEYWORD_RETURN);
    addKeyword(Token.KEYWORD_SWITCH);
    addKeyword(Token.KEYWORD_THIS);
    addKeyword(Token.KEYWORD_THROW);
    addKeyword(Token.KEYWORD_TRUE);
    addKeyword(Token.KEYWORD_TRY);
    addKeyword(Token.KEYWORD_TYPEOF);
    addKeyword(Token.KEYWORD_VAR);
    addKeyword(Token.KEYWORD_VOID);
    addKeyword(Token.KEYWORD_WHILE);
    addKeyword(Token.KEYWORD_WITH);

    // reserved keywords

    addKeyword(Token.KEYWORD_ABSTRACT);
    addKeyword(Token.KEYWORD_BOOLEAN);
    addKeyword(Token.KEYWORD_BYTE);
    addKeyword(Token.KEYWORD_CHAR);
    addKeyword(Token.KEYWORD_CLASS);
    addKeyword(Token.KEYWORD_CONST);
    addKeyword(Token.KEYWORD_DEBUGGER);
    addKeyword(Token.KEYWORD_DOUBLE);
    addKeyword(Token.KEYWORD_ENUM);
    addKeyword(Token.KEYWORD_EXPORT);
    addKeyword(Token.KEYWORD_EXTENDS);
    addKeyword(Token.KEYWORD_FINAL);
    addKeyword(Token.KEYWORD_FLOAT);
    addKeyword(Token.KEYWORD_GOTO);
    addKeyword(Token.KEYWORD_IMPLEMENTS);
    addKeyword(Token.KEYWORD_IMPORT);
    addKeyword(Token.KEYWORD_INT);
    addKeyword(Token.KEYWORD_INTERFACE);
    addKeyword(Token.KEYWORD_LONG);
    addKeyword(Token.KEYWORD_NATIVE);
    addKeyword(Token.KEYWORD_PACKAGE);
    addKeyword(Token.KEYWORD_PRIVATE);
    addKeyword(Token.KEYWORD_PROTECTED);
    addKeyword(Token.KEYWORD_PUBLIC);
    addKeyword(Token.KEYWORD_SHORT);
    addKeyword(Token.KEYWORD_STATIC);
    addKeyword(Token.KEYWORD_SUPER);
    addKeyword(Token.KEYWORD_SYNCHRONIZED);
    addKeyword(Token.KEYWORD_THROWS);
    addKeyword(Token.KEYWORD_TRANSIENT);
    addKeyword(Token.KEYWORD_VOLATILE);
  }

  /**
   * Returns true if the end of input has been reached.
   */
  private boolean isEOF() {
    return c == -1;
  }

  /**
   * Returns true if the current character is a line terminator.
   */
  private boolean isLineTerminator() {
    return c == '\n'
        || c == '\r'
        || c == '\u2028'
        || c == '\u2029';
  }

  /**
   * Returns true if the current character is a whitespace character.
   */
  private boolean isWhitespace() {
    return c == '\u0009'
        || c == '\u000B'
        || c == '\u000C'
        || c == '\u0020'
        || c == '\u00A0';
  }

  /**
   * Returns true if the current character is an octal digit.
   */
  private boolean isOctalDigit() {
    return c == '0' || c == '1' || c == '2' || c == '3'
        || c == '4' || c == '5' || c == '6' || c == '7';
  }

  /**
   * Returns true if the current character is a decimal digit.
   */
  private boolean isDecimalDigit() {
    return c == '0' || c == '1' || c == '2' || c == '3' || c == '4'
        || c == '5' || c == '6' || c == '7' || c == '8' || c == '9';
  }

  /**
   * Returns true if the current character is a hexadecimal digit.
   */
  private boolean isHexadecimalDigit() {
    return c == '0' || c == '1' || c == '2' || c == '3' || c == '4'
        || c == '5' || c == '6' || c == '7' || c == '8' || c == '9'
        || c == 'a' || c == 'b' || c == 'c' || c == 'd' || c == 'e' || c == 'f'
        || c == 'A' || c == 'B' || c == 'C' || c == 'D' || c == 'E' || c == 'F';
  }

  /**
   * Returns true if the current character is a valid identifier start
   * character.
   */
  private boolean isIdentifierStart() {
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '$' || c == '_';
  }

  /**
   * Returns true if the current character is a valid as part of an identifier.
   */
  private boolean isIdentifierPart() {
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
        || (c >= '0' && c <= '9') || c == '$' || c == '_';
  }

  /**
   * Consumes the current character and reads the next.
   */
  private void readChar() {
    curPosition++;

    if (curPosition < maxPosition) {
      c = input.charAt(curPosition);
    } else {
      c = -1;
    }
  }

  /**
   * Unread the current character.  This MUST NOT be used to unread past a
   * line terminator character.
   */
  private void unreadChar() throws CompilerException {
    if (curPosition > 0) {
      if (isLineTerminator()) {
        throw new CompilerException("current character must not be a line terminator");
      }
      c = input.charAt(--curPosition);
    } else {
      c = -1;
    }
  }

  /**
   * Reads an identifier escape sequence.
   */
  private void readIdentifierEscapeSequence() throws CompilerException {
    readChar();

    if (isEOF()) {
      throwCompilerException("EOF in escape sequence");
    } else if (isLineTerminator()) {
      throwCompilerException("Line terminator in escape sequence");
    } else if (c == 'u') {
      readChar();
      if (isHexadecimalDigit()) {
        readHexEscapeSequence(4);
      } else {
        throwCompilerException("Invalid escape sequence");
      }
    } else {
      throwCompilerException("Invalid escape sequence");
    }
  }

  /**
   * Reads an string escape sequence.
   */
  private void readStringEscapeSequence() throws CompilerException {
    readChar();

    if (isEOF()) {
      throwCompilerException("EOF in escape sequence");
    } else if (isLineTerminator()) {
      throwCompilerException("Line terminator in escape sequence");
    } else if (c == 'b') {
      c = 0x0008;
    } else if (c == 't') {
      c = 0x0009;
    } else if (c == 'n') {
      c = 0x000a;
    } else if (c == 'v') {
      c = 0x000b;
    } else if (c == 'f') {
      c = 0x000c;
    } else if (c == 'r') {
      c = 0x000d;
    } else if (c == 'u') {
      readChar();
      if (isHexadecimalDigit()) {
        readHexEscapeSequence(4);
      } else {
        unreadChar();
      }
    } else if (c == 'x') {
      readChar();
      if (isHexadecimalDigit()) {
        readHexEscapeSequence(2);
      } else {
        unreadChar();
      }
    } else if (isOctalDigit()) {
      readOctalEscapeSequence();
    } else {
      // other characters escape themselves
    }
  }

  /**
   * Reads a hexadecimal escape sequence.
   *
   * @param count number of characters to read
   * @throws CompilerException if the escape sequence was malformed
   */
  private void readHexEscapeSequence(int count) throws CompilerException {
    int value = Character.digit((char) c, BASE_HEXADECIMAL);

    while (--count > 0) {
      readChar();

      if (!isHexadecimalDigit()) {
        throwCompilerException("Bad escape sequence");
      } else {
        value = (value << 4) + Character.digit((char) c, BASE_HEXADECIMAL);
      }
    }

    c = value;
  }

  /**
   * Reads an octal escape sequence of up to 3 octal digits.
   */
  private void readOctalEscapeSequence() throws CompilerException {

    int value = Character.digit((char) c, BASE_OCTAL);

    readChar();
    if (isOctalDigit()) {
      value = (value << 3) + Character.digit((char) c, BASE_OCTAL);
      readChar();
      if (isOctalDigit()) {
        value = (value << 3) + Character.digit((char) c, BASE_OCTAL);
      } else {
        unreadChar();
      }
    } else {
      unreadChar();
    }

    c = value;
  }

  /**
   * Skips a line terminator, updating the current line number.
   */
  private void skipLineTerminator() throws CompilerException {
    // This is not mentioned in the JavaScript specification, but "\r\n" is
    // usually recognised as a single newline and not as two separate newline
    // characters.

    if (c == '\r') {
      readChar();
      if (c == '\n') {
        readChar();
      }
    } else {
      readChar();
    }

    lineNumber++;
  }

  /**
   * Returns the next token from the input.
   */
  public Token nextToken() throws CompilerException {
    oldPosition = curPosition;

    if (isEOF()) {
      return Token.EOF;
    } else if (isLineTerminator()) {
      return tokenizeLineTerminator();
    } else if (isWhitespace()) {
      return tokenizeWhitespace();
    } else if (c == '/') {
      return tokenizeSlash();
    } else if (c == '.') {
      return tokenizeNumeric();
    } else if (isDecimalDigit()) {
      return tokenizeNumeric();
    } else if (c == '\'' || c == '\"') {
      return tokenizeString();
    } else if (isIdentifierStart()) {
      return tokenizeIdentifier();
    } else {
      return tokenizeOperator();
    }
  }

  /**
   * Tokenizes a line terminator.
   */
  private Token tokenizeLineTerminator() throws CompilerException {
    do {
      skipLineTerminator();
    } while (isLineTerminator());

    return Token.NEWLINE;
  }

  /**
   * Tokenizes whitespace.
   */
  private Token tokenizeWhitespace() {
    do {
      readChar();
    } while (isWhitespace());

    return Token.WHITESPACE;
  }

  /**
   * Tokenizes tokens that start with a forward slash.
   */
  private Token tokenizeSlash() throws CompilerException {
    readChar();

    if (c == '/') {
      return tokenizeSingleLineComment();
    } else if (c == '*') {
      return tokenizeMultiLineComment();
    } else if (c == '=') {
      readChar();
      return Token.OPERATOR_DIVIDEASSIGNMENT;
    } else {
      return Token.OPERATOR_DIVIDE;
    }
  }

  /**
   * Tokenizes a single line comment.
   */
  private Token tokenizeSingleLineComment() {
    do {
      readChar();
    }  while (!isEOF() && !isLineTerminator());

    return Token.SINGLELINECOMMENT;
  }

  /**
   * Tokenizes a multi line comment.
   */
  private Token tokenizeMultiLineComment() throws CompilerException {
    boolean lineTerminator = false;

    readChar();

    while (true) {
      if (isEOF()) {
        break;
      } else if (isLineTerminator()) {
        skipLineTerminator();
        lineTerminator = true;
      } else if (c == '*') {
        readChar();
        if (c == '/') {
          readChar();
          break;
        }
      } else {
        readChar();
      }
    }

    if (lineTerminator) {
      return Token.MULTILINECOMMENT;
    } else {
      return Token.SINGLELINECOMMENT;
    }
  }

  /**
   * Tokenizes a numeric literal.
   *
   * <p>Older versions of the JavaScript(TM) and ECMAScript specifications
   * included support for octal numeric literals.  Although the latest
   * version of the the specification (3rd edition, ECMA-262, 3rd December
   * 1999) doesn't provide support for them we do for compatibility reasons.
   *
   * <p>As a special case, numbers that start with "08" or "09" are treated
   * as decimal.  Strictly speaking such literals should be interpreted as
   * two numeric literals, a zero followed by a decimal whose leading digit
   * is 8 or 9.  However two consecutive integers are not legal according to
   * the ECMAScript grammar.
   */
  private Token tokenizeNumeric() throws CompilerException {
    int state = TOKENIZENUMERIC_ENTRY_POINT;

    while (true) {
      switch (state) {
        // entry point
        case TOKENIZENUMERIC_ENTRY_POINT:
          if (c == '.') {
            state = TOKENIZENUMERIC_LEADING_ZERO;
          } else if (c == '0') {
            state = TOKENIZENUMERIC_LEADING_DECIMAL;
          } else {
            state = TOKENIZENUMERIC_DECIMAL_LITERAL;
          }
          break;

        // leading decimal point
        case TOKENIZENUMERIC_LEADING_ZERO:
          readChar();
          if (isDecimalDigit()) {
            state = TOKENIZENUMERIC_DECIMAL_POINT;
          } else {
            state = TOKENIZENUMERIC_RETURN_OPERATOR_DOT;
          }
          break;

        // leading zero
        case TOKENIZENUMERIC_LEADING_DECIMAL:
          readChar();
          if (isOctalDigit()) {
            state = TOKENIZENUMERIC_OCTAL_LITERAL;
          } else if (c == 'x' || c == 'X') {
            state = TOKENIZENUMERIC_LEADING_OX;
          } else if (isDecimalDigit()) {
            state = TOKENIZENUMERIC_DECIMAL_LITERAL;
          } else if (c == '.') {
            state = TOKENIZENUMERIC_DECIMAL_POINT;
          } else if (c == 'e' || c == 'E') {
            state = TOKENIZENUMERIC_EXPONENT_SYMBOL;
          } else {
            state = TOKENIZENUMERIC_RETURN_DECIMAL;
          }
          break;

        // octal literal
        case TOKENIZENUMERIC_OCTAL_LITERAL:
            readChar();
          if (isOctalDigit()) {
            // loop
          } else {
            state = TOKENIZENUMERIC_RETURN_OCTAL;
          }
          break;

        // leading '0x' or '0X'
        case TOKENIZENUMERIC_LEADING_OX:
          readChar();
          if (isHexadecimalDigit()) {
            state = TOKENIZENUMERIC_HEXADECIMAL_LITERAL;
          } else {
            throwCompilerException("Invalid hexadecimal literal");
          }
          break;

        // hexadecimal literal
        case TOKENIZENUMERIC_HEXADECIMAL_LITERAL:
          readChar();
          if (isHexadecimalDigit()) {
            // loop
          } else {
            state = TOKENIZENUMERIC_RETURN_HEXADECIMAL;
          }
          break;

        // decimal literal
        case TOKENIZENUMERIC_DECIMAL_LITERAL:
          readChar();
          if (isDecimalDigit()) {
            // loop
          } else if (c == '.') {
            state = TOKENIZENUMERIC_DECIMAL_POINT;
          } else if (c == 'e' || c == 'E') {
            state = TOKENIZENUMERIC_EXPONENT_SYMBOL;
          } else {
            state = TOKENIZENUMERIC_RETURN_DECIMAL;
          }
          break;

        // decimal point
        case TOKENIZENUMERIC_DECIMAL_POINT:
          readChar();
          if (isDecimalDigit()) {
            state = TOKENIZENUMERIC_FRACTIONAL_PART;
          } else if (c == 'e' || c == 'E') {
            state = TOKENIZENUMERIC_EXPONENT_SYMBOL;
          } else {
            state = TOKENIZENUMERIC_RETURN_FLOAT;
          }
          break;

        // fractional part
        case TOKENIZENUMERIC_FRACTIONAL_PART:
          readChar();
          if (isDecimalDigit()) {
            // loop
          } else if (c == 'e' || c == 'E') {
            state = TOKENIZENUMERIC_EXPONENT_SYMBOL;
          } else {
            state = TOKENIZENUMERIC_RETURN_FLOAT;
          }
          break;

        // exponent symbol
        case TOKENIZENUMERIC_EXPONENT_SYMBOL:
          readChar();
          if (c == '+' || c == '-') {
            state = TOKENIZENUMERIC_EXPONENT_SIGN;
          } else if (isDecimalDigit()) {
            state = TOKENIZENUMERIC_EXPONENT_PART;
          } else {
            state = TOKENIZENUMERIC_UNREAD_ONE;
          }
          break;

        // exponent sign
        case TOKENIZENUMERIC_EXPONENT_SIGN:
          readChar();
          if (isDecimalDigit()) {
            state = TOKENIZENUMERIC_EXPONENT_PART;
          } else {
            state = TOKENIZENUMERIC_UNREAD_TWO;
          }
          break;

        // exponent part
        case TOKENIZENUMERIC_EXPONENT_PART:
          readChar();
          if (isDecimalDigit()) {
            // loop
          } else {
            state = TOKENIZENUMERIC_RETURN_FLOAT;
          }
          break;

        // unread two characters
        case TOKENIZENUMERIC_UNREAD_TWO:
          unreadChar();
          state = TOKENIZENUMERIC_UNREAD_ONE;
          break;

        // unread one character
        case TOKENIZENUMERIC_UNREAD_ONE:
          unreadChar();
          state = TOKENIZENUMERIC_RETURN_FLOAT;
          break;

        // floating literal
        case TOKENIZENUMERIC_RETURN_FLOAT:
          return new Token(Token.TYPE_FLOAT, input.substring(oldPosition, curPosition));

        // decimal literal
        case TOKENIZENUMERIC_RETURN_DECIMAL:
          return new Token(Token.TYPE_DECIMAL, input.substring(oldPosition, curPosition));

        // octal literal
        case TOKENIZENUMERIC_RETURN_OCTAL:
          return new Token(Token.TYPE_OCTAL, input.substring(oldPosition, curPosition));

        // hexadecimal literal
        case TOKENIZENUMERIC_RETURN_HEXADECIMAL:
          return new Token(Token.TYPE_HEXADECIMAL, input.substring(oldPosition, curPosition));

        // '.' operator
        case TOKENIZENUMERIC_RETURN_OPERATOR_DOT:
          return Token.OPERATOR_DOT;
      }
    }
  }

  /**
   * Tokenizes a ECMAScript string.  The current character is used as the
   * quote character.
   */
  private Token tokenizeString() throws CompilerException {
    StringBuffer buffer = new StringBuffer();
    int quote = c;

    // skip the leading quote
    readChar();

    while (true) {
      if (c == quote) {
        break;
      } else if (isEOF()) {
        throwCompilerException("EOF in string literal");
      } else if (isLineTerminator()) {
        throwCompilerException("Line terminator in string literal");
      } else if (c == '\\') {
        readStringEscapeSequence();
        buffer.append((char) c);
      } else {
        buffer.append((char) c);
      }

      readChar();
    }

    // skip the trailing quote
    readChar();

    return new Token(Token.TYPE_STRING, buffer.toString());
  }

  /**
   * Tokenizes a ECMAScript identifier.  On entry the current character must
   * be a valid identifier start character.
   */
  private Token tokenizeIdentifier() throws CompilerException {
    StringBuffer buffer = new StringBuffer();

    // identifier start

    buffer.append((char) c);
    readChar();

    // identifier part

    while (true) {
      if (isIdentifierPart()) {
        buffer.append((char) c);
      } else if (c == '\\') {
        readIdentifierEscapeSequence();
        if (isIdentifierPart()) {
          buffer.append((char) c);
        } else {
          throwCompilerException("Invalid escaped character in identifier");
        }
      } else {
        break;
      }

      readChar();
    }

    // If this identifier matches a keyword we need to return that keyword
    // token.

    Token token = (Token) keywords.get(buffer.toString());

    if (token != null) {
      return token;
    } else {
      return new Token(Token.TYPE_IDENTIFIER, buffer.toString());
    }
  }

  /**
   * Tokenizes an operator.
   */
  private Token tokenizeOperator() throws CompilerException {
    // TODO
    //   * analyse a suitable JavaScript corpus (Mozilla tests?) and order
    //     these tests in descending order of frequency.
    //   * consider using a switch statement for the first operator character.

    if (c == ';') {
      readChar();
      return Token.OPERATOR_SEMICOLON;

    } else if (c == ',') {
      readChar();
      return Token.OPERATOR_COMMA;

    } else if (c == '(') {
      readChar();
      return Token.OPERATOR_OPENPAREN;

    } else if (c == ')') {
      readChar();
      return Token.OPERATOR_CLOSEPAREN;

    } else if (c == '{') {
      readChar();
      return Token.OPERATOR_OPENBRACE;

    } else if (c == '}') {
      readChar();
      return Token.OPERATOR_CLOSEBRACE;

    } else if (c == '[') {
      readChar();
      return Token.OPERATOR_OPENSQUARE;

    } else if (c == ']') {
      readChar();
      return Token.OPERATOR_CLOSESQUARE;

    } else if (c == '?') {
      readChar();
      return Token.OPERATOR_CONDITIONAL;

    } else if (c == ':') {
      readChar();
      return Token.OPERATOR_COLON;

    } else if (c == '+') {
      readChar();
      if (c == '+') {
        readChar();
        return Token.OPERATOR_PLUSPLUS;
      } else if (c == '=') {
        readChar();
        return Token.OPERATOR_PLUSASSIGNMENT;
      } else {
        return Token.OPERATOR_PLUS;
      }

    } else if (c == '-') {
      readChar();
      if (c == '-') {
        readChar();
        return Token.OPERATOR_MINUSMINUS;
      } else if (c == '=') {
        readChar();
        return Token.OPERATOR_MINUSASSIGNMENT;
      } else {
        return Token.OPERATOR_MINUS;
      }

    } else if (c == '*') {
      readChar();
      if (c == '=') {
        readChar();
        return Token.OPERATOR_MULTIPLYASSIGNMENT;
      } else {
        return Token.OPERATOR_MULTIPLY;
      }

    } else if (c == '%') {
      readChar();
      if (c == '=') {
        readChar();
        return Token.OPERATOR_MODULOASSIGNMENT;
      } else {
        return Token.OPERATOR_MODULO;
      }

    } else if (c == '=') {
      readChar();
      if (c == '=') {
        readChar();
        if (c == '=') {
          readChar();
          return Token.OPERATOR_EQUALEQUALEQUAL;
        } else {
          return Token.OPERATOR_EQUALEQUAL;
        }
      } else {
        return Token.OPERATOR_ASSIGNMENT;
      }

    } else if (c == '!') {
      readChar();
      if (c == '=') {
        readChar();
        if (c == '=') {
          readChar();
          return Token.OPERATOR_NOTEQUALEQUAL;
        } else {
          return Token.OPERATOR_NOTEQUAL;
        }
      } else {
        return Token.OPERATOR_LOGICALNOT;
      }

    } else if (c == '&') {
      readChar();
      if (c == '&') {
        readChar();
        return Token.OPERATOR_LOGICALAND;
      } else if (c == '=') {
        readChar();
        return Token.OPERATOR_BITWISEANDASSIGNMENT;
      } else {
        return Token.OPERATOR_BITWISEAND;
      }

    } else if (c == '|') {
      readChar();
      if (c == '|') {
        readChar();
        return Token.OPERATOR_LOGICALOR;
      } else if (c == '=') {
        readChar();
        return Token.OPERATOR_BITWISEORASSIGNMENT;
      } else {
        return Token.OPERATOR_BITWISEOR;
      }

    } else if (c == '^') {
      readChar();
      if (c == '=') {
        readChar();
        return Token.OPERATOR_BITWISEXORASSIGNMENT;
      } else {
        return Token.OPERATOR_BITWISEXOR;
      }

    } else if (c == '~') {
      readChar();
      return Token.OPERATOR_BITWISENOT;

    } else if (c == '<') {
      readChar();
      if (c == '<') {
        readChar();
        if (c == '=') {
          readChar();
          return Token.OPERATOR_SHIFTLEFTASSIGNMENT;
        } else {
          return Token.OPERATOR_SHIFTLEFT;
        }
      } else if (c == '=') {
        readChar();
        return Token.OPERATOR_LESSTHANOREQUAL;
      } else {
        return Token.OPERATOR_LESSTHAN;
      }

    } else if (c == '>') {
      readChar();
      if (c == '>') {
        readChar();
        if (c == '>') {
          readChar();
          if (c == '=') {
            readChar();
            return Token.OPERATOR_SHIFTRIGHTUNSIGNEDASSIGNMENT;
          } else {
            return Token.OPERATOR_SHIFTRIGHTUNSIGNED;
          }
        } else if (c == '=') {
          readChar();
          return Token.OPERATOR_SHIFTRIGHTASSIGNMENT;
        } else {
          return Token.OPERATOR_SHIFTRIGHT;
        }
      } else if (c == '=') {
        readChar();
        return Token.OPERATOR_GREATERTHANOREQUAL;
      } else {
        return Token.OPERATOR_GREATERTHAN;
      }

    } else if (c == '\\') {
      // Although not an operator we check for this symbol last since its
      // probably the least likely input.  A '\' indicates an identifier that
      // starts with an escaped character.

      readIdentifierEscapeSequence();
      if (isIdentifierStart()) {
        return tokenizeIdentifier();
      } else {
        throwCompilerException("Invalid escaped character in identifier");
      }
    }

    // Give up.
    return tokenizeUnknown();
  }

  /**
   * Tokenizes an unknown character.
   */
  private Token tokenizeUnknown() {
    readChar();

    return new Token(Token.TYPE_UNKNOWN, input.substring(oldPosition, curPosition));
  }

  /**
   * Throws a new CompilerException with the given message.  This method will
   * never normally return.
   *
   * @throws CompilerException
   */
  private void throwCompilerException(String message) throws CompilerException {
    throw new CompilerException(message, getLineNumber());
  }
}
