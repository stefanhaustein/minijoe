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

package com.google.minijoe.html.css;

import java.util.*; 

import javax.microedition.lcdui.Font;

import com.google.minijoe.common.Util;
import com.google.minijoe.html.Element;
import com.google.minijoe.html.HtmlWidget;
import com.google.minijoe.html.SystemRequestHandler;

/**
 * This class represents a CSS style sheet. It is also used to represent parts
 * of a style sheet in a tree structure, where the depth of the tree equals the
 * length of the longest selector.
 * <p>
 * The properties field contains the style that is valid for the corresponding
 * selector path denoted by the position in the tree. Accordingly, the
 * properties field of the root style sheet contains the Style that is applied
 * for the "*" selector (matching anything).
 * <p>
 * To apply the style sheet, the tree is visited in a depth first search for
 * matching selector parts. Each visited node with a non-empty properties field
 * is inserted in a queue. The queue is ordered by the specificity of the node.
 * <p>
 * After all matching selectors were visited, the StyleSheet objects are fetched
 * from the queue in the order of ascending specificity and their properties are
 * applied. Thus, values denoted by more specific selectors overwrite values
 * from less specific rules.
 * <p>
 * Note that child and descendant selectors are inverted, so the evaluation of a
 * style sheet can always start at the element it is applied to.
 * 
 * @author Stefan Haustein
 */
public class StyleSheet {
  
  /*
   * SELECT_XXX constants are used to specify the selector type in
   * selectAttributeOperation and in parseSelector().
   */
  private static final char SELECT_ID = 1;
  private static final char SELECT_CLASS = 2;
  private static final char SELECT_PSEUDOCLASS = 3;
  private static final char SELECT_NAME = 4;
  private static final char SELECT_DESCENDANT = 6;
  private static final char SELECT_ATTRIBUTE_NAME = 7;
  private static final char SELECT_ATTRIBUTE_VALUE = 8;
  private static final char SELECT_ATTRIBUTE_INCLUDES = 9;
  private static final char SELECT_ATTRIBUTE_DASHMATCH = 10;
  private static final char SELECT_CHILD = 11;

  /**
   * Specificity weight for element name and pseudoclass selectors.
   */
  private static final int SPECIFICITY_D = 1;

  /**
   * Specificity weight for element name selectors.
   */
  private static final int SPECIFICITY_C = 100 * SPECIFICITY_D;

  /**
   * Specificity weight for id selectors.
   */                                        
  private static final int SPECIFICITY_B = 100 * SPECIFICITY_C;

  /**
   * Specificity weight for !important selectors
   */
  static final int SPECIFICITY_IMPORTANT = 100 * SPECIFICITY_B;

  /**
   * A table mapping element names to sub-style sheets for the corresponding
   * selection path.
   */
  public Hashtable selectElementName;

  /**
   * A table mapping pseudoclass names to sub-style sheets for the corresponding
   * selection path.
   */
  private Hashtable selectPseudoclass;

  /**
   * A list of attribute names for selectors. Forms attribute selectors together
   * with selectAttributeOperation and selectAttributeValue.
   */
  private Vector selectAttributeName;

  /**
   * A list of attribute operations for selectors (one of the
   * SELECT_ATTRIBUTE_XX constants). Forms attribute selectors together with
   * selectAttributeName and selectAttributeValue.
   */
  private StringBuffer selectAttributeOperation;

  /**
   * A list of Hashtables, mapping attribute values to sub-style sheets for the
   * corresponding selection path. Forms attribute selectors together with
   * selectAttributeName and selectAttributeOperation.
   */
  private Vector selectAttributeValue;

  /**
   * Reference to child selector selector sub-style sheet.
   */
  private StyleSheet selectChild;

  /**
   * Reference to descendant selector sub-style sheet.
   */
  private StyleSheet selectDescendants;

  /**
   * Properties for * rules 
   */
  private Vector properties;

  /**
   * Creates a new style sheet with default rules for HTML.
   * 
   * <table>
   * <tr><td>a </td><td>color: #0000ff; decoration: underline</td></tr>
   * <tr><td>b </td><td>font-weight: 700; </td></tr>
   * <tr><td>body </td><td>display: block; padding: 5px; </td></tr>
   * <tr><td>dd </td><td>display: block; </td></tr>
   * <tr><td>dir </td><td>display: block; margin-top: 2px; 
   *  margin-bottom: 2px; margin-left: 10px; </td></tr>
   * <tr><td>div </td><td>display: block; </td></tr>
   * <tr><td>dl </td><td>display: block; </td></tr>
   * <tr><td>dt </td><td>display: block; </td></tr>
   * <tr><td>h1 .. h6</td><td>display: block; font-weight: 700; 
   *  margin-top: 2px; margin-bottom: 2px; </td></tr>
   * <tr><td>hr </td><td>border-top-color: #888888; border-top-width: 1px; 
   *  border-top-style: solid; display: block; 
   *  margin-top: 2px; margin-bottom: 2px; </td></tr>
   * <tr><td>li </td><td>display: list-item; margin-top: 2px; 
   *  margin-bottom: 2px; </td></tr></td></tr>
   * <tr><td>ol </td><td>display: block; list-style-type: decimal; 
   *  margin-left: 10px; </td></tr>
   * <tr><td>p </td><td>display: block; margin-top: 2px; 
   *  margin-bottom: 2px; </td></tr>
   * <tr><td>th </td><td>display: table-cell; font-weight: 700;  
   *  padding: 1px;</td></tr>
   * <tr><td>tr </td><td>display: table-row;</td></tr>
   * <tr><td>td </td><td>display: table-cell; padding: 1px; </td></tr>
   * <tr><td>ul </td><td>display: block; margin-left: 10px; </td></tr>
   * <tr><td>img </td><td>display: inline-block; </td></tr>
   * </table>
   */
  public StyleSheet(boolean init) {
    if(!init) {
      return;
    }
    Font font = new Style().getFont();

    // Set default indent with to sufficient space for ordered lists with
    // two digits and the default paragraph spacing to 50% of the font height
    // (so top and bottom spacing adds up to a full line)
    int defaultIndent = font.stringWidth("88. ") * 1000;
    int defaultParagraphSpace = Math.max(1, font.getHeight() / 2) * 1000;
    
    put(":link", new Style().set(Style.COLOR, 0x0ff0000ff, Style.ARGB).
        set(Style.TEXT_DECORATION, Style.UNDERLINE, Style.ENUM));
    put("address", new Style().set(Style.DISPLAY, Style.BLOCK, Style.ENUM));
    put("b", new Style().set(Style.FONT_WEIGHT, 700000, Style.NUMBER));
    put("blockquote", new Style().set(Style.DISPLAY, Style.BLOCK, Style.ENUM).
        set(Style.MARGIN_TOP, defaultParagraphSpace, Style.PX).
        set(Style.MARGIN_RIGHT, defaultIndent, Style.PX).
        set(Style.MARGIN_BOTTOM, defaultParagraphSpace, Style.PX).
        set(Style.MARGIN_LEFT, defaultIndent, Style.PX));
    put("body", new Style().set(Style.DISPLAY, Style.BLOCK, Style.ENUM).
        set(Style.PADDING_TOP | Style.MULTIVALUE_TRBL, 
            defaultParagraphSpace / 2, Style.PX, 0));
    put("button", new Style().
        set(Style.DISPLAY, Style.INLINE_BLOCK, Style.ENUM).
        set(Style.PADDING_TOP | Style.MULTIVALUE_TRBL, 3000, Style.PX, 0));
    put("center", new Style().set(Style.DISPLAY, Style.BLOCK, Style.ENUM).
        set(Style.MARGIN_TOP, defaultParagraphSpace, Style.PX).
        set(Style.MARGIN_BOTTOM, defaultParagraphSpace, Style.PX).
        set(Style.TEXT_ALIGN, Style.CENTER, Style.ENUM));
    put("dd", new Style().set(Style.DISPLAY, Style.BLOCK, Style.ENUM).
        set(Style.MARGIN_LEFT, defaultIndent, Style.PX)
    );
    put("dir", new Style().set(Style.DISPLAY, Style.BLOCK, Style.ENUM).
        set(Style.MARGIN_TOP, defaultParagraphSpace, Style.PX).
        set(Style.MARGIN_BOTTOM, defaultParagraphSpace, Style.PX).
        set(Style.MARGIN_LEFT, defaultIndent, Style.PX).
        set(Style.LIST_STYLE_TYPE, Style.SQUARE, Style.ENUM));
    put("div", new Style().set(Style.DISPLAY, Style.BLOCK, Style.ENUM));
    put("dl", new Style().set(Style.DISPLAY, Style.BLOCK, Style.ENUM));
    put("dt", new Style().set(Style.DISPLAY, Style.BLOCK, Style.ENUM));
    put("form", new Style().set(Style.DISPLAY, Style.BLOCK, Style.ENUM));
    for (int i = 1; i <= 6; i++) {
      put("h" + i, new Style().set(Style.DISPLAY, Style.BLOCK, Style.ENUM).
          set(Style.FONT_WEIGHT, 700000, Style.NUMBER).
          set(Style.MARGIN_TOP, defaultParagraphSpace, Style.PX).
          set(Style.MARGIN_BOTTOM, defaultParagraphSpace, Style.PX));
    }
    put("hr", new Style().set(Style.DISPLAY, Style.BLOCK, Style.ENUM).
        set(Style.BORDER_TOP_WIDTH, 1000, Style.PX).
        set(Style.BORDER_TOP_STYLE, Style.SOLID, Style.ENUM).
        set(Style.BORDER_TOP_COLOR, 0x0ff888888, Style.ARGB).
        set(Style.MARGIN_TOP, defaultParagraphSpace, Style.PX).
        set(Style.MARGIN_BOTTOM, defaultParagraphSpace, Style.PX));
    put("img", new Style().set(Style.DISPLAY, Style.INLINE_BLOCK, Style.ENUM));
    put("input", new Style().
        set(Style.DISPLAY, Style.INLINE_BLOCK, Style.ENUM));    
    put("li", new Style().set(Style.DISPLAY, Style.LIST_ITEM, Style.ENUM).
        set(Style.MARGIN_TOP, defaultParagraphSpace, Style.PX).
        set(Style.MARGIN_BOTTOM, defaultParagraphSpace, Style.PX));
    put("marquee", new Style().set(Style.DISPLAY, Style.BLOCK, Style.ENUM));
    put("menu", new Style().set(Style.DISPLAY, Style.BLOCK, Style.ENUM).
        set(Style.MARGIN_TOP, defaultParagraphSpace, Style.PX).
        set(Style.MARGIN_BOTTOM, defaultParagraphSpace, Style.PX).
        set(Style.MARGIN_LEFT, defaultIndent, Style.PX).
        set(Style.LIST_STYLE_TYPE, Style.SQUARE, Style.ENUM));
    put("ol", new Style().set(Style.DISPLAY, Style.BLOCK, Style.ENUM).
        set(Style.MARGIN_LEFT, defaultIndent, Style.PX).
        set(Style.LIST_STYLE_TYPE, Style.DECIMAL, Style.ENUM));
    put("p", new Style().set(Style.DISPLAY, Style.BLOCK, Style.ENUM).
        set(Style.MARGIN_TOP, defaultParagraphSpace, Style.PX).
        set(Style.MARGIN_BOTTOM, defaultParagraphSpace, Style.PX));
    put("pre", new Style().set(Style.DISPLAY, Style.BLOCK, Style.ENUM).
        set(Style.WHITE_SPACE, Style.PRE, Style.ENUM).
        set(Style.MARGIN_TOP, defaultParagraphSpace, Style.PX).
        set(Style.MARGIN_BOTTOM, defaultParagraphSpace, Style.PX));
    put("select[multiple]", new Style().
        set(Style.DISPLAY, Style.BLOCK, Style.ENUM).
       // set(Style.BACKGROUND_COLOR, 0x0ffff0000, Style.ARGB).
        set(Style.PADDING_TOP | Style.MULTIVALUE_TRBL, 1000, Style.PX, 0).
        set(Style.BORDER_TOP_WIDTH | Style.MULTIVALUE_TRBL, 1000, Style.PX, 0).
        set(Style.BORDER_TOP_COLOR | Style.MULTIVALUE_TRBL, 
            0x0ff888888, Style.ARGB, 0).
        set(Style.BORDER_TOP_STYLE | Style.MULTIVALUE_TRBL, 
            Style.SOLID, Style.ENUM, 0));
    put("script", new Style().set(Style.DISPLAY, Style.NONE, Style.ENUM));
    put("strong", new Style().set(Style.FONT_WEIGHT, Style.BOLD, Style.ENUM));
    put("style", new Style().set(Style.DISPLAY, Style.NONE, Style.ENUM));
    
    put("table", new Style().set(Style.DISPLAY, Style.TABLE, Style.ENUM).
        set(Style.CLEAR, Style.BOTH, Style.ENUM));
    put("td", new Style().set(Style.DISPLAY, Style.TABLE_CELL, Style.ENUM).
        set(Style.PADDING_TOP | Style.MULTIVALUE_TRBL, 1000, Style.PX, 0).
        set(Style.TEXT_ALIGN, Style.LEFT, Style.ENUM));
    put("th", new Style().set(Style.DISPLAY, Style.TABLE_CELL, Style.ENUM).
        set(Style.FONT_WEIGHT, 700000, Style.NUMBER).
        set(Style.PADDING_TOP | Style.MULTIVALUE_TRBL, 1000, Style.PX, 0).
        set(Style.TEXT_ALIGN, Style.CENTER, Style.ENUM));
    put("tr", new Style().set(Style.DISPLAY, Style.TABLE_ROW, Style.ENUM));
    put("ul", new Style().set(Style.DISPLAY, Style.BLOCK, Style.ENUM).
        set(Style.MARGIN_LEFT, defaultIndent, Style.PX).
        set(Style.LIST_STYLE_TYPE, Style.SQUARE, Style.ENUM));
    put("ul ul", new Style().
        set(Style.LIST_STYLE_TYPE, Style.CIRCLE, Style.ENUM));
    put("ul ul ul", new Style().
        set(Style.LIST_STYLE_TYPE, Style.DISC, Style.ENUM));
  }

  /**
   * Reads a style sheet from the given css string.
   * 
   * @param htmlWidget HtmlWidget owning the HTML document this style sheet is
   *                   attached to
   * @param url URL of this style sheet (or the containing document)                  
   * @param css the CSS string to load the style sheet from
   * @return this
   * @throws IOException if there is an underlying stream exception
   */
  public StyleSheet read(HtmlWidget htmlWidget, String url, String css) {
    CssTokenizer ct = new CssTokenizer(htmlWidget, url, css);

    int position = 0;
    int cut = url.lastIndexOf('#');
    int[] nesting;
    if(cut != -1) {
      String[] nestingStr = Util.split(url.substring(cut + 1), ',');
      nesting = new int[nestingStr.length];
      for (int i = 0; i < nesting.length; i++) {
        nesting[i] = Integer.parseInt(nestingStr[i]);
      }
    } else {
      nesting = new int[0];
    }
    
    boolean inMedia = false;

    while (ct.ttype != CssTokenizer.TT_EOF) {
      if (ct.ttype == CssTokenizer.TT_ATKEYWORD) {
        if ("media".equals(ct.sval)) {
          ct.nextToken(false);
          inMedia = false;
          do {
            if(ct.ttype != ',') {
              inMedia |= htmlWidget.checkMediaType(ct.sval);
            }
            ct.nextToken(false);
          } while (ct.ttype != '{' && ct.ttype != CssTokenizer.TT_EOF);
          ct.nextToken(false);
          if (!inMedia) {
            int level = 1;
            do {
              switch (ct.ttype) {
              case '}': 
                level--;
                break;
              case '{':
                level++;
                break;
              case CssTokenizer.TT_EOF:
                return this;
              }
              ct.nextToken(false);
            } while (level > 0);
          }
        } else if ("import".equals(ct.sval)){
          ct.nextToken(false);
          String importUrl = ct.sval;
          ct.nextToken(false);
          StringBuffer media = new StringBuffer();
          while (ct.ttype != ';' && ct.ttype != CssTokenizer.TT_EOF) {
            media.append(ct.sval);
            ct.nextToken(false);
          }
          if (htmlWidget.checkMediaType(media.toString())) {
            StringBuffer buf = new StringBuffer();
            buf.append(Util.getAbsoluteUrl(url, importUrl));
            buf.append('#');
            for (int i = 0; i < nesting.length; i++) {
              buf.append(nesting[i]);
              buf.append(',');
            }
            buf.append(position);
            ct.htmlWidget.getResource(buf.toString(), 
                SystemRequestHandler.TYPE_STYLESHEET, this);
          }
          ct.nextToken(false);
          position++;
        } else {
          ct.debug("unsupported @" + ct.sval);
          ct.nextToken(false);
        }
      } else if (ct.ttype == '}') {
        if (!inMedia) {
          ct.debug("unexpected }");
        }
        inMedia = false;
        ct.nextToken(false);
      } else {
        // no @keyword or } -> regular selector
        Vector targets = new Vector();
        targets.addElement(parseSelector(ct));
        while (ct.ttype == ',') {
          ct.nextToken(false);
          targets.addElement(parseSelector(ct));
        }

        Style style = new Style();
        if (ct.ttype == '{') {
          ct.nextToken(false);
          style.read(ct);
          ct.assertTokenType('}');
        } else {
          ct.debug("{ expected");
        }

        for (int i = 0; i < targets.size(); i++) {
          Style target = (Style) targets.elementAt(i);
          if (target == null) {
            continue;
          }
          target.position = position;
          target.nesting = nesting;
          target.set(style);
        }
        ct.nextToken(false);
        position++;
      }
    }
    return this;
  }

  /**
   * Parse a selector. The tokenizer must be at the first token of the selector.
   * When returning, the current token will be ',' or '{'.
   * <p>
   * This method brings selector paths into the tree form described in the class
   * documentation.
   * 
   * @param ct the css tokenizer
   * @return the node at the end of the tree path denoted by this selector,
   *         where the corresponding CSS properties will be stored
   * @throws IOException
   */
  private Style parseSelector(CssTokenizer ct) {

    boolean error = false;
    
    int specificity = 0;
    StyleSheet result = this;

    loop : while (true) {
      switch (ct.ttype) {
        case CssTokenizer.TT_IDENT: {
          if (result.selectElementName == null) {
            result.selectElementName = new Hashtable();
          }
          result = descend(result.selectElementName, ct.sval.toLowerCase());
          specificity += SPECIFICITY_D;
          ct.nextToken(true);
          break;
        }
        case '*': {
          // no need to do anything...
          ct.nextToken(true);
          continue;
        }
        case '[': {
          ct.nextToken(false);
          String name = ct.sval.toLowerCase();
          ct.nextToken(false);
          char type;
          String value = null;
          
          if (ct.ttype == ']') {
            type = SELECT_ATTRIBUTE_NAME;
          } else {
            switch (ct.ttype) {
              case CssTokenizer.TT_INCLUDES:
                type = SELECT_ATTRIBUTE_INCLUDES;
                break;
              case '=':
                type = SELECT_ATTRIBUTE_VALUE;
                break;
              case CssTokenizer.TT_DASHMATCH:
                type = SELECT_ATTRIBUTE_DASHMATCH;
                break;
              default:
                error = true;
                break loop;
            }
            ct.nextToken(false);
            if (ct.ttype != CssTokenizer.TT_STRING) {
              error = true;
              break loop;
            }
            value = ct.sval;
            ct.nextToken(false);
            ct.assertTokenType(']');
            specificity += SPECIFICITY_C;
          }
          result = result.createAttributeSelector(type, name, value);
          ct.nextToken(true);
          break;
        }
        case '.':
          ct.nextToken(false);
          error = ct.ttype != CssTokenizer.TT_IDENT;
          result = result.createAttributeSelector(SELECT_ATTRIBUTE_INCLUDES, "class", ct.sval);
          specificity += SPECIFICITY_C;
          ct.nextToken(true);
          break;

        case CssTokenizer.TT_HASH:
          result = result.createAttributeSelector(SELECT_ATTRIBUTE_VALUE, "id", ct.sval);
          specificity += SPECIFICITY_B;
          ct.nextToken(true);
          break;

        case ':': 
          ct.nextToken(false);
          error = ct.ttype != CssTokenizer.TT_IDENT;
          if (result.selectPseudoclass == null) {
            result.selectPseudoclass = new Hashtable();
          }
          result = descend(result.selectPseudoclass, ct.sval);
          specificity += SPECIFICITY_C;
          ct.nextToken(true);
          break;

        case CssTokenizer.TT_S:
          ct.nextToken(false);
          if (ct.ttype == '{' || ct.ttype == ',' || ct.ttype == -1) {
            break loop;
          }
          if (ct.ttype == '>') {
            if (result.selectChild == null) {
              result.selectChild = new StyleSheet(false);
            }
            result = result.selectChild;
            ct.nextToken(false);
          } else {
            if (result.selectDescendants == null) {
              result.selectDescendants = new StyleSheet(false);
            }
            result = result.selectDescendants;
          }
          break;

        case '>':
          if (result.selectChild == null) {
            result.selectChild = new StyleSheet(false);
          }
          result = result.selectChild;
          ct.nextToken(false);
          break;

        default: // unknown
          break loop;
      }
    }

    // state: behind all recognized tokens -- check for unexpected stuff
    if (error || (ct.ttype != ',' && ct.ttype != '{')) {
      ct.debug("Unrecognized selector");
      // parse to '{', ',' or TT_EOF to get to a well-defined state
      while (ct.ttype != ',' && ct.ttype != CssTokenizer.TT_EOF
          && ct.ttype != '{') {
        ct.nextToken(false);
      }
      return null;
    }

    Style style = new Style();
    style.specificity = specificity;
    if (result.properties == null) {
      result.properties = new Vector();
    }
    result.properties.addElement(style);
    
    return style;
  }

  private StyleSheet createAttributeSelector(char type, String name, String value) {
    long selectorSpecificity = SPECIFICITY_C;
    if (type == SELECT_ID) {
      type = SELECT_ATTRIBUTE_VALUE;
      selectorSpecificity = SPECIFICITY_B;
    } else if (type == SELECT_CLASS) {
      type = SELECT_ATTRIBUTE_INCLUDES;
    }
    int index = -1;
    if (selectAttributeOperation == null) {
      selectAttributeOperation = new StringBuffer();
      selectAttributeName = new Vector();
      selectAttributeValue = new Vector();
    } else {
      for (int j = 0; j < selectAttributeOperation.length(); j++) {
        if (selectAttributeOperation.charAt(j) == type
            && selectAttributeName.elementAt(j).equals(name)) {
          index = j;
        }
      }
    }

    if (type == SELECT_ATTRIBUTE_NAME) {
      if (index == -1) {
        index = selectAttributeOperation.length();
        selectAttributeOperation.append(type);
        selectAttributeName.addElement(name);
        selectAttributeValue.addElement(new StyleSheet(false));
      }
      return (StyleSheet) selectAttributeValue.elementAt(index);
    } 
    
    if (index == -1) {
      index = selectAttributeOperation.length();
      selectAttributeOperation.append(type);
      selectAttributeName.addElement(name);
      selectAttributeValue.addElement(new Hashtable());
    }
    return descend((Hashtable) selectAttributeValue.elementAt(index), value);
  }
  
  
  /**
   * Returns the style sheet denoted by the given key from the hashtable. If not
   * yet existing, a corresponding entry is created with the given specificity.
   */
  private static StyleSheet descend(Hashtable h, String key) {
    StyleSheet s = (StyleSheet) h.get(key);
    if (s == null) {
      s = new StyleSheet(false);
      h.put(key, s);
    }
    return s;
  }

  /**
   * Helper method for collectStyles(). Determines whether the given key is 
   * in the given map. If so, the style search continues in the corresponding 
   * style sheet.
   * 
   * @param element the element under consideration (may be the target element
   *            or any parent)
   * @param map corresponding sub style sheet map
   * @param key element name or attribute value
   * @param queue queue of matching rules to be processed further
   */
  private void collectStyles(Element element, Hashtable map, String key,
      Vector queue, Vector children, Vector descendants) {
    if (key == null || map == null) {
      return;
    }
    StyleSheet sh = (StyleSheet) map.get(key);
    if (sh != null) {
      sh.collectStyles(element, queue, children, descendants);
    }
  }

  /**
   * Performs a depth first search of all matching selectors and enqueues the
   * corresponding style information.
   * 
   * @param element The element currently under consideration
   * @param s the style to be modified
   * @param queue the queue
   */
  public void collectStyles(Element element, Vector queue, Vector children, Vector descendants) {
    
    if (properties != null) {
      // enqueue the style at the current node according to its specificity

      for (int i = 0; i < properties.size(); i++) {
        Style p = (Style) properties.elementAt(i);
        int index = queue.size();
        while (index > 0) {
          Style s = (Style) queue.elementAt(index - 1);
          if (s.compare(p) < 0) {
            break;
          }
          if (s == p) {
            index = -1;
            break;
          }
          index--;
        }
        if (index != -1) {
          queue.insertElementAt(p, index);
        }
      }
    }
      
    if (selectAttributeOperation != null) {
      for (int i = 0; i < selectAttributeOperation.length(); i++) {
        int type = selectAttributeOperation.charAt(i);
        String name = (String) selectAttributeName.elementAt(i);
        String value = element.getAttributeValue(name);
        if (value == null) {
          continue;
        }
        if (type == SELECT_ATTRIBUTE_NAME) {
          ((StyleSheet) selectAttributeValue.elementAt(i)).collectStyles(
              element, queue, children, descendants);
        } else {
          Hashtable valueMap = (Hashtable) selectAttributeValue.elementAt(i);
          if (type == SELECT_ATTRIBUTE_VALUE) {
            collectStyles(element, valueMap, value, queue, children, descendants);
          } else {
            String[] values = Util.split(value,
                type == SELECT_ATTRIBUTE_INCLUDES ? ' ' : ',');
            for (int j = 0; j < values.length; j++) {
              collectStyles(element, valueMap, values[j], queue, children, descendants);
            }
          }
        }
      }
    }

    if (selectElementName != null) {
      collectStyles(element, selectElementName, element.getName(), queue, children, descendants);
    }

    if (selectChild != null) {
      children.addElement(selectChild);
    }

    if (selectPseudoclass != null && element.isFocusable()) {
      collectStyles(element, selectPseudoclass, "link", queue, children, descendants);
    }
    
    if (selectDescendants != null) {
      descendants.addElement(selectDescendants);
    }
  }

  /**
   * Internal method used to simplify building the default style sheet.
   * 
   * @param selector element name
   * @param style default style for the element
   */
  private void put(String selector, Style style) {

    if (selectElementName == null) {
      selectElementName = new Hashtable();
    }
    
    boolean simple = true;
    for (int i = 0; i < selector.length(); i++){
      char c = selector.charAt(i);
      if (c < 'a' || c > 'z') {
        simple = false;
        break;
      }
    }

    if (simple) {
      StyleSheet s = new StyleSheet(false);
      s.properties = new Vector();
      s.properties.addElement(style);
      style.specificity = SPECIFICITY_D - SPECIFICITY_IMPORTANT;
      selectElementName.put(selector, s);
    } else {
      CssTokenizer ct = new CssTokenizer(null, null, selector + "{");
      Style target = parseSelector(ct);
      target.set(style);
      // copy important
      target.specificity += style.specificity - SPECIFICITY_IMPORTANT; 
    }

  }

  /** 
   * Print the style sheet to stdout for debugging purposes.
   * 
   * @param current the current selector
   */
  public void dump(String current) {

    if (properties != null) {
      System.out.print(current.length() == 0 ? "*" : current);
      System.out.print(" {");
      for (int i = 0; i < properties.size(); i++) {
        ((Style) properties.elementAt(i)).dump("");
      }
      System.out.println("}");
    }

    if (selectElementName != null) {
      for (Enumeration e = selectElementName.keys(); e.hasMoreElements();) {
        String key = (String) e.nextElement();
        ((StyleSheet) selectElementName.get(key)).dump(key + current);
      }
    }

    if (selectAttributeOperation != null) {
      for (int i = 0; i < selectAttributeOperation.length(); i++) {
        int type = selectAttributeOperation.charAt(i);
        StringBuffer p = new StringBuffer(current);
        p.append('[');
        p.append((String) selectAttributeName.elementAt(i));

        if (type == SELECT_ATTRIBUTE_NAME) {
          p.append(']');
          ((StyleSheet) selectAttributeValue.elementAt(i)).dump(p.toString());
        } else {
          switch (type) {
            case SELECT_ATTRIBUTE_VALUE:
              p.append('=');
              break;
            case SELECT_ATTRIBUTE_INCLUDES:
              p.append("~=");
              break;
            case SELECT_ATTRIBUTE_DASHMATCH:
              p.append("|=");
              break;
          }
          Hashtable valueMap = (Hashtable) selectAttributeValue.elementAt(i);
          for (Enumeration e = valueMap.keys(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            ((StyleSheet) valueMap.get(key)).dump(p.toString() + '"' + key + "\"]");
          }
        }
      }
    }

    if (selectDescendants != null) {
      selectDescendants.dump(current + " ");
    }

    if (selectChild != null) {
      selectChild.dump(current + " > ");
    }
  }
}
