# Introduction #

This page describes how to implement Javascript objects in Java


# Details #

The wiki page "AccessingJavaFromJavascript" describes how to create a JsObject subclass to make Java methods accessible from Javascript.

To implement whole Javascript objects in Java, some additional steps are required:

  1. We need to create a prototype holding the methods
  1. We need a prototype-based constructor
  1. A factory
  1. Register the factory with the global environment

So if we want to transform the global static counter created in "AccessingJavaFromJavascript" into a counter object we can instantiate from Javascript, we need to add create a prototype and to replace the previous constructor with a prototype-based constructor. All method IDs need to be registered with the prototype now:

```
  public static final JsObject COUNTER_PROTOTYPE = new Counter(OBJECT_PROTOTYPE)
      .add("add", new JsFunction (Counter.ID_ADD, 1));

  public Counter(JsObject prototype) {
    super(prototype);
  }
```

The eval method and method IDs do not need to be changed.

Note that in the example, the prototype is an instance of the class we will be using for the instances as well. This is strictly necessary only if we need to support static Javascript methods.

We still need a factory for the new object and to register it with the global environment. Here, we do both tasks in a single class:

```
public class MySystem implements JsObjectFactory {

  static final int FACTORY_ID_COUNTER = 0;
  static MySystem instance = new MySystem();

  public static JsObject createGlobal(){
    JsObject global = JsSystem.createGlobal();
    global.addVar("Counter", new JsFunction(instance, JsSystem.FACTORY_ID_COUNTER, 
        JsDate.DATE_PROTOTYPE, JsObject.ID_NOOP, 0));
    return global;
  }

  public JsObject newInstance(int type) {
    switch(type){
      case JsSystem.FACTORY_ID_COUNTER: 
        return new Counter(Counter.COUNTER_PROTOTYPE);
      default:
        throw new IllegalArgumentException();
    } 
  }
```

The last argument in the factory registration call is the method id of the constructor. Since we do not perform any functionality in the constructor, we use ID\_NOOP here, which is defined in JsObject as an empty method.

When using MySystem.createGlobal() to create the global environment (instead of JsSystem), a new counter can now be created in Javascript with "new Counter()".

The factory should be reused for all custom classes if feasible in order to avoid unnecessary overhead.

## Registering Subclasses ##

Subclasses can be registered like classes inherited directly from JsObject.

  * Please make sure the superclass prototype (not the general JsObject prototype) is used in the prototype declaration for the subclass
  * The method IDs must not overlap, except for methods overwritten in the subclass.