package com.google.minijoe.html.js;

import com.google.minijoe.html5.js.JsWindow;
import com.google.minijoe.sys.JsObject;
import com.google.minijoe.sys.JsObjectFactory;

public class HtmlJsFactory implements JsObjectFactory {
	
	private static HtmlJsFactory factory;
	
	
	//Html Js Native Object types
	public static final int HTML_IMAGE_TYPE = 1;
	
	private JsWindow environment;
	
	/**
	 * Initialises the factory singleton, or nothing if it already exists.
	 * 
	 * @param callbackContext  JsObject with context to use for callbacks by 
	 * any objects created by the factory
 	 * @param eventLock	 Object used as lock for callback to synchronise on
	 */
	public static HtmlJsFactory getFactory(JsWindow env) {
		if (factory == null) {
			factory = new HtmlJsFactory();
			factory.environment = env;;
		}
		return factory;
	}

	public JsObject newInstance(int type) {
		switch(type){
	      case HTML_IMAGE_TYPE:
	    	  //return new ImageObject(environment.getCallbackScope(), 
	    		//	  this);
	      default:
	        throw new IllegalArgumentException();
	    }
	}

}
