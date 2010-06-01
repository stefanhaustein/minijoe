package com.google.minijoe.html5.js;

import com.google.minijoe.html.js.XMLHttpRequestObject;
import com.google.minijoe.sys.JsObject;
import com.google.minijoe.sys.JsObjectFactory;

public class Html5JsFactory implements JsObjectFactory {
	
	private static Html5JsFactory factory;	
	
	//Html5 Js Native Object types
	public static final int HTML5_XHR_TYPE = 1;
	public static final int HTML5_CANVAS_TYPE = 2;
	
	private JsWindow environment;
	
	/**
	 * Initialises the factory singleton, or nothing if it already exists.
	 * 
	 * @param callbackContext  JsObject with context to use for callbacks by any 
	 * objects created by the factory
 	 * @param eventLock	 Object used as lock for callback to synchronise on
	 */
	public static Html5JsFactory getFactory(JsWindow env) {
		if (factory == null) {
			factory = new Html5JsFactory();
			factory.environment = env;;
		}
		return factory;
	}

	public JsObject newInstance(int type) {
		switch(type){
	      case HTML5_XHR_TYPE: 
	    	  return new XMLHttpRequestObject(environment, 
	    			  environment.getEventLock());
	    	  
	      case HTML5_CANVAS_TYPE: 
	    	  return new CanvasObject(environment, 
	    			  environment.getEventLock(),
	    			  environment.getRootDocument());
	    	  
	      default:
	        throw new IllegalArgumentException();
	    }
	}
}
