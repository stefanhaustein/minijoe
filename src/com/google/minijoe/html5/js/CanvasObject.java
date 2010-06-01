package com.google.minijoe.html5.js;

import javax.microedition.lcdui.Image;

import com.google.minijoe.html.BlockWidget;
import com.google.minijoe.html.Element;
import com.google.minijoe.html.HtmlWidget;
import com.google.minijoe.sys.JsArray;
import com.google.minijoe.sys.JsFunction;
import com.google.minijoe.sys.JsObject;

public class CanvasObject extends JsObject {

	private static final JsObject CANVAS_PROTOTYPE = new JsObject(
			JsFunction.OBJECT_PROTOTYPE);

	private JsObject callBackScope;

	private Element canvasElement;
	private BlockWidget canvasWidget;

	private HtmlWidget root;

	private Image canvasBuffer;

	private static Object callbackEventLock;

	public static final int ID_GET_CONTEXT = 3001;

	public CanvasObject(JsObject scope, Object lock, HtmlWidget root) {
		super(CANVAS_PROTOTYPE);

		this.root = root;
		this.callBackScope = scope;
		this.callbackEventLock = lock;
				
		addVar("getContext", new JsFunction(ID_GET_CONTEXT, 1));
	}
	
	public void setCanvasElement(Element canvasElement) {
		this.canvasElement = canvasElement;
	}
	
	public void setCanvasBuffer(Image canvasBuffer) {
		this.canvasBuffer = canvasBuffer;
	}

	public void evalNative(int index, JsArray stack, int sp, int parCount) {

		switch (index) {
		case ID_GET_CONTEXT:
			System.out.println("get context called "+this.canvasBuffer);
			//FIXME: check if first param == "2d" before returning 2d context
	        
			stack.setObject(sp, new Context2D(canvasBuffer));
	        break;    
		default:
			super.evalNative(index, stack, sp, parCount);
		}
	}

	public String toString() {
		return "[Canvas]";
	}
}
