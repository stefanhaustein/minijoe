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

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

import com.google.minijoe.html.uibase.GraphicsUtils;

/**
 * Helper for drawing the input controls. Ideally, parts of this would be replaces with a better
 * default style sheet.
 * 
 * @author Peter Baldwin
 * @author Stefan Haustein
 */
public class Skin {

  public static final int INPUT_TYPE_TEXT = 0;
  public static final int INPUT_TYPE_PASSWORD = 1;
  public static final int INPUT_TYPE_SUBMIT = 2;
  public static final int INPUT_TYPE_RESET = 3;
  public static final int INPUT_TYPE_BUTTON = 4;
  public static final int INPUT_TYPE_SELECT = 5;
  public static final int INPUT_TYPE_RADIOBUTTON = 6;
  public static final int INPUT_TYPE_CHECKBOX = 7;
  public static final int INPUT_TYPE_OPTION = 8;
  public static final int INPUT_TYPE_TEXTAREA = 9;
  public static final int INPUT_TYPE_IMAGE = 10;

  public static final int COLOR_WHITE = 0xffffffff;
  public static final int COLOR_MEDIUM_GRAY = 0x0ff888888;
  public static final int COLOR_BLACK = 0xff000000;

  private static final int CHECK_BLUE = 0xFF3B63A3;
  private static final int CHECK_DISABLED = 0xFFC8C8C8;
  private static final int CHECK_BOX_OUTLINE_DARK = 0xFF808080;
  private static final int CHECK_BOX_OUTLINE_LIGHT = 0xFFB3B3B3;
  private static final int CHECK_BOX_GREY_GRADIENT_DARK = 0xFF0000DE;
  private static final int CHECK_BOX_GREY_GRADIENT_LIGHT = 0xFF0000FF;

  // Colors for the various bits of an anti-aliased bubble
  // against a white background.
  private static int AA_BUBBLE_TOP_CORNER[][]
                                            = {{0xffffff, 0xf7fbff, 0xf0f3ff},
    {0xf7fbff, 0xc7ddf7, 0x76a5da},
    {0xebf3fd, 0x76a5da, 0xb2d3ee}};

  private static int AA_BUBBLE_TOP_LINES[]
                                         = {0xe7efff, 0x6192c7, 0xeaeff3};

  private static int AA_BUBBLE_SIDE_GRADIENTS[][]
                                                = {{0xe3ebfb, 0xe3e7fb},
    {0x6192c7, 0x2c5992},
    {0xe3ebf7, 0xb2d3ee}};

  private static int AA_BUBBLE_BOTTOM_CORNER[][]
                                               = {{0xe7effb, 0x4875ac, 0x85ace0},
    {0xf3f3fb, 0xb2cce8, 0x4875ac},
    {0xfbfbff, 0xebeffb, 0xd7dff3}};

  private static int AA_BUBBLE_BOTTOM_LINES[]
                                            = {0xb6d4f3, 0x2c5992, 0xd3d7f3};

  // Colors for gradient fill.
  private static int AA_BUBBLE_FILL_TOP_GRADIENT = 0xf7fbff;
  private static int AA_BUBBLE_FILL_BOTTOM_GRADIENT = 0xc7ddf7;

  private static Skin instance = new Skin();

  public static Skin get() {
    return instance;
  }

  public static void set(Skin skin) {
    instance = skin;
  }

  /**
   * Draw an antialiased bubble box, antialised for a white background. If any
   * of the colors are null then the default colors will be used.
   *
   * @param g The graphics context to render to.
   * @param x The X coordinate of the rectangle.
   * @param y The Y coordinate of the rectangle.
   * @param w The width of the rectangle.
   * @param h The height of the rectangle.
   * @param colorAABubbleTopCorner A 3x3 array of the top corner pixels' RGB
   * values.
   * @param colorAABubbleTopLines A 1x3 array of the top lines RGB values.
   * @param colorAABubbleSideGradients A 3x2 array of the side gradients's RGB
   * values.
   * @param colorAABubbleBottomCorner A 3x3 array of the bottom corner pixel RGB
   * values.
   * @param colorAABubbleBottomLines A 1x3 array of the bottom lines RGB values.
   */
  public static void drawAABubbleBox(Graphics g, int x, int y, int w,
      int h, int colorAABubbleTopCorner[][], int colorAABubbleTopLines[],
      int colorAABubbleSideGradients[][], int colorAABubbleBottomCorner[][],
      int colorAABubbleBottomLines[]) {

    for (int ypos = 0; ypos < 3; ypos++) {
      for (int xpos = 0; xpos < 3; xpos++) {
        // Top corners
        g.setColor(colorAABubbleTopCorner[ypos][xpos]);
        g.fillRect(x + xpos, y + ypos, 1, 1);
        g.fillRect(x + w - (xpos + 1), y + ypos, 1, 1);
        // Bottom corners
        g.setColor(colorAABubbleBottomCorner[ypos][xpos]);
        g.fillRect(x + xpos, y + ypos + (h - 3), 1, 1);
        g.fillRect(x + w - (xpos + 1), y + ypos + (h - 3), 1, 1);
      }
      // Top lines
      g.setColor(colorAABubbleTopLines[ypos]);
      g.fillRect(x + 3, y + ypos, w - 6, 1);
      // Bottom lines
      g.setColor(colorAABubbleBottomLines[ypos]);
      g.fillRect(x + 3, y + ypos + (h - 3), w - 6, 1);
    }
    // Side Gradients
    for (int line = 0; line < 3; line++) {
      GraphicsUtils.drawGradientVertical(
          g,
          colorAABubbleSideGradients[line][0],
          colorAABubbleSideGradients[line][1],
          x + line, y + 3, 1, h - 6);
      GraphicsUtils.drawGradientVertical(
          g,
          colorAABubbleSideGradients[line][0],
          colorAABubbleSideGradients[line][1],
          x + w - (line + 1), y + 3, 1, h - 6);
    }
  }

  /**
   * Paint a box used in CheckBox.
   */
  public static void drawCheckBox(Graphics g, int x, int y, int boxSize,
      boolean isSelected, boolean isFocusable) {
    g.setColor(COLOR_WHITE);
    g.fillRect(x, y, boxSize, boxSize);

    // Edges
    int lowerRightCorner = CHECK_BOX_OUTLINE_LIGHT;
    int upperLeftCorner = CHECK_BOX_OUTLINE_DARK;

    // Gradient fill
    int lowerRightGradient = CHECK_BOX_GREY_GRADIENT_LIGHT;
    int upperLeftGradient = CHECK_BOX_GREY_GRADIENT_DARK;

    // Set lighter colors if CheckBox is disabled
    if (!isFocusable) {
      upperLeftCorner = CHECK_BOX_OUTLINE_LIGHT;
      upperLeftGradient = CHECK_BOX_GREY_GRADIENT_LIGHT;
    }

    // Try a diagonal grey gradient
    int greySize = boxSize * 3 / 2;
    for (int i = 0; i < greySize; i++) {
      int greyShade = ((lowerRightGradient - upperLeftGradient) * i) / greySize + upperLeftGradient;
      greyShade &= 0xff;
      g.setColor((greyShade << 16) | (greyShade << 8) | greyShade);
      int iter1 = i > boxSize ? (i - boxSize) : 0;
      int iter2 = i > boxSize ? boxSize : i;
      g.drawLine(x + iter1, y + iter2, x + iter2, y + iter1);
    }

    if (isSelected) {
      g.setColor(isFocusable ? CHECK_BLUE : CHECK_DISABLED);
      drawCheck(g, x, y, boxSize);
    }

    // The box drawn is actually boxSize + 1
    // by boxSize + 1 pixels as the implementation of drawLine is inclusive.
    g.setColor(lowerRightCorner);

    g.drawLine(x, y + boxSize, x + boxSize, y + boxSize);
    g.drawLine(x + boxSize, y, x + boxSize, y + boxSize);
    g.setColor(upperLeftCorner);
    g.drawLine(x, y, x + boxSize, y);
    g.drawLine(x, y, x, y + boxSize);
  }

  /**
   * Draw a check mark with a thickness proportional to its size in the current
   * color.
   */
  public static void drawCheck(Graphics g, int x, int y, int boxSize) {
    // Draw a parallel line translated up/down in y, to simulate
    // a thicker check for varying box sizes
    int inset = boxSize < 20 ? 1 : 2;
    for (int offset = 0; offset <= (boxSize / 6); offset += 1) {
      drawSingleCheck(g, x + inset, y + inset, boxSize - 2 * inset - 1, offset);
      if (offset > 0) {
        drawSingleCheck(g, x + inset, y + inset, boxSize - 2 * inset - 1,
            -offset);
      }
    }
  }

  /**
   * Draw a check mark with a thickness of one pixel.
   */
  public static void drawSingleCheck(Graphics g, int cornerX,
      int cornerY, int boxSize, int offset) {

    // Use x, y translations to make check look centered in box
    int xTrans = -boxSize / 8;
    int yTrans = -boxSize / 4;

    // This is the lowest (shared) point within the check
    int sharedPointX = cornerX + boxSize / 2;
    int sharedPointY = cornerY + boxSize;

    // "/" line
    g.drawLine(sharedPointX + xTrans, sharedPointY + yTrans - offset, cornerX
        + boxSize + xTrans, cornerY + boxSize / 2 + yTrans - offset);
    // "\" line
    g.drawLine(sharedPointX + xTrans, sharedPointY + yTrans - offset, cornerX
        + boxSize / 4 + xTrans, cornerY + boxSize * 3 / 4 + yTrans - offset);
  }


  public int calculateWidth(int type, Font font, String text) {
    switch(type) {
      case Skin.INPUT_TYPE_CHECKBOX:
      case Skin.INPUT_TYPE_RADIOBUTTON:
        return font.getHeight() - 2;   
      case Skin.INPUT_TYPE_OPTION:
        // alternative 
        return font.getHeight() + font.stringWidth(text);
        // does not force line breaks
        //w = containerWidth; 

      case INPUT_TYPE_SELECT:
        return font.getHeight() + font.stringWidth(text) + 6;

      default:
        return 6 + font.stringWidth(text);
    }
  }

  /**
   * Calculate the height of the given UI element type for the given font.
   */
  public int calculateHeight(int type, Font font) {
    int fh = font.getHeight();
    switch(type) {
      case INPUT_TYPE_CHECKBOX:
      case INPUT_TYPE_RADIOBUTTON:
        return fh - 2;
      case INPUT_TYPE_OPTION:
        return fh;
      default:
        return fh + 6;
    }
  }

  /**
   * Draw the given UI element type witht the given parameters.
   */
  public void drawControl(Graphics g, int type, int dx, int dy, int w, int h, Font font, int color, String text, boolean selected, boolean focused) {
    int fh = font.getHeight();

    switch(type) {
      case INPUT_TYPE_OPTION:  
        g.setColor(color);
        g.setFont(font);
        g.drawString(GraphicsUtils.getFittedString(text, font, w - fh, false), 
            dx + fh, dy, Graphics.LEFT | Graphics.TOP);

        drawCheckBox(g, dx + 2, dy + 2, fh - 5, selected, true);
        if (focused) {
          drawFocusRect(g, dx + 1, dy + 1, fh - 2, fh - 2, false);
        }
        break;

      case INPUT_TYPE_CHECKBOX:
        drawCheckBox(g, dx + 1, dy + 1, w - 3, selected, true);
        if (focused) {
          drawFocusRect(g, dx, dy, w, h, false);
        }
        break;

      case INPUT_TYPE_TEXTAREA:
        drawTextBox(g, dx, dy, w, h, focused);
        g.setColor(0);
        g.setFont(font);
        int count = (h - 6) / fh;
        String [] lines = GraphicsUtils.breakUpString(font, text, w, count);
        count = Math.min(count, lines.length);
        for (int i = 0; i < count; i++) {
          g.drawString(lines[i], dx + 3, dy + 3 + i * fh, Graphics.TOP | Graphics.LEFT);
        }
        break;

      case INPUT_TYPE_TEXT:
      case INPUT_TYPE_PASSWORD:
        drawTextBox(g, dx, dy, w, h, focused);
        g.setColor(color);
        g.drawString(GraphicsUtils.getFittedString(
            type == INPUT_TYPE_PASSWORD && text.length() > 0 ? "*****" : text,  font, 
                w - 6, false), dx + 3, dy +  3, Graphics.TOP | Graphics.LEFT);
        break;

      case INPUT_TYPE_RADIOBUTTON:
        drawButton(g, dx, dy, w, h, true, focused);
        if (selected) {
          g.setColor(0);
          g.fillRoundRect(dx + 3, dy + 3, w - 7, h - 7, 4, 4);
        }
        break;

      case INPUT_TYPE_SELECT:
        drawButton(g, dx, dy, w, h, false, focused);
        g.setColor(0);
        g.setFont(font);
        g.drawString(GraphicsUtils.getFittedString(text, font, w - 3 - fh / 2, 
            false), dx + 3, dy + 3, Graphics.TOP | Graphics.LEFT);
        g.fillTriangle(dx + w - 4,  dy + h / 2 - fh / 4, 
            dx + w - 4 - fh / 2, dy + h / 2 - fh / 4, 
            dx + w - 4 - fh / 4, dy + h / 2 + fh / 4);
        break;

      case INPUT_TYPE_BUTTON:
        drawButton(g, dx, dy, w, h, true, focused);
        break;

      case INPUT_TYPE_SUBMIT:
      case INPUT_TYPE_RESET:
        drawButton(g, dx, dy, w, h, true, focused);
        g.setColor(color);
        g.setFont(font);
        g.drawString(GraphicsUtils.getFittedString(text, font, w - 6, false), 
            dx + 3, dy +  3, Graphics.TOP | Graphics.LEFT);
        break;
    }
  }

  /**
   * Draws a text input box.
   */
  public void drawTextBox(Graphics g, int dx, int dy, 
      int width, int height, boolean focus) {
    g.setColor(COLOR_WHITE);
    g.fillRect(dx + 3, dy + 3, width - 5, height - 5);

    int rightX, upperY, leftX, lowerY;

    if (focus) {
      drawFocusRect(g, dx, dy, width, height, true);
    } else {
      // Don't draw the outermost rectangle; leaving background color
      // to show through

      // Medium grey - right and bottom
      lowerY = dy + height - 2;
      upperY = dy + 1;
      rightX = dx + width - 2;
      leftX = dx + 1;
      g.setColor(0xffb3b3b3);
      g.drawLine(rightX, upperY, rightX, lowerY);
      g.drawLine(leftX, lowerY, rightX, lowerY);

      // Dark grey - top and left
      g.setColor(0xff808080);
      g.drawLine(leftX, upperY, rightX, upperY);
      g.drawLine(leftX, upperY, leftX, lowerY);

      // Light grey - inner shadow
      g.setColor(0xffe7e7e7);
      lowerY = dy + height - 3;
      upperY = dy + 2;
      rightX = dx + width - 3;
      leftX = dx + 2;
      g.drawLine(leftX, upperY, rightX, upperY);
      g.drawLine(leftX, upperY, leftX, lowerY);

      // Square in innermost corner
      g.setColor(0xffd9d9d9);
      g.drawLine(leftX, upperY, leftX, upperY);
    }
  }


  public static void drawAAGradientFilledBubbleBox(Graphics g, int x, int y, int w, int h) {
    drawAABubbleBox(g, x, y, w, h);
    // The background
    GraphicsUtils.drawGradientVertical(g, AA_BUBBLE_FILL_TOP_GRADIENT,
        AA_BUBBLE_FILL_BOTTOM_GRADIENT,
        x + 3, y + 3, w - 6, h - 6);
  }

  /**
   * Draw an antialiased bubble box, antialised for a white background. Uses
   * default antialiased colors.
   *
   * @param g The graphics context to render to.
   * @param x The X coordinate of the rectangle.
   * @param y The Y coordinate of the rectangle.
   * @param w The width of the rectangle.
   * @param h The height of the rectangle.
   */
  public static void drawAABubbleBox(Graphics g, int x, int y, int w,
      int h) {
    drawAABubbleBox(g, x, y, w, h, AA_BUBBLE_TOP_CORNER,
        AA_BUBBLE_TOP_LINES, AA_BUBBLE_SIDE_GRADIENTS,
        AA_BUBBLE_BOTTOM_CORNER, AA_BUBBLE_BOTTOM_LINES);
  }


  public void fillFocusRect(Graphics g, int x, int y, int w, int h) {
    g.setColor(0xc7ddf7);
    g.fillRect(x, y, w, h);
  }


  /**
   * Draws the focus for text fields and check boxes, 
   * but with a simplified outer frame to avoid problems on non-white
   * backgrounds.
   */
  public void drawFocusRect(Graphics g, int dx, int dy, int width, int height, boolean innerBevel) {

    g.setColor(0xff5691f0);
    g.drawRect(dx + 1, dy + 1, width - 3, height - 3);

    // Medium blue outer focus rectangle
    g.setColor(0xffc3d7f8);
    g.drawRect(dx, dy, width - 1, height - 1);

    if (innerBevel) {
      // Lighter blue inner bevel
      g.setColor(0xffd5e4fd);
      int lowerY = dy + height - 3;
      int upperY = dy + 2;
      int rightX = dx + width - 3;
      int leftX = dx + 2;
      g.drawLine(leftX, upperY, rightX, upperY);
      g.drawLine(leftX, upperY, leftX, lowerY);
      g.setColor(0xffbed5f9);
      g.drawLine(leftX, upperY, leftX, upperY);
    }
  }

  /**
   * Draws a button or radio button.
   */
  public void drawButton(Graphics g, int x, int y, int w, int h, 
      boolean threeD, boolean focus) {

    if (focus) {
      if (threeD) {
        drawAAGradientFilledBubbleBox(g, x, y, w, h);
      } else {
        drawAABubbleBox(g, x, y, w, h);
      }
    } else {
      if (threeD) {
        GraphicsUtils.drawGradientVertical(g, COLOR_WHITE, COLOR_MEDIUM_GRAY,
            x + 2, y + 2, w - 4, h - 4);
      }
      int lowerY = y + h - 2;
      int upperY = y + 1;
      int rightX = x + w - 2;
      int leftX = x + 1;
      g.setColor(0xff808080);
      g.drawLine(rightX, upperY + 1, rightX, lowerY - 1);
      g.drawLine(leftX + 1, lowerY, rightX - 1, lowerY);

      // Dark grey - top and left
      g.setColor(0xffb3b3b3);
      g.drawLine(leftX + 1, upperY, rightX - 1, upperY);
      g.drawLine(leftX, upperY + 1, leftX, lowerY - 1);
    }
  }
}
