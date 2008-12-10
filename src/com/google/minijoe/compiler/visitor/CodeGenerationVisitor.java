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
import com.google.minijoe.compiler.Config;
import com.google.minijoe.compiler.Token;
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
import com.google.minijoe.compiler.ast.Node;
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
import com.google.minijoe.sys.JsFunction;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Code generation visitor. This needs two passes, the first pass is used to
 * determine the offsets of jump locations, the second pass writes the actual
 * byte code.
 *
 * @author Andy Hayward
 * @author Stefan Haustein
 */
public class CodeGenerationVisitor implements Visitor {
  public static final byte BLOCK_COMMENT = (byte) 0x00;
  public static final byte BLOCK_GLOBAL_STRING_TABLE = (byte) 0x10;
  public static final byte BLOCK_NUMBER_LITERALS = (byte) 0x20;
  public static final byte BLOCK_STRING_LITERALS = (byte) 0x30;
  public static final byte BLOCK_REGEX_LITERALS = (byte) 0x40;
  public static final byte BLOCK_FUNCTION_LITERALS = (byte) 0x50;
  public static final byte BLOCK_LOCAL_VARIABLE_NAMES = (byte) 0x60;
  public static final byte BLOCK_CODE = (byte) 0x80;
  public static final byte BLOCK_LINENUMBER = (byte) 0xE0;
  public static final byte BLOCK_DEBUG = (byte) 0xF0;
  public static final byte BLOCK_END = (byte) 0xFF;

  // pop the previous value of the assignment target. should be optimized away
  // in most cases.

  private DataOutputStream dos;
  private ByteArrayOutputStream codeStream = new ByteArrayOutputStream(0);

  private Hashtable globalStringMap = new Hashtable();
  private Vector globalStringTable = new Vector();

  private Vector functionLiterals = new Vector();
  private Vector numberLiterals = new Vector();
  private Vector stringLiterals = new Vector();

  private Hashtable localVariableTable = new Hashtable();

  private Hashtable jumpLabels = new Hashtable();
  private Vector unresolvedJumps = new Vector();
  private Vector labelSet = new Vector();

  private Vector lineNumberVector = new Vector();

  // TODO consider getting rid of this

  private Expression pendingAssignment;

  private Statement currentBreakStatement;
  private Statement currentContinueStatement;
  private Statement currentTryStatement;
  private String currentTryLabel;
  
  private boolean enableLocalsOptimization = false;

  CodeGenerationVisitor parent;

  private class LineNumber {
    private LineNumber(int programCounter, int lineNumber) {
      this.programCounter = programCounter;
      this.lineNumber = lineNumber;
    }

    int programCounter;
    int lineNumber;
  }

  public CodeGenerationVisitor(DataOutputStream stream) {
    this.dos = stream;
    this.globalStringMap = new Hashtable();
    this.globalStringTable = new Vector();
  }

  public CodeGenerationVisitor( CodeGenerationVisitor parent, FunctionLiteral function,
      DataOutputStream dos) throws CompilerException {
    this.parent = parent;
    this.globalStringMap = parent.globalStringMap;
    this.globalStringTable = parent.globalStringTable;
    this.dos = dos;
    this.enableLocalsOptimization = function.enableLocalsOptimization;

    for (int i = 0; i < function.variables.length; i++) {
      Identifier variable = function.variables[i];
      addToGlobalStringTable(variable.string);
      localVariableTable.put(variable, variable);
    }

    for (int i = 0; i < function.variables.length; i++) {
      addToGlobalStringTable(function.variables[i].string);
    }

    for (int i = 0; i < function.functions.length; i++) {
      if (function.functions[i] != null) {
        function.functions[i].visitStatement(this);
      }
    }

    for (int i = 0; i < function.statements.length; i++) {
      if (function.statements[i] != null) {
        function.statements[i].visitStatement(this);
      }
    }

    byte[] byteCode = codeStream.toByteArray();

    // TODO remove this magic numbers.
    int flags = Config.FASTLOCALS && function.enableLocalsOptimization ? 0x01 : 0x00;

    if (function.name != null) {
      writeCommentBlock("function " + function.name.string);
    }

    writeStringLiteralBlock();
    writeNumberLiteralBlock();
    writeFunctionLiteralBlock();
    writeLocalVariableNameBlock(function.variables);
    writeCodeBlock(function.variables.length, function.parameters.length, flags, byteCode);
    writeLineNumberBlock();
    writeEndMarker();
  }

  //
  // utility methods
  //

  private void writeMagic() throws CompilerException {
    try {
      dos.write('M');
      dos.write('i');
      dos.write('n');
      dos.write('i');
      dos.write('J');
      dos.write('o');
      dos.write('e');
      dos.write(0x00); // version
    } catch (IOException e) {
      throw new CompilerException(e);
    }
  }

  private void writeCommentBlock(String comment) throws CompilerException {
    try {
      if (comment != null) {
        dos.write(BLOCK_COMMENT);
        dos.writeUTF(comment);
      }
    } catch (IOException e) {
      throw new CompilerException(e);
    }
  }

  private void writeGlobalStringTableBlock() throws CompilerException {
    try {
      if (globalStringTable.size() > 0) {
        dos.write(BLOCK_GLOBAL_STRING_TABLE);
        dos.writeShort(globalStringTable.size());
        for (int i = 0; i < globalStringTable.size(); i++) {
          dos.writeUTF((String) globalStringTable.elementAt(i));
        }
      }
    } catch (IOException e) {
      throw new CompilerException(e);
    }
  }

  private void writeNumberLiteralBlock() throws CompilerException {
    try {
      if (numberLiterals.size() > 0) {
        dos.write(BLOCK_NUMBER_LITERALS);
        dos.writeShort(numberLiterals.size());
        for (int i = 0; i < numberLiterals.size(); i++) {
          dos.writeDouble(((Double) numberLiterals.elementAt(i)).doubleValue());
        }
      }
    } catch (IOException e) {
      throw new CompilerException(e);
    }
  }

  private void writeStringLiteralBlock() throws CompilerException {
    try {
      if (stringLiterals.size() > 0) {
        dos.write(BLOCK_STRING_LITERALS);
        dos.writeShort(stringLiterals.size());
        for (int i = 0; i < stringLiterals.size(); i++) {
          dos.writeShort((short) ((Integer) globalStringMap.get(stringLiterals
              .elementAt(i))).intValue());
        }
      }
    } catch (IOException e) {
      throw new CompilerException(e);
    }
  }

  private void writeLocalVariableNameBlock(Identifier[] variables) throws CompilerException {
    try {
      if (variables != null) {
        dos.write(BLOCK_LOCAL_VARIABLE_NAMES);
        dos.writeShort(variables.length);
        for (int i = 0; i < variables.length; i++) {
          dos.writeShort((short) ((Integer) globalStringMap.get(variables[i].string)).intValue());
        }
      }
    } catch (IOException e) {
      throw new CompilerException(e);
    }
  }

  private void writeFunctionLiteralBlock() throws CompilerException {
    try {
      if (functionLiterals.size() > 0) {
        dos.write(BLOCK_FUNCTION_LITERALS);
        dos.writeShort(functionLiterals.size());
        for (int i = 0; i < functionLiterals.size(); i++) {
          dos.write((byte[]) functionLiterals.elementAt(i));
        }
      }
    } catch (IOException e) {
      throw new CompilerException(e);
    }
  }

  private void writeCodeBlock(int localVariableCount, int paramenterCount,
      int flags, byte[] code) throws CompilerException {
    try {
      dos.write(BLOCK_CODE);
      dos.writeShort(localVariableCount);
      dos.writeShort(paramenterCount);
      dos.write(flags);
      dos.writeShort(code.length);

      for (int i = 0; i < unresolvedJumps.size(); i += 2) {
        String label = (String) unresolvedJumps.elementAt(i);
        int address = ((Integer) unresolvedJumps.elementAt(i + 1)).intValue();
        Integer target = (Integer) jumpLabels.get(label);

        if (target == null) {
          throw new CompilerException("Unresolved Jump Label: " + label);
        }

        int delta = target.intValue() - address - 2;

        code[address + 0] = (byte) (delta >> 8);
        code[address + 1] = (byte) (delta & 255);
      }

      dos.write(code);
    } catch (IOException e) {
      throw new CompilerException(e);
    }
  }

  private void writeLineNumberBlock() throws CompilerException {
    try {
      dos.write(BLOCK_LINENUMBER);
      int lineNumberCount = lineNumberVector.size();
      dos.writeShort(lineNumberVector.size());
      for (int i = 0; i < lineNumberCount; i++) {
        LineNumber lineNumber = (LineNumber) lineNumberVector.elementAt(i);
        dos.writeShort(lineNumber.programCounter & 0xffff);
        dos.writeShort(lineNumber.lineNumber & 0xffff);
      }
    } catch (IOException e) {
      throw new CompilerException(e);
    }
  }

  private void writeEndMarker() throws CompilerException {
    try {
      dos.write(BLOCK_END);
    } catch (IOException e) {
      throw new CompilerException(e);
    }
  }

  private void writeOp(int op) {
    codeStream.write(op);
  }

  private void writeOpGet(Identifier identifier) {
    int index = identifier.index;

    if (Config.FASTLOCALS && enableLocalsOptimization &&  index >= 0) {
      writeXop(JsFunction.XOP_LCL_GET, index);
    } else {
      writeXop(JsFunction.XOP_PUSH_STR, getStringLiteralIndex(identifier.string));
      writeOp(JsFunction.OP_CTX_GET);
    }
  }

  private void writeOpSet(Identifier identifier) {
    int index = identifier.index;

    if (Config.FASTLOCALS && enableLocalsOptimization &&  index >= 0) {
      writeXop(JsFunction.XOP_LCL_SET, index);
    } else {
      writeXop(JsFunction.XOP_PUSH_STR, getStringLiteralIndex(identifier.string));
      writeOp(JsFunction.OP_CTX_SET);
    }
  }

  void visitWithNewLabelSet(Node node) throws CompilerException {
    Vector saveLabelSet = labelSet;
    labelSet = new Vector();
    if (node instanceof Statement) {
      ((Statement) node).visitStatement(this);
    } else if (node instanceof Expression) {
      ((Expression) node).visitExpression(this);
    }
    labelSet = saveLabelSet;
  }

  /**
   * Write a variable-length operation with an immediate parameter to the given
   * output stream.
   */
  void writeXop(int opcode, int param) {
    if (opcode == JsFunction.XOP_ADD) {
      switch (param) {
        case 1:
          writeOp(JsFunction.OP_INC);
          return;
        case -1:
          writeOp(JsFunction.OP_DEC);
          return;
      }
    }

    if ((param & 0x0ff80) == 0 || (param & 0x0ff80) == 0xff80) {
      codeStream.write(opcode << 1);
      codeStream.write(param);
    } else {
      codeStream.write((opcode << 1) | 1);
      codeStream.write(param >> 8);
      codeStream.write(param & 255);
    }
  }

  void writeJump(int op, Object base, String type) {
    int pos = codeStream.size() + 1;

    if (base instanceof String) {
      type = type + "-" + base;
    } else if (base instanceof Node) {
      type = type + "=" + base.hashCode();
    } else if (base == null) {
      throw new RuntimeException("Invalid position for " + type);
    } else {
      throw new RuntimeException("Illegal Jump base object");
    }

    Integer target = (Integer) jumpLabels.get(type);
    if (jumpLabels.get(type) == null) {
      writeXop(op, 32767);
      unresolvedJumps.addElement(type);
      unresolvedJumps.addElement(new Integer(pos));
    } else {
      // minus one for pc after decoding 8 bit imm
      int delta = target.intValue() - pos - 1;

      if (delta > -127 && delta < 128) {
        codeStream.write(op << 1);
        codeStream.write(delta);
      } else {
        // minus one more for pc after decoding 16 bit imm
        codeStream.write((op << 1) | 1);
        // minus one more for pc after decoding 16 bit imm
        delta -= 1;
        codeStream.write(delta >> 8);
        codeStream.write(delta & 255);
      }
    }
  }

  void setLabel(Node node, String label) {
    Integer pos = new Integer(codeStream.size());
    jumpLabels.put(label + "=" + node.hashCode(), pos);
    for (int i = 0; i < labelSet.size(); i++) {
      jumpLabels.put(label + "-" + labelSet.elementAt(i), pos);
    }
  }

  private void writeBinaryOperator(Token type) {
    if (type == Token.OPERATOR_ASSIGNMENT) {
      // should be handled as special case
      writeOp(JsFunction.OP_DROP);
    } else if (type == Token.OPERATOR_BITWISEAND
        || type == Token.OPERATOR_BITWISEANDASSIGNMENT) {
      writeOp(JsFunction.OP_AND);
    } else if (type == Token.OPERATOR_BITWISEOR
        || type == Token.OPERATOR_BITWISEORASSIGNMENT) {
      writeOp(JsFunction.OP_OR);
    } else if (type == Token.OPERATOR_BITWISEXOR
        || type == Token.OPERATOR_BITWISEXORASSIGNMENT) {
      writeOp(JsFunction.OP_XOR);
    } else if (type == Token.OPERATOR_COMMA) {
      // should be handled as special case in caller to avoid swap
      writeOp(JsFunction.OP_SWAP);
      writeOp(JsFunction.OP_DROP);
    } else if (type == Token.OPERATOR_DIVIDE
        || type == Token.OPERATOR_DIVIDEASSIGNMENT) {
      writeOp(JsFunction.OP_DIV);
    } else if (type == Token.OPERATOR_EQUALEQUAL) {
      writeOp(JsFunction.OP_EQEQ);
    } else if (type == Token.OPERATOR_EQUALEQUALEQUAL) {
      writeOp(JsFunction.OP_EQEQEQ);
    } else if (type == Token.OPERATOR_GREATERTHAN) {
      writeOp(JsFunction.OP_GT);
    } else if (type == Token.OPERATOR_GREATERTHANOREQUAL) {
      writeOp(JsFunction.OP_LT);
      writeOp(JsFunction.OP_NOT);
    } else if (type == Token.OPERATOR_LESSTHAN) {
      writeOp(JsFunction.OP_LT);
    } else if (type == Token.OPERATOR_LESSTHANOREQUAL) {
      writeOp(JsFunction.OP_GT);
      writeOp(JsFunction.OP_NOT);
    } else if (type == Token.OPERATOR_MINUS
        || type == Token.OPERATOR_MINUSASSIGNMENT) {
      writeOp(JsFunction.OP_SUB);
    } else if (type == Token.OPERATOR_MODULO
        || type == Token.OPERATOR_MODULOASSIGNMENT) {
      writeOp(JsFunction.OP_MOD);
    } else if (type == Token.OPERATOR_MULTIPLY
        || type == Token.OPERATOR_MULTIPLYASSIGNMENT) {
      writeOp(JsFunction.OP_MUL);
    } else if (type == Token.OPERATOR_NOTEQUAL) {
      writeOp(JsFunction.OP_EQEQ);
      writeOp(JsFunction.OP_NOT);
    } else if (type == Token.OPERATOR_NOTEQUALEQUAL) {
      writeOp(JsFunction.OP_EQEQEQ);
      writeOp(JsFunction.OP_NOT);
    } else if (type == Token.OPERATOR_PLUS
        || type == Token.OPERATOR_PLUSASSIGNMENT) {
      writeOp(JsFunction.OP_ADD);
    } else if (type == Token.OPERATOR_SHIFTLEFT
        || type == Token.OPERATOR_SHIFTLEFTASSIGNMENT) {
      writeOp(JsFunction.OP_SHL);
    } else if (type == Token.OPERATOR_SHIFTRIGHT
        || type == Token.OPERATOR_SHIFTRIGHTASSIGNMENT) {
      writeOp(JsFunction.OP_SHR);
    } else if (type == Token.OPERATOR_SHIFTRIGHTUNSIGNED
        || type == Token.OPERATOR_SHIFTRIGHTUNSIGNEDASSIGNMENT) {
      writeOp(JsFunction.OP_ASR);
    } else if (type == Token.KEYWORD_IN) {
      writeOp(JsFunction.OP_IN);
    } else if (type == Token.KEYWORD_INSTANCEOF) {
      writeOp(JsFunction.OP_INSTANCEOF);
    } else {
      throw new IllegalArgumentException("Not binary: " + type.toString());
    }
  }

  private void writeUnaryOperator(Token type) {
    if (type == Token.OPERATOR_PLUS) {
      writeXop(JsFunction.XOP_ADD, 0);
    } else if (type == Token.OPERATOR_MINUS) {
      writeOp(JsFunction.OP_NEG);
    } else if (type == Token.OPERATOR_BITWISENOT) {
      writeOp(JsFunction.OP_INV);
    } else if (type == Token.OPERATOR_LOGICALNOT) {
      writeOp(JsFunction.OP_NOT);
    } else if (type == Token.KEYWORD_VOID) {
      writeOp(JsFunction.OP_DROP);
      writeOp(JsFunction.OP_PUSH_UNDEF);
    } else if (type == Token.KEYWORD_TYPEOF) {
      writeOp(JsFunction.OP_TYPEOF);
    } else {
      throw new IllegalArgumentException("Not unary: " + type.toString());
    }
  }

  /** value must be on stack, is kept on stack */
  private void writeVarDef(String name, boolean initialize) {
    if (initialize) {
      writeXop(JsFunction.XOP_PUSH_STR, getStringLiteralIndex(name));
      writeOp(JsFunction.OP_CTX_SET);
    }
  }

  private void addToGlobalStringTable(String s) {
    if (globalStringMap.get(s) == null) {
      globalStringMap.put(s, new Integer(globalStringTable.size()));
      globalStringTable.addElement(s);
    }
  }

  private int getStringLiteralIndex(String string) {
    int i = stringLiterals.indexOf(string);
    if (i == -1) {
      i = stringLiterals.size();
      addToGlobalStringTable(string);
      stringLiterals.addElement(string);
    }
    return i;
  }

  //
  // nodes
  //

  public Program visit(Program program) throws CompilerException {
    for (int i = 0; i < program.functions.length; i++) {
      program.functions[i].visitStatement(this);
    }

    for (int i = 0; i < program.statements.length; i++) {
      program.statements[i].visitStatement(this);
    }

    writeMagic();
    writeGlobalStringTableBlock();
    writeStringLiteralBlock();
    writeNumberLiteralBlock();
    writeFunctionLiteralBlock();
    writeCodeBlock(0, 0, 0x00, codeStream.toByteArray());
    writeLineNumberBlock();
    writeEndMarker();

    return program;
  }

  //
  // statements
  //

  private void addLineNumber(Statement statement) {
    if (Config.LINENUMBER) {
      lineNumberVector.addElement(new LineNumber(codeStream.size(), statement.getLineNumber()));
    }
  }

  public Statement visit(FunctionDeclaration statement) throws CompilerException {
    addLineNumber(statement);

    statement.literal.visitExpression(this);
    writeOp(JsFunction.OP_DROP);
    return statement;
  }

  public Statement visit(BlockStatement statement) throws CompilerException {
    for (int i = 0; i < statement.statements.length; i++) {
      statement.statements[i].visitStatement(this);
    }
    return statement;
  }

  public Statement visit(BreakStatement statement) {
    addLineNumber(statement);

    writeJump(JsFunction.XOP_GO, statement.identifier == null
        ? (Object) currentBreakStatement : statement.identifier.string, "break");
    return statement;
  }

  public Statement visit(CaseStatement statement) {
    throw new RuntimeException("should not be visited");
  }

  public Statement visit(ContinueStatement statement) {
    addLineNumber(statement);

    writeJump(JsFunction.XOP_GO,
        statement.identifier == null
        ? (Object) currentBreakStatement
            : statement.identifier.string,
    "continue");
    return statement;
  }

  public Statement visit(DoStatement statement) throws CompilerException {
    addLineNumber(statement);

    Statement saveBreakStatement = currentBreakStatement;
    Statement saveContinueStatement = currentContinueStatement;
    currentBreakStatement = statement;
    currentContinueStatement = statement;

    setLabel(statement, "do");

    visitWithNewLabelSet(statement.statement);

    setLabel(statement, "continue");
    visitWithNewLabelSet(statement.expression);
    writeOp(JsFunction.OP_NOT);
    writeJump(JsFunction.XOP_IF, statement, "do");
    setLabel(statement, "break");

    currentBreakStatement = saveBreakStatement;
    currentContinueStatement = saveContinueStatement;
    return statement;
  }

  public Statement visit(EmptyStatement statement) {
    return statement;
  }

  public Statement visit(ExpressionStatement statement) throws CompilerException {
    addLineNumber(statement);

    statement.expression.visitExpression(this);
    writeOp(JsFunction.OP_DROP);
    return statement;
  }

  public Statement visit(ForInStatement statement) throws CompilerException {
    addLineNumber(statement);

    Statement saveBreakStatement = currentBreakStatement;
    Statement saveContinueStatement = currentContinueStatement;

    currentBreakStatement = statement;
    currentContinueStatement = statement;

    statement.expression.visitExpression(this);
    writeOp(JsFunction.OP_ENUM);
    setLabel(statement, "continue");
    writeJump(JsFunction.XOP_NEXT, statement, "break");

    if (statement.variable instanceof Identifier) {
      writeXop(JsFunction.XOP_PUSH_STR,
               getStringLiteralIndex(((Identifier) statement.variable).string));
      writeOp(JsFunction.OP_CTX_SET);
    } else if (statement.variable instanceof VariableDeclaration) {
      writeVarDef(((VariableDeclaration) statement.variable).identifier.string, true);
    } else {
      throw new IllegalArgumentException();
    }
    writeOp(JsFunction.OP_DROP);

    statement.statement.visitStatement(this);
    writeJump(JsFunction.XOP_GO, statement, "continue");
    setLabel(statement, "break");
    writeOp(JsFunction.OP_DROP);

    currentBreakStatement = saveBreakStatement;
    currentContinueStatement = saveContinueStatement;

    return statement;
  }

  public Statement visit(ForStatement statement) throws CompilerException {
    addLineNumber(statement);

    if (statement.initial != null) {
      statement.initial.visitExpression(this);
      if (!(statement.initial instanceof VariableExpression)) {
        writeOp(JsFunction.OP_DROP);
      }
    }

    Statement saveBreakStatement = currentBreakStatement;
    Statement saveContinueStatement = currentContinueStatement;

    currentBreakStatement = statement;
    currentContinueStatement = statement;

    setLabel(statement, "start");

    if (statement.condition != null) {
      visitWithNewLabelSet(statement.condition);
      writeJump(JsFunction.XOP_IF, statement, "break");
    }

    if (statement.statement != null) {
      visitWithNewLabelSet(statement.statement);
    }

    setLabel(statement, "continue");

    if (statement.increment != null) {
      visitWithNewLabelSet(statement.increment);
      writeOp(JsFunction.OP_DROP);
    }

    writeJump(JsFunction.XOP_GO, statement, "start");

    setLabel(statement, "break");

    currentBreakStatement = saveBreakStatement;
    currentContinueStatement = saveContinueStatement;

    return statement;
  }

  public Statement visit(IfStatement statement) throws CompilerException {
    addLineNumber(statement);

    statement.expression.visitExpression(this);
    if (statement.falseStatement == null) {
      writeJump(JsFunction.XOP_IF, statement, "endif");
      statement.trueStatement.visitStatement(this);
    } else {
      writeJump(JsFunction.XOP_IF, statement, "else");
      statement.trueStatement.visitStatement(this);
      writeJump(JsFunction.XOP_GO, statement, "endif");
      setLabel(statement, "else");
      statement.falseStatement.visitStatement(this);
    }
    setLabel(statement, "endif");
    return statement;
  }

  public Statement visit(ReturnStatement statement) throws CompilerException {
    addLineNumber(statement);

    if (statement.expression == null) {
      writeOp(JsFunction.OP_PUSH_UNDEF);
    } else {
      statement.expression.visitExpression(this);
    }
    writeOp(JsFunction.OP_RET);
    return statement;
  }

  public Statement visit(SwitchStatement statement) throws CompilerException {
    addLineNumber(statement);

    statement.expression.visitExpression(this);

    Statement saveBreakStatemet = currentBreakStatement;
    currentBreakStatement = statement;

    String defaultLabel = "break";

    for (int i = 0; i < statement.clauses.length; i++) {
      CaseStatement cs = statement.clauses[i];
      if (cs.expression == null) {
        defaultLabel = "case" + i;
      } else {
        writeOp(JsFunction.OP_DUP);
        cs.expression.visitExpression(this);
        writeOp(JsFunction.OP_EQEQEQ);
        writeOp(JsFunction.OP_NOT);
        writeJump(JsFunction.XOP_IF, statement, "case" + i);
      }
    }

    writeOp(JsFunction.OP_DROP);
    writeJump(JsFunction.XOP_GO, statement, defaultLabel);

    for (int i = 0; i < statement.clauses.length; i++) {
      setLabel(statement, "case" + i);
      Statement[] statements = statement.clauses[i].statements;
      for (int j = 0; j < statements.length; j++) {
        statements[j].visitStatement(this);
      }
    }
    setLabel(statement, "break");

    currentBreakStatement = saveBreakStatemet;
    return statement;
  }

  public Statement visit(ThrowStatement statement) throws CompilerException {
    addLineNumber(statement);

    statement.expression.visitExpression(this);
    if (currentTryStatement == null) {
      writeOp(JsFunction.OP_THROW);
    } else {
      writeJump(JsFunction.XOP_GO, currentTryStatement, "catch");
    }
    return statement;
  }

  public Statement visit(TryStatement statement) throws CompilerException {
    addLineNumber(statement);

    Statement saveTryStatement = currentTryStatement;
    String saveTryLabel = currentTryLabel;

    currentTryStatement = statement;
    currentTryLabel = statement.catchBlock == null ? "catch" : "finally";

    statement.tryBlock.visitStatement(this);

    writeJump(JsFunction.XOP_GO, statement, "end");

    if (statement.catchBlock != null) {
      setLabel(statement, "catch");
      if (statement.finallyBlock == null) {
        currentTryLabel = saveTryLabel;
        currentTryStatement = saveTryStatement;
      } else {
        currentTryLabel = "finally";
      }

      // add var and init from stack
      writeVarDef(statement.catchIdentifier.string, true);
      writeOp(JsFunction.OP_DROP);
      statement.catchBlock.visitStatement(this);

      writeJump(JsFunction.XOP_GO, statement, "end");
    }

    // reset everything
    currentTryStatement = saveTryStatement;
    currentTryLabel = saveTryLabel;

    if (statement.finallyBlock != null) {
      // finally block for the case that an exception was thrown --
      // it is kept on the stack and rethrown at the end
      setLabel(statement, "finally");
      statement.finallyBlock.visitStatement(this);

      if (currentTryStatement == null) {
        writeOp(JsFunction.OP_THROW);
      } else {
        writeJump(JsFunction.XOP_GO, currentTryStatement, "catch");
      }
    }

    // finally block if no exception was thrown
    setLabel(statement, "end");

    if (statement.finallyBlock != null) {
      statement.finallyBlock.visitStatement(this);
    }

    return statement;
  }

  public Statement visit(VariableStatement statement) throws CompilerException {
    for (int i = 0; i < statement.declarations.length; i++) {
      statement.declarations[i].visitExpression(this);
    }
    return statement;
  }

  public Statement visit(WhileStatement statement) throws CompilerException {
    addLineNumber(statement);

    Statement saveBreakStatement = currentBreakStatement;
    Statement saveContinueStatement = currentContinueStatement;

    currentBreakStatement = statement;
    currentContinueStatement = statement;

    setLabel(statement, "continue");
    visitWithNewLabelSet(statement.expression);
    writeJump(JsFunction.XOP_IF, statement, "break");

    visitWithNewLabelSet(statement.statement);

    writeJump(JsFunction.XOP_GO, statement, "continue");

    setLabel(statement, "break");

    currentBreakStatement = saveBreakStatement;
    currentContinueStatement = saveContinueStatement;
    return statement;
  }

  public Statement visit(WithStatement statement) throws CompilerException {
    addLineNumber(statement);

    if (currentTryStatement == null) {
      statement.expression.visitExpression(this);
      writeOp(JsFunction.OP_WITH_START);
      statement.statement.visitStatement(this);
      writeOp(JsFunction.OP_WITH_END);
    } else {
      // if an exception is thrown inside the with statement,
      // it is necessary to restore the context
      Statement saveTryStatement = currentTryStatement;
      String saveTryLabel = currentTryLabel;
      currentTryLabel = "finally";
      currentTryStatement = statement;
      statement.expression.visitExpression(this);
      writeOp(JsFunction.OP_WITH_END);
      statement.statement.visitStatement(this);
      writeOp(JsFunction.OP_WITH_END);
      writeJump(JsFunction.XOP_GO, statement, "end");

      currentTryStatement = saveTryStatement;
      currentTryLabel = saveTryLabel;

      setLabel(statement, "finally");
      writeOp(JsFunction.OP_WITH_END);
      writeOp(JsFunction.OP_THROW);
      setLabel(statement, "end");
    }
    return statement;
  }

  //
  // expression
  //

  public Expression visit(Identifier identifier) throws CompilerException {
    Expression pa = pendingAssignment;
    pendingAssignment = null;

    Identifier localVariable = (Identifier) localVariableTable.get(identifier);

    if (localVariable != null) {
      identifier = localVariable;
    }

    if (pa == null) {
      writeOpGet(identifier);
    } else if (pa instanceof AssignmentExpression) {
      ((AssignmentExpression) pa).rightExpression.visitExpression(this);
      writeOpSet(identifier);
    } else if (pa instanceof AssignmentOperatorExpression) {
      writeOpGet(identifier);
      ((AssignmentOperatorExpression) pa).rightExpression.visitExpression(this);
      writeBinaryOperator(((AssignmentOperatorExpression) pa).type);
      writeOpSet(identifier);
    } else if (pa instanceof IncrementExpression) {
      IncrementExpression ie = (IncrementExpression) pa;
      writeOpGet(identifier);
      writeXop(JsFunction.XOP_ADD, ((IncrementExpression) pa).value);
      writeOpSet(identifier);
      if (ie.post) {
        writeXop(JsFunction.XOP_ADD, -((IncrementExpression) pa).value);
      }
    } else if (pa instanceof DeleteExpression) {
      writeOp(JsFunction.OP_CTX);
      writeXop(JsFunction.XOP_PUSH_STR, getStringLiteralIndex(identifier.string));
      writeOp(JsFunction.OP_DEL);
    } else {
      throw new IllegalArgumentException();
    }

    return identifier;
  }

  public Expression visit(BinaryOperatorExpression expression) throws CompilerException {
    expression.leftExpression.visitExpression(this);
    expression.rightExpression.visitExpression(this);
    writeBinaryOperator(expression.operator);
    return expression;
  }

  public Expression visit(UnaryOperatorExpression expression) throws CompilerException {
    expression.subExpression.visitExpression(this);
    writeUnaryOperator(expression.operator);
    return expression;
  }

  public Expression visit(AssignmentExpression expression) throws CompilerException {

    Expression savePendingAssignment = pendingAssignment;
    pendingAssignment = expression;
    expression.leftExpression.visitExpression(this);
    if (pendingAssignment != null) {
      throw new RuntimeException("Pending assignment was not resolved");
    }
    pendingAssignment = savePendingAssignment;
    return expression;
  }

  public Expression visit(AssignmentOperatorExpression expression) throws CompilerException {
    Expression savePendingAssignment = pendingAssignment;
    pendingAssignment = expression;
    expression.leftExpression.visitExpression(this);
    if (pendingAssignment != null) {
      throw new RuntimeException("Pending assignment was not resolved");
    }
    pendingAssignment = savePendingAssignment;
    return expression;
  }

  public Expression visit(CallExpression expression) throws CompilerException {

    if (expression.function instanceof PropertyExpression) {
      PropertyExpression pe = (PropertyExpression) expression.function;
      pe.leftExpression.visitExpression(this);
      writeOp(JsFunction.OP_DUP);
      pe.rightExpression.visitExpression(this);
      writeOp(JsFunction.OP_GET);
    } else {
      writeOp(JsFunction.OP_PUSH_GLOBAL);
      expression.function.visitExpression(this);
    }
    // push arguments
    for (int i = 0; i < expression.arguments.length; i++) {
      expression.arguments[i].visitExpression(this);
    }

    if (currentTryStatement == null) {
      writeXop(JsFunction.XOP_CALL, expression.arguments.length);
    } else {
      writeXop(JsFunction.XOP_TRY_CALL, expression.arguments.length);
      writeJump(JsFunction.XOP_GO, currentTryStatement, currentTryLabel);
    }
    return expression;
  }

  public Expression visit(ConditionalExpression expression) throws CompilerException {
    expression.expression.visitExpression(this);
    writeJump(JsFunction.XOP_IF, expression, "else");
    expression.trueExpression.visitExpression(this);
    writeJump(JsFunction.XOP_GO, expression, "endif");
    setLabel(expression, "else");
    expression.falseExpression.visitExpression(this);
    setLabel(expression, "endif");
    return expression;
  }

  public Expression visit(DeleteExpression expression) throws CompilerException {
    Expression savePendingAssignment = pendingAssignment;
    pendingAssignment = expression;
    expression.subExpression.visitExpression(this);
    if (pendingAssignment != null) {
      throw new RuntimeException("Pending assignment was not resolved");
    }
    pendingAssignment = savePendingAssignment;
    return expression;
  }

  public Expression visit(LogicalAndExpression expression) throws CompilerException {
    expression.leftExpression.visitExpression(this);
    writeOp(JsFunction.OP_DUP);
    // jump (= skip) if false since false && any = false
    writeJump(JsFunction.XOP_IF, expression, "end");
    writeOp(JsFunction.OP_DROP);
    expression.rightExpression.visitExpression(this);
    setLabel(expression, "end");
    return expression;
  }

  public Expression visit(LogicalOrExpression expression) throws CompilerException {
    expression.leftExpression.visitExpression(this);
    writeOp(JsFunction.OP_DUP);
    // jump (= skip) if true since true && any =
    writeOp(JsFunction.OP_NOT);
    writeJump(JsFunction.XOP_IF, expression, "end");
    writeOp(JsFunction.OP_DROP);
    expression.rightExpression.visitExpression(this);
    setLabel(expression, "end");
    return expression;
  }

  public Expression visit(NewExpression expression) throws CompilerException {
    expression.function.visitExpression(this);
    writeOp(JsFunction.OP_NEW);
    if (expression.arguments != null) {
      for (int i = 0; i < expression.arguments.length; i++) {
        expression.arguments[i].visitExpression(this);
      }
      writeXop(JsFunction.XOP_CALL, expression.arguments.length);
    } else {
      writeXop(JsFunction.XOP_CALL, 0);
    }
    writeOp(JsFunction.OP_DROP);
    return expression;
  }

  public Expression visit(IncrementExpression expression) throws CompilerException {
    Expression savePendingAssignment = pendingAssignment;
    pendingAssignment = expression;
    expression.subExpression.visitExpression(this);
    if (pendingAssignment != null) {
      throw new RuntimeException("Pending assignment was not resolved");
    }
    pendingAssignment = savePendingAssignment;
    return expression;
  }

  public Expression visit(PropertyExpression expression) throws CompilerException {
    Expression pa = pendingAssignment;
    pendingAssignment = null;

    if (pa == null) {
      expression.leftExpression.visitExpression(this);
      expression.rightExpression.visitExpression(this);
      writeOp(JsFunction.OP_GET);
    } else if (pa instanceof AssignmentExpression) {
      // push value
      ((AssignmentExpression) pa).rightExpression.visitExpression(this);
      // push object
      expression.leftExpression.visitExpression(this);
      // push property
      expression.rightExpression.visitExpression(this);
      writeOp(JsFunction.OP_SET);
    } else if (pa instanceof AssignmentOperatorExpression) {
      // this case is a bit tricky...
      AssignmentOperatorExpression aoe = (AssignmentOperatorExpression) pa;
      expression.leftExpression.visitExpression(this);
      expression.rightExpression.visitExpression(this);
      // duplicate object and member
      writeOp(JsFunction.OP_DDUP);
      writeOp(JsFunction.OP_GET);
      // push value
      aoe.rightExpression.visitExpression(this);
      // exec assignment op
      writeBinaryOperator(aoe.type);
      // move result value below object and property
      writeOp(JsFunction.OP_ROT);
      writeOp(JsFunction.OP_SET);
    } else if (pa instanceof IncrementExpression) {
      IncrementExpression ie = (IncrementExpression) pa;
      expression.leftExpression.visitExpression(this);
      expression.rightExpression.visitExpression(this);
      // duplicate object and member
      writeOp(JsFunction.OP_DDUP);
      writeOp(JsFunction.OP_GET);
      // increment / decrement
      writeXop(JsFunction.XOP_ADD, ie.value);
      // move result value below object and property
      writeOp(JsFunction.OP_ROT);
      writeOp(JsFunction.OP_SET);
      if (ie.post) {
        writeXop(JsFunction.XOP_ADD, -ie.value);
      }
    } else if (pa instanceof DeleteExpression) {
      expression.leftExpression.visitExpression(this);
      expression.rightExpression.visitExpression(this);
      writeOp(JsFunction.OP_DEL);
    }
    return expression;
  }


  /**
   * Used in for statements only. Does not leave anything on the stack
   * -- in contrast to all other expressions. Handled properly in
   * visit(ForStatement).
   */
  public Expression visit(VariableExpression expression) throws CompilerException {
    for (int i = 0; i < expression.declarations.length; i++) {
      expression.declarations[i].visitExpression(this);
    }
    return expression;
  }

  public Expression visit(VariableDeclaration declaration) throws CompilerException {
    if (declaration.initializer != null) {
      declaration.initializer.visitExpression(this);
      writeVarDef(declaration.identifier.string, true);
      writeOp(JsFunction.OP_DROP);
    } else {
      writeVarDef(declaration.identifier.string, false);
    }
    return declaration;
  }

  //
  // Identifiers and literals
  //

  public Expression visit(ThisLiteral literal) {
    writeOp(JsFunction.OP_PUSH_THIS);
    return literal;
  }

  public Expression visit(NullLiteral literal) {
    writeOp(JsFunction.OP_PUSH_NULL);
    return literal;
  }

  public Expression visit(BooleanLiteral literal) {
    writeOp(literal.value ? JsFunction.OP_PUSH_TRUE
        : JsFunction.OP_PUSH_FALSE);
    return literal;
  }

  public Expression visit(NumberLiteral literal) {
    double v = literal.value;
    if (32767 >= v && v >= -32767 && v == Math.floor(v)) {
      writeXop(JsFunction.XOP_PUSH_INT, (int) v);
    } else {
      Double d = new Double(v);
      int i = numberLiterals.indexOf(d);
      if (i == -1) {
        i = numberLiterals.size();
        numberLiterals.addElement(d);
      }
      writeXop(JsFunction.XOP_PUSH_NUM, i);
    }

    return literal;
  }

  public Expression visit(StringLiteral literal) {
    writeXop(JsFunction.XOP_PUSH_STR, getStringLiteralIndex(literal.string));
    return literal;
  }

  public Expression visit(ArrayLiteral literal) throws CompilerException {
    writeOp(JsFunction.OP_NEW_ARR);
    for (int i = 0; i < literal.elements.length; i++) {
      if (literal.elements[i] == null) {
        writeOp(JsFunction.OP_PUSH_UNDEF);
      } else {
        literal.elements[i].visitExpression(this);
      }
      writeOp(JsFunction.OP_APPEND);
    }
    return literal;
  }

  public Expression visit(FunctionLiteral literal) throws CompilerException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    new CodeGenerationVisitor(this, literal, new DataOutputStream(baos));

    functionLiterals.addElement(baos.toByteArray());

    writeXop(JsFunction.XOP_PUSH_FN, functionLiterals.size() - 1);

    if (literal.name != null) {
      writeVarDef(literal.name.string, true);
    }
    return literal;
  }

  public Expression visit(ObjectLiteral literal) throws CompilerException {
    writeOp(JsFunction.OP_NEW_OBJ);
    for (int i = 0; i < literal.properties.length; i++) {
      literal.properties[i].visitExpression(this);
    }
    return literal;
  }

  public Expression visit(ObjectLiteralProperty property) throws CompilerException {
    property.name.visitExpression(this);
    property.value.visitExpression(this);
    writeOp(JsFunction.OP_SET_KC);

    return property;
  }

  public Statement visit(LabelledStatement statement) throws CompilerException {
    labelSet.addElement(statement.identifier.string);
    statement.statement.visitStatement(this);
    return statement;
  }
}
