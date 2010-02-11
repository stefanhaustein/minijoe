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

package com.google.minijoe.html;

import java.util.Vector;

/**
 * Interface that is used by HtmlWidget to ask for embedded resources (images) or links. 
 * Also used to request text input or a popup.
 * 
 * @author Stefan Haustein
 */
public interface SystemRequestHandler {

  /** 
   * Request method constant for HTTP GET requests.
   */
  static final int METHOD_GET = 1;

  /**
   * Request method  constant for HTTP POST requsts.
   */

  static final int METHOD_POST = 2;
  
  /** 
   * Request for a full new document, e.g clicking on a link. 
   * Application may open the platform browser or open a larger 
   * window with the built in browser.
   */
  static final int TYPE_DOCUMENT = 1;
  
  /** 
   * Request for an embedded image. The source document 
   * expects to receive the requested image as a GoogleImage 
   * object via the addResource method. 
   */
  static final int TYPE_IMAGE = 2;
  
  /**
   * Request for a CSS style sheet.
   */
  static final int TYPE_STYLESHEET = 3;
  
  /**
   * Callback method called by the HTML document to request a resource.
   * 
   * @param source the HTML document requesting the resource
   * @param requestMethod one of the METHOD_* constants
   * @param url the URL of the requested resource
   * @param expectedContentType expected content type. Must be one of the TYPE_* constants
   * @param data request data -- will be sent for HTTP POST requests
   */
  void requestResource(HtmlWidget source, int requestMethod, String url,
      int expectedContentType, byte[] data);
  
  /**
   * Callback method called by the HTML widget to rquest a popup for a select
   * element (unfortunately, this is needed since the HTML widget is not able
   * to get control over the soft keys).
   * 
   * @param source the select element that was clicked
   * @param options list of selectable options
   * @param selectedIndex index of the currently selected option
   */
  void requestPopup(InputWidget source, Vector options, int selectedIndex);
  
  void requestTextInput(InputWidget source, String text, int constraints, boolean multiline);
}
