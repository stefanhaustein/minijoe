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

import com.google.minijoe.sys.JsFunction;
import com.google.minijoe.sys.JsObject;

public class XMLHttpRequestConstructor extends JsFunction {

	private static final int XMLHTTPREQUEST_TYPE_ID = 1;

	private static final JsObject XMLHTTPREQUEST_CONSTRUCTOR_PROTOTYPE 
		= new JsObject(JsFunction.FUNCTION_PROTOTYPE);

	public XMLHttpRequestConstructor(HtmlJsFactory factory) {
		super(factory, XMLHTTPREQUEST_TYPE_ID,
				XMLHTTPREQUEST_CONSTRUCTOR_PROTOTYPE, 0, 0);
	}

}
