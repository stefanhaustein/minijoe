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
import com.google.minijoe.html.uibase.GraphicsUtils;
import com.google.minijoe.html.uibase.Widget;

import java.util.Vector;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Widget for rendering HTML block elements. For text content and for inline
 * elements without block content, TextFragmentWidgets are created.
 * 
 * @author Stefan Haustein
 */
public class BlockWidget extends Widget {

  /**
   * Index for the flag for unassigned traversal in 
   * addChildren() and addElement().
   */
  private static final int FLAG_UNASSIGNED_TRAVERSAL = 0;

  /**
   * Index for the flag for preserving leading space in 
   * addChildren() and addElement().
   */
  private static final int FLAG_PRESERVE_LEADING_SPACE = 1;

  /** True if this element may be traversed. Set in the constructor. */
  protected final boolean focusable;

  /** The HTML element this widget corresponds to. */
  protected Element element;

  /**
   * If false, a call to doLayout() is required before this component can be
   * rendered properly.
   */
  protected boolean layoutValid;

  /**
   * If false, a call to calculateWidth() is required before the minWidth and
   * maxWidth values can be used. Used internally in getMinWidth() and 
   * getMaxWidth() only.
   */
  protected boolean widthValid;

  /**
   * Width base value for percentages, set in doLayout
   */
  protected int containingWidth;

  /**
   * Left margin including adjustment for AUTO values
   */
  protected int marginLeft;

  /**
   * Right margin including adjustment for AUTO values
   */
  protected int marginRight;

  /** 
   * X-Coordinate of the CSS box inside this widget. 
   * The widget may be larger than the CSS box to make sure
   * components outside the CSS box are drawn.
   */  
  protected int boxX;

  /** Y-Coordinate of the CSS box inside this widget {@see boxX}. */
  protected int boxY;

  /** Width of the CSS box inside this widget {@see boxX}. */
  protected int boxWidth;

  /** Height of the CSS box inside this widget {@see boxX}. */
  protected int boxHeight;

  /** 
   * Minimal width of this component, as set by calculateWidth(). Use
   * getMinimumWidth to obtain this value.
   */
  protected int minimumWidth;

  /** 
   * Maximal width of this component, as set by calculateWidth(). Use
   * getMaximumWidth to obtain this value.
   */
  protected int maximumWidth;

  /** Contains the child elements in document order (!= display order). */
  protected Vector children = new Vector();

  /** The image if this block represents an image. */
  protected Image image;

  /**
   * Constructs a new BlockWidget for the given element. Does not recurse.
   * Used only in constructors for subclasses.
   */
  protected BlockWidget(Element element, boolean traversable) {
    this.element = element;
    this.focusable = traversable;
    if (traversable) {
      element.setFocused();
    }
  }

  /**
   * Construct a new BlockWidget for the given element with the given flags.
   * 
   * @param element the HTML element this widget belongs to
   * @param flags {@see addChildren()}
   */
  public BlockWidget(Element element, boolean[] flags) {
    this(element, flags[FLAG_UNASSIGNED_TRAVERSAL]);
    flags[0] = false;
    String name = element.getName();
    if (name.equals("img")) {
      String src = element.getAttributeValue("src");
      if (src != null) {
        Object img = element.htmlWidget.getResource(src, 
            SystemRequestHandler.TYPE_IMAGE, this);
        if (img instanceof Image) {
          image = (Image) img;
        }
        flags[FLAG_PRESERVE_LEADING_SPACE] = true;
      }
    } else {
      addChildren(element, flags);
    }
  }

  /**
   * Add widgets for the given child element to this block widget. 
   * 
   * @param element the tree to be added recursively
   * @param flags {@see addChildren}
   */
  void addElement(Element e, boolean[] flags) {
    boolean parentUnassignedTraversal = flags[FLAG_UNASSIGNED_TRAVERSAL];
    boolean focusable = e.isFocusable();
    flags[FLAG_UNASSIGNED_TRAVERSAL] |= focusable;

    Style style = e.getComputedStyle();
    String name = e.getName();

    int labelIndex = children.size();
    int display = style.getEnum(Style.DISPLAY);

    if (display == Style.NONE) {
      // ignore
    } else if (display == Style.TABLE
        || display == Style.INLINE_TABLE
        || "table".equals(e.getName())) { 
      children.addElement(new TableWidget(e));
    } else if (name.equals("input")) {
      if (!"hidden".equals(e.getAttributeValue("type"))) {
        children.addElement(new InputWidget(e));
      }
    } else if (("select".equals(name) && !e.getAttributeBoolean("multiple")) || 
        "option".equals(name) || "textarea".equals(name) || "button".equals(name)) {
      children.addElement(new InputWidget(e));
    } else if (style.isBlock(false)) {
      children.addElement(new BlockWidget(e, flags));
    } else {
      // Element does not get its own block widget, just add the element's 
      // children to this block
      addChildren(e, flags);
    }

    String label = e.getAttributeValue("id");
    if (label == null) {
      label = e.getAttributeValue("name");
    }
    if (label != null) {
      e.htmlWidget.labels.put(label, children.size() == 0 ? this
          : children.elementAt(Math.min(children.size() - 1, labelIndex)));
    }
    String accesskey = e.getAttributeValue("accesskey");
    if (accesskey != null && accesskey.length() == 1) {
      e.htmlWidget.accesskeys.put(new Character(accesskey.charAt(0)), e);
    }

    flags[FLAG_UNASSIGNED_TRAVERSAL] = focusable && parentUnassignedTraversal;
  }

  /**
   * Add widgets for the child elements of the given element to this block 
   * widget. The flags parameter is used as in and out parameter. The flag
   * with the index FLAG_PRESERVE_LEADING_SPACE controls whether leading
   * space needs to be preserved or will be trimmed. The flag with the index
   * FLAG_UNASSIGNED_TRAVERSAL is set if a link element was visited, but 
   * no visible element to carry the link was encountered so far.
   * <p>
   * This method normalizes white space as follows (if not instructed by the
   * style to preserve all whitespace):
   * <ol>
   * <li>sequences of CR, LF, TAB and SPACE will be normalized to a single space
   * <li>whitespace before or after a block element will be removed
   * <li>whitespace at the beginning or end of a block element will be removed
   * <li>whitespace at the beginning of a new inline element will be removed if 
   *     there already was whitespace before the the element.
   * <li>whitespace immediately after the end of an inline element will be 
   *     removed if there already was whitespace inside the element at the end.
   * </ol>
   * 
   * @param element element containing the child elements to be added
   * @param flags flags controlling traversability and leading white space, 
   *        see above. 
   */
  void addChildren(Element e, boolean[] flags) {
    Style style = e.getComputedStyle();
    StringBuffer buf = new StringBuffer();
    boolean isBlock = style.isBlock(true);
    boolean preserveLeadingSpace = 
      flags[FLAG_PRESERVE_LEADING_SPACE] && !isBlock;
    boolean preserveAllSpace = style.getEnum(Style.WHITE_SPACE) == Style.PRE;  

    for (int i = 0; i < e.getChildCount(); i++) {
      switch (e.getChildType(i)) {
        case Element.ELEMENT:
          Element child = e.getElement(i);
          if ("br".equals(child.getName())) {
            rTrim(buf);
            buf.append("\n");
            preserveLeadingSpace = false;
          } else {           
            // make sure space between </li> and <li> is always ignored, even
            // if <li> is set to inline style. 
            boolean childIsBlock = child.getComputedStyle().isBlock(true) || 
            "li".equals(child.getName());
            // before block elements, always remove all whitespace
            if (!preserveAllSpace && childIsBlock) {
              rTrim(buf); // does not trim /n!
            }
            // insert text aggregated in case Element.TEXT if any
            if (buf.length() > 0) {
              TextFragmentWidget fragment = new TextFragmentWidget(e,
                  buf.toString(), flags[0]);
              flags[FLAG_UNASSIGNED_TRAVERSAL] = false;
              children.addElement(fragment);
              preserveLeadingSpace = buf.charAt(buf.length() - 1) > ' ';
              buf.setLength(0);
            }
            // tell the recursive call whether there is whitespace 
            // in front of the next element
            flags[FLAG_PRESERVE_LEADING_SPACE] = preserveLeadingSpace;
            addElement(e.getElement(i), flags);
            // preserver leading space only if the call did not signal 
            // whitespace at the end and the sub-elment was not a block
            preserveLeadingSpace = 
              flags[FLAG_PRESERVE_LEADING_SPACE] && !childIsBlock;
          }
          break;
        case Element.TEXT:
          // Collect text nodes until we encounter an element or the end tag;
          // make sure duplicated spaces are removed.
          if (preserveAllSpace) {
            buf.append (e.getText(i));
          } else {
            appendTrimmed(buf, e.getText(i), preserveLeadingSpace);
            if (buf.length() > 0) {
              preserveLeadingSpace = buf.charAt(buf.length() - 1) > ' ';
            }
          }
          break;
      }
    }
    // for block elements, remove whitespace at the end
    if (isBlock) {
      rTrim(buf);
    }
    // append all remaining text collected in case Element.TEXT above
    if (buf.length() > 0) {
      TextFragmentWidget fragment = new TextFragmentWidget(e,
          buf.toString(), flags[0]);
      children.addElement(fragment);
      flags[FLAG_UNASSIGNED_TRAVERSAL] = false;
      preserveLeadingSpace = !isBlock && buf.charAt(buf.length() - 1) > ' ';
    }
    // signal trailing white space to the caller
    flags[FLAG_PRESERVE_LEADING_SPACE] = preserveLeadingSpace;
  }   

  /**
   * Mark this widget and all parent block widgets as invalid (needing a call to
   * doLayout()).
   */
  public void invalidate(boolean layout) {
    if (layout) {
      layoutValid = false;
      widthValid = false;
    }
    if (getParent() != null) {
      getParent().invalidate(layout);
    }
  }

  /**
   * True if this component may receive the focus (= represents a link; input
   * elements are currently not supported).
   */
  public boolean isFocusable() {
    return focusable;
  }

  /**
   * Determines whether this element has a fixed height, or the height is
   * determined by the content.
   */
  boolean isHeightFixed() {
    if (element.getComputedStyle().lengthIsFixed(Style.HEIGHT, false)) {
      return true;
    } 
    if (!element.getComputedStyle().lengthIsFixed(Style.HEIGHT, true) || 
        !(getParent() instanceof BlockWidget)) {
      return false;
    }  

    return ((BlockWidget) getParent()).isHeightFixed();
  }

  /**
   * Lays the content out according to the CSS style associated with the 
   * element. 
   * 
   * @param outerMaxWidth the maximum available width for this element including
   *        margins, borders and padding
   * @param layoutContext the layout context of the parent element (for nested
   *        block element in regular flow, null otherwise)
   * @param shrinkWrap true if the element is a floating element or positioned
   *        element where the width is not limited by the with of the display 
   *        but must be determined from the content width.
   */
  public void doLayout(int outerMaxWidth, final int viewportWidth, 
      LayoutContext parentLayoutContext, boolean shrinkWrap) {

    // If the with did not change and none of the children changed and the
    // layout is not influenced by flow objects of the parent, we do not
    // need to re-layout
    if (HtmlWidget.OPTIMIZE && layoutValid && parentLayoutContext == null 
        && containingWidth == outerMaxWidth) {
      return;
    }

    layoutValid = true;
    containingWidth = outerMaxWidth;
    BlockWidget previousBlock = null;

    removeAllChildren();

    Style style = element.getComputedStyle();
    int display = style.getEnum(Style.DISPLAY);

    // Left and right margins are stored in members to avoid the auto margin
    // calculation in other places. 
    marginLeft = style.getPx(Style.MARGIN_LEFT, containingWidth);
    marginRight = style.getPx(Style.MARGIN_RIGHT, containingWidth);

    int marginTop = style.getPx(Style.MARGIN_TOP, containingWidth);
    int marginBottom = style.getPx(Style.MARGIN_BOTTOM, containingWidth);

    int borderLeft = style.getPx(Style.BORDER_LEFT_WIDTH, containingWidth);
    int borderTop =  style.getPx(Style.BORDER_TOP_WIDTH, containingWidth);
    int borderBottom = style.getPx(Style.BORDER_BOTTOM_WIDTH, containingWidth);
    int borderRight = style.getPx(Style.BORDER_RIGHT_WIDTH, containingWidth);

    int paddingLeft = style.getPx(Style.PADDING_LEFT, containingWidth);
    int paddingTop = style.getPx(Style.PADDING_TOP, containingWidth);
    int paddingBottom = style.getPx(Style.PADDING_BOTTOM, containingWidth);
    int paddingRight = style.getPx(Style.PADDING_RIGHT, containingWidth);

    int left = marginLeft + borderLeft + paddingLeft;
    int right = marginRight + borderRight + paddingRight;
    int top = marginTop + borderTop + paddingTop;
    int bottom = marginBottom + borderBottom + paddingBottom;

    // ShrinkWrap means we need to calculate the width based on the contents
    // for floats, table entries etc. without a fixed width
    if (shrinkWrap) {
      outerMaxWidth = style.lengthIsFixed(Style.WIDTH, true)
      ? style.getPx(Style.WIDTH, outerMaxWidth) + left + right
          : Math.min(outerMaxWidth, getMaximumWidth(containingWidth));
      // Otherwise, if this is not a table cell and the width is fixed, we need 
      // to calculate the value for auto margins here (This is typically used 
      // to center the contents).
    } else if (display != Style.TABLE_CELL && 
        style.lengthIsFixed(Style.WIDTH, true)) {
      int remaining = (containingWidth - 
          style.getPx(Style.WIDTH, containingWidth) - left - right);

      if (style.getEnum(Style.MARGIN_LEFT) == Style.AUTO) {
        if (style.getEnum(Style.MARGIN_RIGHT) == Style.AUTO) {
          marginLeft = marginRight = remaining / 2;
          left += marginLeft;
          right += marginRight;
        } else {
          marginLeft = remaining;
          left += marginLeft;
        } 
      } else {
        right += remaining;
        marginRight += remaining;
      }
    }

    boxWidth = outerMaxWidth;

    boolean fixedHeight = isHeightFixed();
    if (fixedHeight) {
      boxHeight = top + style.getPx(Style.HEIGHT, getParent().getHeight()) + bottom;
    }

    // If this is an image element, handle image here and return
    if (image != null) {
      boolean fixedWidth = style.lengthIsFixed(Style.WIDTH, true);

      if (fixedHeight && fixedWidth) {
        int w = style.getPx(Style.WIDTH, containingWidth);
        int h = style.getPx(Style.HEIGHT, containingWidth);
        if ((w != image.getWidth() || h != image.getHeight()) && w > 0 && h > 0) {
          image = GraphicsUtils.createScaledImage(image, 0, 0, image.getWidth(), 
              image.getHeight(), w, h, GraphicsUtils.SCALE_SIMPLE | GraphicsUtils.SCALE_PROCESS_ALPHA);
        }
        boxWidth = w + left + right;
      } else {
        boxWidth = image.getWidth() + left + right;
        boxHeight = image.getHeight() + top + bottom;
      }
      setWidth(boxWidth);
      setHeight(boxHeight);
      if (parentLayoutContext != null) {
        parentLayoutContext.advance(boxHeight);
      }
      return;
    }

    // calculate the maximum inner width (outerMaxWidth minus borders, padding,
    // margin) -- the maximum width available for child layout
    int innerMaxWidth = outerMaxWidth - left - right;

    // Keeps track of y-position and borders for the regular layout set by
    // floating elements. The viewport width is taken into account here.
    LayoutContext layoutContext = new LayoutContext(
        Math.min(innerMaxWidth, viewportWidth - left - right), style, parentLayoutContext, 
        left, top);

    // line break positions determined when laying out TextFragmentWidget
    // are carried over from one TextFragmentWidget to another.
    int breakPosition = -1;

    // Index of first widget on a single line. Used when adjusting position
    // for top/bottom/left/right/center align
    int lineStartIndex = 0;

    // This is the position where regular in-flow widgets are inserted. 
    // Positioned widget are inserted at the end so they are always on top of 
    // regular widgets.
    int childInsertionIndex = 0;

    // iterate all child widgets
    for (int childIndex = 0; childIndex < children.size(); childIndex++) {
      Widget child = (Widget) children.elementAt(childIndex);
      Style childStyle;
      int childPosition;
      int childWidth;

      if (child instanceof TextFragmentWidget) {
        TextFragmentWidget fragment = (TextFragmentWidget) child;
        addChild(childInsertionIndex, fragment);
        childStyle = fragment.element.getComputedStyle();
        childPosition = childStyle.getEnum(Style.POSITION);

        fragment.setX(left);
        fragment.setY(top + layoutContext.getCurrentY());
        fragment.setWidth(innerMaxWidth);

        // line-break and size the fragment
        breakPosition = fragment.doLayout(childIndex, layoutContext, 
            breakPosition, lineStartIndex, childInsertionIndex);

        // update position and status accordingly
        if (fragment.getLineCount() > 1) {       
          lineStartIndex = childInsertionIndex;
        }
        childInsertionIndex++;
        childWidth = fragment.getWidth();
      } else {
        // break positions are valid only for sequences of TextFragmentWidget
        breakPosition = -1;
        BlockWidget block = (BlockWidget) child;
        childStyle = block.element.getComputedStyle();
        int childDisplay = childStyle.getEnum(Style.DISPLAY);
        childPosition = childStyle.getEnum(Style.POSITION);
        int floating = childStyle.getEnum(Style.FLOAT);

        if (childPosition == Style.ABSOLUTE || childPosition == Style.FIXED){
          // absolute or fixed position: move block to its position; leave
          // anything else unaffected (in particular the layout context).
          addChild(block);

          block.doLayout(innerMaxWidth, viewportWidth, null, true);
          int left1 = marginLeft + borderLeft;
          int right1 = marginRight + borderRight;
          int top1 = marginTop + borderTop;
          int bottom1 = marginBottom + borderBottom;
          int iw = boxWidth - left1 - right1;

          if (childStyle.getEnum(Style.RIGHT) != Style.AUTO) {
            block.setX(boxWidth - block.boxX - right1 - block.boxWidth - 
                childStyle.getPx(Style.RIGHT, iw));
          } else if (childStyle.getEnum(Style.LEFT) != Style.AUTO) {
            block.setX(left1 + childStyle.getPx(Style.LEFT, iw) - block.boxX);
          } else {
            block.setX(left1 - block.boxX);
          }
          if (childStyle.getEnum(Style.TOP) != Style.AUTO) {
            block.setY(top1 - block.boxY +
                childStyle.getPx(Style.TOP, getHeight() - top1 - bottom1));
          } else if (childStyle.getEnum(Style.BOTTOM) != Style.AUTO) {
            block.setY(top1 - block.boxY + boxHeight -
                childStyle.getPx(Style.TOP, getHeight() - top1 - bottom1));
          } else {
            block.setY(top + layoutContext.getCurrentY() - block.boxY);
          }
        } else if (floating == Style.LEFT || floating == Style.RIGHT){
          // float: easy. just call layout for the block and place it.
          // the block is added to the layout context, but the current 
          // y-position remains unchanged (advance() is not called)

          addChild(block);
          block.doLayout(innerMaxWidth, viewportWidth, null, true);
          layoutContext.placeBox(block.boxWidth, block.boxHeight, 
              floating, childStyle.getEnum(Style.CLEAR));
          block.setX(left + layoutContext.getBoxX() - block.boxX);
          block.setY(top + layoutContext.getBoxY() - block.boxY);
        } else if (childDisplay == Style.BLOCK || 
            childDisplay == Style.LIST_ITEM) {  
          // Blocks and list items always start a new paragraph (implying a new
          // line.

          // if there is a pending line, adjust the alignement for it
          if (layoutContext.getLineHeight() > 0) {
            if (lineStartIndex != childInsertionIndex) {
              adjustLine(lineStartIndex, childInsertionIndex, layoutContext);
            }
            layoutContext.advance(layoutContext.getLineHeight());
            previousBlock = null;
          }            

          // if the position is relative, the widget is inserted on top of 
          // others. Other adjustments for relative layout are made at the end
          if (childPosition == Style.RELATIVE) {
            addChild(block);
          } else {
            addChild(childInsertionIndex++, block);
          }

          if (layoutContext.clear(childStyle.getEnum(Style.CLEAR))) {
            previousBlock = null;
          }

          // check whether we can collapse margins with the previous block
          if (previousBlock != null) {
            int m1 = previousBlock.getElement().getComputedStyle().getPx(
                Style.MARGIN_BOTTOM, outerMaxWidth);
            int m2 = childStyle.getPx(Style.MARGIN_TOP, outerMaxWidth);
            // m1 has been applied already, the difference between m1 and m2
            // still needs to be applied
            int delta;
            if (m1 < 0) {
              if (m2 < 0) {
                delta = -(m1 + m2);
              } else {
                delta = -m1;
              }
            } else if (m2 < 0) {
              delta = -m2;
            } else {
              delta = -Math.min(m1, m2);
            }
            layoutContext.advance(delta);
          }

          int saveY = layoutContext.getCurrentY();
          block.doLayout(innerMaxWidth, viewportWidth, layoutContext, false);
          block.setX(left - block.boxX);
          block.setY(top + saveY - block.boxY);
          lineStartIndex = childInsertionIndex;
          previousBlock = block;
        } else { 
          // inline-block, needs to be inserted in the regular text flow 
          // similar to text fragments.
          addChild(childInsertionIndex, block);
          previousBlock = null;
          block.doLayout(innerMaxWidth, viewportWidth, null, childDisplay != Style.TABLE);
          int avail = layoutContext.getHorizontalSpace(block.boxHeight);
          if (avail >= block.boxWidth) {
            layoutContext.placeBox(
                block.boxWidth, block.boxHeight, Style.NONE, 0);
          } else {
            // line break necessary
            adjustLine(lineStartIndex, childInsertionIndex, layoutContext);
            lineStartIndex = childInsertionIndex;
            layoutContext.advance(layoutContext.getLineHeight());
            layoutContext.placeBox(
                block.boxWidth, block.boxHeight, Style.NONE, 0);    
            layoutContext.advance(layoutContext.getBoxY() - 
                layoutContext.getCurrentY());
            layoutContext.setLineHeight(block.boxHeight);
          }
          block.setX(left + layoutContext.getBoxX() - block.boxX);
          block.setY(top + layoutContext.getCurrentY() - block.boxY);
          childInsertionIndex++;
        }
        childWidth = block.boxWidth;
      }

      // Make adjustments for relative positioning
      if (childPosition == Style.RELATIVE) {
        if (childStyle.isSet(Style.RIGHT)) {
          child.setX(child.getX() + boxWidth - childWidth - 
              childStyle.getPx(Style.RIGHT, getWidth()));
        } else {
          child.setX(child.getX() + childStyle.getPx(Style.LEFT, getWidth()));
        }
        child.setY(child.getY() + childStyle.getPx(Style.TOP, getHeight()));
      }
    }

    // Still need to adjust alignment if there is a pending line.
    if (lineStartIndex != childInsertionIndex && 
        layoutContext.getLineHeight() != 0) {
      adjustLine(lineStartIndex, childInsertionIndex, layoutContext);
    }

    // make sure currentY() reflects the full contents of the layout context
    layoutContext.advance(layoutContext.getLineHeight());
    if (parentLayoutContext == null) {
      layoutContext.clear(Style.BOTH);
    }

    // if the height is not fixed, set it to the actual height here
    if (!fixedHeight) {
      boxHeight = (top + layoutContext.getCurrentY() + bottom);
    }

    // Adjust the parent's layout context to the new y positon
    if (parentLayoutContext != null) {
      parentLayoutContext.adjustCurrentY(layoutContext.getCurrentY());
      parentLayoutContext.advance(
          boxHeight - layoutContext.getCurrentY() - top);
    }

    // adjust dimensions and box coordinates for the case where children are 
    // partially outside the dimensions
    adjustDimensions();
  }

  /**
   * Adjust dimensions to include all child widgets and adjust 
   * boxX, boxY, boxW, and boxH to reflect the original position and dimensions
   */
  private void adjustDimensions() {  
    int minX = 0;
    int minY = 0;
    int maxX = boxWidth;
    int maxY = boxHeight;
    Style style = element.getComputedStyle();

    if (!(this instanceof HtmlWidget) && 
        style.getEnum(Style.OVERFLOW) != Style.HIDDEN) {

      int cnt = getChildCount();
      for (int i = 0; i < cnt; i++) {
        Widget child = getChild(i);
        minX = Math.min(minX, child.getX());
        minY = Math.min(minY, child.getY());
        maxX = Math.max(maxX, child.getX() + child.getWidth());
        maxY = Math.max(maxY, child.getY() + child.getHeight());
      }

      boxX = -minX;
      boxY = -minY;

      if (minX < 0 || minY < 0) {
        for (int i = 0; i < cnt; i++) {
          Widget child = getChild(i);
          child.setX(child.getX() - minX);
          child.setY(child.getY() - minY);
        }
      }
    }
    setDimensions(getX() - boxX, getY() - boxY, maxX - minX, maxY - minY);
  }

  /**
   * Dump the internal state of this object for debugging purposes.
   */
  public void handleDebug() {
    if (DEBUG) {
	  System.out.println("Element path:");
      element.dumpPath();
      System.out.println("Computed Style: ");
      element.dumpStyle();

      System.out.println();
      System.out.println("Width: " + getWidth() + 
        " min: " + getMinimumWidth(containingWidth) + 
        " max: " + getMaximumWidth(containingWidth) + 
        " spec: " + getSpecifiedWidth(containingWidth) +
        " x: " + getX() + " y: " + getY() + 
        " marginLeft: " + marginLeft + " marginRight: " + marginRight);
    }
  }

  /**
   * Adjust the horizontal align for child widgets on the same line.
   * 
   * @param startIndex first child widget on the line
   * @param endIndex last child widget on the line + 1
   * @param layoutContext Layout context object holding all relevant layout
   *    information (line height, available width, space distribution factors)
   */
  void adjustLine(int startIndex, int endIndex, LayoutContext layoutContext) {
    int lineHeight = layoutContext.getLineHeight();
    int remainingWidth = layoutContext.getHorizontalSpace(lineHeight);    
    int indent = layoutContext.getAdjustmentX(remainingWidth);

    for (int i = startIndex; i < endIndex; i++) {
      Widget w = getChild(i);
      if (w instanceof TextFragmentWidget
          && ((TextFragmentWidget) w).getLineCount() > 1) {
        Util.assertEquals(i, startIndex);
        TextFragmentWidget tfw = (TextFragmentWidget) w;
        tfw.adjustLastLine(indent, lineHeight, layoutContext);
      } else {
        w.setX(w.getX() + indent);
        int dy = layoutContext.getAdjustmentY(lineHeight - w.getHeight());
        w.setY(w.getY() + dy);
      }
    }
  }

  /**
   * Adjust the vertical child positions according to the vertical alignment
   * if there is excessive space in this block that can be distributed.
   * 
   * @param verticalGap the space to distribute
   */
  void adjustVerticalPositions(int verticalGap) {
    int factorY = 0;
    switch (element.getComputedStyle().getEnum(Style.VERTICAL_ALIGN)) {
      case Style.TOP:
        factorY = 0;
        break;
      case Style.BOTTOM:
        factorY = 2;
        break;
      default:
        factorY = 1;
    }

    int addY = factorY * verticalGap / 2;
    if (addY > 0) {
      for (int childIndex = 0; childIndex < getChildCount(); childIndex++) {
        Widget w = getChild(childIndex);
        w.setY(w.getY() + addY);
      }
    }
  }

  /**
   * Returns the minimum width of this block including borders.
   * 
   * @param containerWidth the width of the container
   */
  public int getMinimumWidth(final int containerWidth) {
    if (!widthValid) {
      calculateWidth(containerWidth);
    }
    return minimumWidth;
  }

  /**
   * Returns the maximum width of this block including borders.
   * 
   * @param containerWidth the width of the container
   */
  public int getMaximumWidth(final int containerWidth) {
    if (!widthValid) {
      calculateWidth(containerWidth);
    }
    return maximumWidth;    
  }

  /**
   * Returns the specified width of this block including borders.
   * 
   * @param containerWidth the width of the container
   */
  public int getSpecifiedWidth(int containerWidth) {
    Style style = element.getComputedStyle();
    return style.getPx(Style.WIDTH, containerWidth) +
    style.getPx(Style.BORDER_LEFT_WIDTH) + 
    style.getPx(Style.BORDER_RIGHT_WIDTH) + 
    style.getPx(Style.MARGIN_LEFT) +  style.getPx(Style.MARGIN_RIGHT) + 
    style.getPx(Style.PADDING_LEFT) + style.getPx(Style.PADDING_RIGHT);
  }
  
  
  /**
   * Calculates the minimum and maximum widths of a block and stores them
   * in minimumWidth and maximumWidth. Do not call this method or use
   * the values directly, use getMinimumWidth() or getMaximumWidht() instead.
   * 
   * @param containerWidth Width of the container.
   */
  protected void calculateWidth(int containerWidth) {
    Style style = element.getComputedStyle();
    int border = style.getPx(Style.BORDER_LEFT_WIDTH) + 
    style.getPx(Style.BORDER_RIGHT_WIDTH) + 
    style.getPx(Style.MARGIN_LEFT) +  style.getPx(Style.MARGIN_RIGHT) + 
    style.getPx(Style.PADDING_LEFT) + style.getPx(Style.PADDING_RIGHT);

    int display = style.getEnum(Style.DISPLAY);

    int minW = style.getPx(Style.WIDTH);
    int maxW = minW;

    int currentLineWidth = 0;

    if (display != Style.TABLE_CELL && 
        style.lengthIsFixed(Style.WIDTH, false)) {
      minW = maxW = style.getPx(Style.WIDTH, containerWidth);
    } else if (image != null) {
      maxW = minW = image.getWidth();
    } else {

      int childContainerWidth = display == Style.TABLE_CELL 
      ? style.getPx(Style.WIDTH, containerWidth) + border
          : containerWidth - border;

      for (int i = 0; i < children.size(); i++) {
        Widget child = (Widget) children.elementAt(i);
        if (child instanceof TextFragmentWidget) {
          TextFragmentWidget fragment = (TextFragmentWidget) child;
          int[] widths = fragment.element.getComputedStyle().getCharWidths();
          Font font = fragment.getFont();
          String text = fragment.text;

          char c = 160;
          int wordWidth = 0;
          for (int j = 0; j < text.length(); j++) {
            char d = text.charAt(j);
            if (Util.canBreak(c, d)) {
              minW = Math.max(minW, wordWidth);
              currentLineWidth += wordWidth;
              if (c == '\n') {
                maxW = Math.max(maxW, currentLineWidth);
                currentLineWidth = 0;
              }
              wordWidth = 0;
            }
            c = d;
            wordWidth += c < widths.length ? widths[c] : font.charWidth(c);
          }
          if (c == '\n') {
            maxW = Math.max(maxW, currentLineWidth);
            currentLineWidth = 0;
          }

          minW = Math.max(minW, wordWidth);
          currentLineWidth += wordWidth;
        } else {
          BlockWidget block = (BlockWidget) child;
          Style childStyle = block.getElement().getComputedStyle();

          int childDisplay = childStyle.getEnum(Style.DISPLAY);
          if (childStyle.getEnum(Style.FLOAT) == Style.NONE && 
              (childDisplay == Style.BLOCK || childDisplay == Style.LIST_ITEM)) {
            maxW = Math.max(maxW, currentLineWidth);
            maxW = Math.max(maxW, block.getMaximumWidth(childContainerWidth));
            currentLineWidth = 0;
          } else {
            currentLineWidth += block.getMaximumWidth(childContainerWidth);
          }

          minW = Math.max(minW, block.getMinimumWidth(childContainerWidth));
        } 
      }
    }    

    maxW = Math.max(maxW, currentLineWidth);

    minimumWidth = minW + border;
    maximumWidth = maxW + border;

    widthValid = true;
  }

  /**
   * Draws the border and fills the area with the background color if set. 
   */
  public void drawContent(Graphics g, int dx, int dy) {
    dx += boxX;
    dy += boxY;

    Style style = element.getComputedStyle();

    int marginTop = style.getPx(Style.MARGIN_TOP, containingWidth);
    int marginBottom = style.getPx(Style.MARGIN_BOTTOM, containingWidth);

    int borderTop = style.getPx(Style.BORDER_TOP_WIDTH, containingWidth);
    int borderRight = style.getPx(Style.BORDER_RIGHT_WIDTH, containingWidth);
    int borderBottom = style.getPx(Style.BORDER_BOTTOM_WIDTH, containingWidth);
    int borderLeft = style.getPx(Style.BORDER_LEFT_WIDTH, containingWidth);

    // set to first pixel on border == outside padding area
    int x0 = dx + marginLeft + borderLeft - 1;
    int x1 = dx + boxWidth - marginRight - borderRight;
    int y0 = dy + marginTop + borderTop - 1;
    int y1 = dy + boxHeight - marginBottom - borderBottom;

    int bg = style.getValue(Style.BACKGROUND_COLOR);

    if ((bg & 0x0ff000000) != 0) {
      g.setColor(bg);
      g.fillRect(x0 + 1, y0 + 1, x1 - x0 - 1, y1 - y0 - 1);
    }
    if (style.backgroundImage != null && style.backgroundImage[0] != null) {
      Image img = style.backgroundImage[0];
      int cx = g.getClipX();
      int cy = g.getClipY();
      int cw = g.getClipWidth();
      int ch = g.getClipHeight();
      int repeat = style.getEnum(Style.BACKGROUND_REPEAT);

      int bgX = repeat == Style.REPEAT_X || repeat == Style.REPEAT ? 0 : 
        style.getBackgroundReferencePoint(Style.BACKGROUND_POSITION_X, 
            x1 - x0 - 1, img.getWidth());
      int bgY = repeat == Style.REPEAT_Y || repeat == Style.REPEAT ? 0 : 
        style.getBackgroundReferencePoint(Style.BACKGROUND_POSITION_Y, 
            y1 - y0 - 1, img.getHeight());

      g.clipRect(x0 + 1, y0 + 1, x1 - x0 - 1, y1 - y0 - 1);
      if (repeat == Style.REPEAT_Y || repeat == Style.REPEAT) {
        do {
          if (repeat == Style.REPEAT) {
            do {
              g.drawImage(img, x0 + 1 + bgX, y0 + 1 + bgY, Graphics.TOP | Graphics.LEFT);
              bgX += img.getWidth();
            } while (bgX < x1 - x0);
            bgX = 0;
          } else {
            g.drawImage(img, x0 + 1 + bgX, y0 + 1 + bgY, Graphics.TOP | Graphics.LEFT);
          }
          bgY += img.getHeight();
        } while (bgY < y1 - y0);
      } else if (repeat == Style.REPEAT_X) {
        do {
          g.drawImage(img, x0 + 1 + bgX, y0 + 1 + bgY, Graphics.TOP | Graphics.LEFT);
          bgX += img.getWidth();
        } while (bgX < x1 - x0);
      } else {
        g.drawImage(img, x0 + 1 + bgX, y0 + 1 + bgY, Graphics.TOP | Graphics.LEFT);
      }
      g.setClip(cx, cy, cw, ch);
    } 

    if (borderTop > 0) {
      g.setColor(style.getValue(Style.BORDER_TOP_COLOR));
      int dLeft = (borderLeft << 8) / borderTop;
      int dRight = (borderRight << 8) / borderTop;
      for (int i = 0; i < borderTop; i++) {
        g.drawLine(x0 - ((i * dLeft) >> 8), y0 - i, x1 + ((i * dRight) >> 8), y0 - i);
      }
    }
    if (borderRight > 0) {
      g.setColor(style.getValue(Style.BORDER_RIGHT_COLOR));
      int dTop = (borderTop << 8) / borderRight;
      int dBottom = (borderBottom << 8) / borderRight;
      for (int i = 0; i < borderRight; i++) {
        g.drawLine(x1 + i, y0 - ((i * dTop) >> 8), x1 + i, y1 + ((i * dBottom) >> 8));
      }
    }
    if (borderBottom > 0) {
      g.setColor(style.getValue(Style.BORDER_BOTTOM_COLOR));
      int dLeft = (borderLeft << 8) / borderBottom;
      int dRight = (borderRight << 8) / borderBottom;
      for (int i = 0; i < borderBottom; i++) {
        g.drawLine(x0 - ((i * dLeft) >> 8), y1 + i, x1 + ((i * dRight) >> 8), y1 + i);
      }
    }
    if (borderLeft > 0) {
      g.setColor(style.getValue(Style.BORDER_LEFT_COLOR));
      int dTop = (borderTop << 8) / borderLeft;
      int dBottom = (borderBottom << 8) / borderLeft;
      for (int i = 0; i < borderLeft; i++) {
        g.drawLine(x0 - i, y0 - ((i * dTop) >> 8), x0 - i, y1 + ((i * dBottom) >> 8));
      }
    }
    if (style.getEnum(Style.DISPLAY) == Style.LIST_ITEM) {
      g.setColor(style.getValue(Style.COLOR));

      Font f = style.getFont();
      // en space -- see http://en.wikipedia.org/wiki/En_%28typography%29
      // using this because on blackberry digit widths are messed up
      int en = f.getHeight() / 2;
      // round up so in doubt the dot size is a bit bigger and the pos is lower 
      int size = (f.getHeight() + 2) / 3; 
      int liy = y0 + 1 + style.getPx(Style.PADDING_TOP, containingWidth);

      switch (style.getValue(Style.LIST_STYLE_TYPE)) {
        case Style.SQUARE:
          g.fillRect(dx - size - en, liy + size, size, size);
          break;
        case Style.CIRCLE:
          g.drawRoundRect(dx - size - en, liy + size, size, size, size / 2, size / 2);
          break;
        case Style.DECIMAL:
          String nr = (getParent().indexOfChild(this) + 1) + ". ";
          g.setFont(f);
          g.drawString(nr, dx - f.stringWidth(nr), liy, Graphics.TOP | Graphics.LEFT);
          break;
        case Style.DISC:
          g.fillRoundRect(dx - size - en, liy + size, size, size, size / 2,
              size / 2);
          break;
      }
    }
    if (image != null) {
      g.drawImage(image, x0 + 1 + style.getPx(Style.PADDING_LEFT, containingWidth), y0 + 1 + style.getPx(Style.PADDING_TOP, containingWidth), Graphics.TOP | Graphics.LEFT);
    }
  }

  public void drawTree(Graphics g, int dx, int dy, int clipX, int clipY, int clipW, int clipH) {
    int overflow = element.getComputedStyle().getEnum(Style.OVERFLOW);

    drawContent(g, dx, dy);
    if (overflow != Style.HIDDEN) {
      super.drawTree(g, dx, dy, clipX, clipY, clipW, clipH);
      drawFocusRect(g, dx, dy);
    } else {
      g.clipRect(dx, dy, getWidth(), getHeight());
      super.drawTree(g, dx, dy, g.getClipX(), g.getClipY(), g.getClipWidth(), g.getClipHeight());
      drawFocusRect(g, dx, dy);
      g.setClip(clipX, clipY, clipW, clipH);
    }
  }

  /**
   * Draws the focus if this widget is directly focused. When
   * HtmlDocumentWidget.DEBUG is set, a red outline of this widget is drawn.
   */
  protected void drawFocusRect(Graphics g, int dx, int dy) {
    // TODO(haustein) Replace with pseudo-class :focus handling
	
    if (isFocused() && focusable) {
      Skin.get().drawFocusRect(g, dx + boxX, dy + boxY, boxWidth, boxHeight, false);
    }

    if (DEBUG && HtmlWidget.debug == this) {
      int x0 = dx + boxX;
      int y0 = dy + boxY;
      int x1 = dx + boxWidth - 1;
      int y1 = dy + boxHeight - 1;

      if (boxX != 0 || boxY != 0 || boxWidth != getWidth() || boxHeight != getHeight()) {
        g.setColor(0x0ff0000);
        g.drawRect(dx, dy, getWidth(), getHeight());
      }
      
      Style style = element.getComputedStyle();
      for (int i = 0; i < 3; i++) {
        int id;
        int color;
        // Colors: Yellow: margin; Purple: padding; light blue: block-level element.
        switch (i) {
        case 0:
          color = 0x88ffff00;
          id = Style.MARGIN_TOP;
          break;
        case 1:
          color = 0x88ff0000;
          id = Style.BORDER_TOP_WIDTH;
          break;
        default: 
          color = 0x088ff00ff;
          id = Style.PADDING_TOP;
        }

        int top = style.getPx(id, containingWidth);
        int right = i == 0 ? marginRight : style.getPx(id + 1, containingWidth);
        int bottom = style.getPx(id + 2, containingWidth);
        int left = i == 0 ? marginLeft : style.getPx(id + 3, containingWidth);
        
        GraphicsUtils.fillRectAlpha(g, x0, y0, x1 - x0 + 1, top, color);
        GraphicsUtils.fillRectAlpha(g, x0, y1 - bottom, x1 - x0 + 1, bottom, color);

        GraphicsUtils.fillRectAlpha(g, x0, y0 + top, left, y1 - y0 + 1 - top - bottom, color);
        GraphicsUtils.fillRectAlpha(g, x1 - x0 + 1 - right, y0 + top, right, y1 - y0 + 1 - top - bottom, color);

        y0 += top;
        x0 += left;
        y1 -= bottom;
        x1 -= right;
      }
      GraphicsUtils.fillRectAlpha(g, x0, y0, x1 - x0 + 1, y1 - y0 + 1, 0x440000ff);
    }
  }

  /**
   * Notify the element that the focus was received and request a redraw.
   */
  protected void handleFocusChange(boolean focused) {
    if (isFocusable() && focused) {
      element.setFocused();
    }
    invalidate(false);
  }
  
  /**
   * Returns the element owning this Widget.
   */
  public Element getElement() {
    return element;
  }

  /**
   * Appends the given text to the given string buffer, normalizing whitespace
   * and trimming the string as specified.
   * 
   * @param pending the buffer to append to
   * @param text text string to be appended
   * @param ltrim remove whitespace before the first character
   */
  static void appendTrimmed(StringBuffer pending, String text, 
      boolean preserveLeadingSpace) {

    if (text == null) {
      return;
    }

    boolean wasSpace = !preserveLeadingSpace;

    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);

      if (c == '\r') {
        continue;
      }

      if (c <= ' ') {
        if (!wasSpace) {
          pending.append(' ');
          wasSpace = true;
        }
      } else {
        pending.append(c);
        wasSpace = false;
      }
    }
  }

  /**
   * Removes white space at the end of the given string buffer.
   */
  static void rTrim(StringBuffer buf) {
    if (buf.length() > 0 && buf.charAt(buf.length() - 1) == ' ') {
      buf.setLength(buf.length() - 1);
    }
  }
}
