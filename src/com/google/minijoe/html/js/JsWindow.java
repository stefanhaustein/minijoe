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

package com.google.minijoe.html.js;

import com.google.minijoe.html.HtmlWidget;
import com.google.minijoe.sys.JsArray;
import com.google.minijoe.sys.JsFunction;
import com.google.minijoe.sys.JsObject;
import com.google.minijoe.sys.JsSystem;

import java.util.Vector;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;

/**
 * Provides a simple JavaScript environment providing a set of 
 * methods and fields described in at
 * http://docs.google.com/a/google.com/Doc?id=cdj7wdfh_48dw4jmcfc
 * 
 * @author Stefan Haustein
 */
public class JsWindow extends JsObject implements Runnable {

  static final int ID_GET_ELEMENT_BY_ID = 1000;
  static final int ID_WRITE = 1001;
  static final int ID_SET_TIMEOUT = 1003;
  static final int ID_SET_INTERVAL = 1004;
  
  static int scheduleId;
  
  static final JsObject PROTOTYPE = new JsObject(OBJECT_PROTOTYPE);
  /** 
   * entries are Object[] containing execution time, method, interval
   */
  Vector schedule = new Vector();
  static int timerId;
  JsArray stack = new JsArray();
  boolean stop = false;
  Object eventLock = new Object();
  HtmlWidget rootDocument;
  
  
  public Object getEventLock() {
	return eventLock;
}

/**
   * Constructs a new environment.
   * 
   */
  public JsWindow(HtmlWidget rootDocument) {
    super(PROTOTYPE);
    
    this.rootDocument = rootDocument;
    scopeChain = JsSystem.createGlobal();
    
    addVar("getElementById", new JsFunction(ID_GET_ELEMENT_BY_ID, 1));
    addVar("write", new JsFunction(ID_WRITE, 1));
   
    addVar("setTimeout", new JsFunction(ID_SET_TIMEOUT, 2));
    addVar("setInterval", new JsFunction(ID_SET_INTERVAL, 2));
    addVar("document", this);
    addVar("window", this);
    
    if (rootDocument != null) {
	    addVar("width", new Double(rootDocument.getWidth()));
	    addVar("height", new Double(rootDocument.getHeight()));
	    addVar("innerWidth", new Double(rootDocument.getWidth()));
	    addVar("innerHeight", new Double(rootDocument.getHeight()));
    } else {
    	System.out.println("null doc root passed to JsWIndow");
    }
    stack.setObject(0, this);
  }

  /**
   * Java implementation of the provided JS methods and fields.
   */
  public void evalNative(int id, JsArray stack, int sp, int parCount){
    switch(id){
      case ID_GET_ELEMENT_BY_ID:
        stack.setObject(sp, this);
        break;
        
      case ID_WRITE:
        write(stack.getString(sp + 2));
        break;
        
      case ID_SET_TIMEOUT:
        stack.setNumber(sp, 
            schedule((JsFunction) stack.getObject(sp + 2), stack.getInt(sp + 3), false)); 
        break;

      case ID_SET_INTERVAL:
        stack.setNumber(sp, 
            schedule((JsFunction) stack.getObject(sp + 2), stack.getInt(sp + 3), true)); 
        break;

    default:
      super.evalNative(id, stack, sp, parCount);
    }
  }

  public HtmlWidget getRootDocument() {
	return rootDocument;
}

private void write(String string) {
    System.out.println("document.write: " + string);
  }

  /**
   * Schedule a callback
   * 
   * @param function function to be called
   * @param dt time interval
   * @param repeat if true, calls are rescheduled automatically 
   * @return a id that can be used to terminate an interval
   */
  private int schedule(JsFunction function, int dt, boolean repeat) {
    // this should be binary search but if there should not be more than 1-3 entries anyway...
    
    long t0 = System.currentTimeMillis() + dt;
    
    synchronized (schedule) {
      int i = schedule.size() - 1;
      while (i >= 0 && ((Long) ((Object[]) schedule.elementAt(i))[1]).longValue() > t0) {
        i--;
      }
      
      if (repeat) {
        schedule.insertElementAt(new Object[]{
            new Integer(++timerId), 
            new Long(t0), 
            function, 
            new Integer(dt)}, i + 1);
      } else {
        schedule.insertElementAt(new Object[]{
            new Integer(++timerId), 
            new Long(t0), 
            function}, i + 1);
      }
      return timerId;
    }
  }

  /** 
   * Runs the scheduler.
   */
  public void run() {
    try {
      while (!stop) {
        if (schedule.size() == 0) {
          synchronized (eventLock) {
            eventLock.wait(20);
          }
          continue;
        }
        
        Object[] next;
        synchronized (schedule) {
          next = (Object[]) schedule.elementAt(0);
          schedule.removeElementAt(0);
        }
        long time = ((Long) next[1]).longValue();
        JsFunction call = (JsFunction) next[2];
        synchronized (eventLock) {
          eventLock.wait (Math.max(5, time - System.currentTimeMillis()));
          // Note: stack[0] is filled with 'this' in the constructor 
          stack.setObject(1, this);
          stack.setObject(2, call);
          call.eval(stack, 1, 0);
        }
        //screen.drawTree(g, dx, dy, clipX, clipY, clipW, clipH); //FIXME: need to get hold of g
        if (next.length == 4) {
          schedule(call, ((Integer) next[3]).intValue(), true);
        }
      }
    } catch (Exception e) {
    	//FIXME need to handle exception better then just swallowing with err mesg
    	System.out.println(e);
    }
  }
  
  /**
   * The scope to use whe doing callbacks into JS functions
   * @return
   */
  public JsObject getCallbackScope() {
	  return this;
  }
  

  /**
   * Calls the given JavaScript function with the given key event. 
   * Google internal cursor  key codes are converted to PC key codes.
   * 
   * @param f the function to be called
   * @param key the key event
   */
  public void keyEvent(JsFunction f, int code, int action) {
    JsObject event = new JsObject(JsObject.OBJECT_PROTOTYPE);

    switch(action){
      case Canvas.LEFT:
        code = 37;
        break;
      case Canvas.UP:
        code = 38;
        break;
      case Canvas.RIGHT: 
        code = 39;
        break;
      case Canvas.DOWN:
        code = 40;
        break; 
    }
   
    event.addVar("keyCode", new Double(code));
    synchronized (eventLock) {
      stack.setObject(1, this);
      stack.setObject(2, f);
      stack.setObject(3, event);
      f.eval(stack, 1, 1);
    }
  }
  
  public String toString(){
	  return "[object JsWindow]";
  }

}
