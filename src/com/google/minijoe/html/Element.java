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

import com.google.minijoe.html.css.Style;
import com.google.minijoe.html.css.StyleSheet;

import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Stylable XML/HTML Element representation. Also used to represent an anonymous
 * parent element of the document root while parsing, in order to simplify the
 * process and to avoid an additional document class. 
 * 
 * @author Stefan Haustein
 */
public class Element {

  /**
   * Type constant for child elements.
   */
  public static final int ELEMENT = 1;

  /**
   * Type constant for text content.
   */
  public static final int TEXT = 2;

  private static final int FLAG_BLOCK = 1;
  private static final int FLAG_EMPTY = 2;
  private static final int FLAG_LIST_ITEM = 4;
  private static final int FLAG_TABLE_CELL = 8;
  private static final int FLAG_TABLE_ROW = 16;
  private static final int FLAG_PARAGRAPH = 32;
  private static final int FLAG_IGNORE_CONTENT = 64;
  private static final int FLAG_ADD_COMMENTS = 128;

  private static final int TAG_SCRIPT = 0x0010000;
  private static final int TAG_STYLE = 0x0020000;
  private static final int TAG_META = 0x0030000;
  private static final int TAG_LINK = 0x0040000;
  private static final int TAG_TITLE = 0x0050000;
  private static final int TAG_BASE = 0x0060000;

  /**
   * Hashtable mapping tag names to flags and tag name constants.
   */
  private static Hashtable flagMap = new Hashtable();

  static {
    setFlags("area", FLAG_EMPTY);
    setFlags("base", FLAG_EMPTY | TAG_BASE);
    setFlags("basefont", FLAG_EMPTY);
    setFlags("br", FLAG_EMPTY);
    setFlags("body", FLAG_BLOCK);
    setFlags("center", FLAG_BLOCK|FLAG_PARAGRAPH);
    setFlags("col", FLAG_EMPTY);
    setFlags("dd", FLAG_BLOCK | FLAG_LIST_ITEM);
    setFlags("dir", FLAG_BLOCK);
    setFlags("div", FLAG_BLOCK);
    setFlags("dl", FLAG_BLOCK);
    setFlags("dt", FLAG_BLOCK | FLAG_LIST_ITEM);
    setFlags("frame", FLAG_EMPTY);
    setFlags("h1", FLAG_BLOCK | FLAG_PARAGRAPH);
    setFlags("h2", FLAG_BLOCK | FLAG_PARAGRAPH);
    setFlags("h3", FLAG_BLOCK | FLAG_PARAGRAPH);
    setFlags("h4", FLAG_BLOCK | FLAG_PARAGRAPH);
    setFlags("h5", FLAG_BLOCK | FLAG_PARAGRAPH);
    setFlags("h6", FLAG_BLOCK | FLAG_PARAGRAPH);
    setFlags("hr", FLAG_BLOCK | FLAG_EMPTY);
    setFlags("img", FLAG_EMPTY);
    setFlags("input", FLAG_EMPTY);
    setFlags("isindex", FLAG_EMPTY);
    setFlags("li", FLAG_BLOCK | FLAG_LIST_ITEM);
    setFlags("link", FLAG_EMPTY | TAG_LINK);
    setFlags("marquee", FLAG_BLOCK);
    setFlags("menu", FLAG_BLOCK);
    setFlags("meta", FLAG_EMPTY | TAG_META);
    setFlags("ol", FLAG_BLOCK);
    setFlags("option", FLAG_BLOCK);
    setFlags("p", FLAG_BLOCK  | FLAG_PARAGRAPH);
    setFlags("param", FLAG_EMPTY);
    setFlags("pre", FLAG_BLOCK);
    setFlags("script", FLAG_IGNORE_CONTENT | TAG_SCRIPT);
    setFlags("style", FLAG_ADD_COMMENTS | TAG_STYLE);
    setFlags("table", FLAG_BLOCK);
    setFlags("title", TAG_TITLE);
    setFlags("td", FLAG_BLOCK | FLAG_TABLE_CELL);
    setFlags("th", FLAG_BLOCK | FLAG_TABLE_CELL);
    setFlags("tr", FLAG_BLOCK | FLAG_TABLE_ROW);
    setFlags("ul", FLAG_BLOCK);
    setFlags("xmp", FLAG_BLOCK);
  }

  private static void setFlags(String name, int i) {
    flagMap.put(name, new Integer(i));
  }

  HtmlWidget htmlWidget;
  private Element parent;
  private String name;
  private Hashtable attributes;
  private Vector content;  
  private Style computedStyle;

  /**
   * Create a new element with the given name. 
   * 
   * @param htmlWidget the htmlWidget this element belongs to
   * @param name the name of this element
   */
  public Element(HtmlWidget htmlWidget, String name) {
    this.htmlWidget = htmlWidget;
    this.name = name;
  }

  /**
   * Add the given child element at the end of this element's content.
   * 
   * @param element child element to be added.
   */
  public void addElement(Element element) {
    if (content == null) {
      content = new Vector();
    }
    content.addElement(element);
    element.parent = this;
  }

  /**
   * Add the given text at the end of this element's content.
   * 
   * @param text the text to add.
   */
  public void addText(String text) {
    if (content == null) {
      content = new Vector();
    }
    content.addElement(text);
  }

  /**
   * Set the given attribute to the given value.
   * 
   * @param attrName attribute name
   * @param value attribute value
   */
  public void setAttribute(String attrName, String value) {
    if (attributes == null) {
      attributes = new Hashtable();
    }
    if (value == null) {
      attributes.remove(attrName);
    } else {
      attributes.put(attrName, value);
    }
  }

  /**
   * Returns the type (ELEMENT or TEXT) of the child node with the given index.
   * 
   * @param index node index
   * @return ELEMENT or TEXT
   */
  public int getChildType(int index) {
    return content.elementAt(index) instanceof Element ? ELEMENT : TEXT;
  }

  /**
   * Returns the number of child nodes.
   * 
   * @return number of child nodes.
   */
  public int getChildCount() {
    return content == null ? 0 : content.size();
  }

  /**
   * Returns the child element with the given index. Note that the child node at
   * the given index needs to be an element, otherwise a class cast exception
   * will be thrown. Use getChildType() to ensure the correct type.
   */
  public Element getElement(int index) {
    return (Element) content.elementAt(index);
  }

  /**
   * Returns the first child element with the given name, or null if there is no
   * such element.
   */
  public Element getElement(String childName) {
    if (content != null) {
      for (int i = 0; i < getChildCount(); i++) {
        if (getChildType(i) == ELEMENT
            && getElement(i).getName().equals(childName)) {
          return getElement(i);
        }
      }
    }
    return null;
  }

  /**
   * Returns the name of this element.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the text node with the given index. Use getChildType() to ensure
   * the correct child node type. In the case of a mismatch, a class cast
   * exception will be thrown.
   */
  public String getText(int index) {
    return (String) content.elementAt(index);
  }

  /**
   * Returns the attribute value interpreted as a boolean.
   * If the attribute is not set or the value is "false"
   * or "0", false is returned. Otherwise, true is returned.
   */
  public boolean getAttributeBoolean(String attrName) {
    String v = getAttributeValue(attrName);
    if (v == null) {
      return false;
    }
    v = v.trim().toLowerCase();
    return !"false".equals(v) && !"0".equals(v);
  }

  /**
   * Returns the value of the given attribute.
   * 
   * @param attrName name of the attribute
   * @return attribute value or null
   */
  public String getAttributeValue(String attrName) {
    return attributes == null ? null : (String) attributes.get(attrName);
  }

  /**
   * Returns the integer value of the given attribute. If there is no such
   * attribute, or the value is not parseable to an integer, 0 is returned.
   * 
   * @param attrName name of the attribute
   * @return attribute integer value or 0 if not available
   */  
  public int getAttributeInt(String attrName, int dflt) {
    String v = getAttributeValue(attrName);
    if (v == null) {
      return dflt;
    }
    try {
      return Integer.parseInt(v);
    }
    catch (NumberFormatException e) {
      return dflt;
    }
  }

  /**
   * Returns <a href="http://www.w3.org/TR/CSS21/cascade.html#computed-value">
   * computed css values</a>, taking the cascade and inheritance into 
   * account, but not actual layout or rendering.  If a style was not yet set 
   * (i.e. SyleSheet.apply() was not called), null is returned.
   */
  public Style getComputedStyle() {
    return computedStyle;
  }

  /**
   * Used by StryleSheet.apply() to set the computed style for this element.
   */
  public void setComputedStyle(Style style) {
    this.computedStyle = style;
  }

  /**
   * Sets the focused flag for this element.
   */
  public void setFocused() {
    htmlWidget.focusedElement = this;
  }

  /**
   * Returns the distance to an ancestor with the given name. If there is no
   * such ancestor, -1 is returned.
   */
  public int getAncestorDistance(String aName) {
    Element p = parent;
    int dist = 1;
    while (p != null) {
      if (p.getName().equals(aName)) {
        return dist;
      }
      dist++;
      p = p.parent;
    }
    return -1;
  }

  /**
   * Returns true if this element is currently focused. Used by widgets to make 
   * sure the focus highlight spans all elements involved, not just the primary 
   * widget that is actually traversed. 
   */
  public boolean isFocused() {
    return htmlWidget.focusedElement == this;
  }

  /**
   * Returns true if the element is focusable. Currently only 
   * links are focusable. 
   */
  public boolean isFocusable() {
    return getLink(false) != null;
  }

  /**
   * Returns the link address if this element constitutes a
   * hyperlink or is contained in an element constituting a hyperlink.
   * Otherwise, null is returned.
   */
  public String getLink(boolean includeParents) {
    String href = getAttributeValue("href");
    return href != null 
    ? href 
        : (includeParents && parent != null ? parent.getLink(true) : null);
  }

  /**
   * Returns the parent element, or null if this is the root element.
   */
  public Element getParent() {
    return parent;
  }

  /** 
   * Returns all text content in a single string.
   */
  public String getText() {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < getChildCount(); i++) {
      if (getChildType(i) == TEXT) {
        sb.append(getText(i));
      } else if (getChildType(i) == ELEMENT) {
        sb.append(getElement(i).getText());
      }

    }
    return sb.toString();
  }

  /**
   * Parses the contents of an element (the part between the start and the end
   * tag, not including the attributes).
   * 
   * @param parser the parser
   * @throws XmlPullParserException should never be thrown in relaxed mode
   * @throws IOException for underlying IO exceptions
   */
  void parseContent(KXmlParser parser) 
  throws XmlPullParserException, IOException {

    int autoClose = 0;
    int flags = 0;
    Integer flagObject = (Integer) flagMap.get(name);
    if (flagObject != null) {
      flags = flagObject.intValue();
      if ((flags & FLAG_TABLE_ROW) != 0) {
        autoClose = FLAG_TABLE_ROW;
      } else if ((flags & FLAG_LIST_ITEM) != 0) {
        autoClose = FLAG_LIST_ITEM;
      } else if ((flags & FLAG_TABLE_CELL) != 0) {
        autoClose = FLAG_TABLE_CELL | FLAG_TABLE_ROW;
      } else if ((flags & FLAG_PARAGRAPH) != 0) {
        autoClose = FLAG_BLOCK;
      }
    }

    int position = parser.getLineNumber();

    if ((flags & FLAG_EMPTY) == 0) {
      loop : while (true) {
        switch (parser.getEventType()) {
          case KXmlParser.START_TAG:
            String childName = parser.getName().toLowerCase();
            Element child;
            Integer childFlagObject = (Integer) flagMap.get(childName);
            int childFlags = childFlagObject == null ? 0 : childFlagObject.intValue();

            if ((autoClose & childFlags) != 0) {
              // closing <name> implied by <childName>
              break loop;
            } else {
              if ((childFlags & FLAG_TABLE_CELL) != 0 && (flags & FLAG_TABLE_ROW) == 0) {
                child = new Element(htmlWidget, "tr");
                addElement(child);
              } else {
                child = new Element(htmlWidget, childName);
                addElement(child);
                for (int i = 0; i < parser.getAttributeCount(); i++) {
                  child.setAttribute(parser.getAttributeName(i).toLowerCase(),
                      parser.getAttributeValue(i));
                }
                parser.nextToken();
              }
              child.parseContent(parser);
            }
            break;

          case KXmlParser.CDSECT:
          case KXmlParser.ENTITY_REF:
          case KXmlParser.TEXT:
            if ((flags & FLAG_IGNORE_CONTENT) == 0) {
              addText(parser.getText());
            }
            parser.nextToken();
            break;

          case KXmlParser.END_TAG:
            String endName = parser.getName().toLowerCase();
            if (endName.equals(name)) {
              // direct match -> advance and leave loop         
              parser.nextToken();
              break loop;
            } else {
              Integer endFlags = (Integer) flagMap.get(endName);
              if (endFlags == null || (endFlags.intValue() & FLAG_EMPTY) == 0) {
                int delta = getAncestorDistance(endName);
                if (delta <= 2 && delta != -1) {
                  // parent match -> don't advance, but leave loop
                  break loop;
                }
              }
            }
            // ignore unmatched end tags
            parser.nextToken();
            break;

          case KXmlParser.END_DOCUMENT:
            break loop;

          case KXmlParser.COMMENT:
            // add comments as text for script and style elements, otherwise
            if ((flags & FLAG_ADD_COMMENTS) != 0) {
              addText(parser.getText());
            }
            parser.nextToken();
            break;

          default:
            // ignore other content (DTD, comments, PIs etc.)
            parser.nextToken();
        }
      }
    }

    // perform action on element 
    switch (flags & 0xffff0000) {
      case TAG_STYLE:
        if (htmlWidget.checkMediaType(getAttributeValue("media"))) {
          htmlWidget.styleSheet.read(htmlWidget, 
              htmlWidget.getBaseURL() + "#" + position, getText());
        }
        break;
      case TAG_TITLE:
        htmlWidget.title = getText();
        break;
      case TAG_LINK:
        String rel = getAttributeValue("rel");
        if (!"stylesheet alternate".equals(rel) && 
            ("stylesheet".equals(rel) || 
                "text/css".equals(getAttributeValue("type")))
                && htmlWidget.checkMediaType(getAttributeValue("media"))) {
          htmlWidget.requestHandler.requestResource(htmlWidget, SystemRequestHandler.METHOD_GET,
              htmlWidget.getAbsoluteUrl(getAttributeValue("href")) + "#" + position, 
              SystemRequestHandler.TYPE_STYLESHEET, null);        
        } 
        break;
      case TAG_BASE:
        String href = getAttributeValue("href");
        if (href != null) {
          htmlWidget.baseURL = href;
        }
    }
  }

  /**
   * Applies the given style sheet to this element and recursively to all child
   * elements, setting the computedStyle field to the computed CSS values.
   * <p>
   * Technically, it builds a queue of applicable styles and then applies them 
   * in the order of ascending specificity. After the style sheet has been 
   * applied, the inheritance rules and finally the style attribute are taken 
   * into account.
   * <p>
   * Thread safety: This method first builds up the new style and then 
   * assigns computed style. Other members are not changed. The element
   * tree and the style sheet should not be modified while executing
   * this method. 
   * 
   * @param styleSheet the style sheet to apply.
   * @param queue style queue used internally, must be set to new Vector()
   */
  public void apply(Vector applyHere, Vector applyAnywhere) {

    if (htmlWidget.styleOutdated) {
      return;
    }

    Style previousStyle = computedStyle;
    Style style = new Style();

    Vector queue = new Vector();
    Vector childStyles = new Vector();
    Vector descendantStyles = new Vector();

    int size = applyHere.size();
    for (int i = 0; i < size; i++) {
      StyleSheet styleSheet = (StyleSheet) applyHere.elementAt(i);
      styleSheet.collectStyles(this, queue, childStyles, descendantStyles);
    }
    size = applyAnywhere.size();
    for (int i = 0; i < size; i++) {
      StyleSheet styleSheet = (StyleSheet) applyAnywhere.elementAt(i);
      descendantStyles.addElement(styleSheet);
      styleSheet.collectStyles(this, queue, childStyles, descendantStyles);
    }

    for (int i = 0; i < queue.size(); i++) {
      style.set(((Style) queue.elementAt(i)));
    }

    String styleAttr = getAttributeValue("style");
    if (styleAttr != null) {
      style.read(htmlWidget, styleAttr);
    }

    if (parent != null) {
      style.inherit(parent.computedStyle);
    }

    // handle legacy stuff 

    applyHtmlAttributes(style);
    computedStyle = style;

    // recurse....

    for (int i = 0; i < getChildCount(); i++) {
      if (getChildType(i) == Element.ELEMENT) {
        getElement(i).apply(childStyles, descendantStyles);
      }
    }

    if (!htmlWidget.needsBuild) {
      htmlWidget.needsBuild = previousStyle == null || 
      previousStyle.getEnum(Style.DISPLAY) != 
        computedStyle.getEnum(Style.DISPLAY) || 
        previousStyle.isBlock(false) != computedStyle.isBlock(false);
    }
  }

  /**
   * Apply HTML attributes that influence the style (align, color, valign).
   */
  private void applyHtmlAttributes(Style style) {

    String s = getAttributeValue("align");
    if (s == null) {
      s = getAttributeValue("halign");
    }
    if (s != null) {
      s = s.toLowerCase().trim();
      if ("left".equals(s)) {
        style.set(Style.TEXT_ALIGN, Style.LEFT, Style.ENUM);
      } else if ("right".equals(s)) {
        style.set(Style.TEXT_ALIGN, Style.RIGHT, Style.ENUM);
      } else if ("center".equals(s)) {
        style.set(Style.TEXT_ALIGN, Style.CENTER, Style.ENUM);
      }
    }
    s = getAttributeValue("width");
    if (s != null) {
      try {
        if (s.endsWith("%")) {
          style.set(Style.WIDTH, 1000 * Integer.parseInt(
              s.substring(0, s.length() - 1)), Style.PERCENT);
        } else {
          if (s.endsWith("px")) {
            s = s.substring(0, s.length() - 2);
          }
          style.set(Style.WIDTH, 1000 * Integer.parseInt(s), Style.PX);
        }
      } catch (NumberFormatException e) {
        // do nothing for unparseable width attributes
      }
    }
    s = getAttributeValue("height");
    if (s != null) {
      try {
        if (s.endsWith("%")) {
          style.set(Style.HEIGHT, 1000 * Integer.parseInt(
              s.substring(0, s.length() - 1)), Style.PERCENT);
        } else {
          if (s.endsWith("px")) {
            s = s.substring(0, s.length() - 2);
          }
          style.set(Style.HEIGHT, 1000 * Integer.parseInt(s), Style.PX);
        }
      } catch (NumberFormatException e) {
        // do nothing for unparseable height attributes
      }
    }
    s = getAttributeValue("bgcolor");
    if (s != null) {
      style.setColor(Style.BACKGROUND_COLOR, s, 0);
    }

    boolean table = "table".equals(name);
    if (table || "td".equals(name) || "th".equals(name)) {
      s = getTableAttributeValue("valign");
      if (s != null) {
        s = s.toLowerCase().trim();
        if ("top".equals(s)) {
          style.set(Style.VERTICAL_ALIGN, Style.TOP, Style.ENUM);
        } else if ("bottom".equals(s)) {
          style.set(Style.VERTICAL_ALIGN, Style.BOTTOM, Style.ENUM);
        } else if ("center".equals(s)) {
          style.set(Style.VERTICAL_ALIGN, Style.MIDDLE, Style.ENUM);
        }
      } 
      s = getTableAttributeValue("border");
      if (s != null) {
        int border = 1;
        try {
          border = Integer.parseInt(s);
        } catch (NumberFormatException e) {
          // ignore
        }
        if (!table) {
          border = Math.min(border, 1);
        }

        style.set(Style.BORDER_TOP_STYLE | Style.MULTIVALUE_TRBL, 
            Style.SOLID, Style.ENUM, 0);
        style.set(Style.BORDER_TOP_COLOR | Style.MULTIVALUE_TRBL, 
            0xffcccccc, Style.ARGB, 0);
        style.set(Style.BORDER_TOP_WIDTH | Style.MULTIVALUE_TRBL, 
            1000 * border, Style.PX, 0);
      }
      if (!table) {
        s = getTableAttributeValue("cellpadding");
        if (s != null) {
          int padding = 0;
          try {
            padding = Integer.parseInt(s);
          } catch (NumberFormatException e) {
            // ignore
          }
          style.set(Style.PADDING_TOP | Style.MULTIVALUE_TRBL, 1000 * padding,
              Style.PX, 0);
        }
        s = getTableAttributeValue("cellspacing");
        if (s != null) {
          int spacing = 0;
          try {
            spacing = Integer.parseInt(s);
          } catch (NumberFormatException e) {
            // ignore
          }
          style.set(Style.MARGIN_TOP | Style.MULTIVALUE_TRBL, 500 * spacing, 
              Style.PX, 0);
        }
      }
    } 
    if ("font".equals(name)) {
      String color = getAttributeValue("color");
      if (color != null) {
        style.setColor(Style.COLOR, color, 0);
      } 
    } else if ("img".equals(name)) {
      int i = getAttributeInt("vspace", -1);
      if (i >= 0) {
        style.set(Style.PADDING_TOP, i * 1000, Style.PX);
        style.set(Style.PADDING_BOTTOM, i * 1000, Style.PX);
      }
      i = getAttributeInt("hspace", -1);
      if (i >= 0) {
        style.set(Style.PADDING_LEFT, i * 1000, Style.PX);
        style.set(Style.PADDING_RIGHT, i * 1000, Style.PX);
      }
    }
  }

  /**
   * Looks up table attributes in parent elements of the same table (tr, table)
   * 
   * @param attribute name
   * @return attribute value or null if not present
   */
  private String getTableAttributeValue(String attr) {
    String s = getAttributeValue(attr);
    if (s == null && parent != null && ("table".equals(parent.name) || 
        "tr".equals(parent.name))) {
      s = parent.getTableAttributeValue(attr);
    }
    return s;
  }

  /**
   * Dumps the XML tree in a human readable format for debugging 
   * purposes. Calls should be enclosed in if(DEBUG) statements.
   * 
   * @param indent initial indentiation
   */
  public void dumpStyle() {
    if (getComputedStyle() != null) {
      getComputedStyle().dump("  ");
    }
  }

  /** 
   * Dumps this the attributes and style for this element and for all parent 
   * elements. All calls should be enclosed in if(DEBUG) statements.
   */
  public String dumpPath() {
    String indent = (parent != null) ? parent.dumpPath() : "";
    System.out.print(indent + "<" + name);
    if (attributes != null) {
      for (Enumeration e = attributes.keys(); e.hasMoreElements();) {
        Object key = e.nextElement();
        System.out.print(" " + key + "='" + attributes.get(key) + "'");
      }
    }
    System.out.println("> ");
    return indent + "  ";
  }

  /**
   * Remove the child node with the given index.
   * 
   * @param i index of the child node to be removed
   */
  public void remove(int i) {
    content.removeElementAt(i);
  }

  /**
   * Sets the parent element. Currently used to detach the html root element 
   * from the document pseudo element after parsing. This is necessary to make
   * CSS behave correctly. 
   * 
   * @param parent the new parent element
   */
  public void setParent(Element parent) {
    this.parent = parent;    
  }
}
