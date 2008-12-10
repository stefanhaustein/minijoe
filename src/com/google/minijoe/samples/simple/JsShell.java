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
package com.google.minijoe.samples.simple;

import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import com.google.minijoe.compiler.Eval;
import com.google.minijoe.sys.JsObject;

/**
 * A simple MIDlet demonstrating how to use the Eval class for compiling 
 * and interpreting JS.
 * 
 * @author Stefan Haustein
 */
public class JsShell extends MIDlet implements CommandListener {

  static final int MAX_INPUT_SIZE = 32000;
  static final Command CMD_EVAL = new Command("Eval", Command.OK, 0);
  static final Command CMD_BACK = new Command("Back", Command.BACK, 1);
  static final Command CMD_EXIT = new Command("Exit", Command.EXIT, 1);
  
  TextBox textBox = new TextBox("JsShell", "", MAX_INPUT_SIZE, TextField.ANY);
  Vector history = new Vector();
  JsObject global = Eval.createGlobal();
  
  public JsShell() {
    textBox.addCommand(CMD_EVAL);
    textBox.addCommand(CMD_BACK);
    textBox.addCommand(CMD_EXIT);
    
    textBox.setCommandListener(this);
  }
  
  protected void destroyApp(boolean unconditional)
      throws MIDletStateChangeException {
  }

  protected void pauseApp() {
  }

  protected void startApp() throws MIDletStateChangeException {
    Display.getDisplay(this).setCurrent(textBox);
  }

  public void commandAction(Command cmd, Displayable d) {
    if (cmd == CMD_EXIT) {
      notifyDestroyed();
    } else if (cmd == CMD_BACK) {
      int size = history.size();
      if (size == 0) {
        textBox.setString("");
      } else {
        textBox.setString((String) history.elementAt(size - 1));
        history.removeElementAt(size - 1);
      }
    } else if (cmd == CMD_EVAL) {
      String expr = textBox.getString();
      history.addElement(expr);
      try {
        textBox.setString("" + Eval.eval(expr, global));
      } catch (Exception e) {
        textBox.setString(e.toString());
      }
    }
  }
}
