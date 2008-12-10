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

import j2meunit.framework.TestCase;

/**
 * Unit tests for the MiniJoe Lexer class, to ensure that is conforms to the
 * ECMAScript v3 specification.  The testXXXX method names refer to sections
 * within that specification.
 *
 * @author Andy Hayward
 * @see <a href="http://www.mozilla.org/js/language/E262-3.pdf">ECMAScript v3 Specification</a>
 */
public class LexerTest extends TestCase {
  public LexerTest() {
    super();
  }

  public LexerTest(String name) {
    super(name);
  }

//  public void testSection_7_1() {
//    // The Unicode format control characters (characters in the category
//    // "Cf") can occur anywhere in the source text of an ECMAScript program.
//    // These characters are removed from the source text before applying the
//    // lexical grammar.
//
//    // TODO implement this in the Lexer and write unit tests
//  }

  public void testSection_7_2() throws CompilerException {
    // White space may occur between any two tokens, and may occur within
    // strings (where they are considered significant characters forming part
    // of the literal string value), but cannot appear within any other kind
    // of token.
    //
    // Whitespace characters are:
    //
    // 0x0009       tab                     <TAB>           \t
    // 0x000B       vertical tab            <VT>            \v
    // 0x000C       form feed               <FF>            \f
    // 0x0020       space                   <SP>            " "
    // 0x00A0       non-breaking space      <NBSP>
    //
    // all other characters in category "Zs".

    assertLexOutput(new Token[] {
        new Token(Token.TYPE_IDENTIFIER, "ab"),
        Token.WHITESPACE,
        new Token(Token.TYPE_IDENTIFIER, "cd")},
        "ab\u0009cd");
    assertLexOutput(new Token[] {
        new Token(Token.TYPE_IDENTIFIER, "ab"),
        Token.WHITESPACE,
        new Token(Token.TYPE_IDENTIFIER, "cd")},
        "ab\u000Bcd");
    assertLexOutput(new Token[] {
        new Token(Token.TYPE_IDENTIFIER, "ab"),
        Token.WHITESPACE,
        new Token(Token.TYPE_IDENTIFIER, "cd")},
        "ab\u000Ccd");
    assertLexOutput(new Token[] {
        new Token(Token.TYPE_IDENTIFIER, "ab"),
        Token.WHITESPACE,
        new Token(Token.TYPE_IDENTIFIER, "cd")},
        "ab\u0020cd");
    assertLexOutput(new Token[] {
        new Token(Token.TYPE_IDENTIFIER, "ab"),
        Token.WHITESPACE,
        new Token(Token.TYPE_IDENTIFIER, "cd")},
        "ab\u00A0cd");

    assertLexOutput(new Token[] {
        new Token(Token.TYPE_STRING, "ab\u0009cd")},
        "\"ab\u0009cd\"");
    assertLexOutput(new Token[] {
        new Token(Token.TYPE_STRING, "ab\u000Bcd")},
        "\"ab\u000Bcd\"");
    assertLexOutput(new Token[] {
        new Token(Token.TYPE_STRING, "ab\u000Ccd")},
        "\"ab\u000Ccd\"");
    assertLexOutput(new Token[] {
        new Token(Token.TYPE_STRING, "ab\u0020cd")},
        "\"ab\u0020cd\"");
    assertLexOutput(new Token[] {
        new Token(Token.TYPE_STRING, "ab\u00A0cd")},
        "\"ab\u00A0cd\"");

    assertLexOutput(new Token[] {
        Token.OPERATOR_PLUS,
        Token.WHITESPACE,
        Token.OPERATOR_PLUS},
        "+ +");
  }

  public void testSection_7_3() throws CompilerException {
    // Line terminators may occur between any two tokens, but cannot appear
    // within any token, not even a string.
    //
    // Line terminator characters are:
    //
    // 0x000A       line feed               <LF>           \n
    // 0x000D       carriage return         <CR>           \r
    // 0x2028       line separator          <LS>
    // 0x2029       paragraph separator     <PS>

    assertLexOutput(new Token[] {
        Token.OPERATOR_PLUS,
        Token.NEWLINE,
        Token.OPERATOR_PLUS},
        "+\n+");
    assertLexOutput(new Token[] {
        Token.OPERATOR_PLUS,
        Token.NEWLINE,
        Token.OPERATOR_PLUS},
        "+\r+");
    assertLexOutput(new Token[] {
        Token.OPERATOR_PLUS,
        Token.NEWLINE,
        Token.OPERATOR_PLUS},
        "+\u2028+");
    assertLexOutput(new Token[] {
        Token.OPERATOR_PLUS,
        Token.NEWLINE,
        Token.OPERATOR_PLUS},
        "+\u2029+");
  }

  public void testSection_7_4() throws CompilerException {
    // Single line comments consist of all characters from the '//' marker to
    // the end of the line.  However the line terminator is not considered
    // part of the comment but should be recognised separately.

    assertLexOutput(new Token[] {
        Token.SINGLELINECOMMENT,
        Token.NEWLINE,
        new Token(Token.TYPE_DECIMAL, "42")},
        "// some comment \n42");

    // Unlike Java (!) unicode escape sequences are not recognised within
    // comments.

    assertLexOutput(new Token[] {
        Token.SINGLELINECOMMENT,
        Token.NEWLINE,
        new Token(Token.TYPE_DECIMAL, "42")},
        "// some \\u000a comment \n42");

    // A multi-line comment that spans a single line is recognised as a
    // single-line comment.

    assertLexOutput(new Token[] {
        Token.SINGLELINECOMMENT,
        Token.NEWLINE,
        new Token(Token.TYPE_DECIMAL, "42")},
        "/* some comment */\n42");

    // A multi-line comment that spans multiple lines is recognised as a
    // multi-line comment.

    assertLexOutput(new Token[] {
        Token.MULTILINECOMMENT,
        new Token(Token.TYPE_DECIMAL, "42")},
        "/* some \n comment */42");
    assertLexOutput(new Token[] {
        Token.MULTILINECOMMENT,
        new Token(Token.TYPE_DECIMAL, "42")},
        "/* some \n comment \n*/42");

    // Test for '*' characters within multi-line comments.

    assertLexOutput(new Token[] {
        Token.MULTILINECOMMENT,
        new Token(Token.TYPE_DECIMAL, "42")},
        "/* some *\n comment */42");
    assertLexOutput(new Token[] {
        Token.MULTILINECOMMENT,
        new Token(Token.TYPE_DECIMAL, "42")},
        "/* some \n comment **/42");
  }

  public void testSection_7_5_2() throws CompilerException {
    // ECMAScript keywords should be returned as the specific keyword token.

    assertLexOutput(new Token[] {Token.KEYWORD_BREAK}, "break");
    assertLexOutput(new Token[] {Token.KEYWORD_CASE}, "case");
    assertLexOutput(new Token[] {Token.KEYWORD_CATCH}, "catch");
    assertLexOutput(new Token[] {Token.KEYWORD_CONTINUE}, "continue");
    assertLexOutput(new Token[] {Token.KEYWORD_DEFAULT}, "default");
    assertLexOutput(new Token[] {Token.KEYWORD_DELETE}, "delete");
    assertLexOutput(new Token[] {Token.KEYWORD_DO}, "do");
    assertLexOutput(new Token[] {Token.KEYWORD_ELSE}, "else");
    assertLexOutput(new Token[] {Token.KEYWORD_FALSE}, "false");
    assertLexOutput(new Token[] {Token.KEYWORD_FINALLY}, "finally");
    assertLexOutput(new Token[] {Token.KEYWORD_FOR}, "for");
    assertLexOutput(new Token[] {Token.KEYWORD_FUNCTION}, "function");
    assertLexOutput(new Token[] {Token.KEYWORD_IF}, "if");
    assertLexOutput(new Token[] {Token.KEYWORD_IN}, "in");
    assertLexOutput(new Token[] {Token.KEYWORD_INSTANCEOF}, "instanceof");
    assertLexOutput(new Token[] {Token.KEYWORD_NEW}, "new");
    assertLexOutput(new Token[] {Token.KEYWORD_NULL}, "null");
    assertLexOutput(new Token[] {Token.KEYWORD_RETURN}, "return");
    assertLexOutput(new Token[] {Token.KEYWORD_SWITCH}, "switch");
    assertLexOutput(new Token[] {Token.KEYWORD_THIS}, "this");
    assertLexOutput(new Token[] {Token.KEYWORD_THROW}, "throw");
    assertLexOutput(new Token[] {Token.KEYWORD_TRUE}, "true");
    assertLexOutput(new Token[] {Token.KEYWORD_TRY}, "try");
    assertLexOutput(new Token[] {Token.KEYWORD_TYPEOF}, "typeof");
    assertLexOutput(new Token[] {Token.KEYWORD_VAR}, "var");
    assertLexOutput(new Token[] {Token.KEYWORD_VOID}, "void");
    assertLexOutput(new Token[] {Token.KEYWORD_WHILE}, "while");
    assertLexOutput(new Token[] {Token.KEYWORD_WITH}, "with");
  }

  public void testSection_7_5_3() throws CompilerException {
    // ECMAScript reserved keywords should be returned as the specific
    // keyword token.

    assertLexOutput(new Token[] {Token.KEYWORD_ABSTRACT}, "abstract");
    assertLexOutput(new Token[] {Token.KEYWORD_BOOLEAN}, "boolean");
    assertLexOutput(new Token[] {Token.KEYWORD_BYTE}, "byte");
    assertLexOutput(new Token[] {Token.KEYWORD_CHAR}, "char");
    assertLexOutput(new Token[] {Token.KEYWORD_CLASS}, "class");
    assertLexOutput(new Token[] {Token.KEYWORD_CONST}, "const");
    assertLexOutput(new Token[] {Token.KEYWORD_DEBUGGER}, "debugger");
    assertLexOutput(new Token[] {Token.KEYWORD_DOUBLE}, "double");
    assertLexOutput(new Token[] {Token.KEYWORD_ENUM}, "enum");
    assertLexOutput(new Token[] {Token.KEYWORD_EXPORT}, "export");
    assertLexOutput(new Token[] {Token.KEYWORD_EXTENDS}, "extends");
    assertLexOutput(new Token[] {Token.KEYWORD_FINAL}, "final");
    assertLexOutput(new Token[] {Token.KEYWORD_FLOAT}, "float");
    assertLexOutput(new Token[] {Token.KEYWORD_GOTO}, "goto");
    assertLexOutput(new Token[] {Token.KEYWORD_IMPLEMENTS}, "implements");
    assertLexOutput(new Token[] {Token.KEYWORD_IMPORT}, "import");
    assertLexOutput(new Token[] {Token.KEYWORD_INT}, "int");
    assertLexOutput(new Token[] {Token.KEYWORD_INTERFACE}, "interface");
    assertLexOutput(new Token[] {Token.KEYWORD_LONG}, "long");
    assertLexOutput(new Token[] {Token.KEYWORD_NATIVE}, "native");
    assertLexOutput(new Token[] {Token.KEYWORD_PACKAGE}, "package");
    assertLexOutput(new Token[] {Token.KEYWORD_PRIVATE}, "private");
    assertLexOutput(new Token[] {Token.KEYWORD_PROTECTED}, "protected");
    assertLexOutput(new Token[] {Token.KEYWORD_PUBLIC}, "public");
    assertLexOutput(new Token[] {Token.KEYWORD_SHORT}, "short");
    assertLexOutput(new Token[] {Token.KEYWORD_STATIC}, "static");
    assertLexOutput(new Token[] {Token.KEYWORD_SUPER}, "super");
    assertLexOutput(new Token[] {Token.KEYWORD_SYNCHRONIZED}, "synchronized");
    assertLexOutput(new Token[] {Token.KEYWORD_THROWS}, "throws");
    assertLexOutput(new Token[] {Token.KEYWORD_TRANSIENT}, "transient");
    assertLexOutput(new Token[] {Token.KEYWORD_VOLATILE}, "volatile");
  }

  public void testSection_7_6() throws CompilerException {
    // '$' and '_' characters are permitted anywhere in an identifier.

    assertLexOutput(new Token[] {new Token(Token.TYPE_IDENTIFIER, "$abcd")}, "$abcd");
    assertLexOutput(new Token[] {new Token(Token.TYPE_IDENTIFIER, "ab$cd")}, "ab$cd");
    assertLexOutput(new Token[] {new Token(Token.TYPE_IDENTIFIER, "abcd$")}, "abcd$");

    assertLexOutput(new Token[] {new Token(Token.TYPE_IDENTIFIER, "_abcd")}, "_abcd");
    assertLexOutput(new Token[] {new Token(Token.TYPE_IDENTIFIER, "ab_cd")}, "ab_cd");
    assertLexOutput(new Token[] {new Token(Token.TYPE_IDENTIFIER, "abcd_")}, "abcd_");

    // identifiers may not start with a decimal digit

    assertLexOutput(new Token[] {
        new Token(Token.TYPE_DECIMAL, "0"),
        new Token(Token.TYPE_IDENTIFIER, "abcd")},
        "0abcd");
    assertLexOutput(new Token[] {
        new Token(Token.TYPE_DECIMAL, "9"),
        new Token(Token.TYPE_IDENTIFIER, "abcd")},
        "9abcd");

    // identifiers may contain decimal digits

    assertLexOutput(new Token[] {new Token(Token.TYPE_IDENTIFIER, "_0abcd")}, "_0abcd");
    assertLexOutput(new Token[] {new Token(Token.TYPE_IDENTIFIER, "_ab0cd")}, "_ab0cd");
    assertLexOutput(new Token[] {new Token(Token.TYPE_IDENTIFIER, "_abcd0")}, "_abcd0");

    // unicode escape sequences are valid within identifiers

    assertLexOutput(new Token[] {new Token(Token.TYPE_IDENTIFIER, "_abcd")}, "\u005fabcd");
    assertLexOutput(new Token[] {new Token(Token.TYPE_IDENTIFIER, "ab_cd")}, "ab\u005fcd");
    assertLexOutput(new Token[] {new Token(Token.TYPE_IDENTIFIER, "abcd_")}, "abcd\u005f");
  }

  public void testSection_7_7() throws CompilerException {
    // ECMAScript operators should be returned as the specific operator token.

    assertLexOutput(new Token[] {Token.OPERATOR_ASSIGNMENT}, "=");
    assertLexOutput(new Token[] {Token.OPERATOR_BITWISEAND}, "&");
    assertLexOutput(new Token[] {Token.OPERATOR_BITWISEANDASSIGNMENT}, "&=");
    assertLexOutput(new Token[] {Token.OPERATOR_BITWISENOT}, "~");
    assertLexOutput(new Token[] {Token.OPERATOR_BITWISEOR}, "|");
    assertLexOutput(new Token[] {Token.OPERATOR_BITWISEORASSIGNMENT}, "|=");
    assertLexOutput(new Token[] {Token.OPERATOR_BITWISEXOR}, "^");
    assertLexOutput(new Token[] {Token.OPERATOR_BITWISEXORASSIGNMENT}, "^=");
    assertLexOutput(new Token[] {Token.OPERATOR_CLOSEBRACE}, "}");
    assertLexOutput(new Token[] {Token.OPERATOR_CLOSEPAREN}, ")");
    assertLexOutput(new Token[] {Token.OPERATOR_CLOSESQUARE}, "]");
    assertLexOutput(new Token[] {Token.OPERATOR_COLON}, ":");
    assertLexOutput(new Token[] {Token.OPERATOR_COMMA}, ",");
    assertLexOutput(new Token[] {Token.OPERATOR_CONDITIONAL}, "?");
    assertLexOutput(new Token[] {Token.OPERATOR_DIVIDE}, "/");
    assertLexOutput(new Token[] {Token.OPERATOR_DIVIDEASSIGNMENT}, "/=");
    assertLexOutput(new Token[] {Token.OPERATOR_DOT}, ".");
    assertLexOutput(new Token[] {Token.OPERATOR_EQUALEQUAL}, "==");
    assertLexOutput(new Token[] {Token.OPERATOR_EQUALEQUALEQUAL}, "===");
    assertLexOutput(new Token[] {Token.OPERATOR_GREATERTHAN}, ">");
    assertLexOutput(new Token[] {Token.OPERATOR_GREATERTHANOREQUAL}, ">=");
    assertLexOutput(new Token[] {Token.OPERATOR_LESSTHAN}, "<");
    assertLexOutput(new Token[] {Token.OPERATOR_LESSTHANOREQUAL}, "<=");
    assertLexOutput(new Token[] {Token.OPERATOR_LOGICALAND}, "&&");
    assertLexOutput(new Token[] {Token.OPERATOR_LOGICALNOT}, "!");
    assertLexOutput(new Token[] {Token.OPERATOR_LOGICALOR}, "||");
    assertLexOutput(new Token[] {Token.OPERATOR_MINUS}, "-");
    assertLexOutput(new Token[] {Token.OPERATOR_MINUSASSIGNMENT}, "-=");
    assertLexOutput(new Token[] {Token.OPERATOR_MINUSMINUS}, "--");
    assertLexOutput(new Token[] {Token.OPERATOR_MODULO}, "%");
    assertLexOutput(new Token[] {Token.OPERATOR_MODULOASSIGNMENT}, "%=");
    assertLexOutput(new Token[] {Token.OPERATOR_MULTIPLY}, "*");
    assertLexOutput(new Token[] {Token.OPERATOR_MULTIPLYASSIGNMENT}, "*=");
    assertLexOutput(new Token[] {Token.OPERATOR_NOTEQUAL}, "!=");
    assertLexOutput(new Token[] {Token.OPERATOR_NOTEQUALEQUAL}, "!==");
    assertLexOutput(new Token[] {Token.OPERATOR_OPENBRACE}, "{");
    assertLexOutput(new Token[] {Token.OPERATOR_OPENPAREN}, "(");
    assertLexOutput(new Token[] {Token.OPERATOR_OPENSQUARE}, "[");
    assertLexOutput(new Token[] {Token.OPERATOR_PLUS}, "+");
    assertLexOutput(new Token[] {Token.OPERATOR_PLUSASSIGNMENT}, "+=");
    assertLexOutput(new Token[] {Token.OPERATOR_PLUSPLUS}, "++");
    assertLexOutput(new Token[] {Token.OPERATOR_SEMICOLON}, ";");
    assertLexOutput(new Token[] {Token.OPERATOR_SHIFTLEFT}, "<<");
    assertLexOutput(new Token[] {Token.OPERATOR_SHIFTLEFTASSIGNMENT}, "<<=");
    assertLexOutput(new Token[] {Token.OPERATOR_SHIFTRIGHT}, ">>");
    assertLexOutput(new Token[] {Token.OPERATOR_SHIFTRIGHTASSIGNMENT}, ">>=");
    assertLexOutput(new Token[] {Token.OPERATOR_SHIFTRIGHTUNSIGNED}, ">>>");
    assertLexOutput(new Token[] {Token.OPERATOR_SHIFTRIGHTUNSIGNEDASSIGNMENT}, ">>>=");
  }

  public void testSection_7_8_3() throws CompilerException {
    // Numeric literals (octal, decimal and hexadecimal)

    assertLexOutput(new Token[] {new Token(Token.TYPE_DECIMAL, "0")}, "0");
    assertLexOutput(new Token[] {new Token(Token.TYPE_DECIMAL, "1")}, "1");
    assertLexOutput(new Token[] {new Token(Token.TYPE_DECIMAL, "2")}, "2");
    assertLexOutput(new Token[] {new Token(Token.TYPE_DECIMAL, "3")}, "3");
    assertLexOutput(new Token[] {new Token(Token.TYPE_DECIMAL, "4")}, "4");
    assertLexOutput(new Token[] {new Token(Token.TYPE_DECIMAL, "5")}, "5");
    assertLexOutput(new Token[] {new Token(Token.TYPE_DECIMAL, "6")}, "6");
    assertLexOutput(new Token[] {new Token(Token.TYPE_DECIMAL, "7")}, "7");
    assertLexOutput(new Token[] {new Token(Token.TYPE_DECIMAL, "8")}, "8");
    assertLexOutput(new Token[] {new Token(Token.TYPE_DECIMAL, "9")}, "9");

    assertLexOutput(new Token[] {new Token(Token.TYPE_OCTAL, "00")}, "00");
    assertLexOutput(new Token[] {new Token(Token.TYPE_OCTAL, "01")}, "01");
    assertLexOutput(new Token[] {new Token(Token.TYPE_OCTAL, "02")}, "02");
    assertLexOutput(new Token[] {new Token(Token.TYPE_OCTAL, "03")}, "03");
    assertLexOutput(new Token[] {new Token(Token.TYPE_OCTAL, "04")}, "04");
    assertLexOutput(new Token[] {new Token(Token.TYPE_OCTAL, "05")}, "05");
    assertLexOutput(new Token[] {new Token(Token.TYPE_OCTAL, "06")}, "06");
    assertLexOutput(new Token[] {new Token(Token.TYPE_OCTAL, "07")}, "07");

    assertLexOutput(new Token[] {new Token(Token.TYPE_DECIMAL, "08")}, "08");
    assertLexOutput(new Token[] {new Token(Token.TYPE_DECIMAL, "09")}, "09");

    assertLexOutput(new Token[] {new Token(Token.TYPE_OCTAL, "000")}, "000");
    assertLexOutput(new Token[] {new Token(Token.TYPE_OCTAL, "011")}, "011");
    assertLexOutput(new Token[] {new Token(Token.TYPE_OCTAL, "022")}, "022");
    assertLexOutput(new Token[] {new Token(Token.TYPE_OCTAL, "033")}, "033");
    assertLexOutput(new Token[] {new Token(Token.TYPE_OCTAL, "044")}, "044");
    assertLexOutput(new Token[] {new Token(Token.TYPE_OCTAL, "055")}, "055");
    assertLexOutput(new Token[] {new Token(Token.TYPE_OCTAL, "066")}, "066");
    assertLexOutput(new Token[] {new Token(Token.TYPE_OCTAL, "077")}, "077");

    assertLexOutput(new Token[] {new Token(Token.TYPE_DECIMAL, "088")}, "088");
    assertLexOutput(new Token[] {new Token(Token.TYPE_DECIMAL, "099")}, "099");

    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0x0")}, "0x0");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0x1")}, "0x1");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0x2")}, "0x2");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0x3")}, "0x3");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0x4")}, "0x4");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0x5")}, "0x5");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0x6")}, "0x6");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0x7")}, "0x7");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0x8")}, "0x8");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0x9")}, "0x9");

    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0xa")}, "0xa");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0xb")}, "0xb");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0xc")}, "0xc");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0xd")}, "0xd");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0xe")}, "0xe");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0xf")}, "0xf");

    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0xA")}, "0xA");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0xB")}, "0xB");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0xC")}, "0xC");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0xD")}, "0xD");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0xE")}, "0xE");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0xF")}, "0xF");

    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0X0")}, "0X0");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0X1")}, "0X1");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0X2")}, "0X2");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0X3")}, "0X3");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0X4")}, "0X4");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0X5")}, "0X5");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0X6")}, "0X6");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0X7")}, "0X7");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0X8")}, "0X8");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0X9")}, "0X9");

    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0Xa")}, "0Xa");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0Xb")}, "0Xb");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0Xc")}, "0Xc");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0Xd")}, "0Xd");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0Xe")}, "0Xe");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0Xf")}, "0Xf");

    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0XA")}, "0XA");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0XB")}, "0XB");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0XC")}, "0XC");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0XD")}, "0XD");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0XE")}, "0XE");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0XF")}, "0XF");

    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0x00")}, "0x00");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0x11")}, "0x11");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0x22")}, "0x22");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0x33")}, "0x33");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0x44")}, "0x44");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0x55")}, "0x55");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0x66")}, "0x66");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0x77")}, "0x77");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0x88")}, "0x88");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0x99")}, "0x99");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0xaa")}, "0xaa");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0xbb")}, "0xbb");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0xcc")}, "0xcc");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0xdd")}, "0xdd");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0xee")}, "0xee");
    assertLexOutput(new Token[] {new Token(Token.TYPE_HEXADECIMAL, "0xff")}, "0xff");

    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "0.")}, "0.");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "1.")}, "1.");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "2.")}, "2.");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "3.")}, "3.");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "4.")}, "4.");

    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, ".0")}, ".0");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, ".1")}, ".1");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, ".2")}, ".2");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, ".3")}, ".3");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, ".4")}, ".4");

    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "0.0")}, "0.0");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "1.1")}, "1.1");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "2.2")}, "2.2");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "3.3")}, "3.3");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "4.4")}, "4.4");

    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "0e0")}, "0e0");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "1e1")}, "1e1");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "2e2")}, "2e2");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "3e3")}, "3e3");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "4e4")}, "4e4");

    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "0E0")}, "0E0");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "1E1")}, "1E1");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "2E2")}, "2E2");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "3E3")}, "3E3");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "4E4")}, "4E4");

    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "0.e0")}, "0.e0");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "1.e1")}, "1.e1");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "2.e2")}, "2.e2");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "3.e3")}, "3.e3");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "4.e4")}, "4.e4");

    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, ".0e0")}, ".0e0");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, ".1e1")}, ".1e1");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, ".2e2")}, ".2e2");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, ".3e3")}, ".3e3");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, ".4e4")}, ".4e4");

    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "0.0e0")}, "0.0e0");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "1.1e1")}, "1.1e1");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "2.2e2")}, "2.2e2");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "3.3e3")}, "3.3e3");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "4.4e4")}, "4.4e4");

    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "0.e-0")}, "0.e-0");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "1.e-1")}, "1.e-1");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "2.e-2")}, "2.e-2");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "3.e-3")}, "3.e-3");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "4.e-4")}, "4.e-4");

    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, ".0e-0")}, ".0e-0");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, ".1e-1")}, ".1e-1");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, ".2e-2")}, ".2e-2");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, ".3e-3")}, ".3e-3");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, ".4e-4")}, ".4e-4");

    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "0.0e-0")}, "0.0e-0");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "1.1e-1")}, "1.1e-1");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "2.2e-2")}, "2.2e-2");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "3.3e-3")}, "3.3e-3");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "4.4e-4")}, "4.4e-4");

    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "0.e+0")}, "0.e+0");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "1.e+1")}, "1.e+1");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "2.e+2")}, "2.e+2");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "3.e+3")}, "3.e+3");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "4.e+4")}, "4.e+4");

    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, ".0e+0")}, ".0e+0");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, ".1e+1")}, ".1e+1");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, ".2e+2")}, ".2e+2");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, ".3e+3")}, ".3e+3");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, ".4e+4")}, ".4e+4");

    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "0.0e+0")}, "0.0e+0");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "1.1e+1")}, "1.1e+1");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "2.2e+2")}, "2.2e+2");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "3.3e+3")}, "3.3e+3");
    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "4.4e+4")}, "4.4e+4");

    assertLexOutput(new Token[] {new Token(Token.TYPE_FLOAT, "4.4e+4")}, "4.4e+4");
  }

  public void testSection_7_8_4() throws CompilerException {
    // A string literal is zero or more characters enclosed in single or
    // double quotes.

    assertLexOutput(new Token[] {new Token(Token.TYPE_STRING, "")}, "\'\'");
    assertLexOutput(new Token[] {new Token(Token.TYPE_STRING, "")}, "\"\"");

    assertLexOutput(new Token[] {new Token(Token.TYPE_STRING, "foo")}, "\'foo\'");
    assertLexOutput(new Token[] {new Token(Token.TYPE_STRING, "foo")}, "\"foo\"");

    // A string literal enclosed in single quotes may contain double quote
    // characters, a string literal enclosed in double quotes may contain
    // single quote characters

    assertLexOutput(new Token[] {new Token(Token.TYPE_STRING, "foo\"bar")}, "\'foo\"bar\'");
    assertLexOutput(new Token[] {new Token(Token.TYPE_STRING, "foo\'bar")}, "\"foo\'bar\"");

    // A string literal enclosed in single quotes may contain escaped single
    // quotes, a string literal enclosed in double quotes may contain escaped
    // double quotes.

    assertLexOutput(new Token[] {new Token(Token.TYPE_STRING, "foo\'bar")}, "\'foo\\\'bar\'");
    assertLexOutput(new Token[] {new Token(Token.TYPE_STRING, "foo\"bar")}, "\"foo\\\"bar\"");

    // Although deprecated in the latest ECMAScript specification, string
    // literals may contain octal escape sequences.

    assertLexOutput(new Token[] {new Token(Token.TYPE_STRING, "\u00a9")}, "\"\\251\"");

    // String literals may contain hex and unicode escape sequences.

    assertLexOutput(new Token[] {new Token(Token.TYPE_STRING, "\u00a9")}, "\"\\xa9\"");
    assertLexOutput(new Token[] {new Token(Token.TYPE_STRING, "\u00a9")}, "\"\\u00a9\"");

    // Invalid escape sequences should not cause an error.
    // TODO add support for this.

    // assertLexOutput(new Token[] {new Token(Token.TYPE_STRING, "xG")}, "\"\\xG\"");
    // assertLexOutput(new Token[] {new Token(Token.TYPE_STRING, "xCG")}, "\"\\xCG\"");

    // String literals may contain single character escapes.

    assertLexOutput(new Token[] {new Token(Token.TYPE_STRING, "\u0008")}, "\"\\b\"");
    assertLexOutput(new Token[] {new Token(Token.TYPE_STRING, "\u0009")}, "\"\\t\"");
    assertLexOutput(new Token[] {new Token(Token.TYPE_STRING, "\n")},     "\"\\n\"");
    assertLexOutput(new Token[] {new Token(Token.TYPE_STRING, "\u000B")}, "\"\\v\"");
    assertLexOutput(new Token[] {new Token(Token.TYPE_STRING, "\u000C")}, "\"\\f\"");
    assertLexOutput(new Token[] {new Token(Token.TYPE_STRING, "\r")},     "\"\\r\"");
    assertLexOutput(new Token[] {new Token(Token.TYPE_STRING, "\"")},     "\"\\\"\"");
    assertLexOutput(new Token[] {new Token(Token.TYPE_STRING, "\'")},     "\"\\\'\"");
    assertLexOutput(new Token[] {new Token(Token.TYPE_STRING, "\\")},     "\"\\\\\"");

    // Other escaped characters in string literals escape themselves

    assertLexOutput(new Token[] {new Token(Token.TYPE_STRING, "a")}, "\"\\a\"");
    assertLexOutput(new Token[] {new Token(Token.TYPE_STRING, "A")}, "\"\\A\"");
    assertLexOutput(new Token[] {new Token(Token.TYPE_STRING, "z")}, "\"\\z\"");
    assertLexOutput(new Token[] {new Token(Token.TYPE_STRING, "Z")}, "\"\\Z\"");
    assertLexOutput(new Token[] {new Token(Token.TYPE_STRING, "@")}, "\"\\@\"");
    assertLexOutput(new Token[] {new Token(Token.TYPE_STRING, "#")}, "\"\\#\"");
  }

//  public void testSection_7_8_5() {
//    // Regular expression literals
//
//    // TODO implement this in the Lexer and write unit tests
//  }

  private void assertLexOutput(Token[] expected, String input) throws CompilerException {
    Lexer lexer = new Lexer(input);

    for (int i = 0; i < expected.length; i++) {
      assertEquals(expected[i], lexer.nextToken());
    }

    assertEquals(Token.EOF, lexer.nextToken());
  }
}
