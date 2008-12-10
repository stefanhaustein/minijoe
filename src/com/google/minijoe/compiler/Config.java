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
 *
 * @author Andy Hayward
 */
public class Config {
  /**
   * Private constructor to prevent instantiation.
   */
  private Config() {
  }

  /**
   * Enable all debugging.
   */
  public static final boolean DEBUG_ALL = false;

  /**
   * Display the rewritten source before execution.
   */
  public static final boolean DEBUG_SOURCE = DEBUG_ALL | false;

  /**
   * Display the parse tree before execution.
   */
  public static final boolean DEBUG_PARSETREE = DEBUG_ALL | false;

  /**
   * Display the disassembly before execution.
   */
  public static final boolean DEBUG_DISSASSEMBLY = DEBUG_ALL | false;

  /**
   * Fast locals support.
   */
  public static final boolean FASTLOCALS = true;

  /**
   * Line number support.
   */
  public static final boolean LINENUMBER = true;
}
