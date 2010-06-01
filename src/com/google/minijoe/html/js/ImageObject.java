package com.google.minijoe.html.js;

import com.google.minijoe.html.BlockWidget;
import com.google.minijoe.html.Element;
import com.google.minijoe.html.HtmlWidget;
import com.google.minijoe.sys.JsArray;
import com.google.minijoe.sys.JsFunction;
import com.google.minijoe.sys.JsObject;

public class ImageObject extends JsObject {

	private static final JsObject IMAGE_PROTOTYPE = new JsObject(
			JsFunction.OBJECT_PROTOTYPE);

	private JsObject callBackScope;

	private Element imgElement;
	private BlockWidget imgWidget;

	private HtmlWidget root;

	private static Object callbackEventLock;

	public static final int ID_INIT_IMAGE = 2001;

	public ImageObject(JsObject scope, Object lock, HtmlWidget root) {
		super(IMAGE_PROTOTYPE);

		this.root = root;
		this.callBackScope = scope;
		this.callbackEventLock = lock;

		addVar("onLoad", null);
		addVar("src", null);
	}

	public void evalNative(int index, JsArray stack, int sp, int parCount) {

		switch (index) {
		case ID_INIT_IMAGE:
			this.imgElement = new Element(root, "img");
			break;
		default:
			super.evalNative(index, stack, sp, parCount);
		}
	}
	
	
		public String toString() {
		return "[Image]";
	}

	public void setObject(String key, Object v) {
		super.setObject(key, v);
		if ("src".equals(key) && (v instanceof String)) {
			this.imgElement.setAttribute("src", (String) v);

			// TODO: need to make this the onLoad callback once Blockwidget
			// has support for onload callbacks add to it!
			this.imgWidget = new BlockWidget(this.imgElement, new boolean[] {
					false, true });
		}
	}

	//Callback from blockwidget notifying us that its finsihed loading the img data
	private void onLoadCallBack(JsFunction callBack) {
		System.out.println("finished loading img");
		addVar("width", new Integer(this.imgWidget.getWidth()));
		addVar("height", new Integer(this.imgWidget.getHeight()));
		
		JsFunction onLoadCallBack = (JsFunction) this.getObject("onLoad");
		if (onLoadCallBack != null) {
			synchronized (callbackEventLock) {
				JsArray callBackStack = new JsArray();
				callBackStack.setObject(0, this.callBackScope);
				callBackStack.setObject(1, callBack);

				callBack.eval(callBackStack, 0, 1);
			}
		}
	}
}
