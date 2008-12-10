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

/**
 * @author Andy Hayward
 */
public class Token {
  //
  // Types
  //

  public static final int TYPE_INVALID               = 0;

  public static final int TYPE_NEWLINE               = 1;
  public static final int TYPE_MULTILINECOMMENT      = 2;
  public static final int TYPE_SINGLELINECOMMENT     = 3;
  public static final int TYPE_WHITESPACE            = 4;

  public static final int TYPE_KEYWORD               = 5;
  public static final int TYPE_OPERATOR              = 6;

  public static final int TYPE_IDENTIFIER            = 7;
  public static final int TYPE_STRING                = 8;
  public static final int TYPE_REGEX                 = 9;
  public static final int TYPE_OCTAL                 = 10;
  public static final int TYPE_DECIMAL               = 11;
  public static final int TYPE_HEXADECIMAL           = 12;
  public static final int TYPE_FLOAT                 = 13;

  public static final int TYPE_EOF                   = 14;
  public static final int TYPE_UNKNOWN               = 15;

  //
  // Whitespace tokens
  //

  public static final Token NEWLINE = new Token(TYPE_NEWLINE);
  public static final Token MULTILINECOMMENT = new Token(TYPE_MULTILINECOMMENT);
  public static final Token SINGLELINECOMMENT = new Token(TYPE_SINGLELINECOMMENT);
  public static final Token WHITESPACE = new Token(TYPE_WHITESPACE);

  //
  // Keyword tokens
  //

  public static final Token KEYWORD_BREAK                 = new Token(TYPE_KEYWORD, "break");
  public static final Token KEYWORD_CASE                  = new Token(TYPE_KEYWORD, "case");
  public static final Token KEYWORD_CATCH                 = new Token(TYPE_KEYWORD, "catch");
  public static final Token KEYWORD_CONTINUE              = new Token(TYPE_KEYWORD, "continue");
  public static final Token KEYWORD_DEFAULT               = new Token(TYPE_KEYWORD, "default");
  public static final Token KEYWORD_DELETE                = new Token(TYPE_KEYWORD, "delete");
  public static final Token KEYWORD_DO                    = new Token(TYPE_KEYWORD, "do");
  public static final Token KEYWORD_ELSE                  = new Token(TYPE_KEYWORD, "else");
  public static final Token KEYWORD_FALSE                 = new Token(TYPE_KEYWORD, "false");
  public static final Token KEYWORD_FINALLY               = new Token(TYPE_KEYWORD, "finally");
  public static final Token KEYWORD_FOR                   = new Token(TYPE_KEYWORD, "for");
  public static final Token KEYWORD_FUNCTION              = new Token(TYPE_KEYWORD, "function");
  public static final Token KEYWORD_IF                    = new Token(TYPE_KEYWORD, "if");
  public static final Token KEYWORD_IN                    = new Token(TYPE_KEYWORD, "in");
  public static final Token KEYWORD_INSTANCEOF            = new Token(TYPE_KEYWORD, "instanceof");
  public static final Token KEYWORD_NEW                   = new Token(TYPE_KEYWORD, "new");
  public static final Token KEYWORD_NULL                  = new Token(TYPE_KEYWORD, "null");
  public static final Token KEYWORD_RETURN                = new Token(TYPE_KEYWORD, "return");
  public static final Token KEYWORD_SWITCH                = new Token(TYPE_KEYWORD, "switch");
  public static final Token KEYWORD_THIS                  = new Token(TYPE_KEYWORD, "this");
  public static final Token KEYWORD_THROW                 = new Token(TYPE_KEYWORD, "throw");
  public static final Token KEYWORD_TRUE                  = new Token(TYPE_KEYWORD, "true");
  public static final Token KEYWORD_TRY                   = new Token(TYPE_KEYWORD, "try");
  public static final Token KEYWORD_TYPEOF                = new Token(TYPE_KEYWORD, "typeof");
  public static final Token KEYWORD_VAR                   = new Token(TYPE_KEYWORD, "var");
  public static final Token KEYWORD_VOID                  = new Token(TYPE_KEYWORD, "void");
  public static final Token KEYWORD_WHILE                 = new Token(TYPE_KEYWORD, "while");
  public static final Token KEYWORD_WITH                  = new Token(TYPE_KEYWORD, "with");

  //
  // Reserved keyword tokens
  //

  public static final Token KEYWORD_ABSTRACT              = new Token(TYPE_KEYWORD, "abstract");
  public static final Token KEYWORD_BOOLEAN               = new Token(TYPE_KEYWORD, "boolean");
  public static final Token KEYWORD_BYTE                  = new Token(TYPE_KEYWORD, "byte");
  public static final Token KEYWORD_CHAR                  = new Token(TYPE_KEYWORD, "char");
  public static final Token KEYWORD_CLASS                 = new Token(TYPE_KEYWORD, "class");
  public static final Token KEYWORD_CONST                 = new Token(TYPE_KEYWORD, "const");
  public static final Token KEYWORD_DEBUGGER              = new Token(TYPE_KEYWORD, "debugger");
  public static final Token KEYWORD_DOUBLE                = new Token(TYPE_KEYWORD, "double");
  public static final Token KEYWORD_ENUM                  = new Token(TYPE_KEYWORD, "enum");
  public static final Token KEYWORD_EXPORT                = new Token(TYPE_KEYWORD, "export");
  public static final Token KEYWORD_EXTENDS               = new Token(TYPE_KEYWORD, "extends");
  public static final Token KEYWORD_FINAL                 = new Token(TYPE_KEYWORD, "final");
  public static final Token KEYWORD_FLOAT                 = new Token(TYPE_KEYWORD, "float");
  public static final Token KEYWORD_GOTO                  = new Token(TYPE_KEYWORD, "goto");
  public static final Token KEYWORD_IMPLEMENTS            = new Token(TYPE_KEYWORD, "implements");
  public static final Token KEYWORD_IMPORT                = new Token(TYPE_KEYWORD, "import");
  public static final Token KEYWORD_INT                   = new Token(TYPE_KEYWORD, "int");
  public static final Token KEYWORD_INTERFACE             = new Token(TYPE_KEYWORD, "interface");
  public static final Token KEYWORD_LONG                  = new Token(TYPE_KEYWORD, "long");
  public static final Token KEYWORD_NATIVE                = new Token(TYPE_KEYWORD, "native");
  public static final Token KEYWORD_PACKAGE               = new Token(TYPE_KEYWORD, "package");
  public static final Token KEYWORD_PRIVATE               = new Token(TYPE_KEYWORD, "private");
  public static final Token KEYWORD_PROTECTED             = new Token(TYPE_KEYWORD, "protected");
  public static final Token KEYWORD_PUBLIC                = new Token(TYPE_KEYWORD, "public");
  public static final Token KEYWORD_SHORT                 = new Token(TYPE_KEYWORD, "short");
  public static final Token KEYWORD_STATIC                = new Token(TYPE_KEYWORD, "static");
  public static final Token KEYWORD_SUPER                 = new Token(TYPE_KEYWORD, "super");
  public static final Token KEYWORD_SYNCHRONIZED          = new Token(TYPE_KEYWORD, "synchronized");
  public static final Token KEYWORD_THROWS                = new Token(TYPE_KEYWORD, "throws");
  public static final Token KEYWORD_TRANSIENT             = new Token(TYPE_KEYWORD, "transient");
  public static final Token KEYWORD_VOLATILE              = new Token(TYPE_KEYWORD, "volatile");

  //
  // Operator tokens
  //

  public static final Token OPERATOR_ASSIGNMENT                   = new Token(TYPE_OPERATOR, "=");
  public static final Token OPERATOR_BITWISEAND                   = new Token(TYPE_OPERATOR, "&");
  public static final Token OPERATOR_BITWISEANDASSIGNMENT         = new Token(TYPE_OPERATOR, "&=");
  public static final Token OPERATOR_BITWISENOT                   = new Token(TYPE_OPERATOR, "~");
  public static final Token OPERATOR_BITWISEOR                    = new Token(TYPE_OPERATOR, "|");
  public static final Token OPERATOR_BITWISEORASSIGNMENT          = new Token(TYPE_OPERATOR, "|=");
  public static final Token OPERATOR_BITWISEXOR                   = new Token(TYPE_OPERATOR, "^");
  public static final Token OPERATOR_BITWISEXORASSIGNMENT         = new Token(TYPE_OPERATOR, "^=");
  public static final Token OPERATOR_CLOSEBRACE                   = new Token(TYPE_OPERATOR, "}");
  public static final Token OPERATOR_CLOSEPAREN                   = new Token(TYPE_OPERATOR, ")");
  public static final Token OPERATOR_CLOSESQUARE                  = new Token(TYPE_OPERATOR, "]");
  public static final Token OPERATOR_COLON                        = new Token(TYPE_OPERATOR, ":");
  public static final Token OPERATOR_COMMA                        = new Token(TYPE_OPERATOR, ",");
  public static final Token OPERATOR_CONDITIONAL                  = new Token(TYPE_OPERATOR, "?");
  public static final Token OPERATOR_DIVIDE                       = new Token(TYPE_OPERATOR, "/");
  public static final Token OPERATOR_DIVIDEASSIGNMENT             = new Token(TYPE_OPERATOR, "/=");
  public static final Token OPERATOR_DOT                          = new Token(TYPE_OPERATOR, ".");
  public static final Token OPERATOR_EQUALEQUAL                   = new Token(TYPE_OPERATOR, "==");
  public static final Token OPERATOR_EQUALEQUALEQUAL              = new Token(TYPE_OPERATOR, "===");
  public static final Token OPERATOR_GREATERTHAN                  = new Token(TYPE_OPERATOR, ">");
  public static final Token OPERATOR_GREATERTHANOREQUAL           = new Token(TYPE_OPERATOR, ">=");
  public static final Token OPERATOR_LESSTHAN                     = new Token(TYPE_OPERATOR, "<");
  public static final Token OPERATOR_LESSTHANOREQUAL              = new Token(TYPE_OPERATOR, "<=");
  public static final Token OPERATOR_LOGICALAND                   = new Token(TYPE_OPERATOR, "&&");
  public static final Token OPERATOR_LOGICALNOT                   = new Token(TYPE_OPERATOR, "!");
  public static final Token OPERATOR_LOGICALOR                    = new Token(TYPE_OPERATOR, "||");
  public static final Token OPERATOR_MINUS                        = new Token(TYPE_OPERATOR, "-");
  public static final Token OPERATOR_MINUSASSIGNMENT              = new Token(TYPE_OPERATOR, "-=");
  public static final Token OPERATOR_MINUSMINUS                   = new Token(TYPE_OPERATOR, "--");
  public static final Token OPERATOR_MODULO                       = new Token(TYPE_OPERATOR, "%");
  public static final Token OPERATOR_MODULOASSIGNMENT             = new Token(TYPE_OPERATOR, "%=");
  public static final Token OPERATOR_MULTIPLY                     = new Token(TYPE_OPERATOR, "*");
  public static final Token OPERATOR_MULTIPLYASSIGNMENT           = new Token(TYPE_OPERATOR, "*=");
  public static final Token OPERATOR_NOTEQUAL                     = new Token(TYPE_OPERATOR, "!=");
  public static final Token OPERATOR_NOTEQUALEQUAL                = new Token(TYPE_OPERATOR, "!==");
  public static final Token OPERATOR_OPENBRACE                    = new Token(TYPE_OPERATOR, "{");
  public static final Token OPERATOR_OPENPAREN                    = new Token(TYPE_OPERATOR, "(");
  public static final Token OPERATOR_OPENSQUARE                   = new Token(TYPE_OPERATOR, "[");
  public static final Token OPERATOR_PLUS                         = new Token(TYPE_OPERATOR, "+");
  public static final Token OPERATOR_PLUSASSIGNMENT               = new Token(TYPE_OPERATOR, "+=");
  public static final Token OPERATOR_PLUSPLUS                     = new Token(TYPE_OPERATOR, "++");
  public static final Token OPERATOR_SEMICOLON                    = new Token(TYPE_OPERATOR, ";");
  public static final Token OPERATOR_SHIFTLEFT                    = new Token(TYPE_OPERATOR, "<<");
  public static final Token OPERATOR_SHIFTLEFTASSIGNMENT          = new Token(TYPE_OPERATOR, "<<=");
  public static final Token OPERATOR_SHIFTRIGHT                   = new Token(TYPE_OPERATOR, ">>");
  public static final Token OPERATOR_SHIFTRIGHTASSIGNMENT         = new Token(TYPE_OPERATOR, ">>=");
  public static final Token OPERATOR_SHIFTRIGHTUNSIGNED           = new Token(TYPE_OPERATOR, ">>>");
  public static final Token OPERATOR_SHIFTRIGHTUNSIGNEDASSIGNMENT = new Token(TYPE_OPERATOR, ">>>=");

  //
  // Other tokens
  //

  public static final Token EOF = new Token(TYPE_EOF);

  //
  // Internal state
  //

  private int type;
  private String value;

  /**
   * Public constructor.
   */
  public Token(int type) {
    this(type, null);
  }

  /**
   * Public constructor.
   */
  public Token(int type, String value) {
    this.type = type;
    this.value = value;
  }

  /**
   * Implementation of equals().
   */
  public boolean equals(Object object) {
    if (this.getClass() == object.getClass()) {
      Token token = (Token) object;

      switch (type) {
        case TYPE_FLOAT:
        case TYPE_OCTAL:
        case TYPE_DECIMAL:
        case TYPE_HEXADECIMAL:
        case TYPE_REGEX:
        case TYPE_STRING:
        case TYPE_IDENTIFIER:
          // these token types are equal iff their types and values are equal
          return (this.type == token.type && this.value.equals(token.value));

        default:
          // otherwise tokens are equal only iff they're the the same token
          return (this == token);
      }
    }

    return false;
  }

  /**
   * Returns the type of this token.
   */
  public int getType() {
    return type;
  }

  /**
   * Returns the value of this token as a string.
   */
  public String getValue() {
    return value;
  }

  /**
   * Implementation of hashcode.
   */
  public int hashCode() {
    return type ^ value.hashCode();
  }

  /**
   * Return true is this token represents whitespace.
   */
  public boolean isWhitespace() {
    return type == TYPE_NEWLINE
        || type == TYPE_MULTILINECOMMENT
        || type == TYPE_SINGLELINECOMMENT
        || type == TYPE_WHITESPACE;
  }

  /**
   * Return true is this token represents whitespace except newlines.
   */
  public boolean isWhitespaceNotNewline() {
    return type == TYPE_MULTILINECOMMENT
        || type == TYPE_SINGLELINECOMMENT
        || type == TYPE_WHITESPACE;
  }

  /**
   * Return true is this token represents the EOF.
   */
  public boolean isEOF() {
    return type == TYPE_EOF;
  }

  /**
   * Return true is this token represents an identifier.
   */
  public boolean isIdentifier() {
    return type == TYPE_IDENTIFIER;
  }

  /**
   * Return true is this token represents a newline.
   */
  public boolean isNewLine() {
    return type == TYPE_NEWLINE;
  }

  /**
   * Return true is this token represents a numeric literal.
   */
  public boolean isNumericLiteral() {
    return type == TYPE_FLOAT
        || type == TYPE_OCTAL
        || type == TYPE_DECIMAL
        || type == TYPE_HEXADECIMAL;
  }

  /**
   * Return true is this token represents a regex literal.
   */
  public boolean isRegexLiteral() {
    return type == TYPE_REGEX;
  }

  /**
   * Return true is this token represents a string literal.
   */
  public boolean isStringLiteral() {
    return type == TYPE_STRING;
  }

  /**
   * Returns a string representation of this token.
   */
  public final String toString() {
    switch (type) {
      case TYPE_NEWLINE:
        return "NEWLINE";

      case TYPE_MULTILINECOMMENT:
        return "MULTILINECOMMENT:";

      case TYPE_SINGLELINECOMMENT:
        return "SINGLELINECOMMENT:";

      case TYPE_WHITESPACE:
        return "WHITESPACE:";

      case TYPE_KEYWORD:
        return "KEYWORD: " + value;

      case TYPE_OPERATOR:
        return "OPERATOR: " + value;

      case TYPE_OCTAL:
        return "OCTAL: " + value;

      case TYPE_FLOAT:
        return "FLOAT: " + value;

      case TYPE_DECIMAL:
        return "DECIMAL: " + value;

      case TYPE_HEXADECIMAL:
        return "HEXADECIMAL: " + value;

      case TYPE_REGEX:
        return "REGEX: " + value;

      case TYPE_STRING:
        return "STRING: " + value;

      case TYPE_IDENTIFIER:
        return "IDENTIFIER: " + value;

      case TYPE_EOF:
        return "EOF";

      case TYPE_UNKNOWN:
        return "UNKNOWN: " + value;

      default:
        throw new IllegalArgumentException();
    }
  }
}
