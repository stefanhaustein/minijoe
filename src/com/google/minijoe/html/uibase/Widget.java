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

package com.google.minijoe.html.uibase;

import java.util.Vector;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;

import com.google.minijoe.html.HtmlWidget;

public class Widget {
  public static final int KEY_PRESSED = 0;
  public static final int KEY_REPEATED = 1;
  public static final int KEY_RELEASED = 2;

  public static final int POINTER_PRESSED = 3;
  public static final int POINTER_DRAGGED = 4;
  public static final int POINTER_RELEASED = 5;

  public static final int KEYCODE_UP = -1;
  public static final int KEYCODE_DOWN = -2;
  public static final int KEYCODE_LEFT = -3;
  public static final int KEYCODE_RIGHT = -4;
  public static final int KEYCODE_SELECT = -5;
  public static final int KEYCODE_LSK = -6;
  public static final int KEYCODE_RSK = -7;
  public static final int KEYCODE_CLEAR = -8;

  public static final boolean DEBUG = false;
  
  /** The widget that has the "debug focus" if debug is enabled. */
  public static Widget debug;
  
  int x;
  int y;
  int w;
  int h;
  Window window;
  Vector children;
  /** Direct child widget that has the focus or contains the focused widget */
  Widget focus;
  Widget parent;

  /**
   * Add a child widget. Calls addChild(getChildCount(), w).
   */
  public final void addChild(Widget w) {
    addChild(getChildCount(), w);
  }

  /**
   * Add a child widget at the given position. Automatically requests a repaint.
   */
  public final void addChild(int pos, Widget w) {
    if (children == null) {
      children = new Vector();
    }
    children.insertElementAt(w, pos);
    w.parent = (Widget) this;
    invalidate(false);
  }
  
  /**
   * Called by the widget management system immediately before the next draw call when 
   * invalidate(true) was called.
   * 
   * @param maxWidth The horizontal space available for layout.
   */
  public void doLayout(int maxWidth) {
    int cnt = getChildCount();
    for(int i = 0; i < cnt; i++) {
      getChild(i).doLayout(maxWidth);
    }
  }

  /**
   * Draw the content of this widget. Called by drawTree() before the child widgets are drawn.
   * 
   * @param g the graphics context for drawing
   * @param dx The x-coordinate of the upper left corner of the widget in the given graphics context
   * @param dy The y-coordinate of the upper left corner of the widget in the given graphics context
   */
  public void drawContent(Graphics g, int dx, int dy) {
  }
  
  /**
   * Calls drawContent() and then drawTree() for all child widget which are inside the given 
   * clipping region.
   * 
   * @param g The graphics context for drawing.
   * @param dx The x-coordinate of the upper left corner of the widget in the given graphics context
   * @param dy The y-coordinate of the upper left corner of the widget in the given graphics context
   * @param clipX x-coordinate of the upper left corner of the clipping window
   * @param clipY y-coordinate of the upper left corner of the clipping window
   * @param clipW width of the clipping window
   * @param clipH height of the clipping window
   */
  public void drawTree(Graphics g, int dx, int dy, int clipX, int clipY, int clipW, int clipH) {
    drawContent(g, dx, dy);
    int cnt = getChildCount();

    for(int i = 0; i < cnt; i++) {
      Widget c = getChild(i);
      int cx = dx + c.getX();
      int cy = dy + c.getY();

      if (cx < clipX + clipW && cx + c.getWidth() >= clipX && 
          cy < clipY + clipH && cy + c.getHeight() >= clipY) {
        c.drawTree(g, cx, cy, clipX, clipY, clipW, clipH);
      }
    }
  }

  /**
   * Dispatch the pointer event to the dispatchPointerEvent() method of the child containing
   * the point. If no corresponding child is found, call handlePointerEvent();
   */
  public Widget dispatchPointerEvent(int type, int x, int y) {
	if (DEBUG && type == POINTER_PRESSED && Widget.debug != null && Widget.debug.parent == this) {
	  Widget.debug = this;
	  handleDebug();
      invalidate(false);
      return this;
    }
	  
    for (int i = getChildCount() - 1; i >= 0; i--) {
      Widget c = getChild(i);
      int cx = c.getX();
      int cy = c.getY();
      if (x >= cx && x < cx + c.getWidth() && y >= cy && y < cy + c.getHeight()) {
        c = c.dispatchPointerEvent(type, x - cx, y - cy);
        if (c != null) {
          return c;
        }
      }
    }
    
    if (DEBUG) {
      if (type == POINTER_PRESSED) {
        Widget.debug = this;
        handleDebug();
        invalidate(false);
      }
      return this;
    }
    return handlePointerEvent(type, x, y) ? this : null;
  }

  /**
   * Dispatch a key event to the handler, walking along the focus chain.
   * @param type the key event type (KEY_PRESSED, KEY_RELEASED, KEY_REPEATED).
   * @param keyCode the code point for the character corresponding to the key pressed. Negative
   *    if there is no printable character associated with this key.
   * @param action the "game action" associated with this key event
   * @return the widget that handled the event, or null if no widget handled the event.
   */
  public Widget dispatchKeyEvent(int type, int keyCode, int action) {
    if (focus == null) {
      return null;
    }
    if (focus == this) {
      return handleKeyEvent(type, keyCode, action) ? this : null;   
    }
    return focus.dispatchKeyEvent(type, keyCode, action); 
  }

  /**
   * Find a focusable widgets in the given window.
   * 
   * @param wx x-coordinate of the top left corner
   * @param wy y-coordinate of the top left corner
   * @param ww window width
   * @param wh window height
   * @param result focusable widgets will be added to this vector.
   */
  public void findFocusableWidgets(int wx, int wy, int ww, int wh, Vector result) {	
    int cnt = getChildCount();
    for (int i = 0; i < cnt; i++) {
      Widget c = getChild(i);
      int cx = c.getX();
      int cy = c.getY();
      if (cx + c.getWidth() >= wx && cx <= wx + ww && 
          cy + c.getHeight() >= wy && cy <= wy + wh) {
        if (c.isFocusable()) {
          result.addElement(c);
        }
        c.findFocusableWidgets(wx - cx, wy - cy, ww, wh, result);
      }
    }
  }

  public final Widget getChild(int i) {
    return (Widget) children.elementAt(i);
  }

  public final int getChildCount() {
    return children == null ? 0 : children.size();
  }

  public Widget getFocusedWidget() {
    if (focus == null) {
      return null;
    }
    return focus == this ? this : focus.getFocusedWidget();
  }

  public Widget getParent() {
    return parent;
  }

  public final Widget getRoot() {
    Widget p = parent;
    if (p == null) {
      return this;
    }
    while (true) {
      Widget pp = p.getParent();
      if (pp == null) {
        break;
      }
      p = pp;
    }
    return p;
  }
    
  public final int getWidth() {
    return w;
  }

  public final int getHeight() {
    return h;
  }

  public final int getX() {
    return x;
  }

  public final int getY() {
    return y;
  }

  /**
   * Returns the coordinates of this widget, relative to the given root widget, in the given
   * array.
   */
  public void getRelativeCoords(Widget root, int[] coords) {
    if (this == root) {
      coords[0] = 0;
      coords[1] = 0;
    } else {
      getParent().getRelativeCoords(root, coords);
      coords[0] += x;
      coords[1] += y;
    }
  }
  
  protected void handleDebug() {
  }
  
  /**
   * Overwrite this method to react to focus changes.
   */
  protected void handleFocusChange(boolean focused) {
  }

  /**
   * Overwrite this method to react to pointer events. Default implementation focuses on press and
   * then sends a select key event on release.
   */
  public boolean handlePointerEvent(int type, int x, int y) {
    if (isFocusable()) {
      if (type == POINTER_PRESSED) {
        requestFocus();
      } else if (type == POINTER_RELEASED) {
        getRoot().dispatchKeyEvent(KEY_PRESSED, KEYCODE_SELECT, Canvas.FIRE);
      }
      return true;
    }
    return false;
  }

  /**
   * Overwrite this method to react to key events.
   * 
   * @param type one of KEY_PRESSED, KEY_RELEASED and KEY_REPEATED
   * @param keyCode the code point of the character corresponding to the key. Will be negative
   *   if there is no printable character corresponding to this key (e.g. KEYCODE_LEFT)
   * @param action The action code associated with this key (cf Canvas documentation).
   * @return true if the event has been consumed.
   */
  public boolean handleKeyEvent(int type, int keyCode, int action) {
    return false;
  }

  /**
   * Returns true if this widget has the input focus.
   */
  public boolean isFocused() {
    return focus == this;
  }  

  /**
   * Overwrite this method returning true in order to make a widget focusable.
   */
  public boolean isFocusable() {
    return false;
  }

  /**
   * Notifies the widget system that this widget needs redrawing. If the layout parameter is set,
   * a re-layout is requested, too.
   */
  public void invalidate(boolean layout) {
    if (parent != null) {
      parent.invalidate(layout);
    } else if (window != null) {
      window.invalidate(layout);
    }
  }


  /**
   * Finds the index for the given child widget. Returns -1 if not found.
   */
  public final int indexOfChild(Widget widget) {
    return children == null ? -1 : children.indexOf(widget);
  }

  /**
   * Removes all child widgets.
   */
  public final void removeAllChildren() {
    children = null;
    invalidate(false);
  }

  /**
   * Request the focus for this widget. Also tries to scroll the widget into view.
   */
  public void requestFocus() {
    Widget root = getRoot();
    Widget old = root.getFocusedWidget();
    if (old != null) {
      old.setFocused(false);
    }
    setFocused(true);
  }
  
  /**
   * Update the focus chain and call handleFocusChange.
   */
  final void setFocused(boolean focused) {
    Widget current = this;
    while (current.parent != null) {
      current.parent.focus = focused ? current : null;
      current = current.parent;
    }
    focus = focused ? this : null;
    handleFocusChange(focused);
  }

  public void scrollIntoView() {
    scrollTo(0, 0, getWidth(), getHeight());
  }
  
  public void scrollTo(int x, int y, int w, int h) {
    if (parent != null) {
      parent.scrollTo(x + getX(), y + getY(), w, h);
    }
  }
  
  /** 
   * Sets the position and size of this widget.
   */
  public void setDimensions(int x, int y, int w, int h) {
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
  }

  /**
   * Set the Window for this widget.
   */
  public void setWindow(Window window) {
    this.window = window;
  }

  /**
   * Set the x-position, relative to the parent.
   */
  public final void setX(int x) {
    this.x = x;
  }

  /**
   * Set the y-position, relative to the parent.
   */
  public final void setY(int y) {
    this.y = y;
  }

  /**
   * Set the width of this widget.
   */
  public final void setWidth(int w) {
    this.w = w;
  }

  /**
   * Set the height of this widget.
   */
  public final void setHeight(int h) {
    this.h = h;
  }


}
