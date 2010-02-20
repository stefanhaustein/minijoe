package com.google.minijoe.html;

/**
 * This interface can be used to extend the range of supported 
 * Elements in the HtmlWidget.
 * 
 * @author Stefan Haustein
 */
public interface ElementHandler {
  public static final Object DEFAULT_HANDLING = new Object();
  public static final Object IGNORE_ELEMENT = new Object();
  
  /**
   * Handle the given element. If a BlockWidget is returned, it is inserted
   * into the widget structure. IF DEFAULT_HANDLING is returned, the element
   * is treated as a regular element. If IGNORE_ELEMENT is returned, the
   * element is ignored.
   */
  public Object handle(Element element, boolean inBody);
}
