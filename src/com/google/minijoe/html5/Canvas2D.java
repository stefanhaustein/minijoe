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

package com.google.minijoe.html5;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import com.google.minijoe.html.BlockWidget;
import com.google.minijoe.html.Element;
import com.google.minijoe.html.uibase.Widget;
import com.google.minijoe.html5.js.CanvasObject;
import com.google.minijoe.html5.js.Html5JsFactory;
import com.google.minijoe.html5.js.JsWindow;
import com.google.minijoe.sys.JsFunction;

/**
 * Simple canvas implementation for the MiniJoe HTML5 browser.
 * 
 * @author Stefan Haustein 
 */
public class Canvas2D extends BlockWidget {

	Image canvas;
	JsWindow env;
	CanvasObject jsObject;

	Canvas2D(Element element) {
		super(element, false);
		this.env = element.getHtmlWidget().globalScope;
		//System.out.println("e data:"+element.getAttributeInt("width", 0));
		canvas = Image.createImage(
				element.getAttributeInt("width", 1),
				element.getAttributeInt("height", 1)); //default 1 because 0 will throw exception
		
		this.jsObject = (CanvasObject) Html5JsFactory.getFactory(env).newInstance(Html5JsFactory.HTML5_CANVAS_TYPE);
		this.jsObject.setCanvasBuffer(canvas);
	}
	
	public CanvasObject getCanvasObject(){
		return this.jsObject;
	}

	public void drawContent(Graphics g, int dx, int dy) {
		synchronized (env.getEventLock()) {
			g.drawImage(canvas, dx, dy, Graphics.TOP | Graphics.LEFT);
		}
	}
	
	public boolean handleKeyEvent(int type, int keyCode, int action) {
		System.out.println("handle key"+env.getObject("onkeyup"));
		if (type == Widget.KEY_PRESSED) {
			Object o = env.getObject("onkeydown");
			if (o instanceof JsFunction) {
				env.keyEvent((JsFunction) o, keyCode, action);
			}
			return true;
		}
		if (type == Widget.KEY_RELEASED) {
			Object o = env.getObject("onkeyup");
			if (o instanceof JsFunction) {
				env.keyEvent((JsFunction) o, keyCode, action);
			}
			return true;
		}
		return false;
	}
	
	public boolean isFocusable() {
		return true;
	}
	
	public String toString() {
		return "canvas widget";
	}
}
