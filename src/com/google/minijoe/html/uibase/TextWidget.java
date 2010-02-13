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

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

public class TextWidget extends Widget {

  int BORDER_TOP = 1;
  int BORDER_RIGHT = 2;
  int BORDER_BOTTOM = 4;
  int BORDER_LEFT = 5;

  int backgroundColor = 0x0ffffff;
  int borderColor = 0x0ffffff;
  int textColor = 0;
  int align = Graphics.HCENTER;
  Font font;
  String text;

  public TextWidget(Font font) {
    this.font = font;
    this.setHeight(font.getHeight() + 4);  	
  }

  public void setText(String text) {
    this.text = text;
    invalidate(false);
  }

  public void setBackgroundColor(int fc) {
    this.backgroundColor = fc;
  }

  public void setTextColor(int tc) {
    this.textColor = tc;
  }

  public void setBorderColor(int bc) {
    this.borderColor = bc;
  }

  public void setAlign(int align) {
    this.align = align;
  }

  public void drawContent(Graphics g, int dx, int dy) {
    if (text == null) {
      return;
    }
    
    g.setColor(backgroundColor);
    g.fillRect(dx, dy, getWidth(), getHeight());

    g.setColor(textColor);
    g.setFont(font);

    int x0;
    int hAlign = align & (Graphics.LEFT|Graphics.RIGHT|Graphics.HCENTER); 
    switch(hAlign) {
      case Graphics.RIGHT: 
        x0 = dx + getWidth() - 2;
        break;
      case Graphics.HCENTER:
        x0 = dx + getWidth() / 2;
        break;
      default:
        x0 = dx + 2;
    }

    if (text != null) {
      g.drawString(GraphicsUtils.getFittedString(text, font, getWidth() - 4, true), x0, dy + 2, hAlign | Graphics.TOP);
    }
  }
}
