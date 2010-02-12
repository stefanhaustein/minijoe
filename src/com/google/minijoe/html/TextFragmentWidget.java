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
import com.google.minijoe.html.uibase.Widget;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/**
 * Widget representing a (HTML) text fragment.
 * 
 * TODO: Consider caching all line breaking opportunities in a string
 * buffer or array
 * 
 * @author Stefan Haustein
 */
public class TextFragmentWidget extends Widget {
  
  /** Element this widget corresponds to */
  Element element;
  private boolean focusable;
  String text;
  short[] indices;
  private int firstLineYOffset;
  private int mainYOffset;
  private int lastLineYOffset;

  /**
   * Creates a new TextFragmentWidget
   */
  public TextFragmentWidget(Element element, String text, boolean focusable) {
    this.element = element;
    this.text = text;
    this.focusable = focusable;
    if (focusable && isFocused()) {
      element.setFocused();
    }
  }
  
  /**
   * This widget can be traversed.
   */
  public boolean isFocusable() {
    return focusable;
  }

  /**
   * Returns the number of lines for the current layout state. If called before
   * the first call of doLayout(), 1 is returned. 
   */
  public int getLineCount() {
    return indices == null ? 1 : indices.length / 3;
  }

  /**
   * Returns the width of the nth line in pixels.
   */
  public int getLineWidth(int n) {
    return indices == null ? getFont().stringWidth(text) : 
      getFont().substringWidth(text, indices[n * 3], indices[n * 3 + 1]);
  }

  /**
   * Returns the GoogleFont associated with the computed style for the element
   * owning this text fragment.
   */
  public Font getFont() {
    return element.getComputedStyle().getFont();
  }

  /**
   * Layouts this text fragment.
   * 
   * Precondition: x, y, w are set correctly.
   * 
   * @param index index of this widget in the parent widget
   * @param x0 start position for the first character, relative to getX()
   * @param breakPos first line break position. 
   * @param lineHeight height of the current line
   */
  public int doLayout(int myIndex, LayoutContext borders, int breakPos, 
      int lineStartIndex, int insertionIndex) {

    firstLineYOffset = 0;
    mainYOffset = 0;
    lastLineYOffset = 0;
    
    BlockWidget parent = (BlockWidget) getParent();

    Font font = getFont();
    int maxWidth = getWidth();
    int fontHeight = font.getHeight();

    int availableWidth = borders.getHorizontalSpace(fontHeight);

    // breakpos invalid?
    if (breakPos == -1) {
      breakPos = Math.max(0, 
          findBreakPosition(parent, myIndex, 0, availableWidth, 
              availableWidth == maxWidth));
    }
    
    int len = text.length();
    
    if (breakPos > len) {
      int w = Math.min(font.stringWidth(text), maxWidth);
      borders.placeBox(w, fontHeight, Style.NONE, 0);
      
      indices = null;
      setX(getX() + borders.getBoxX());
      setWidth(w);
      setHeight(fontHeight);
      return breakPos - len;
    }

    StringBuffer buf = new StringBuffer();
    int lastBreak = 0;

    int h = Math.max(borders.getLineHeight(), fontHeight);
    borders.setLineHeight(h);
    firstLineYOffset = borders.getAdjustmentY(h - fontHeight);
    mainYOffset = h - fontHeight - firstLineYOffset;
    
    do {
      int end = breakPos;
      if (end > lastBreak && text.charAt(end - 1) <= ' ') {
        end--;
      }

      buf.append((char) lastBreak);
      buf.append((char) (end - lastBreak));
      
      int w = Math.min(font.substringWidth(text, lastBreak, end - lastBreak), 
          maxWidth);
      
      if (lineStartIndex != insertionIndex) {
        ((BlockWidget) getParent()).adjustLine(lineStartIndex, 
            insertionIndex, borders);
        lineStartIndex = insertionIndex;
      }
      
      borders.placeBox(w, fontHeight, borders.getLineHeight(), 0);
      buf.append((char) (borders.getBoxX() + 
          borders.getAdjustmentX(availableWidth - w)));
      borders.advance(borders.getLineHeight());
      
      lastBreak = breakPos;
      
      availableWidth = borders.getHorizontalSpace(fontHeight);
      breakPos = Math.max(lastBreak, 
          findBreakPosition(parent, myIndex, lastBreak, availableWidth, 
          availableWidth == maxWidth));
      
      h += fontHeight;
    } while (breakPos <= len);

    buf.append((char) lastBreak);
    buf.append((char) (text.length() - lastBreak));
    int w = Math.min(font.substringWidth(text, lastBreak, 
        text.length() - lastBreak), borders.getHorizontalSpace(fontHeight));
    
    borders.placeBox(w, fontHeight, Style.NONE, 0);
    buf.append((char) borders.getBoxX());
    
    indices = new short[buf.length()];
    for (int i = 0; i < indices.length; i++) {
      indices[i] = (short) buf.charAt(i);
    }
    
    setHeight(h);
    
    return breakPos - len;
  }

  /**
   * Draws the text.
   */
  public void drawContent(Graphics g, int dx, int dy) {
    // System.out.println("drawing tfw: "+text);
    Style style = element.getComputedStyle();
    Font font = style.getFont();
    int textColor = style.getValue(Style.COLOR);
    if (isFocused() && !element.isFocused()) {
      element.setFocused();
    }
    boolean focus = element.isFocused();

    if (indices == null) {
      if (focus) {
    	Skin.get().fillFocusRect(g, dx, dy, 
            font.stringWidth(text), font.getHeight());
      }
      g.setColor(textColor);
      g.setFont(font);
      g.drawString(text, dx, dy, Graphics.TOP | Graphics.LEFT);
    } else {
      int y = dy + firstLineYOffset;
      int clipY = g.getClipY();
      int clipH = g.getClipHeight();
      int fh = font.getHeight();

      for (int i = 0; i < indices.length; i += 3) {
        if (clipY < y + fh &&  y < clipY + clipH) {
          int start = indices[i];
          int len = indices[i + 1];

          if (focus) {
            Skin.get().fillFocusRect(g, dx + indices[i + 2], y, 
                font.substringWidth(text, start, len), font.getHeight());
          }
          g.setColor(textColor);
          g.setFont(font);
          g.drawSubstring(text, start, len, dx + indices[i + 2], y, Graphics.TOP | Graphics.LEFT);
        }
        y += fh;
        if (i == 0) {
          y += mainYOffset;
        } 
        if (i == indices.length - 6) {
          y += lastLineYOffset;
        } 
      }
    }
    
    if (HtmlWidget.debug == this) {
      g.setColor(0x00ff00);
      g.drawRect(dx, dy, getWidth() - 1, getHeight() - 1);
      if (getLineCount() > 1) {
       
        int fh = font.getHeight();
        int y = dy;
        g.setColor(0x0ff0000);
        g.drawLine(dx, y, dx, y + firstLineYOffset);
        y += firstLineYOffset;
        
        for (int i = 0; i < indices.length; i += 3) {
          g.setColor(0x0000ff);
          g.drawRect(dx + indices[i + 2], y, 
              font.substringWidth(text, indices[i], indices[i + 1]), fh - 1);
          y += fh;
          if (i == 0) {
            g.setColor(0x0ff0000);
            g.drawLine(dx + 2, y, dx + 2, y + mainYOffset);
            y += mainYOffset;
          } 
          if (i == indices.length - 6) {
            g.setColor(0x0ff0000);
            g.drawLine(dx + 4, y, dx + 4, y + lastLineYOffset);
            y += lastLineYOffset;
          }
        }
      }
    }
  }

  /**
   * Finds a suitable line break position in a run of TextFragmentWidgets,
   * starting at character index startCharIndex (relative to the start of
   * the first child). Assumes no kerning for performance reasons,
   * which may result in breaking the line slightly too early.
   * 
   * @param myIndex the child index of this text fragment in the parent widget
   * @param startCharIndex start character index
   * @param maxWidth maximum line width
   * @param force if true, the next break position is returned, even if it does
   *        not fit within maxWidth
   * @return character index for break (first character on new line); May be 
   *         outside of this TextFragmentWidget;
   *         Integer.MAX_VALUE if no break is necessary;
   *         Integer.MIN_VALUE if force is false and no suitable break position
   *         could be found
   */
  final int findBreakPosition(BlockWidget parent,
      int myIndex, int startCharIndex, int maxWidth, boolean force) {    
    int len = text.length();
    
    if (startCharIndex >= len) {
      TextFragmentWidget next = getNextSibling(parent, myIndex);
      if (next == null) {
        return Integer.MAX_VALUE;
      }
      int result = next.findBreakPosition(parent,
          myIndex + 1, startCharIndex - len, maxWidth, force);
      
      return result > Integer.MIN_VALUE && result < Integer.MAX_VALUE 
          ? result + len : result;
    }
    char startChar = text.charAt(startCharIndex);
    return findBreakPosition(parent, myIndex, startChar, startCharIndex + 1, 
        getFont().charWidth(startChar),  
        force ? Integer.MAX_VALUE : Integer.MIN_VALUE, maxWidth);
  }
  
  /** 
   * Internal helper method for the package visible method with the same name.
   * Delegates break position search to next sibling if necessary.
   */
  private final int findBreakPosition(BlockWidget parent, int myIndex, 
      char currentChar, int nextCharIndex, int w, int bestPos, int maxWidth) {
    int[] widths = element.getComputedStyle().getCharWidths();
    Font font = getFont();
    int len = text.length();
    
    while (true) {
      if (currentChar == '\n') {
        return nextCharIndex;
      }
      if (w > maxWidth && bestPos != Integer.MAX_VALUE) {
        return bestPos;
      }
      if (nextCharIndex >= len) {
        break;
      }
      char nextChar = text.charAt(nextCharIndex);
      if (Util.canBreak(currentChar, nextChar)) {
        bestPos = nextCharIndex;
      }
      nextCharIndex++;
      currentChar = nextChar;
      // we do not consider kerning for performance reasons. This may result in 
      // breaking the line slightly too early, which should be fine.
      // TODO(haustein) consider summing up word lengths instead
      w += currentChar < widths.length ? widths[currentChar] : 
        font.charWidth(currentChar);
    }

    TextFragmentWidget next = getNextSibling(parent, myIndex);
    if (next == null) {
      return w <= maxWidth ? Integer.MAX_VALUE : bestPos;
    }
    int result = next.findBreakPosition(parent,
        myIndex + 1, currentChar, nextCharIndex - len, w, 
        bestPos == Integer.MIN_VALUE || bestPos == Integer.MAX_VALUE 
        ? bestPos : bestPos - len, maxWidth);
    
    return Integer.MIN_VALUE < result && result < Integer.MAX_VALUE 
        ? result + len : result;
  }
  
  /** 
   * Returns the next TextFragmentWidget sibling or null, if the next widget
   * is not an instance of TextFragmentWidget or this widget is the last child.
   *  
   * @param myIndex the index of this TextFragmentWidget in the parent widget
   * @return the next TextFragmentWidget if available; otherwise null
   */
  TextFragmentWidget getNextSibling(BlockWidget parent, int myIndex) {
   
    Util.assertTrue(parent.children.elementAt(myIndex) == this);
    if (myIndex + 1 >= parent.children.size()) {
      return null;
    }
    Object next = parent.children.elementAt(myIndex + 1);
    return next instanceof TextFragmentWidget  
        ? (TextFragmentWidget) next : null;
  }
  
  protected void handleFocusChange(boolean focused) {
    if (isFocusable() && focused) {
      element.setFocused();
    }
    invalidate(false);
  }

  /**
   * Returns the element owning the text fragment.
   */
  public Element getElement() {
    return element;
  }

  /**
   * Adjusts the vertical and horizontal alignment for the last line.
   * 
   * @param indent horizontal indent for the last line
   * @param lineH line height of the last line
   * @param context current LayoutContext
   */
  public void adjustLastLine(int indent, int lineH, LayoutContext context) {
    lastLineYOffset = context.getAdjustmentY(lineH - getFont().getHeight());
    setHeight(getHeight() + lastLineYOffset);
    indices[indices.length - 1] += indent;
  }

  /**
   * Dumps members for debugging.
   */
  public void printDebugInfo() {
    element.dumpPath();
    System.out.print("Style: ");
    element.getComputedStyle().dump();
    
    System.out.println();
    System.out.println("x: " + getX() + " y: " + getY() + 
        " w: " + getWidth() + " h: " + getHeight());
    System.out.println("lh " + getFont().getHeight() + 
        " YOffs0: " + firstLineYOffset + " mainYOffs: " + mainYOffset + 
        " lastYOffs: " + lastLineYOffset);
  }

  public void doLayout(int maxWidth) {
	// Do nothing -- handled by BlockWidget.
  }
}
