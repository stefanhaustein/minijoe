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

package com.google.minijoe.samples.runtime;

import javax.microedition.lcdui.Graphics;

/**
 * Polygon filling implementation. Note: The methods in this class 
 * are not thread safe! Algorithm source: W.D.Fellner: Computergrafik
 * 
 * @author Stefan Haustein
 */
public class PolyFill {

  private PolyFill() {
  }

  public static boolean useAlpha = false;

  private static boolean inFill = false;
  private static int lastColor = 0;
  private static boolean useFillArray;
  private static final int INITIAL_POINTS = 32;
  private static int[] fillArray = new int[256];

  /** Last (unclosed?) segment start */
  private static int segmentStart;

  /** current position in points array */
  private static int pos;

  /** Current X position for lineTo */
  private static int currentX;

  /** Current Y position for lineTo */
  private static int currentY;

  /** x coordinates for line segments */
  private static int[] pointsX = new int[INITIAL_POINTS];

  /** y coordinates for line segments */
  private static int[] pointsY = new int[INITIAL_POINTS];

  /** next coordinate index for path */
  private static int[] nextPos  = new int[INITIAL_POINTS];

  private static PolyFill reuseHead = new PolyFill();
  private static PolyFill reusePtr;

  private static PolyFill edges;

  private static int bottomY;

  private int yTop;
  private int xInt;
  private int deltaY;
  private int deltaX;
  private PolyFill next;
  private PolyFill reuse;
  
  private static PolyFill newEdge(){

//  return new PolyFill();

    PolyFill result = reusePtr;
    if (result.reuse == null) {
      result.reuse = new PolyFill();
    }

    reusePtr = result.reuse;
    return result;
  }

  /**
   * Start a new path that will be filled. Note: There is only one global data structure and
   * no synchronization. Take care.
   */
  public  static void beginPath(){

    if (inFill) {
      throw new RuntimeException("Fill Threading issue");
    }

    segmentStart = 0;
    pos = 0;
  }

  /**
   * Extend the current path with a line to the given coordinates.
   */
  public static void lineTo(int x, int y){

    if (pos + 4 >= pointsX.length) {

      int l = pointsX.length * 3 / 2;
      int[] temp = new int[l];
      System.arraycopy(pointsX, 0, temp, 0, pos);
      pointsX = temp;

      temp = new int[l];
      System.arraycopy(pointsY, 0, temp, 0, pos);
      pointsY = temp;

      temp = new int[l];
      System.arraycopy(nextPos, 0, temp, 0, pos);
      nextPos = temp;
    }

    if (segmentStart == pos) {
      pointsX[pos] = currentX;
      pointsY[pos] = currentY;
      nextPos[pos] = pos + 1;
      pos++;
    }

    pointsX[pos] = currentX = x;
    pointsY[pos] = currentY = y;
    nextPos[pos] = pos + 1;

    pos++;
  }

  /**
   * Move to the given coordinates, possibly starting a new sub path.
   */
  public static void moveTo(int x, int y){

    if (pos > segmentStart) {
      nextPos[pos - 1] = segmentStart;
    }

    segmentStart = pos;
    currentX = x;
    currentY = y;
  }

  private static final int nextY(int k){

    int compareY = pointsY[k];
    int newY;
    do {
      k = nextPos[k];
      newY = pointsY[k];
    }
    while(newY == compareY);

    return newY;
  }


  private static final void insert(int p1x, int p1y, int p2x, int p2y, int yNext){

    int dx = ((p2x - p1x) << 16) / (p2y - p1y);
    int x2 = p2x << 16;

    if (p2y > p1y && p2y < yNext) {
      p2y--;
      x2 -= dx;
    } else if (p2y < p1y && p2y > yNext) {
      p2y++;
      x2 += dx;
    }

    int dy = p2y - p1y;

    int maxX;
    int maxY;

    if (dy > 0){
      maxY = p2y; 
      maxX = x2;
      dy++;
    } else {
      maxY = p1y;
      maxX = p1x << 16;
      dy = 1 - dy;
    }

    PolyFill newEdge = newEdge();
    newEdge.yTop = maxY;
    newEdge.deltaY = dy;
    newEdge.xInt = maxX;
    newEdge.deltaX = dx;

    PolyFill edge1 = edges;
    while (edge1.next.yTop >= maxY) {
      edge1 = edge1.next;
    }
    newEdge.next = edge1.next; 
    edge1.next = newEdge;
  }

  /**
   * Fill the path defined by beginPath(), moveTo() and lineTo().
   */
  public static void fill(Graphics g, int color){

    int alpha = color >>> 24;

    if (useAlpha ? alpha == 0 : alpha < 64) {
      return;
    }

    inFill = true;

    if (useAlpha ? alpha == 255 : alpha > 192) {
      useFillArray = false;
      g.setColor(color & 0x0ffffff);
    } else {
      useFillArray = true;
      if (color != lastColor) {
        lastColor = color;
        if (useAlpha){
          for (int i = 0; i < 255; i++) {
            fillArray[i] = color;
          }
        } else {
          for (int i = 0; i < 255; i += 2) {
            fillArray[i] = color | 0xff000000;
            fillArray[i + 1] = color & 0xff000000;
          }
        } 
      }
    }

    if (pos > segmentStart) {
      nextPos[pos - 1] = segmentStart;
    }
    reusePtr = reuseHead;

    // begin Edge_Sort
    edges = newEdge();
    PolyFill edge1 = newEdge();

    edges.next = edge1;
    edge1.next = null;

    edges.yTop = Integer.MAX_VALUE;
    edge1.yTop = Integer.MIN_VALUE;

    bottomY = Integer.MAX_VALUE;

    for (int k = 0; k < pos; k++) {
      int p1x = pointsX[k];
      int p1y = pointsY[k];
      int next = nextPos[k];

      if (pointsY[next] != p1y) {
        insert(p1x, p1y, pointsX[next], pointsY[next], nextY(next));
      } else {
        drawHorizontalLine(g, p1x, p1y, pointsX[next]);
      }
      if (p1y < bottomY) {
        bottomY = p1y;
      }
    }
    // end Edge sort

    PolyFill lActEdge = edges.next;

    for (int scan = edges.next.yTop; scan >= bottomY; scan--) {
      // addition: care about discontinuities
      if (scan > edges.next.yTop) {
        scan = edges.next.yTop;
      }

      // update List Ptr
      while (lActEdge.next.yTop >= scan) {
        lActEdge = lActEdge.next;
      }

      // sort intersections
      PolyFill edge2 = edges.next;
      do {
        edge1 = edges;
        while (edge1.next.xInt < edge2.next.xInt) {
          edge1 = edge1.next;
        }
        if (edge1 != edge2){
          PolyFill edge3 = edge2.next.next;
          edge2.next.next = edge1.next;
          edge1.next = edge2.next;
          edge2.next = edge3;
          if (edge1.next == lActEdge){
            lActEdge = edge2;
          }
        } else {
          edge2 = edge2.next;
        }
      }
      while(edge2 != lActEdge);
      // end sort intersections

      // begin fill
      edge1 = edges;
      do {
        edge1 = edge1.next;
        int qx = (edge1.xInt + 32768) >> 16;
        edge1 = edge1.next;
        drawHorizontalLine(g, qx, scan, (edge1.xInt + 32768) >> 16);
      } while(edge1 != lActEdge);
      // end fill

      // begin update edges
      edge1 = edges;

      PolyFill prevEdge = edge1;
      do {
        edge1 = prevEdge.next;
        if (edge1.deltaY > 1) {
          edge1.deltaY--;
          edge1.xInt -= edge1.deltaX;
          prevEdge = edge1;
        } else {
          prevEdge.next = edge1.next;
          if (edge1 == lActEdge) {
            lActEdge = prevEdge;
          }
        }
      } while(prevEdge != lActEdge);
      // end update edges
    }

    inFill = false;
  }


  private static final void drawHorizontalLine(Graphics g, int p1x, int p1y, int p2x) {
    if (useFillArray) {
      if (p2x < p1x) {
        int swap = p2x;
        p2x = p1x;
        p1x = swap;
      }
//      System.out.println("x+y&1="+((p1x+p1y) & 1));
      g.drawRGB(fillArray, (p1x + p1y) & 1, 256, p1x, p1y, p2x - p1x, 1, true);
    } else {
      g.drawLine(p1x, p1y, p2x, p1y);
    }
  }
}
