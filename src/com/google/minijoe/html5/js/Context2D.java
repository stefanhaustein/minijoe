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

package com.google.minijoe.html5.js;

import com.google.minijoe.sys.JsArray;
import com.google.minijoe.sys.JsObject;

import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/** 
 * Graphics context: partial implementation of Javascript Context2D. 
 * Provides drawing methods. 
 * 
 * @author Stefan Haustein
 */
public class Context2D extends JsObject {
  
  static final int ID_BEGIN_PATH = 100;
  static final int ID_CLOSE_PATH = 101;
  static final int ID_FILL = 102;
  static final int ID_FILL_RECT = 103;
  static final int ID_FILL_STYLE = 104;
  static final int ID_FILL_STYLE_SET = 105;

  static final int ID_LINE_TO = 106;
  static final int ID_MOVE_TO = 107;
  static final int ID_QUADRATIC_CURVE_TO = 108;

  static final int ID_RESTORE = 109;
  static final int ID_ROTATE = 110;
  static final int ID_SAVE = 111;
  static final int ID_SCALE = 112;
  static final int ID_STROKE = 113;
  static final int ID_STROKE_RECT = 114;
  static final int ID_TRANSLATE = 115;
  static final int ID_DRAW_IMAGE = 116;
  static final int ID_TEXT_STYLE = 117;
  static final int ID_TEXT_STYLE_SET = 118;
  static final int ID_MEASURE_TEXT = 119;
  static final int ID_CLEAR_RECT = 120;
  static final int ID_ARC = 121;
  static final int ID_BEZIER_CURVE_TO = 122;
  static final int ID_STROKE_STYLE = 123;
  static final int ID_STROKE_STYLE_SET = 124;
  static final int ID_DRAW_TEXT = 125;
  
  static final Hashtable COLORS = new Hashtable();

  static final JsObject CONTEXT_PROTOTYPE = new JsObject(JsObject.OBJECT_PROTOTYPE)
      .addNative("beginPath", ID_BEGIN_PATH, 0)
      .addNative("closePath", ID_CLOSE_PATH, 0)
      .addNative("fill", ID_FILL, 0)
      .addNative("fillStyle", ID_FILL_STYLE, -1)
      .addNative("fillRect", ID_FILL_RECT, 4)
      .addNative("lineTo", ID_LINE_TO, 2)
      .addNative("moveTo", ID_MOVE_TO, 2)
      .addNative("quadraticCurveTo", ID_QUADRATIC_CURVE_TO, 4)
      .addNative("rotate", ID_ROTATE, 1)
      .addNative("save", ID_SAVE, 0)
      .addNative("stroke", ID_STROKE, 0)
      .addNative("strokeRect", ID_STROKE_RECT, 4)
      .addNative("scale", ID_SCALE, 2)
      .addNative("translate", ID_TRANSLATE, 2)
      .addNative("drawImage", ID_DRAW_IMAGE, 9)
      .addNative("restore", ID_RESTORE, 0)
      .addNative("textStyle", ID_TEXT_STYLE, -1)
      .addNative("mozTextStyle", ID_TEXT_STYLE, -1)
      .addNative("clearRect", ID_CLEAR_RECT, 4)
      .addNative("arc", ID_ARC, 6)
      .addNative("bezierCurveTo", ID_BEZIER_CURVE_TO, 6)
      .addNative("strokeStyle", ID_STROKE_STYLE, -1)
      .addNative("drawText", ID_DRAW_TEXT, 1)
      .addNative("mozDrawText", ID_DRAW_TEXT, 1)
      .addNative("measureText", ID_MEASURE_TEXT, 1)
      .addNative("mozMeasureText", ID_MEASURE_TEXT, 1)
  ;

  // Value caches for curve calculations
  static final double[] EXP2 = new double[15];
  static final double[] EXP3 = new double[15];
  static final double[] ZT1MT = new double[15];
  static final double[] DT1MT2 = new double[15];

  // init constants, colors
  static{
    COLORS.put("red", new Integer(0x0ff0000));
    COLORS.put("green", new Integer(0x000ff00));
    COLORS.put("blue", new Integer(0x00000ff));
    COLORS.put("white", new Integer(0x0ffffff));
    COLORS.put("black", new Integer(0));
    COLORS.put("yellow", new Integer(0x0ffff00));
    COLORS.put("grey", new Integer(0x0ff888888));

    double delta = 1.0 / 16.0;
    double pos = delta;
    for (int i = 0; i < 15; i++){
      double l = pos * pos;
      EXP2[i] = l;
      EXP3[i] = l * pos;
      ZT1MT[i] = ((2.0 * pos * (1 - pos)));
      DT1MT2[i] = ((3.0 * pos * (1 - pos) * (1 - pos)));

//    System.out.println("i: "+i+ " pos: "+ fpToString(pos)
//    + " exp2:"+fpToString(EXP2[i]) 
//    + " 2t(1-t) "+fpToString(ZT1MT[i])
//    + " exp3: "+fpToString(EXP3[i])
//    + " 3t(1-t)^2 "+fpToString(DT1MT2[i]));

      pos += delta;
    }
  }
  
  private boolean virgin;
  private Image buffer;
  private Graphics graphics;

  private int pathPos = 0;
  private double[] path = new double[32]; 
  private boolean fill;

  private int scrStartX;
  private int scrStartY;

  private int currentScrX;
  private int currentScrY;
  private int currentFillColor = 0x0ff000000;
  private int currentLineColor = 0x0ff000000; 

  //TODO Consider changing to float and special cases for neut/simple matrices
  private double translateX = 0;
  private double translateY = 0;
  private double scaleX = 1;
  private double scew1 = 0;
  private double scaleY = 1;
  private double scew2 = 0;

  private Vector stack = new Vector();
  private Font font;
 
  public Context2D(Image graphicsBuffer){
    super(CONTEXT_PROTOTYPE);
    
    this.graphics = graphicsBuffer.getGraphics();
    
    this.font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
  }

  static String[] split(String s, char c) {
    int count = 1;
    for (int i = 0; i < s.length(); i++) {
      if (s.charAt(i) == c) {
        count++;
      }
    }
    String[] parts = new String[count];
    int lastCut = 0;
    int n = 0;
    for (int i = 0; i < s.length(); i++) {
      if (s.charAt(i) == c) {
        parts[n] = s.substring(lastCut, i);
        lastCut = i + 1;
      }
    }
    parts[n] = s.substring(lastCut, s.length());
    return parts;
  }
  
  
  private final int translateX(double x, double y){
    return (int) (((x * scaleX + y * scew1)) + (translateX));
  }

  private final int translateY(double x, double y){
    return (int) (((y * scaleY + x * scew2)) + (translateY));
  }

  public void evalNative(int id, JsArray stack, int sp, int parCount) {
    switch (id){
      case ID_BEGIN_PATH:
        beginPath();
        break;

      case ID_CLOSE_PATH:
        closePath();
        break;

      case ID_LINE_TO:
        lineTo(stack.getNumber(sp + 2), stack.getNumber(sp + 3));
        break;

      case ID_MOVE_TO:
        moveTo(stack.getNumber(sp + 2), stack.getNumber(sp + 3));
        break;

      case ID_FILL:
        fill();
        break;

      case ID_FILL_RECT:
        double x0 = stack.getNumber(sp + 2);
        double y0 = stack.getNumber(sp + 3);
        double x1 = x0 + stack.getNumber(sp + 4) - 1;
        double y1 = y0 + stack.getNumber(sp + 5) - 1;
        fillRect(x0, y0, x1, y1);
        break;

      case ID_CLEAR_RECT:
        x0 = stack.getNumber(sp + 2);
        y0 = stack.getNumber(sp + 3);
        x1 = x0 + stack.getNumber(sp + 4) - 1;
        y1 = y0 + stack.getNumber(sp + 5) - 1;
        int sfc = currentFillColor;
        currentFillColor = 0x0ffffffff;
        fillRect(x0, y0, x1, y1);
        currentFillColor = sfc;
        break;

      case ID_STROKE:
        stroke();
        break;

      case ID_STROKE_RECT:
        x0 = stack.getNumber(sp + 2);
        y0 = stack.getNumber(sp + 3);
        x1 = x0 + stack.getNumber(sp + 4) - 1;
        y1 = y0 + stack.getNumber(sp + 5) - 1;
        strokeRect(x0, y0, x1, y1);
        break;

      case ID_SAVE:
        save();
        break;

      case ID_RESTORE:
        restore();
        break;

      case ID_QUADRATIC_CURVE_TO:
        if (pathPos + 5 >= path.length) {
          enlargeBuf(5);
        }
        path[pathPos++] = ID_QUADRATIC_CURVE_TO;
        path[pathPos++] = stack.getNumber(sp + 2);
        path[pathPos++] = stack.getNumber(sp + 3);
        path[pathPos++] = stack.getNumber(sp + 4);
        path[pathPos++] = stack.getNumber(sp + 5);
        break;

      case ID_BEZIER_CURVE_TO:
        if (pathPos + 7 >= path.length) {
          enlargeBuf(7);
        }
        path[pathPos++] = ID_BEZIER_CURVE_TO;
        path[pathPos++] = stack.getNumber(sp + 2);
        path[pathPos++] = stack.getNumber(sp + 3);
        path[pathPos++] = stack.getNumber(sp + 4);
        path[pathPos++] = stack.getNumber(sp + 5);
        path[pathPos++] = stack.getNumber(sp + 6);
        path[pathPos++] = stack.getNumber(sp + 7);
        break;

      case ID_ROTATE:
        double ang = -stack.getNumber(sp + 2); 
        double sin = Math.sin(ang);
        double cos = Math.cos(ang);
        transform(cos, -sin, sin, cos, 0, 0);
        break;

      case ID_SCALE:
        transform(stack.getNumber(sp + 2), 0, 0, stack.getNumber(sp + 3), 0, 0);
        break;

      case ID_TRANSLATE:
        transform(1, 0, 0, 1, stack.getNumber(sp + 2), stack.getNumber(sp + 3));
        break;

      case ID_FILL_STYLE_SET:
        currentFillColor = parseColor((String) stack.getObject(sp));
        break;

      case ID_STROKE_STYLE_SET:
        currentLineColor = parseColor((String) stack.getObject(sp));
        break;

      case ID_DRAW_IMAGE:
        System.out.println("DrawImage Not Yet Supported");
        break;

      case ID_FILL_STYLE:
        StringBuffer buf = new StringBuffer(Integer.toString(currentFillColor, 16));
        while (buf.length() < 8) {
          buf.insert(0, '0');
        }
        buf.insert(0, '#');
        stack.setObject(sp, buf.toString());
        break;

      case ID_STROKE_STYLE:
        buf = new StringBuffer(Integer.toString(currentLineColor, 16));
        while (buf.length() < 8) {
          buf.insert(0, '0');
        }
        buf.insert(0, '#');
        stack.setObject(sp, buf.toString());
        break;    

      case ID_TEXT_STYLE:
        break;

      case ID_TEXT_STYLE_SET:
        break;

      case ID_DRAW_TEXT:
        graphics.setColor(currentFillColor);
        graphics.setFont(font);
        graphics.drawString(stack.getString(sp + 2), 
            translateX(0, 0), 
            translateY(0, 0), Graphics.BASELINE | Graphics.LEFT);
        break;
        
      case ID_MEASURE_TEXT:
        stack.setNumber(sp, font.stringWidth(stack.getString(sp + 2)) / scaleX);
        break;
        
      case ID_ARC:
        if (pathPos + 7 >= path.length) {
          enlargeBuf(7);
        }
        path[pathPos++] = ID_ARC;
        path[pathPos++] = stack.getNumber(sp + 2);
        path[pathPos++] = stack.getNumber(sp + 3);
        path[pathPos++] = stack.getNumber(sp + 4);
        path[pathPos++] = stack.getNumber(sp + 5);
        path[pathPos++] = stack.getNumber(sp + 6);
        path[pathPos++] = stack.getNumber(sp + 7);
        break;
        
      default:
        super.evalNative(id, stack, sp, parCount);
    }
  }

  private final void enlargeBuf(int i) {
    i = (path.length + i) * 3 / 2;

    double[] np = new double[i];
    System.arraycopy(path, 0, np, 0, pathPos);
    path = np;
  }

  private final void restore() {
    double[] state = (double[]) this.stack.elementAt(this.stack.size() - 1);
    scaleX = state[0];
    scew1 = state[1];
    scew2 = state[2];
    scaleY = state[3];
    translateX = state[4];
    translateY = state[5];
    this.stack.removeElementAt(this.stack.size() - 1);
  }

  private final void save() {
    this.stack.addElement(new double[]{scaleX, scew1, scew2, scaleY, translateX, translateY});
  }

  private final void transform(double a2, double b2, double c2, double d2, double e2, double f2) {
    double tmp = b2;
    b2 = c2;
    c2 = tmp;

    double a1 = scaleX;
    double b1 = scew1;
    double c1 = scew2;
    double d1 = scaleY;
    double e1 = translateX;
    double f1 = translateY;

    scaleX = a1 * a2 + c1 * b2;
    scew1 =  b1 * a2 + d1 * b2;

    scew2 = a1 * c2 + c1 * d2;
    scaleY = b1 * c2 + d1 * d2;

    translateX = (a1 * e2 + c1 * f2)  + e1;  
    translateY = (b1 * e2 + d1 * f2)  + f1;  
  }

  private final void drawPath(){
    int i = 0;
    virgin = true;
    while (i < pathPos) {
      int type = (int) path[i++];
      double x = path[i++];
      double y = path[i++];

      switch(type){
        case ID_MOVE_TO:
          scrMoveTo(translateX(x, y), translateY(x, y));
          break;
        case ID_LINE_TO:
          // sets currentScrX/Y
          scrLineTo(translateX(x, y), translateY(x, y));
          break;
        case ID_BEZIER_CURVE_TO:
          bezierCurveTo(x, y,
              path[i], 
              path[i + 1], 
              path[i + 2], 
              path[i + 3]);
          i += 4;
          break;
        case ID_QUADRATIC_CURVE_TO:
          quadraticCurveTo(x, y,
              path[i], 
              path[i + 1]);
          i += 2;
          break;
        case ID_ARC:
          arc(x, y, 
              path[i], 
              path[i + 1], 
              path[i + 2], 
              path[i + 3] != 0);
          i += 4;
          break;
        case ID_CLOSE_PATH:
          scrLineTo(scrStartX, scrStartY);
          break;
      }
    }
  }

  private final void scrMoveTo(int i, int j) {
    currentScrX = scrStartX = i;
    currentScrY = scrStartY = j;
    virgin = false;
    if (fill) {
      PolyFill.moveTo(i, j);
    }

  }

  private final void fill() {
    fill = true;
    PolyFill.beginPath();
    drawPath();
    PolyFill.fill(graphics, currentFillColor);
  }


  private final void moveTo(double x, double y) {

    if (pathPos + 3 >= path.length) {
      enlargeBuf(3);
    }
    path[pathPos++] = ID_MOVE_TO;
    path[pathPos++] = x;
    path[pathPos++] = y;
  }


  private final void scrLineTo(int scrX, int scrY){

//    System.out.println("ScrLineTo("+scrX+","+scrY+")");

    if (virgin) {
      scrMoveTo(scrX, scrY);
    } else if (fill) {
      PolyFill.lineTo(scrX, scrY);
    } else {
      graphics.drawLine(currentScrX, currentScrY, scrX, scrY);
    }

    currentScrX = scrX;
    currentScrY = scrY;
  }

  private final void lineTo(double x, double y) {

    if (pathPos + 3 >= path.length) {
      enlargeBuf(3);
    }
    path[pathPos++] = ID_LINE_TO;
    path[pathPos++] = x;
    path[pathPos++] = y;
  }
  
  private final void closePath(){
    if (pathPos + 3 >= path.length) {
      enlargeBuf(3);
    }
    path[pathPos] = ID_CLOSE_PATH;
    // two ignored parameter to simplify drawing logic
    pathPos += 3;
  }
  
  private final void stroke() {
    if ((currentLineColor & 0x0ff000000) == 0) {
      return;
    }
    fill = false;
    graphics.setColor(currentLineColor & 0x0ffffff);
    drawPath();
  }

  private final void beginPath() {
    pathPos = 0;
  }

  private final int parseColor(String style){
    if (style.startsWith("rgba")) {
      int cut0 = style.indexOf('(') + 1;
      int cut1 = style.indexOf(')', cut0);
      String[] rgba = split(style.substring(cut0, cut1), ',');
      return (Integer.parseInt(rgba[0].trim()) << 16) | 
          (Integer.parseInt(rgba[1].trim()) << 8) | 
          Integer.parseInt(rgba[2].trim()) | 
          (((int) (Double.parseDouble(rgba[3].trim()) * 255.0) & 0x0ff) << 24);
    } else if (style.startsWith("rgb")) {
      int cut0 = style.indexOf('(') + 1;
      int cut1 = style.indexOf(')', cut0);
      String[] rgba = split(style.substring(cut0, cut1), ',');
      return (Integer.parseInt(rgba[0].trim()) << 16) |
      (Integer.parseInt(rgba[1].trim()) << 8) |
      Integer.parseInt(rgba[2].trim()) | 0x0ff000000; 
    } else if (style.startsWith("#")) {
      return Integer.parseInt(style.substring(1), 16) | 0x0ff000000;
    }
    Integer c = (Integer) COLORS.get(style);
    return 0x0ff000000 | (c == null ? 0 : c.intValue());
  }

  private final void bezierCurveTo(double cp1x, double cp1y, 
      double cp2x, double cp2y, double x, double y){
    int p0x = currentScrX;
    int p0y = currentScrY;

    int p1x = translateX(cp1x, cp1y);
    int p1y = translateY(cp1x, cp1y);

    int p2x = translateX(cp2x, cp2y);
    int p2y = translateY(cp2x, cp2y);

    int p3x = translateX(x, y);
    int p3y = translateY(x, y);

    int d = Math.max((p0x - p3x) * (p0x - p3x) + (p0y - p3y) * (p0y - p3y),
            Math.max((p0x - p1x) * (p0x - p1x) + (p0y - p1y) * (p0y - p1y),
                     (p2x - p3x) * (p2x - p3x) + (p2y - p3y) * (p2y - p3y)));
    int step;
    if (d > 1000) {  // 
      step = d > 4000 ? 1 : 2;
    } else {
      step = d > 250 ? 4 : 8;
    }
    for (int i = step - 1; i < 15; i += step){
      int newScrX = (int) (EXP3[14 - i] * p0x + DT1MT2[i] * p1x + 
          DT1MT2[14 - i] * p2x + EXP3[i] * p3x);
      int newScrY = (int) (EXP3[14 - i] * p0y + DT1MT2[i] * p1y + 
          DT1MT2[14 - i] * p2y + EXP3[i] * p3y);

      scrLineTo(newScrX, newScrY);
    }
    scrLineTo(p3x, p3y);
  }

  private final void arc(double x, double y, double radius, 
      double startAngle, double endAngle, boolean counterclockwise){

    double pi2 = 2.0 * Math.PI;
    double ang =  startAngle  % pi2;
    double end = endAngle  % pi2;
    double delta = (int) (90 * (scaleX + scaleY) / radius);

//  System.out.println("arc start ang: "+ ang+ "; end: "+end+ "delta: "+delta);

    if (delta < Math.PI / 180.0) {
      delta = Math.PI / 180.0;
    }
    if (delta > Math.PI / 10) {
      delta = Math.PI / 10;
    }

    if (counterclockwise) {
      delta = -delta;
      if (end >= ang) {
        end -= pi2;
      }
    } else {
      if (end <= ang) {
        end += pi2;
      }
    }

//  System.out.println("arc start ang: "+ ang+ "; end: "+end);

    double cx;
    double cy;

    do {
      cx = x + radius * Math.cos(ang);
      cy = y + radius * Math.sin(ang);

      scrLineTo(translateX(cx, cy), translateY(cx, cy));

      ang += delta; 
    }
    while(counterclockwise ? ang >= end : ang <= end);

    cx = x + radius * Math.cos(end);
    cy = y + radius * Math.sin(end);

    scrLineTo(translateX(cx, cy), translateY(cx, cy));
  }

  private final void quadraticCurveTo(double cpx, double cpy, double x, double y){

    int p0x = currentScrX;
    int p0y = currentScrY;

    int p1x = translateX(cpx, cpy);
    int p1y = translateY(cpx, cpy);

    int p2x = translateX(x, y);
    int p2y = translateY(x, y);

    int d = Math.max(
        (p0x - p1x) * (p0x - p1x) + (p0y - p1y) * (p0y - p1y),
        (p1x - p2x) * (p1x - p2x) + (p1y - p2y) * (p1y - p2y));

    int step;
    if (d > 1000) {  
      step = d > 4000 ? 1 : 2;
    } else {
      step = d > 250 ? 4 : 8;
    }

    for (int i = step - 1; i < 15; i += step) {
      int newScrX = (int) (EXP2[14 - i] * p0x + ZT1MT[i] * p1x + EXP2[i] * p2x);
      int newScrY = (int) (EXP2[14 - i] * p0y + ZT1MT[i] * p1y + EXP2[i] * p2y);

      scrLineTo(newScrX, newScrY);
    }

    scrLineTo(p2x, p2y);
  }

  private final void strokeRect(double x0, double y0, double x1, double y1) {
    beginPath();
    moveTo(x0, y0);
    lineTo(x1, y0);
    lineTo(x1, y1);
    lineTo(x0, y1);
    lineTo(x0, y0);
    stroke();
  }

  private final void fillRect(double x0, double y0, double x1, double y1) {
    if (scew1 == 0 && scew2 == 0 && scaleX > 0 && scaleY > 0){
      int sx0 = translateX(x0, y0);
      int sy0 = translateY(x0, y0);
      int sx1 = translateX(x1, y1);
      int sy1 = translateY(x1, y1);
      graphics.setColor(currentFillColor);
      graphics.fillRect(sx0, sy0, sx1 - sx0 + 1, sy1 - sy0 + 1);
    } else {
      beginPath();
      moveTo(x0, y0);
      lineTo(x1, y0);
      lineTo(x1, y1);
      lineTo(x0, y1);
      lineTo(x0, y0);
      fill();
    }
  }
}
