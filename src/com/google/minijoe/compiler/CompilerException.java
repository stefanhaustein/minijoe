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
public class CompilerException extends Exception {
  private Exception exception;
  private int lineNumber;

  private static final long serialVersionUID = -7708214028354270444L;

  public CompilerException(String message) {
    super(message);
  }

  public CompilerException(String message, int lineNumber) {
    super(message);
    
    this.lineNumber = lineNumber;
  }

  public CompilerException(Exception exception) {
    super();
    
    this.exception = exception;
  }

  public Exception getException() {
    return exception;
  }

  public int getLineNumber() {
    if (lineNumber > 0) {
      return lineNumber;
    } else if (exception != null && exception instanceof CompilerException) {
      return ((CompilerException) exception).getLineNumber();
    } else {
      return 0;
    }
  }

  public String getMessage() {
    String message = super.getMessage() + " at line: " + lineNumber;

    if (message != null) {
      return message;
    } else if (exception != null) {
      return exception.getMessage();
    } else {
      return null;
    }
  }

  public void printStackTrace() {
    super.printStackTrace();

    if (exception != null) {
      System.err.print("caused by:");
      exception.printStackTrace();
    }
  }
}
