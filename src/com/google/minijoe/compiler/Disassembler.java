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

import java.io.DataInputStream;
import java.io.IOException;

/**
 * @author Andy Hayward
 */
public class Disassembler {
  static final String HEX_DIGITS = "0123456789ABCDEF";

  static final String[] OPCODES = {
    "NOP",
    "ADD",
    "AND",
    "APPEND",
    "ASR",
    "ENUM",
    "IN",
    "DIV",
    "DUP",
    "EQEQ",
    "CTX_GET",
    "GET",
    "CTX",
    "DEL",
    "GT",
    "THROW",
    "INC",
    "DEC",
    "LT",
    "MOD",
    "MUL",
    "NEG",
    "NEW_ARRAY",
    "NEW_OBJ",
    "NEW",
    "NOT",
    "OR",
    "DROP",
    "PUSH_TRUE",
    "PUSH_FALSE",
    "RET",
    "CTX_SET",
    "SET_KC",
    "SET",
    "SHL",
    "SHR",
    "SUB",
    "SWAP",
    "PUSH_THIS",
    "PUSH_NULL",
    "UNDEF",
    "DDUP",
    "ROT",
    "EQEQEQ",
    "XOR",
    "INV",
    "WITH_START",
    "WITH_END",
    "ABOVE",
    "INSTANCEOF",
    "TYPEOF",
    "PUSH_GLOBAL"
  };

  static final int XCODE_START = 0xEA;

  static final String[] XCODES = {
    "PUSH_FN",
    "PUSH_NUM",
    "GO", "IF",
    "CALL",
    "LINE",
    "LCL_GET",
    "LCL_SET",
    "NEXT",
    "PUSH_INT",
    "PUSH_STR"
  };


  DataInputStream dis;
  String[] globalStringTable;
  String indent = "";
  String[] stringLiterals;
  double[] numberLiterals;
  String[] localVariableNames;

  public Disassembler(DataInputStream dis) {
    this.dis = dis;
  }

  Disassembler(DataInputStream dis, String[] globalStringTable, String indent) {
    this.dis = dis;
    this.globalStringTable = globalStringTable;
    this.indent = indent;
  }

  public void dump() throws IOException{
    StringBuffer buf = new StringBuffer(7);
    for (int i = 0; i < 7; i++) {
      buf.append((char) dis.read());
    }
    String magic = buf.toString();
    System.out.println("Header: \"" + magic + "\" Version " + dis.read());

    if (!"MiniJoe".equals(magic)) {
      throw new IOException("Magic does not match \"MiniJoe\"!");
    }

    dumpTables();

    System.out.println("EOF: " + (dis.read() == -1));
  }

  void dumpTables() throws IOException{
    loop:
      while (true) {
        int type = dis.read();
        System.out.print(indent);
        System.out.print("Block type ");
        printHex(type);
        System.out.print(": ");
        int count;
        switch (type) {
          case 0x00:
            System.out.println("Comment");
            System.out.print(indent);
            System.out.println(dis.readUTF());

            break;

          case 0x10:
            count = dis.readUnsignedShort();
            System.out.println("Global String Table (" + count + " entries)");
            globalStringTable = new String[count];
            for (int i = 0; i < count; i++) {
              globalStringTable[i] = dis.readUTF();
              System.out.println(indent + "  " + i + ": \"" + globalStringTable[i] + "\"");
            }
            break;

          case 0x20:
            count = dis.readUnsignedShort();
            System.out.println("Number Literals (" + count + " entries)");
            numberLiterals = new double[count];
            for (int i = 0; i < count; i++) {
              numberLiterals[i] = dis.readDouble();
              System.out.println(indent + "  " + i + ": " + numberLiterals[i]);
            }
            break;

          case 0x30:
            count = dis.readUnsignedShort();
            System.out.println("String Literals (" + count + " entries)");
            stringLiterals = new String[count];
            for (int i = 0; i < count; i++) {
              int index = dis.readUnsignedShort();
              System.out.println(indent + "  " + i + " -> " + index + ": \"" + globalStringTable[index] + "\"");
              stringLiterals[i] = globalStringTable[index];
            }
            break;

          case 0x40:
            count = dis.readUnsignedShort();
            System.out.println("Regex Literals (" + count + " entries)");
            stringLiterals = new String[count];
            for (int i = 0; i < count; i++) {
              int index = dis.readUnsignedShort();
              System.out.println(indent + "  " + i + " -> " + index + ": \"" + globalStringTable[index] + "\"");
            }
            break;

          case 0x50:
            count = dis.readUnsignedShort();
            System.out.println("Function Literals (" + count + " entries)");
            for (int i = 0; i < count; i++) {
              System.out.println(indent + "  function literal " + i + ": ");
              new Disassembler(dis, globalStringTable, indent + "    ").dumpTables();
            }
            break;

          case 0x60:
            count = dis.readUnsignedShort();
            System.out.println("Local Variable Names (" + count + " entries)");
            localVariableNames = new String[count];
            for (int i = 0; i < count; i++) {
              int index = dis.readUnsignedShort();
              System.out.println(indent + "  " + i + " -> " + index + ": \"" + globalStringTable[index] + "\"");
              localVariableNames[i] = globalStringTable[index];
            }
            break;

          case 0x080:
            int locals = dis.readUnsignedShort();
            int parameters = dis.readUnsignedShort();
            int flags = dis.read();
            int size = dis.readUnsignedShort();
            System.out.println("Code (locals:" + locals + " param:" + parameters + " flags:" + Integer.toBinaryString(flags) + " size: " + size + ")");
            byte[] code = new byte [size];
            dis.readFully(code);
            disassemble(code);
            break;

          case 0xE0:
            count = dis.readUnsignedShort();
            System.out.println("Line Numbers (" + count + " entries)");
            for (int i = 0; i < count; i++) {
              int programCounter = dis.readUnsignedShort();
              int lineNumber = dis.readUnsignedShort();
              System.out.print(indent + "  ");
              printHex(programCounter >> 8);
              printHex(programCounter);
              System.out.println(" line = " + lineNumber);
            }
            break;
            
          case 0x0ff:
            System.out.println("End Marker");
            break loop;

          default:
            System.out.println("Unknown block type -- aborting");
            throw new IOException("Unknown block type: " + type);
        }
      }
  }

  void printHex(int i) {
    System.out.print(HEX_DIGITS.charAt((i >> 4) & 15));
    System.out.print(HEX_DIGITS.charAt(i & 15));
  }

  private void disassemble(byte[] code) {
    int i = 0;

    while (i < code.length) {
      int opcode;

      String name = null;

      System.out.print(indent + "  ");

      printHex(i >> 8);
      printHex(i & 255);
      System.out.print(' ');
      
      printHex(opcode = code[i++]);
      System.out.print(' ');
      if (opcode >= 0) {
        System.out.print("     ");
        if (opcode < OPCODES.length) {
          name = OPCODES[opcode];
        }
        System.out.print(name == null ? "???" : name);
      } else {
        int index = ((opcode & 0x0ff) - XCODE_START) >>> 1;
        if (index < XCODES.length) {
          name = XCODES[index];
        }
        printHex(code[i]);
        int imm;
        if ((opcode & 1) == 0) {
          System.out.print("  ");
          imm = code[i++];
        } else {
          imm = (code[i] << 8) | (code[i + 1] & 0xff);
          i++;
          printHex(code[i++]);
        }
        System.out.print(' ');
        System.out.print(name == null ? "???" : name);
        System.out.print(' ');
        System.out.print("" + imm);

        switch (opcode & 0xfe) {
          case 0xEC:
            System.out.print(" -> " + numberLiterals[imm]);
            break;
          case 0xFE:
            System.out.print(" -> \"" + stringLiterals[imm] + "\"");
            break;
        }
      }
      System.out.println();
    }
  }
}
