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

import com.google.minijoe.compiler.ast.Program;
import com.google.minijoe.compiler.visitor.CodeGenerationVisitor;
import com.google.minijoe.compiler.visitor.DeclarationVisitor;
import com.google.minijoe.compiler.visitor.RoundtripVisitor;
import com.google.minijoe.sys.JsArray;
import com.google.minijoe.sys.JsFunction;
import com.google.minijoe.sys.JsObject;
import com.google.minijoe.sys.JsSystem;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Simple facade for the parser and code generator
 *
 * @author Stefan Haustein
 */
public class Eval extends JsObject {
  static final int ID_EVAL = 100;

  static final JsObject COMPILER_PROTOTYPE = new JsObject(OBJECT_PROTOTYPE);

  public Eval() {
    super(COMPILER_PROTOTYPE);
    scopeChain = JsSystem.createGlobal();
    addVar("eval", new JsFunction(ID_EVAL, 2));
  }

  public static JsObject createGlobal() {
    return new Eval();
  }

  public void evalNative(int index, JsArray stack, int sp, int parCount) {
    switch (index) {
      // object methods

      case ID_EVAL:
        try {
          stack.setObject(
              sp,
              eval(stack.getString(sp + 2),
              stack.isNull(sp + 3) ? stack.getJsObject(sp) : stack.getJsObject(sp + 3))
          );
        } catch (Exception e) {
          throw new RuntimeException("" + e);
        }

        break;

      default:
        super.evalNative(index, stack, sp, parCount);
    }
  }

  public static void compile(String input, OutputStream os) throws CompilerException, IOException {
    Lexer lexer = new Lexer(input);
    Parser parser = new Parser(lexer);

    Program program = parser.parseProgram();

    if (Config.DEBUG_SOURCE) {
      Writer w = new OutputStreamWriter(System.out);
      new RoundtripVisitor(w).visit(program);
      w.flush();
    }

    // handle variable and function declarations
    new DeclarationVisitor().visit(program);

    DataOutputStream dos = new DataOutputStream(os);
    new CodeGenerationVisitor(dos).visit(program);
    dos.flush();
  }
  
  public static Object eval(String input, JsObject context) throws CompilerException, IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    compile(input, baos);
    byte[] code = baos.toByteArray();

    if (Config.DEBUG_DISSASSEMBLY) {
      new Disassembler(new DataInputStream(new ByteArrayInputStream(code))).dump();
    }

    return JsFunction.exec(new DataInputStream(new ByteArrayInputStream(code)), context);
  }
}
