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

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import com.google.minijoe.common.Util;

/**
 * Graphics related static helper methods.
 * 
 * @author Peter Baldwin
 * @author Stefan Haustein
 * @author Erik Wolsheimer
 */
public class GraphicsUtils {

  /**
   * Flag for scaling an image with low quality and without preserving
   * transparency
   */
  public static final int SCALE_SIMPLE = 0;

  /**
   * Flag for high quality scaling. May be combined with PROCESS_ALPHA.
   */
  public static final int SCALE_HIGH_QUALITY = 1;

  /**
   * Flag for taking transparency into account when scaling images.
   */
  public static final int SCALE_PROCESS_ALPHA = 2;

  /**
   * Possibly truncate the given string with ellipsesString, so that it fits
   * within the given width.  The ellipsesString can be changed with
   * {@link #setEllipsesString}.  (Default: "...");
   *
   * @param str - string to fit
   * @param f - font to use
   * @param width - width to fit this string in
   * @param suppressMultipleSpaces if true, eliminate multiple spaces from the
   *          output
   * @return a string appropriate for displaying within the given width
   */
  public static String getFittedString(String str, Font f, int width,
      boolean suppressMultipleSpaces) {

    String ellipses = "...";

    if (str.length() == 0 || width == 0 || f.stringWidth(str) <= width) {
      // Short circuit - it fits, so go no further
      return str;
    }

    // If here, we're going to have to truncate with ellipsis.
    int numChars = str.length();
    // If ellipsesString is too long to fit, we should truncate it.
    if (ellipses.length() > str.length()) {
      ellipses = ellipses.substring(0, str.length());
    }

    int formatToWidth = width - f.stringWidth(ellipses);
    StringBuffer fittedStr = new StringBuffer("");
    int i = 0;
    boolean spaceAdded = false;

    // Build up a string from 0 to n chars such that the final character
    // added puts it over the allotted width for non-ellipsis characters.
    // At that point, delete the offending char and we're done.

    // This is superior to our old method which may have truncated prematurely
    // because ellipsis characters ... are typically much narrower than the
    // characters they were replacing.
    for (;;) {
      if (i >= numChars - 1) {
        // If we've traversed the entire string, go no further. (This
        // shouldn't happen due to short circuiting for strings that fit, but
        // we really don't want an infinite loop).
        break;
      }

      char examinedChar = str.charAt(i);
      if (suppressMultipleSpaces) {
        if (examinedChar == ' ') {
          if (!spaceAdded) {
            spaceAdded = true;
            fittedStr.append(' ');
          } // Else suppress additional spaces in a row
        } else {
          spaceAdded = false;
          fittedStr.append(examinedChar);
        }
      } else {
        // Always append
        fittedStr.append(examinedChar);
      }
      if (f.stringWidth(fittedStr.toString()) > formatToWidth) {
        // The last char we added put it over the target length, so delete it
        fittedStr.deleteCharAt(fittedStr.length() - 1);
        break;
      }
      i += 1;
    }

    // If here, we had to truncate so always add the ellipsis.
    return fittedStr.append(ellipses).toString();
  }

  /**
   * Helper method to extract a color value from a pixel array representing an
   * RGB image. For pixels outside the image, the value of the nearest
   * neighbor inside the image is returned.
   *
   * @param rgb The RGB pixel array
   * @param w Scan line length of the pixel array
   * @param w height of the pixel array
   * @param x x position inside the source image
   * @param y y position inside the source image
   * @return the color value
   */
  public static int getColor(int[] rgb, int w, int h, int x, int y) {
    if (x < 0) {
      x = 0;
    } else if (x >= w) {
      x = w -1;
    }
    if (y < 0) {
      y = 0;
    } else if (y >= h) {
      y = h - 1;
    }
    return rgb[x + (y * w)];
  }

  /**
   * This method fills a scan line of the destination RGB array with data from a
   * source RGB array, both representing images. This method uses 16 bit fixed
   * point numbers for coordinates (multiply by 65536 or shift left 16 bit to
   * convert from integers). This method can be used to perform linear
   * transformations on images. It goes through the source array starting at
   * (srcX, srcY), then adding (srcDx, srcDy) for each pixel in the destination
   * scan line.
   *
   * @param srcRGB The source RGB array
   * @param srcW The length of a scan line in the source RGB array
   * @param srcH The height of the image represented by the source RGB array
   * @param srcX The start x position in the source image in 16 bit fix point format. 
   *    May be outside of the source image.
   * @param srcY The start y position in the source image in 16 bit fix point format.
   * @param srcDx The value that is added to srcX each step in 16 bit fix point format.
   * @param srcDy The value that is added to srcY each step in 16 bit fix point format
   * @param dstRGB The destination RGB array
   * @param dstPos The start array index in the destination RGB array
   * @param dstLen
   * @param highQ Set to true for high quality transformations
   */
  public static void transformScanline(int[] srcRGB, int srcW, int srcH, int srcX, int srcY,
      int srcDx, int srcDy, int[] dstRGB, int dstPos, int dstLen, int flags) {

    boolean processAlpha = (flags & SCALE_PROCESS_ALPHA) != 0;

    if ((flags & SCALE_HIGH_QUALITY) != 0) {

      int xSteps = 1 + (srcDx >> 16);
      int ySteps = 1 + (srcDy >> 16);

      int subDx = srcDx / xSteps;
      int subDy = srcDy / ySteps;

      int div = xSteps * ySteps;

      while (dstLen-- > 0) {

        int alpha = 0;
        int red = 0;
        int green = 0;
        int blue = 0;

        int saveSrcY = srcY;
        for(int i = 0; i < ySteps; i++) {
          int saveSrcX = srcX;
          for(int j = 0; j < xSteps; j ++) {
            int srcXI = srcX >> 16;
          int srcYI = srcY >> 16;

          int c00 = getColor(srcRGB, srcW, srcH, srcXI, srcYI);
          int c10 = getColor(srcRGB, srcW, srcH, srcXI + 1, srcYI);
          int c01 = getColor(srcRGB, srcW, srcH, srcXI, srcYI + 1);
          int c11 = getColor(srcRGB, srcW, srcH, srcXI + 1, srcYI + 1);

          int fx = (srcX & 0x0ffff) >> 8;
          int fy = (srcY & 0x0ffff) >> 8;

          int f00 = (256 - fx) * (256 - fy);
          int f10 = (fx) * (256 - fy);
          int f01 = (256 - fx) * (fy);
          int f11 = (fx) * (fy);

          int r00 = (c00 >> 16) & 255;
          int g00 = (c00 >> 8) & 255;
          int b00 = (c00) & 255;

          int r10 = (c10 >> 16) & 255;
          int g10 = (c10 >> 8) & 255;
          int b10 = (c10) & 255;

          int r01 = (c01 >> 16) & 255;
          int g01 = (c01 >> 8) & 255;
          int b01 = (c01) & 255;

          int r11 = (c11 >> 16) & 255;
          int g11 = (c11 >> 8) & 255;
          int b11 = (c11) & 255;

          // results are in the range 0.. 256*256*256

          red += r00 * f00 + r01 * f01 + r10 * f10 + r11 * f11;
          green += g00 * f00 + g01 * f01 + g10 * f10 + g11 * f11;
          blue += b00 * f00 + b01 * f01 + b10 * f10 + b11 * f11;

          if (processAlpha){
            int a00 = (c00 >> 24) & 255;
            int a10 = (c10 >> 24) & 255;
            int a01 = (c01 >> 24) & 255;
            int a11 = (c11 >> 24) & 255;
            alpha = a00 * f00 + a01 * f01 + a10 * f10 + a11 * f11;
          }

          srcX += subDx;
          }
          srcX = saveSrcX;
          srcY += subDy;
        }
        int color =
          ((red / div) & 0x0ff0000) |
          (((green / div) >> 8) & 0x0ff00) | (((blue / div) >> 16) & 0x0ff);

        dstRGB[dstPos++] = (processAlpha ? (((alpha / div) << 8) & 0x0ff000000)
            : 0x0ff000000) | color;

        srcX += srcDx;
        srcY = saveSrcY + srcDy;
      }
    } else {
      while (dstLen-- > 0) {
        int srcXI = srcX >> 16;
        int srcYI = srcY >> 16;

        if (srcXI >= 0 && srcYI >= 0 && srcXI < srcW && srcYI < srcH) {
          dstRGB[dstPos++] = srcRGB[srcXI + srcYI * srcW];
        }

        srcX += srcDx;
        srcY += srcDy;
      }
    }
  }

  /**
   * Creates a scaled instance of a rectangular area (subimage) of the source image. 
   * This method preserves transparency if the PROCESS_ALPHA flag is set.
   *
   * @param source Source image
   * @param sx Subimage X position inside source image
   * @param sy Y position inside source image
   * @param sw Width of subimage to be scaled (in pixel).
   * @param sh Height of the subimage to be scaled (in pixel).
   * @param dw Destination image width (pixel).
   * @param dh Destination imaeg height (pixel)
   * @param flags DEFAULT or any combination of HIGH_QUALITY and PROCESS_ALPHA
   * @return The scaled image
   */
  public static Image createScaledImage(Image source, int sx,
      int sy, int sw, int sh, int dw, int dh, int flags) {

    int[] dRgb = new int[dw * dh];
    int[] sRgb = new int[sw * sh];

    source.getRGB(sRgb, 0, sw, sx, sy, sw, sh);

    int y16 = 0;

    int dx = (sw << 16) / dw;
    int dy = (sh << 16) / dh;

    for (int y = 0; y < dh; y++) {
      transformScanline(sRgb, sw, sh, 0, y16, dx, 0, dRgb, y * dw,  dw, flags);
      y16 += dy;
    }

    return Image.createRGBImage(dRgb, dw, dh,
        (flags & SCALE_PROCESS_ALPHA) != 0);
  }

  public static String[] breakUpString(Font font, String text, int width, int maxLines) {
    Vector lines = new Vector();

    int pos = 0;
    while (lines.size() < maxLines && pos < text.length() - 1) {
      int bp = GraphicsUtils.findBreakPosition(text, pos, font, width);
      lines.addElement(text.substring(pos, bp));
      pos = bp;
    }

    String[] result = new String[lines.size()];
    for (int i = 0; i < result.length; i++) {
        result[i] = (String) lines.elementAt(i);
    }
    return result;
  }

  public static void fillRectAlpha(Graphics g, int x, int y, int w, int h, int argb) {
    int[] data = new int[Math.max(w, h)];
    for (int i = 0; i < data.length; i++) {
      data[i] = argb;
    }
    if (w >= h) {
      for(int i = 0; i < h; i++) {
        g.drawRGB(data, 0, w, x, y + i, w, 1, true);
      }
    } else {
      for(int i = 0; i < w; i++) {
        g.drawRGB(data, 0, 1, x + i, y, 1, h, true);
      }	
    }
  }
  
  public static int findBreakPosition(String text, int pos, Font font, int maxWidth) {
    char currentChar = text.charAt(pos);
    int nextCharIndex = pos + 1;
    int bestPos = Integer.MAX_VALUE;
    int w = font.charWidth(currentChar);
    int len = text.length();

    while (true) {
      if (currentChar == '\n') {
        return nextCharIndex;
      }
      if (w > maxWidth && bestPos != Integer.MAX_VALUE) {
        return bestPos;
      }
      if (nextCharIndex >= len) {
        return len;
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
      w += font.charWidth(currentChar);
    }
  }

  /**
   * Draws a horizontal gradient rectangle.
   *
   * @param g The graphics context to render to.
   * @param leftColor The left color of the gradient.
   * @param rightColor The right color of the gradient.
   * @param x The X coordinate of the rectangle.
   * @param y The Y coordinate of the rectangle.
   * @param w The width of the rectangle.
   * @param h The height of the rectangle.
   */
  public static void drawGradientHorizontal(Graphics g, int leftColor,
      int rightColor, int x, int y, int w, int h) {
    int r = (x + w);

    int clipX = g.getClipX();
    int clipW = g.getClipWidth();
    int clipR = (clipX + clipW);

    if ((x < clipR) && (r > clipX)) {
      int clipY = g.getClipY();
      int clipH = g.getClipHeight();
      int clipB = (clipY + clipH);

      if ((y < clipB) && ((y + h) > clipY)) {
        int offset, interval = (w << 8);

        if (x < clipX) {
          offset = (clipX - x);
          x = clipX;
        } else {
          offset = 0;
        }

        if (r > clipR) {
          r = clipR;
        }

        do {
          int s = (((offset++) << 16) / interval);
          g.setColor(GraphicsUtils.gradient(leftColor, rightColor, s));
          g.drawLine(x, y, x, y + h - 1);
          x++;
        } while (x < r);
      }
    }
  }

  /**
   * Draws a vertical gradient rectangle.
   *
   * @param g The graphics context to render to.
   * @param topColor The top color of the gradient.
   * @param bottomColor The bottom color of the gradient.
   * @param x The X coordinate of the rectangle.
   * @param y The Y coordinate of the rectangle.
   * @param w The width of the rectangle.
   * @param h The height of the rectangle.
   */
  public static void drawGradientVertical(Graphics g, int topColor,
      int bottomColor, int x, int y, int w, int h) {
    int b = (y + h);

    int clipY = g.getClipY();
    int clipH = g.getClipHeight();
    int clipB = (clipY + clipH);

    if ((y < clipB) && (b > clipY)) {
      int clipX = g.getClipX();
      int clipW = g.getClipWidth();
      int clipR = (clipX + clipW);

      if ((x < clipR) && ((x + w) > clipX)) {
        int offset, interval = (h << 8);

        if (y < clipY) {
          offset = (clipY - y);
          y = clipY;
        } else {
          offset = 0;
        }

        if (b > clipB) {
          b = clipB;
        }

        do {
          int s = (((offset++) << 16) / interval);
          g.setColor(GraphicsUtils.gradient(topColor, bottomColor, s));
          g.drawLine(x, y, x + w - 1, y);
          y++;
        } while (y < b);
      }
    }
  }

  /**
   * Interpolates between two colors.
   *
   * @param a The first color.
   * @param b The second color.
   * @param s Fixed-point representation of how far to interpolate. Between 0
   *          and 0x100.
   * @return The interpolated color.
   */
  public static int gradient(int a, int b, int s) {
    int n = (0x100 - s);
    return (((((a & 0xFF0000) * n) + ((b & 0xFF0000) * s)) >>> 8) & 0xFF0000) | 
        (((((a & 0x00FF00) * n) + ((b & 0x00FF00) * s)) >>> 8) & 0x00FF00) | 
        ((((a & 0x0000FF) * n) + ((b & 0x0000FF) * s)) >>> 8);
  }
}
