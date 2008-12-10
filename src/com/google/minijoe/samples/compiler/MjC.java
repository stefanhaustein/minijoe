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

package com.google.minijoe.samples.compiler;

import com.google.minijoe.compiler.CompilerException;
import com.google.minijoe.compiler.Eval;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Example showing how to use the MiniJoe compiler. Used in the ANT build file
 * to generate the compiled JS resources.
 * 
 * @author Stefan Haustein
 */

public class MjC {

  private MjC() {
  }

  public static void main(String[] argv) throws IOException, CompilerException {
    if (argv.length != 1) {
      System.out.println("Parameter: File to compile.");
      System.out.println("Bytecode will be writen to STDOUT");
      System.exit(0);
    }

    File file = new File(argv[0]);
    DataInputStream dis = new DataInputStream(new FileInputStream(file));
    byte[] data = new byte[(int) file.length()];
    dis.readFully(data);
    String code = new String(data, "UTF-8");
    Eval.compile(code, System.out);
  }
}
