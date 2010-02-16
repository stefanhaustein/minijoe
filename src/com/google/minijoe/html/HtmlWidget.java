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

package com.google.minijoe.html;

import com.google.minijoe.common.Util;
import com.google.minijoe.html.css.StyleSheet;
import com.google.minijoe.html.uibase.Widget;

import java.io.*;
import java.util.*;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Widget class representing a HTML document (or snippet). First builds up a
 * "logical" DOM consisting of Element objects for the (X)HTML code using KXML,
 * then constructs a physical EWL representation, consisting of BlockWidget,
 * InputWidget, TableWidget and TextFragmentWidget.
 * 
 * Support for input elements and CSS can be switched off using ConfigSettings
 * (HTML_DISABLE_INPUT and HTML_DISABLE_CSS) to reduce the memory footprint of
 * this component.
 * 
 * @author Stefan Haustein
 */
public class HtmlWidget extends BlockWidget  {

  /**
   * If set to false, the whole document is laid out again, even if only a 
   * partial layout seems necessary, for instance after an image has been 
   * loaded. Disable this flag for debugging only (too see if the optimization
   * is incorrect and causes an error).
   */
  public static final boolean OPTIMIZE = !DEBUG;


  /** 
   * Viewport width typically assumed by pages designed for 800 pixel displays 
   */
  public static final int MEDIUM_VIEWPORT_WIDTH = 760;

  /** 
   * IPhone viewport width. Typically assumed by pages designed for 1024 pixel 
   * displays.
   */
  public static final int HIGH_VIEWPORT_WIDTH = 960;

  /**
   * Interleaved map of character entity reference names to their resolved 
   * string values. 
   * 
   * TODO(haustein) improve this in KXML instead.
   */
  private static final String[] HTML_ENTITY_TABLE = {
    "auml", "ä", "ouml", "ö", "uuml", "ü", "szlig", "ß",
    "Auml", "Ä", "Ouml", "Ö", "Uuml", "Ü"
  };

  /** CSS style sheet for this document. */
  StyleSheet styleSheet = new StyleSheet(true);

  /** Maps URLs to resources (images, styles) */
  private Hashtable resources = new Hashtable();

  /** Maps URLs to information to the sources of a request. */
  private Hashtable pendingResourceRequests = new Hashtable();

  /** Request handler used by this widget to request resources. */
  protected SystemRequestHandler requestHandler;

  /** URL of the page represented by this widget. */
  private String documentUrl;

  /** Base URL for this document */
  String baseURL;

  /** Maps labels to widgets */
  Hashtable labels = new Hashtable();

  /** Map for access keys */
  Hashtable accesskeys = new Hashtable();

  /** The title of the document. Initialized to "---" to avoid null pointers. */
  String title = "---";

  /** 
   * We need to keep a reference to the focused element since we may need to rebuild
   * the widgets.
   */
  Element focusedElement;

  /** True if a new style sheet is arriving; stops style processing. */
  boolean styleOutdated = false;

  /** True if the widget tree needs to be rebuilt. */
  boolean needsBuild = true;

  /** Enable desktop rendering for CSS debugging purposes. */
  private boolean desktopRendering;

  /**
   * returns true if the media string is null or contains "all" or "screen"; 
   * "handheld" is accepted, too, if not in desktop rendering mode.
   */
  public boolean checkMediaType(String media) {
    if (media == null || media.trim().length() == 0) {
      return true;
    }
    media = media.toLowerCase();
    return media.indexOf("all") != -1 || media.indexOf("screen") != -1 || 
    (media.indexOf("handheld") != -1 && !desktopRendering);
  }

  /**
   * Creates a HTML document widget.
   * 
   * @param requestHandler the object used for requesting resources (embedded images, links)
   * @param documentUrl document URL, used as base URL for resolving relative links
   * @param destopRendering enables desktop rendering for debugging purposes
   */
  public HtmlWidget(SystemRequestHandler requestHandler, String documentUrl, 
      boolean desktopRendering) {
    super(null, false);
    this.requestHandler = requestHandler;
    this.desktopRendering = desktopRendering;

    if (documentUrl != null) {
      this.documentUrl = title = baseURL = documentUrl;
    }
  }

  public void setUrl(String url) {
    documentUrl = baseURL = url;
  }
  
  public void doLayout(int viewportWidth) {
    if (element == null) {
      return;
    }

    if (!layoutValid || viewportWidth != getWidth()) {
      layoutValid = false;
      
      if (desktopRendering) {
        int minWidth = getMinimumWidth(HIGH_VIEWPORT_WIDTH);
        if (minWidth > viewportWidth) {
          viewportWidth = minWidth > MEDIUM_VIEWPORT_WIDTH ? 
            HIGH_VIEWPORT_WIDTH : MEDIUM_VIEWPORT_WIDTH;
        } 
        setWidth(viewportWidth);
        doLayout(viewportWidth, viewportWidth, null, false);
      } else {
        int minW = getMinimumWidth(viewportWidth);
        int w = Math.max(minW, viewportWidth);
        setWidth(w);
        // TODO(haustein) We may need to make the viewport width available to calculations...
        doLayout(w, viewportWidth, null, false);
      }
    }
  }

  /**
   * Loads an HTML document from the given stream. 
   * 
   * @param is The stream to read the document from
   * @throws IOException Thrown if an IO exception occurs while reading from the
   *             stream.
   */
  public void load(InputStream is, String encoding) throws IOException {

    // Obtain the data, build the DOM
    Element htmlElement;

    try {
      Element dummy = new Element(this, "");
      KXmlParser parser = new KXmlParser();
      if (encoding != null && encoding.trim().length() == 0) {
        encoding = "UTF-8";
      }
      parser.setInput(is, encoding);
      parser.setFeature("http://xmlpull.org/v1/doc/features.html#relaxed", true);

      for (int i = 0; i < HTML_ENTITY_TABLE.length; i += 2) {
        parser.defineEntityReplacementText(HTML_ENTITY_TABLE[i], HTML_ENTITY_TABLE[i + 1]);
      }

      dummy.parseContent(parser);

      htmlElement = dummy.getElement("html");

      if (htmlElement == null) {
        htmlElement = new Element(this, "html");
      }
      
      element = htmlElement.getElement("body");
      if (element == null) {
        element = new Element(this, "body");
        htmlElement.addElement(element);

        for (int i = 0; i < dummy.getChildCount(); i++) {
          switch (dummy.getChildType(i)) {
            case Element.TEXT:
              element.addText(dummy.getText(i));
              break;
            case Element.ELEMENT:
              if (!dummy.getElement(i).getName().equals("html")) {
                element.addElement(dummy.getElement(i));
              }
              break;
          }
        }
      }
    } catch (XmlPullParserException e) {
      // this cannot happen since the pull parser must not throw exceptions in
      // relaxed mode
      e.printStackTrace();
      throw new IOException(e.toString());
    }

    // Remove reference to dummy element that was created to simplify parsing
    htmlElement.setParent(null);

    // Apply the default style sheet and style info collected while building
    // the element three.
    applyStyle();
    invalidate(true);
  }

  /**
   * Processes links by calling the request handler.
   */
  public Widget dispatchKeyEvent(int type, int keyCode, int action) {

    Widget handled = super.dispatchKeyEvent(type, keyCode, action);
    if (handled != null || type != KEY_PRESSED) {
      return handled;
    }

    Element element = null;

    if (action == Canvas.FIRE) {
      Widget w = getFocusedWidget();
      if (w instanceof TextFragmentWidget) {
        element = ((TextFragmentWidget) w).getElement();
      } else if (w instanceof BlockWidget) {
        element = ((BlockWidget) w).getElement();
      } 
    } else {
      element = (Element) accesskeys.get(new Character((char) keyCode));
    }

    if (element == null) {
      return null;
    }

    String href = element.getAttributeValue("href");
    while (href == null && element.getParent() != null) {
      element = element.getParent();
      href = element.getAttributeValue("href");
    }

    if (href == null) {
      return null;
    }

    if (href.startsWith("#")) {
      gotoLabel(href.substring(1));
    } else {
      requestHandler.requestResource(this, SystemRequestHandler.METHOD_GET,
          getAbsoluteUrl(href.trim()), SystemRequestHandler.TYPE_DOCUMENT, null);
    }
    return this;
  }

  /**
   * Scrolls to the element with the given label (&lt;a name...) and focuses it
   * (if focusable).
   */
  public void gotoLabel(String label) {
    Widget w = (Widget) labels.get(label);
    if (w == null) {
      return;
    }
    if (w.isFocusable()) {
      w.requestFocus();
    }
    w.scrollIntoView();
  }

  /**
   * Converts a URL to an absolute URL, using the document base URL. If the URL
   * is already absolute, it is returned unchanged.
   */
  public String getAbsoluteUrl(String relativeUrl) {
    return Util.getAbsoluteUrl(baseURL, relativeUrl);
  }

  /**
   * Returns the URL of this document, as set in the constructor.
   */
  public String getUrl() {
    return documentUrl;
  }

  /**
   * Applies the style sheet to the document. 
   */
  void applyStyle() {
    if (element == null) {
      return;
    }
    synchronized (styleSheet) {
      styleOutdated = false;
      element.apply(styleSheet, new Vector());
    }
    if (!OPTIMIZE || needsBuild) {
      synchronized (this) {
        needsBuild = false;
        children = new Vector();
        addChildren(element, new boolean[]{false, true});
        invalidate(true);
      }
    }
  }

  /**
   * Implementations of RequestHandler add resources via this method. Updates
   * requesters with the arrived resource.
   * 
   * @param url URL of the received resource
   * @param resource the received resource object
   */
  public void addResource(String resUrl, Object resource) {

    if (resource instanceof String) {
      styleOutdated = OPTIMIZE;
      synchronized (styleSheet) {
        styleSheet.read(this, resUrl, (String) resource);
      }
      applyStyle();
    } else if (resource instanceof Image) {
      Vector targets = (Vector) pendingResourceRequests.get(resUrl);
      if (targets != null) {
        Image image = (Image) resource;
        pendingResourceRequests.remove(resUrl);

        for (int i = 0; i < targets.size(); i++) {
          Object t = targets.elementAt(i);
          if (t instanceof BlockWidget) {
            BlockWidget block = (BlockWidget) t;
            block.image = image;
            block.invalidate(true);
          } else if (t instanceof Image[]) {
            ((Image[]) t)[0] = image;
            invalidate(true);
          }
        }
      }
    }
    resources.put(resUrl, resource);
  }

  /**
   * Returns the loaded resource for the given URL, if available. If not 
   * available, and the notify object is not null, a request for the resource
   * will be sent to the server. If the notify object is recognized by the
   * HtmlWidget (e.g. a BlockWidget), it is updated when the resource arrives.
   * Otherwise, the notify object is just treated as a flag to trigger the
   * request.
   * 
   * @param url the URL of the resource; if relative, the document base URL 
   *            will be used to determine the absolute URL
   * @param type the type of expected resource (
   * @param notify the object to be notified when the resource arrives. 
   */
  public Object getResource(String url, int type, Object notify) {
    url = Util.getAbsoluteUrl(baseURL, url);

    Object res = resources.get(url);
    if (res == null && notify != null) {
      Vector dependencies = (Vector) pendingResourceRequests.get(url);
      if (dependencies == null) {
        dependencies = new Vector();
        pendingResourceRequests.put(url, dependencies);
      }
      dependencies.addElement(notify);
      if (dependencies.size() == 1) {
        requestHandler.requestResource(this, SystemRequestHandler.METHOD_GET, url,
            type, null);
      }
    }
    return res;
  }

  /**
   * In addition to drawing the contents by calling super, this method
   * makes sure the layout and viewport width are updated if not valid.
   */
  public void drawTree(Graphics g, int dx, int dy, int cx, int cy, int cw, int ch) {
	// Make sure the backgound covers the whole screen.
    if (boxY + boxHeight < getHeight()) {
      boxHeight = getHeight() - boxY;
    }
    if (element != null && element.getComputedStyle() != null) {
      super.drawTree(g, dx, dy, cx, cy, cw, ch);
    }
  }  

  /**
   * Returns the title of this document.
   */
  public String getTitle() {
    return title;
  }

  /**
   * Returns the base URL of this document (used to resolve relative links).
   */
  public String getBaseURL() {
    return baseURL;
  }
}
