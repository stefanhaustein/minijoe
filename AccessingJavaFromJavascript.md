# Introduction #

This page describes how to access Java methods from Javascript


# Details #

Setting values in the Javascript environment from Java is relatively simple. For instance, to create a variable PI and set it to 3.14 from Java, we can just call

```
environment.add("PI", new Double(3.14));
```

It is simple to try this in the MjShell example, just put the above line into the constructor (replacing `environment` with `global`) and verify it works as intended by evaluating the expression `PI`.


Making Java _methods_ available in Javascript is a bit more tricky. Here, we use a simple counter (implemented in Java) to illustrate the steps:

## 1. Create a subclass of JsObject ##

```
class Counter extends JsObject {
```

## 2. Define integer constants for all methods and variables that need to be exposed ##

The constantants should start above 100 since lower values are reserved for JsObject itself.

```
static final int ID_ADD = 100;
```

## 3. Implement the Dispatcher ##

The dispatcher is needed since CLDC does not provide sufficient reflection capabilities to call methods by name.

```
 public void evalNative(int id, JsArray stack, int sp, int parCount){
    switch(id){
      case ID_ADD:
        counter  = counter + stack.getNumber(sp + 2);
        stack.setNumber(sp, counter);
        break;
        
    default:
      super.evalNative(id, stack, sp, parCount);
    }
  }
```

**Note**: Always add a default case that calls super.evalNative() -- otherwise, inherited methods won't work


## 4. Implement the Java Constructor ##

In this case, we just register the add method in the constructor and chain up our environment with the default global environment.

When we want to use our function, we need to use a Counter instance as the environment instead of the global environment provided by `JsSystem.createGlobal();`

```
public Counter() {
   super(new JsObject(OBJECT_PROTOTYPE));
  scopeChain = JsSystem.createGlobal();
  add("add", new JsFunction (Counter.ID_ADD, 1));
}
```


## 5. Test our new Environment ##

Add the following line to the constructor of the shell example (replacing the global environment used in `eval()` with our own global environment, additionally providing the counter:

```
  global = new Counter();
```

Now, it should be possible to test our counter using `add()` calls in the shell.

Creating Javascript objects based on a Java implementation is similar, but requires some additional steps (see "ImplementingJavascriptObjectsInJava")