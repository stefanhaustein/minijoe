/**
 * 
 */
package com.google.minijoe.html5;

import com.google.minijoe.html.Element;
import com.google.minijoe.html.ElementHandler;
import com.google.minijoe.html5.js.JsWindow;

/**
 * @author Maksim Lin
 *
 */
public class CanvasElementHandler implements ElementHandler {
	
	public CanvasElementHandler() {
	}
	
	public Object handle(Element element, boolean inBody) {
		System.out.println("canvas handler made new canvas2d obj"+
				element);
		return new Canvas2D(element);
	}
	
}
