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

import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDletStateChangeException;

import com.google.minijoe.html.SystemRequestHandler;
import com.google.minijoe.html5.CanvasElementHandler;
import com.google.minijoe.html5.js.JsWindow;

/**
 * Example application illustrating HTML5 support. 
 * Currently supported parts of
 * HTML5: - canvas element
 * 
 * coming soon: localStorage, video/audio element, geolocation 
 * 
 * @author Maksim Lin
 */
public class Html5Browser extends HtmlBrowser implements SystemRequestHandler,
		CommandListener, Runnable {

	private Hashtable elementHandlers = new Hashtable();
	private JsWindow jsEnv;

	public Html5Browser() {

	}

	protected void startApp() throws MIDletStateChangeException {

		display = Display.getDisplay(this);

		userAgent = "MiniJoe/0.5 (like Opera Mini/2.0)" + " Platform/"
				+ getAppProperty("microedition.platform") + " Configuration/"
				+ getAppProperty("microedition.configuration") + " Profile/"
				+ getAppProperty("microEdition.profile");

		StringBuffer menu = new StringBuffer(
				"<html><head><title>Bookmarks</title></head><body><ul>");

		addLink(menu, 
				"http://localhost/canvasoids.html");
		addLink(menu, 
			"http://localhost/functionplot.html");
		addLink(menu, 
			"http://manichord.com/test/canvasoids.html");
		
		addLink(menu, 
				"http://minijoe.googlecode.com/svn/trunk/javascript/");
		addLink(menu,
				"http://minijoe.googlecode.com/svn/trunk/javascript/canvasoids.html");
		addLink(menu,
				"http://hsivonen.iki.fi/test/xhtml-suite/xhtml-basic.xhtml");
		addLink(menu, "http://www.google.com/m");
		addLink(menu, "http://mobile.google.com");
		addLink(menu, "http://wwf.mobi/");
		addLink(menu,
				"http://www.bigbaer.com/css_tutorials/css.float.html.tutorial.htm");
		addLink(menu, "http://en.m.wikipedia.org");
		addLink(menu, "http://moblogga.mobi/");
		addLink(menu, "http://qeep.mobi/xmps/homepage.do");
		addLink(menu, "http://wap.cellufun.com/games.asp?g=mob&f=admobmw");
		addLink(menu, "http://freesim.o2.co.uk/R4QAYdLT");

		try {
			
			HtmlScreen scr = showPage(null);
			
			elementHandlers.put("canvas", new CanvasElementHandler());

			scr.htmlWidget.load(new ByteArrayInputStream(menu.toString()
					.getBytes("UTF-8")), "UTF-8");
			scr.setStatus(null);
			scr.repaint();

		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("" + e);
		}
	}

	public void addLink(StringBuffer buf, String href) {
		buf.append("<li><a href='").append(href).append("'>").append(href)
				.append("</a></li>");
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
						requester = (ResourceRequester) requestQueue
								.elementAt(0);
						requestQueue.removeElementAt(0);
					}
					requester.run();
				}
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		}		
		finally {
			threadCount--;
		}
	}

	/**
	 * Overwrite this in a subclass to support additional elements.
	 */
	public Hashtable getElementHandlers() {
		return elementHandlers;
	}
}
