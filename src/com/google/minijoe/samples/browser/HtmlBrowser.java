// Copyright 2010 Google Inc.
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

package com.google.minijoe.samples.browser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import com.google.minijoe.common.Util;
import com.google.minijoe.html.HtmlWidget;
import com.google.minijoe.html.InputWidget;
import com.google.minijoe.html.SystemRequestHandler;

import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextBox;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

/**
 * Example application illustrating how to use the HtmlWidget.
 * 
 * @author Stefan Haustein
 */
public class HtmlBrowser extends MIDlet implements SystemRequestHandler, CommandListener, Runnable {

  static final int COOKIE_NAME = 0;
  static final int COOKIE_VALUE = 1;
  static final int COOKIE_HOST = 2;
  static final int COOKIE_PATH = 3;
  static final int COOKIE_EXPIRES = 4;
  static final int COOKIE_SECURE = 5;

  static final int MAX_INPUT_SIZE = 8000;

  static final String[] MENU_OPTIONS = {"Start Page", "Exit"};
  static final int MENU_START_PAGE = 0;
  static final int MENU_EXIT = 1;
  
  static final Command CMD_CANCEL = new Command("Cancel", Command.CANCEL, 0);
  static final Command CMD_TEXT_OK = new Command("OK", Command.OK, 0);
  static final Command CMD_LIST_OK = new Command("OK", Command.OK, 0);

  Vector screenStack = new Vector();
  Vector cookies = new Vector();
  Vector requestQueue = new Vector();
  HtmlScreen currentScreen;
  boolean exit;
  int threadCount;
  Display display;
  InputWidget currentInput;

  private List list;
  private TextBox textBox;
  private List menu;
  String userAgent;
  
  public HtmlBrowser() {    
  }

  protected void startApp() throws MIDletStateChangeException {

    display = Display.getDisplay(this);
    userAgent = "MiniJoe/0.5 (like Opera Mini/2.0)" + 
    " Platform/" + getAppProperty("microedition.platform") + 
	" Configuration/" + getAppProperty("microedition.configuration") +
    " Profile/" + getAppProperty("microEdition.profile");
    
    
    StringBuffer menu = new StringBuffer("<html><head><title>Bookmarks</title></head><body><ul>");
    
    addLink(menu, "http://hsivonen.iki.fi/test/xhtml-suite/xhtml-basic.xhtml");
    addLink(menu, "http://www.google.com/m");
    addLink(menu, "http://mobile.google.com");
    addLink(menu, "http://wwf.mobi/");
    addLink(menu, "http://www.bigbaer.com/css_tutorials/css.float.html.tutorial.htm");
    addLink(menu, "http://en.m.wikipedia.org");
    addLink(menu, "http://moblogga.mobi/");
    addLink(menu, "http://qeep.mobi/xmps/homepage.do");
    addLink(menu, "http://wap.cellufun.com/games.asp?g=mob&f=admobmw");
    addLink(menu, "http://freesim.o2.co.uk/R4QAYdLT");

    try {
      HtmlScreen scr = showPage(null);
      scr.htmlWidget.load(new ByteArrayInputStream(
          menu.toString().getBytes("UTF-8")), "UTF-8");
      scr.setStatus(null);
      scr.repaint();

    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException("" + e);
    }
  }

  public void addLink(StringBuffer buf, String href) {
    buf.append("<li><a href='").append(href).
    append("'>").append(href).append("</a></li>");
  }

  public HtmlScreen showPage(String url) {
    currentScreen = new HtmlScreen(this, url);
    screenStack.addElement(currentScreen);
    display.setCurrent(currentScreen);
    return currentScreen;
  }

  /**
   * RequestHandler implementation
   */
  public void requestResource(HtmlWidget source, int requestMethod, 
      String url, int expectedContentType, byte[] data) {

	System.out.println("Request: " + url);  
	if (!url.toLowerCase().startsWith("http")) {
	  try {
		platformRequest(url);
      } catch (ConnectionNotFoundException e) {
		e.printStackTrace();
	  }
	  return;
	}
	
	  
    if (requestQueue.size() >= threadCount && threadCount < 4) {
      threadCount++;
      new Thread(this).start();
    }

    if (expectedContentType == SystemRequestHandler.TYPE_DOCUMENT) {
      showPage(url);
      synchronized (requestQueue) {
        requestQueue.insertElementAt(new ResourceRequester(currentScreen, 
            requestMethod, url, expectedContentType, data), 0);
        requestQueue.notify();
      }
    } else {
      HtmlScreen screen = currentScreen;
      for (int i = screenStack.size() - 1; i >= 0; i--) {
        HtmlScreen s = (HtmlScreen) screenStack.elementAt(i);
        if (source == s.htmlWidget) {
          screen = s;
        }
        Object res = s.htmlWidget.getResource(url, -1, null);
        if (res != null) {
          source.addResource(url, res);
          return;
        }
      }
      requestQueue.insertElementAt(new ResourceRequester(screen, 
          requestMethod, url, expectedContentType, data), 
          expectedContentType == SystemRequestHandler.TYPE_STYLESHEET 
          ? 0 : requestQueue.size());
    }
  }



  /**
   * Returns the cookies header value for the give URL
   * @param url the URL to collect cookies for
   * @return the aggregated cookie value
   */
  public String getCookies(String url) {
    int cut0 = url.indexOf("://");
    int cut1 = url.indexOf('/', cut0 + 3);
    if (cut1 == -1){
      cut1 = url.length();
    }

    boolean secure = url.toLowerCase().startsWith("https:");

    StringBuffer buf = new StringBuffer();

    String domain = url.substring(cut0 + 3, cut1);
    String path = cut1 == url.length() ? "/" : url.substring(cut1);

    int cut2 = domain.indexOf(':');
    if (cut2 != -1){
      domain = domain.substring(0, cut2);
    }

    long now = System.currentTimeMillis();
    for (int i = cookies.size() - 1; i >= 0; i--){
      String[] cookie = (String[]) cookies.elementAt(i);
      long expires = Long.parseLong(cookie[COOKIE_EXPIRES]);
      if (expires != -1 && expires < now){
        cookies.removeElementAt(i);
      } else if (secure || cookie[COOKIE_SECURE] == null || 
          cookie[COOKIE_SECURE].toLowerCase().equals("false")) {
        String cDomain = cookie[COOKIE_HOST];
        String cPath = cookie[COOKIE_PATH];
        if (domain.endsWith(cDomain) && path.startsWith(cPath)) {
          if (buf.length() > 0) {
            buf.append("; "); 
          }
          buf.append(cookie[COOKIE_NAME]);
          buf.append('=');
          buf.append(cookie[COOKIE_VALUE]);            
        }
      }
    }
    return buf.length() == 0 ? null : buf.toString();
  }

  public void setCookie(String domain, String path, String key, String value,
      String expires, String secure) {
    for (int i = cookies.size() - 1; i >= 0; i--) {
      String[] exCookie = (String[]) cookies.elementAt(i);
      String exDomain = exCookie[COOKIE_HOST];
      String exPath = exCookie[COOKIE_PATH];
      String exKey = exCookie[COOKIE_NAME];

      if (key.equals(exKey) && domain.equals(exDomain) && path.equals(exPath)) {
        cookies.removeElementAt(i);
      }
    }

    String[] cookie = new String[6];
    cookie[COOKIE_HOST] = domain;
    cookie[COOKIE_PATH] = path;
    cookie[COOKIE_NAME] = key;
    cookie[COOKIE_VALUE] = value;
    cookie[COOKIE_SECURE] = secure;

    long expiresMillis = -1;
    if (expires != null) {
      try {
        expiresMillis = Util.parseHttpDateTime(expires);
      } catch (NumberFormatException e) {
        // ignore invalid date
      }
    }
    cookie[COOKIE_EXPIRES] = "" + expiresMillis;
    cookies.addElement(cookie);
  }

  public void run() {
    try {
      while (!exit) {
        if (requestQueue.size() == 0) {
          synchronized (requestQueue) {
            try {
              requestQueue.wait(1000);
            } catch (InterruptedException e) {
              break;
            }
          }
        } else {
          ResourceRequester requester;
          synchronized (requestQueue) {
            requester = (ResourceRequester) requestQueue.elementAt(0);
            requestQueue.removeElementAt(0);
          }
          requester.run();
        }
      }
    } finally {
      threadCount--;
    }
  }

  public void requestPopup(InputWidget source, Vector options, int selectedIndex) {
    if (list == null) {
      list = new List("Select", List.EXCLUSIVE);
      list.addCommand(CMD_LIST_OK);
      list.addCommand(CMD_CANCEL);
      list.setCommandListener(this);
    }

    currentInput = source;
    list.deleteAll();
    for (int i = 0; i < options.size(); i++) {
      list.append((String) options.elementAt(i), null);
    }
    if (selectedIndex >= 0) {
      list.setSelectedIndex(selectedIndex, true);
    }
    display.setCurrent(list);
  }

  public void requestTextInput(InputWidget source, String text, int constraints, boolean multiline) {
    if (textBox == null) {
      textBox = new TextBox("Input", "", MAX_INPUT_SIZE, javax.microedition.lcdui.TextField.ANY);
      textBox.addCommand(CMD_TEXT_OK);
      textBox.addCommand(CMD_CANCEL);
      textBox.setCommandListener(this);
    }

    currentInput = source;
    textBox.setString(text);
    textBox.setConstraints(constraints);
    display.setCurrent(textBox);
  }


  protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
  }


  protected void pauseApp() {
  }


  /**
   * Handles back and exit commands.
   */
  public void commandAction(Command command, Displayable displayable) {
    if (command == CMD_LIST_OK) {
      currentInput.setSelectedIndex(list.getSelectedIndex());
    } else if (command == CMD_TEXT_OK) {
      currentInput.setText(textBox.getString());
    } else if (command == List.SELECT_COMMAND) {
      switch(menu.getSelectedIndex()) {
        case MENU_START_PAGE:
          currentScreen = (HtmlScreen) screenStack.elementAt(0);
          screenStack.setSize(1);
          break;
        case MENU_EXIT:
          notifyDestroyed();
          break;
      }
    } 
    display.setCurrent(currentScreen);
  }

  public void menu() {
    if (menu == null) {
      menu = new List("Menu", List.IMPLICIT, MENU_OPTIONS, null);
      menu.addCommand(CMD_CANCEL);
      menu.setCommandListener(this);
    }
    display.setCurrent(menu);
  }

  public void back() {
    if (screenStack.size() > 1) {
      currentScreen = (HtmlScreen) screenStack.elementAt(screenStack.size() - 2);
      display.setCurrent(currentScreen);
      screenStack.removeElementAt(screenStack.size() - 1);}
  }

  /** 
   * Overwrite this in a subclass to support additional elements.
   */
  public Hashtable getElementHandlers() {
	return null;
  }
}
