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

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

import com.google.minijoe.html.HtmlWidget;
import com.google.minijoe.html.uibase.ScrollWidget;
import com.google.minijoe.html.uibase.TextWidget;
import com.google.minijoe.html.uibase.Widget;
import com.google.minijoe.html.uibase.Window;

/**
 * Simple HTML screen widget. 
 * 
 * @author Stefan Haustein
 */
public class HtmlScreen extends Canvas implements Window {

  static final int TITLE_BG = 0x4444444; 
  static final int SOFTKEY_BG = 0x444444;
	
  ScrollWidget scrollWidget = new ScrollWidget();
  Widget root = new Widget();
  HtmlBrowser browser;
  HtmlWidget htmlWidget;
  TextWidget titleWidget;
  TextWidget lskWidget;
  TextWidget rskWidget;
  TextWidget statusWidget;
  String url;
  boolean layoutValid;
  Font ctrlFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL);

  
  public HtmlScreen(HtmlBrowser browser, String url) {
	root.setWindow(this);
    
    this.browser = browser;
    this.url = url;

    lskWidget = new TextWidget(ctrlFont);
    rskWidget = new TextWidget(ctrlFont);
    statusWidget = new TextWidget(ctrlFont);
    lskWidget.setBackgroundColor(SOFTKEY_BG);
    rskWidget.setBackgroundColor(SOFTKEY_BG);
    lskWidget.setTextColor(0xffffff);
    rskWidget.setTextColor(0xffffff);
    statusWidget.setBackgroundColor(0x0cccccc);
    statusWidget.setTextColor(0);
    statusWidget.setBorderColor(0x0888888);
    
    lskWidget.setText("Options");
    lskWidget.setAlign(Graphics.LEFT);
    
    if (browser.screenStack.size() != 0) {
    	rskWidget.setText("Back");
    	rskWidget.setAlign(Graphics.RIGHT);
    } else {
    	rskWidget.setText("");
    }
    
    root.addChild(lskWidget);
    root.addChild(rskWidget);
    
    titleWidget = new TextWidget(ctrlFont);
    titleWidget.setBackgroundColor(TITLE_BG);
    titleWidget.setTextColor(0xffffff);
    titleWidget.setText(url);

    htmlWidget = new HtmlWidget(browser, url, false);
    
    scrollWidget.addChild(titleWidget);
    scrollWidget.addChild(htmlWidget);
    
    root.addChild(scrollWidget);
    root.addChild(statusWidget);
    scrollWidget.requestFocus();

    setFullScreenMode(true);
    sizeChanged(getWidth(), getHeight());
  }

  public void setStatus(String status) {
	String title = htmlWidget.getTitle();
	if (title != null && title.trim().length() != 0) {
	  titleWidget.setText(htmlWidget.getTitle());	
	}
	titleWidget.setWidth(Math.max(getWidth(), htmlWidget.getWidth()));
	
	statusWidget.setText(status);
	if (status != null) {
	  statusWidget.setWidth(Math.min(getWidth() * 3 / 2, ctrlFont.stringWidth(status) + 4));
	}
	
  }
  
  public Widget getRoot() {
    return root;
  }
  
  protected void sizeChanged(int w,  int h) {
	int skh = ctrlFont.getHeight() + 4;
	lskWidget.setDimensions(0, h - skh, w / 2, skh);
	rskWidget.setDimensions(w / 2, h - skh, w - w / 2, skh);
	root.setDimensions(0, 0, w, h);
	scrollWidget.setDimensions(0, 0, w, h - skh);
	titleWidget.setWidth(Math.max(w, htmlWidget.getWidth()));
    statusWidget.setY(h - 2 * skh);
	invalidate(true);
  }
  
  protected void paint(Graphics g) {
	if (!layoutValid) {
      root.setDimensions(0, 0, getWidth(), getHeight());
	  root.doLayout(getWidth());
	  layoutValid = true;
	}
    root.drawTree(g, 0, 0, g.getClipX(), g.getClipY(), g.getClipWidth(), g.getClipHeight());
  }
  
  protected void keyPressed(int keyCode) {
	if (keyCode == Widget.KEYCODE_RSK) {
		browser.back();
	} else if (keyCode == Widget.KEYCODE_LSK) {
		browser.menu();
	} else {
		root.dispatchKeyEvent(Widget.KEY_PRESSED, keyCode, getGameAction(keyCode));
	}
  }
  
  protected void keyReleased(int keyCode) {
    root.dispatchKeyEvent(Widget.KEY_RELEASED, keyCode, getGameAction(keyCode));
  }
  
  protected void keyRepeated(int keyCode) {
    root.dispatchKeyEvent(Widget.KEY_REPEATED, keyCode, getGameAction(keyCode));
  }
  
  protected void pointerPressed(int x, int y) {
    root.dispatchPointerEvent(Widget.POINTER_PRESSED, x, y);
  }

  protected void pointerReleased(int x, int y) {
    root.dispatchPointerEvent(Widget.POINTER_RELEASED, x, y);
  }

  protected void pointerDragged(int x, int y) {
    root.dispatchPointerEvent(Widget.POINTER_DRAGGED, x, y);
  }

  public void invalidate(boolean layout) {
    if (layout) {
      layoutValid = false;
    }
    repaint();
  }
}
