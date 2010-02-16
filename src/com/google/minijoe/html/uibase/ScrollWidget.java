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

public class ScrollWidget extends Widget {

  private static final int INITIAL_SCROLL_PX = 8;
  private static final int REPEATED_SCROLL_PX = INITIAL_SCROLL_PX * 4;
  private static final int ANIMATION_TIME = 300;

  private static final int VISIBILITY_NONE = 0;
  private static final int VISIBILITY_PART = 1;
  private static final int VISIBILITY_FULL = 2;

  private static final int FOCUS_CLOSEST = 0;
  private static final int FOCUS_NEXT_DOWN = 1;
  private static final int FOCUS_NEXT_UP = 2;
  private static final int FOCUS_NEXT_LEFT = 3;
  private static final int FOCUS_NEXT_RIGHT = 4;

  private int x0;
  private int y0;
  private int xMax;
  private int yMax;

  private int displayX0;
  private int displayY0;
  private int animationStartX0;
  private int animationStartY0;
  private long targetTime;
  private int dragX0;
  private int dragY0;
  private boolean dragging;

  public Widget dispatchKeyEvent(int type, int keyCode, int action) {
    int delta = INITIAL_SCROLL_PX;
    switch(type) {
      case KEY_RELEASED:
        return super.dispatchKeyEvent(type, keyCode, action);
      case KEY_REPEATED:
        delta = REPEATED_SCROLL_PX;
        break;  
    }

    switch(action) {
      case Canvas.DOWN:
        move(0, delta, true);
        break;
      case Canvas.UP:
        move(0, -delta, true);
        break;
      case Canvas.LEFT:
        move(-delta, 0, true);
        break;
      case Canvas.RIGHT:
        move(delta, 0, true);
        break;
      default:
        return super.dispatchKeyEvent(type, keyCode, action);
    }
    invalidate(false);
    return this;
  }

  public Widget dispatchPointerEvent(int type, int px, int py) {
    if (type == POINTER_PRESSED) {
      dragX0 = px;
      dragY0 = py;
      dragging = false;
    } 
    
    if (type == POINTER_DRAGGED) {
      int dx = dragX0 - px;
      int dy = dragY0 - py;
      
      if (dragging || dx * dx + dy * dy > 10) {
        dragging = true;
        move(dragX0 - px, dragY0 - py, false);
        
        dragX0 = px;
        dragY0 = py;
      }
      return this;
    } else if (!dragging) {
      return super.dispatchPointerEvent(type, px + displayX0, py + displayY0);
    } else {
      return null;
    }
  }
  
  public void drawTree(Graphics g, int dx, int dy, int clipX, int clipY, int clipW, int clipH) {
    g.setColor(0xffffff);
    g.fillRect(dx, dy, w, h);

    g.clipRect(dx, dy, getWidth(), getHeight());

    long t = System.currentTimeMillis();
    if (t < targetTime) {
      int percent = (int) (100 - ((targetTime - t) * 100) / ANIMATION_TIME);
      percent = (10000 - (100-percent) * (100-percent)) / 100;

      displayX0 = (animationStartX0 * (100-percent) + x0 * percent) / 100;
      displayY0 = (animationStartY0 * (100-percent) + y0 * percent) / 100;
      invalidate(false);
    } else {
      displayX0 = x0;
      displayY0 = y0;
    }

    super.drawTree(g, dx - displayX0, dy - displayY0, 
        g.getClipX(), g.getClipY(), g.getClipWidth(), g.getClipHeight());
    g.setClip(clipX, clipY, clipW, clipH);
  }

  public void getRelativeCoords(Widget root, int[] coords) {
    super.getRelativeCoords(root, coords);
    coords[0] -= x0;
    coords[1] -= y0;
  }

  int isVisible(Widget other) {
    int[] coords = new int[2];
    other.getRelativeCoords(this, coords);

    int otherX = coords[0];
    int otherY = coords[1];

    if (otherX + other.getWidth() >= 0 && otherX < getWidth() &&
        otherY + other.getHeight() >= 0 && otherY < getHeight()) {

      // at least partially visible
      return otherX >= 0 && otherX + other.getWidth() <= getWidth() && 
      otherY >= 0 && otherY + other.getHeight() <= getHeight() ? 
          VISIBILITY_FULL : VISIBILITY_PART;
    } 
    return VISIBILITY_NONE;
  }
  
  public void scrollTo(int targetX, int targetY, int targetW, int targetH) {
    if (targetX < x0) {
      x0 = targetX;
    } else if (targetX + targetW > x0 + getWidth()) {
      x0 = targetX + targetW - getWidth();
    }

    if (targetY < y0) {
      y0 = targetY;
    } else if (targetY + targetH > y0 + getHeight()) {
      y0 = targetY + targetH - getHeight();
    }
    updateAnimation();
  }


  private void updateAnimation() {
    if (x0 != displayX0 || y0 != displayY0) {
      animationStartX0 = displayX0;
      animationStartY0 = displayY0;
      targetTime = System.currentTimeMillis() + ANIMATION_TIME;
      invalidate(false);
    }
  }

  private void move(int dx, int dy, boolean checkFocus) {
    Widget focus = getFocusedWidget();
    if (focus != this && focus != null && checkFocus) {
      int[] coords = new int[2];
      focus.getRelativeCoords(this, coords);

      int dir = FOCUS_CLOSEST; // should not happen 
      if (dy < 0) {
        dir = FOCUS_NEXT_UP;
      } else if (dy > 0) {
        dir = FOCUS_NEXT_DOWN;
      } else if (dx < 0) {
        dir = FOCUS_NEXT_LEFT;
      } else if (dx > 0) {
        dir = FOCUS_NEXT_RIGHT;
      }

      if (focusNext(coords[0], coords[1], dir)) {
        if (dy != 0) {
          focus = getFocusedWidget();
          focus.getRelativeCoords(this, coords);
          scrollTo(x0, y0 + coords[1], 0, focus.getHeight());
        }
        return;
      }
    }

    x0 += dx;
    y0 += dy;

    if (y0 + getHeight() > yMax) {
      y0 = yMax - getHeight();
    }
    if (y0 < 0) {
      y0 = 0;
    } 
    if (x0 + getWidth() > xMax) {
      x0 = xMax - getWidth();
    }
    if (x0 < 0) {
      x0 = 0;
    } 

    if (checkFocus && focus == null || focus == this || isVisible(focus) == VISIBILITY_NONE) {
      if (dy > 0) {
        focusNext(0, Integer.MAX_VALUE >> 2, FOCUS_NEXT_UP);
      } else if (dy < 0) {
        focusNext(0, Integer.MIN_VALUE >> 2, FOCUS_NEXT_DOWN);
      } else {
        focusNext(getWidth() / 2, getHeight() / 2, FOCUS_CLOSEST);
      }
    }

    updateAnimation();
  }

  public boolean focusNext(int x, int y, int dir) {
    Vector focusable = new Vector();
    findFocusableWidgets(x0, y0, getWidth(), getHeight(), focusable);
    int cnt = focusable.size(); 
    int[] coords = new int[2];

    Widget best = null;
    int bestDx = 0;
    int bestDy = 0;
    int bestDq = 0;

    for (int i = 0; i < cnt; i++) {
      Widget cand = (Widget) focusable.elementAt(i);
      cand.getRelativeCoords(this, coords);

      int dx = coords[0] - x;
      int dy = coords[1] - y;

      switch(dir) {
        case FOCUS_CLOSEST:
          dx += cand.getWidth() / 2;
          dy += cand.getHeight() /2;
          int dq = dx * dx + dy * dy;
          if (best == null || dq < bestDq) {
            best = cand;
            bestDq = dq;
          }
          break;
        case FOCUS_NEXT_DOWN:
          if (dy > 0 && (best == null || dy < bestDy || (dy == bestDy && dx < bestDx))) {
            best = cand;
            bestDx = dx;
            bestDy = dy;
          }
          break;
        case FOCUS_NEXT_UP:
          if (dy < 0 && (best == null || dy > bestDy || (dy == bestDy && dx > bestDx))) {
            best = cand;
            bestDx = dx;
            bestDy = dy;
          }
          break;
        case FOCUS_NEXT_RIGHT:
          if (dy == 0 && dx > 0 && (best == null || dx < bestDx)) {
            best = cand;
            bestDx = dx;
            bestDy = dy;
          }
          break;

        case FOCUS_NEXT_LEFT:
          if (dy == 0 && dx < 0 && (best == null || dx > bestDx)) {
            best = cand;
            bestDx = dx;
            bestDy = dy;
          }
          break;
      }
    }
    if (best != null) {
      best.requestFocus();
      return true;
    }
    return false;
  }

  public void doLayout(int maxWidth) {
    int cnt = getChildCount();
    xMax = 0;
    yMax = 0;
    int y = 0;
    for (int i = 0; i < cnt; i++) {
      Widget w = getChild(i);
      w.setX(0);
      w.setY(y);
      w.doLayout(maxWidth);
      y += w.getHeight();
      xMax = Math.max(xMax, w.getX() + w.getWidth());
      yMax = Math.max(yMax, w.getY() + w.getHeight());
    }

    // If there is too much space, stretch the last item.
    if (y < getHeight() && getChildCount() != 0) {
      Widget c = getChild(getChildCount() - 1);
      c.setHeight(c.getHeight() + (getHeight() - y));
    }
    
    if(focus == null || focus == this) {
      focusNext(-1, -1, FOCUS_NEXT_DOWN);
    }
  }

  /**
   * If none of the contained elements can take the focus, the scroll widget still needs to get
   * the key events. So we need to make it focusable.
   */
  public boolean isFocusable() {
    return true;
  }

  //  @Override
  //  public void doLayout(int maxWidth) {
  //	super.doLayout(maxWidth)
  //  }
}
