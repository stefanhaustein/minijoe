// Copyright Manichord Pty Ltd.
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

/*
 * Implements as far as possible XMLHttpRequest as per:
 * http://www.w3.org/TR/XMLHttpRequest/
 */

package com.google.minijoe.html.js;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.io.Connector;
import javax.microedition.io.ContentConnection;
import javax.microedition.io.HttpConnection;

import com.google.minijoe.sys.JsArray;
import com.google.minijoe.sys.JsFunction;
import com.google.minijoe.sys.JsObject;

public class XMLHttpRequestObject extends JsObject implements Runnable {

	private static Object callbackEventLock;

	private static final JsObject XMLHTTPREQUEST_PROTOTYPE = new JsObject(
			JsFunction.OBJECT_PROTOTYPE);

	private static final Double READY_STATE_UNSENT = new Double(0);
	private static final Double READY_STATE_OPENED = new Double(1);
	private static final Double READY_STATE_HEADERS_RECEIVED = new Double(2);
	private static final Double READY_STATE_LOADING = new Double(3);
	private static final Double READY_STATE_DONE = new Double(4);

	private static final int XHR_OPEN_METHOD_ID = 1001;
	private static final int XHR_SEND_METHOD_ID = 1002;
	private static final int XHR_SET_REQUEST_HEADER_METHOD_ID = 1003;
	private static final int XHR_ABORT_METHOD_ID = 1004;

	private String url;
	private String httpMethod;
	private String username;
	private String password;
	private Hashtable headers = new Hashtable();
	private StringBuffer responseText = new StringBuffer();
	private byte[] responseBody;
	private String requestData;

	private JsObject callBackScope;

	public XMLHttpRequestObject(JsObject scope, Object lock) {

		super(XMLHTTPREQUEST_PROTOTYPE);
		this.callBackScope = scope;
		this.callbackEventLock = lock;

		addNative("open", XHR_OPEN_METHOD_ID, 5);
		addNative("send", XHR_SEND_METHOD_ID, 1);
		addNative("setRequestHeader", XHR_SET_REQUEST_HEADER_METHOD_ID, 2);

		addVar("status", null);
		addVar("readyState", READY_STATE_UNSENT);
		addVar("responseText", null);
		addVar("responseXML", null); // Note: currently not supported
		addVar("onReadyStateChange", null);
	}

	public void evalNative(int id, JsArray stack, int sp, int parCount) {
		switch (id) {
		case XHR_OPEN_METHOD_ID:
			this.httpMethod = stack.getString(sp + 2);
			this.url = stack.getString(sp + 3);
			// if we get a 3rd param setting sync, just ignore as we always
			// async to not block gui thread
			this.username = stack.getString(sp + 5);
			this.password = stack.getString(sp + 6);
			addVar("readyState", READY_STATE_OPENED);
			break;
		case XHR_SEND_METHOD_ID:
			this.requestData = stack.getString(sp + 2);
			Thread t = new Thread(this);
			t.start();
			break;
		case XHR_SET_REQUEST_HEADER_METHOD_ID:
			this.headers.put(stack.getString(sp + 2), stack.getString(sp + 3));
			break;
		case XHR_ABORT_METHOD_ID:
			// Todo:
			break;
		}
	}

	public void run() {
		readHttp();
	}

	private void readHttp() {
		HttpConnection c = null;
		DataInputStream dis = null;

		try {
			c = (HttpConnection) Connector.open(this.url);

			c.setRequestMethod(this.httpMethod);

			Enumeration keys = this.headers.keys();
			while (keys.hasMoreElements()) {
				Object key = keys.nextElement();
				c.setRequestProperty((String) key, (String) headers.get(key));
			}

			addVar("readyState", READY_STATE_LOADING);
			addVar("status", new Double(c.getResponseCode()));

			int len = (int) c.getLength();
			dis = c.openDataInputStream();
			if (len > 0) {
				this.responseBody = new byte[len];
				dis.readFully(this.responseBody);
				addVar("resBytes", this.responseBody);
				for (int i = 0; i < this.responseBody.length; i++) {
					// TODO: check MIME-type and handle charset correctly
					this.responseText.append((char) this.responseBody[i]);
				}
			} else {
				int ch;
				while ((ch = dis.read()) != -1) {
					// TODO: check MIME-type and handle charset correctly
					this.responseText.append(ch);
				}
			}
		} catch (IOException e) {
			// swallow exception as XHR open() method cannot throw error
			e.printStackTrace();
		} finally {
			if (dis != null)
				try {
					dis.close();
					if (c != null) {
						c.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		addVar("responseText", responseText.toString());
		addVar("readyState", READY_STATE_DONE);
		JsFunction onReadyStateChangeCallBack = (JsFunction) this
				.getObject("onReadyStateChange");
		if (onReadyStateChangeCallBack != null) {
			doCallBack(onReadyStateChangeCallBack);
		} else {
			// System.out.println("NO callback:" + onReadyStateChangeCallBack);
		}
	}

	private void doCallBack(JsFunction callBack) {
		JsObject event = new JsObject(JsObject.OBJECT_PROTOTYPE);

		synchronized (callbackEventLock) {
			JsArray callBackStack = new JsArray();
			callBackStack.setObject(0, this.callBackScope);
			callBackStack.setObject(1, callBack);
			callBackStack.setObject(2, event);

			callBack.eval(callBackStack, 0, 1);
		}
	}

	public String toString() {
		return "[XMLHttpRequest]";
	}
}