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
import com.google.minijoe.html.css.Style;

import java.util.*;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.TextField;

/**
 * Handles input, select and button. Inherits from blockWidget for doLayout(), 
 * but most properties are ignored. 
 * 
 * @author Stefan Haustein
 */
public class InputWidget extends BlockWidget {
  private int type;
  private String text;
  private HtmlWidget document;


  public InputWidget(Element element) {
    super(element, true);
    this.document = element.htmlWidget;
    String name = element.getName();
    if (name.equals("select")){
      type = Skin.INPUT_TYPE_SELECT;
    } else if (name.equals("option")) {
      type = Skin.INPUT_TYPE_OPTION;
      text = element.getText();
    } else if (name.equals("textarea")) {
      type = Skin.INPUT_TYPE_TEXTAREA;
      text = element.getText();
    } else if (name.equals("button")) {
      type = Skin.INPUT_TYPE_BUTTON;
      addChildren(element, new boolean[2]);
    } else {
      String typeName = element.getAttributeValue("type");
      text = element.getAttributeValue("value");
      if ("password".equals(typeName)) {
        type = Skin.INPUT_TYPE_PASSWORD;
      } else if ("submit".equals(typeName)) {
        type = Skin.INPUT_TYPE_SUBMIT;
        if (text == null) {
          text = "Submit";
        }
      } else if ("checkbox".equals(typeName)) {
        type = Skin.INPUT_TYPE_CHECKBOX;
      } else if ("radio".equals(typeName)) {
        type = Skin.INPUT_TYPE_RADIOBUTTON;
      } else if ("reset".equals(typeName)) {
        type = Skin.INPUT_TYPE_RESET;
        if (text == null) {
          text = "Reset";
        }
      } else {
        type = Skin.INPUT_TYPE_TEXT;
      }
    }  
    if (text == null) {
      text = "";
    }
  }


  protected void calculateWidth(int containerWidth) {
    if (type == Skin.INPUT_TYPE_BUTTON) {
      super.calculateWidth(containerWidth);
      return;
    }

    Style style = element.getComputedStyle();
    int fh = style.getFont().getHeight();

    int right = style.getPx(Style.MARGIN_RIGHT, containerWidth);
    int left = style.getPx(Style.MARGIN_LEFT, containerWidth);

    int w;

    if (style.lengthIsFixed(Style.WIDTH, false)) {
      w = left + style.getPx(Style.WIDTH, containerWidth) + right;
    } else {
      switch (type) {
        case Skin.INPUT_TYPE_CHECKBOX:
        case Skin.INPUT_TYPE_RADIOBUTTON:
        case Skin.INPUT_TYPE_OPTION:
        case Skin.INPUT_TYPE_SUBMIT:
        case Skin.INPUT_TYPE_RESET:
          w = Skin.get().calculateWidth(type, style.getFont(), text) + left + right;
          break;

        case Skin.INPUT_TYPE_SELECT:
          int longest = 0;
          String longestText = "";
          for (int i = 0; i < element.getChildCount(); i++) {
            if (element.getChildType(i) == Element.ELEMENT) {
              Element option = element.getElement(i);
              if ("option".equals(option.getName())) {
                String s = option.getText();
                if (text == null || option.getAttributeBoolean("selected")) {
                  text = s;
                }
                int l = style.getFont().stringWidth(s);
                if (l > longest) {
                  longest = l;
                  longestText = s;
                }
              }
            }
          }
          w = Skin.get().calculateWidth(type, style.getFont(), longestText) + left + right;
          break;

        case Skin.INPUT_TYPE_TEXTAREA:
          w = element.getAttributeInt("cols", 40) * fh / 2;
          break;

        default:
          String sizeAttr = element.getAttributeValue("size");
          int size = sizeAttr == null ? 20 : Integer.parseInt(sizeAttr);
          w = size * fh / 2 + left + right;
      }
    }

    minimumWidth = maximumWidth = w;
  }

  /**
   * {@inheritDoc}
   */
  public void doLayout(int maxWidth, LayoutContext border, boolean shrinkWrap) {

    if (containingWidth == maxWidth && layoutValid) {
      return;
    }

    if (type == Skin.INPUT_TYPE_BUTTON) {
      super.doLayout(maxWidth, border, shrinkWrap);
      return;
    }

    this.containingWidth = maxWidth;
    Style style = element.getComputedStyle();
    int top = style.getPx(Style.MARGIN_TOP, maxWidth);
    int bottom = style.getPx(Style.MARGIN_BOTTOM, maxWidth);
    int fh = style.getFont().getHeight();

    boxWidth = Math.min(maxWidth, style.lengthIsFixed(Style.WIDTH, true) 
        ? getSpecifiedWidth(maxWidth) : getMinimumWidth(maxWidth));

    switch(type) {
      case Skin.INPUT_TYPE_TEXTAREA:
        String rowsAttr = element.getAttributeValue("rows");
        int rows = rowsAttr == null ? 3 : Integer.parseInt(rowsAttr);
        boxHeight = rows * fh + 6 + top + bottom;
        break;
      default:
        boxHeight = Skin.get().calculateHeight(type, style.getFont()) + top + bottom;
    }    

    setWidth(boxWidth);
    setHeight(boxHeight);
  }

  /**
   * Set the selected option for this input element. Use for select only.
   */
  public void setSelectedIndex(int index) {

    int actualIndex = 0;
    text = "";
    for (int i = 0; i < element.getChildCount(); i++) {
      if (element.getChildType(i) != Element.ELEMENT) {
        continue;
      }
      Element child = element.getElement(i);
      if (!"option".equals(child.getName())) {
        continue;
      }

      if (actualIndex == index) {
        child.setAttribute("selected", "true");
        text = child.getText();
      } else {
        child.setAttribute("selected", null);
      }
      actualIndex++;
    } 
    invalidate(false);
  }

  public void setText(String text) {
	this.text = text;
    if (type == Skin.INPUT_TYPE_TEXTAREA) {
      while (element.getChildCount() > 0) {
        element.remove(element.getChildCount() - 1);
      }
      element.addText(text);
      invalidate(true);
    } else {
      element.setAttribute("value", text);
      invalidate(false);
    }
  }

  /**
   * Draw this input element.
   */
  public void drawContent(Graphics g, int dx, int dy) {

    Style style = element.getComputedStyle();

    int top = style.getPx(Style.MARGIN_TOP, containingWidth);
    int right = style.getPx(Style.MARGIN_RIGHT, containingWidth);
    int bottom = style.getPx(Style.MARGIN_BOTTOM, containingWidth);
    int left = style.getPx(Style.MARGIN_LEFT, containingWidth);

    int w = boxWidth - left - right;
    int h = boxHeight - bottom - top;

    dx += left + boxX;
    dy += top + boxY;

    Font font = style.getFont();
    int color = style.getValue(Style.COLOR);
    boolean selected = false;

    switch(type) {
      case Skin.INPUT_TYPE_OPTION:  
        selected = element.getAttributeBoolean("selected");
        break;

      case Skin.INPUT_TYPE_RADIOBUTTON:
      case Skin.INPUT_TYPE_CHECKBOX:
        selected = element.getAttributeBoolean("checked");
        break;
    }

    Skin.get().drawControl(g, type, dx, dy, w, h, font, color, text, selected, isFocused());
  }

  /**
   * Handle key input for this widget.
   */
  public boolean handleKeyEvent(int eventType, int keyCode, int action) {
    if (eventType == KEY_RELEASED) {
      return false;
    }

    if (action == Canvas.FIRE) {
      switch (type) {
        case Skin.INPUT_TYPE_SELECT:
          Vector options = new Vector();
          int selectedIndex = -1;
          for (int i = 0; i < element.getChildCount(); i++) {
            if (element.getChildType(i) == Element.ELEMENT) {
              Element child = element.getElement(i);
              if ("option".equals(child.getName())) {
                if (child.getAttributeBoolean("selected")) {
                  selectedIndex = options.size();
                }
                options.addElement(child.getText());
              }
            }
          }
          element.htmlWidget.requestHandler.
          requestPopup(this, options, selectedIndex);
          return true;

        case Skin.INPUT_TYPE_RADIOBUTTON:
          resetRadioButtons(findForm(), element.getAttributeValue("name"));
          // fall-through
        case Skin.INPUT_TYPE_CHECKBOX:
          element.setAttribute("checked", element.getAttributeBoolean("checked") 
              ? "false" : "true");
          invalidate(false);
          return true;

        case Skin.INPUT_TYPE_OPTION:
          element.setAttribute("selected", 
              element.getAttributeBoolean("selected") ? null : "true");
          break;

        case Skin.INPUT_TYPE_TEXT:
        case Skin.INPUT_TYPE_PASSWORD:
        case Skin.INPUT_TYPE_TEXTAREA:
          int constraints = type == Skin.INPUT_TYPE_PASSWORD ? TextField.PASSWORD : TextField.ANY;
          document.requestHandler.requestTextInput(this, text, constraints, type == Skin.INPUT_TYPE_TEXTAREA);
          return true;

        case Skin.INPUT_TYPE_SUBMIT:
          submit();
          return true;
      }
    }
    return false;
  }

  /**
   * Resets all radio buttons with the given name for the given element and 
   * child elements.
   */
  private void resetRadioButtons(Element element, String name) {
    if (name == null) {
      return;
    }
    if (name.equals(element.getAttributeValue("name"))) {
      element.setAttribute("checked", null);
    }
    for (int i = 0; i < element.getChildCount(); i++) {
      if (element.getChildType(i) == Element.ELEMENT) {
        resetRadioButtons(element.getElement(i), name);
      }
    }
  }

  /**
   * Searches the form parent element for this input widget.
   */
  private Element findForm() {
    Element result = element.getParent();

    while (result != null && !"form".equals(result.getName())) {
      result = result.getParent();
    }
    return result;
  }

  /** 
   * Recurses the form elements and collects input data. 
   */
  private void collectFormData(Element element, StringBuffer sb) {
    String name = element.getName();
    String value = null;
    String key = element.getAttributeValue("name");

    if ("input".equals(name)) {
      String t = element.getAttributeValue("type");
      if (!"submit".equals(t) && !"reset".equals(t) &&
          ((!"radio".equals(t) && !"checkbox".equals(t)) || 
              element.getAttributeBoolean("checked"))){
        value = element.getAttributeValue("value");
        if (value == null) {
          value = "";
        }
      }
    } else if ("textarea".equals(name)) {
      value = element.getText();
    } else if ("option".equals(name)) {
      if (element.getAttributeBoolean("selected")) {
        key = element.getParent().getAttributeValue("name");
        value = element.getAttributeValue("value");
        if (value == null) {
          value = element.getText();
        }
      }
    }

    if (value != null && key != null) {
      if (sb.length() != 0) {
        sb.append('&');
      }
      sb.append(Util.encodeURL(key));
      sb.append('=');
      sb.append(Util.encodeURL(value));
    }

    for (int i = 0; i < element.getChildCount(); i++) {
      if (element.getChildType(i) == Element.ELEMENT) {
        collectFormData(element.getElement(i), sb);
      } 
    }
  }

  /**
   * Submits a form. 
   */
  private void submit() {
    Element form = findForm();
    StringBuffer buf = new StringBuffer("");
    collectFormData(form, buf);

    String url = document.getAbsoluteUrl(form.getAttributeValue("action"));
    String method = form.getAttributeValue("method");
    if (method != null && method.toLowerCase().equals("post")) {
      document.requestHandler.requestResource(document, 
          SystemRequestHandler.METHOD_POST, url, SystemRequestHandler.TYPE_DOCUMENT, 
          buf.toString().getBytes());
    } else {
      document.requestHandler.requestResource(document, 
          SystemRequestHandler.METHOD_GET, url + '?' + buf.toString(), 
          SystemRequestHandler.TYPE_DOCUMENT, null);
    }
  }

  /**
   * Avoid parent focus drawing method -- except for the debug highlight.
   */
  public void drawFocusRect(Graphics g, int dx, int dy) {
    if (HtmlWidget.debug == this) {
      super.drawFocusRect(g, dx, dy);
    }
  }
}
