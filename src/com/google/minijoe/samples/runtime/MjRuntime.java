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

package com.google.minijoe.samples.runtime;

import com.google.minijoe.sys.JsFunction;

import java.io.DataInputStream;
import java.io.IOException;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

/**
 * Example illustrating how to execute compiled JS code. Here, the code
 * is included in the JAR by the build script. 
 *  
 * @author Stefan Haustein
 */
public class MjRuntime extends MIDlet implements CommandListener {

  static final Command CMD_EXIT = new Command("Exit", Command.EXIT, 1);
  
  static final String[] SAMPLES = {
    "canvasout.mjc", "canvasoids.mjc", "clock.mjc", "functionplot.mjc", 
    "helloworld.mjc"};

  List applicationList = new List("JS Apps", List.IMPLICIT, SAMPLES, null);

  public MjRuntime() {
    applicationList.addCommand(CMD_EXIT);
    applicationList.setCommandListener(this);
  }
  
  protected void destroyApp(boolean unconditional)
      throws MIDletStateChangeException {
  }

  protected void pauseApp() {
  }

  public void startApp() throws MIDletStateChangeException {
    Display.getDisplay(this).setCurrent(applicationList);   
  }
  
  public void commandAction(Command cmd, Displayable d) {
    if (cmd == CMD_EXIT) {
      notifyDestroyed();
    } else {
      String name = "/" + applicationList.getString(
          applicationList.getSelectedIndex());
      Environment env = new Environment(this);
      Display.getDisplay(this).setCurrent(env.screen);
      try {
        JsFunction.exec(
            new DataInputStream(getClass().getResourceAsStream(name)), env);
        
        new Thread(env).start();
      } catch (IOException e) {
        throw new RuntimeException(e.toString());
      }    
    }
  }
}
