# Introduction #

This is a short list of recommended steps to become familiar with the code base.

# Details #

## Preparation ##

Download and unpack the zip or check out the code base with SVN.

You will also need:

  * Ant: http://ant.apache.org/
  * Antenna: http://antenna.sourceforge.net/
  * SUN Wireless Toolkit (WTK): http://java.sun.com/products/sjwtoolkit/

The MiniJoe Ant scripts require that

  * Antenna is installed in the Ant extensions directory
  * The environment variable WTK\_HOME points to the WTK home directory.

## Code Examples ##

### The Shell ###

The shell example is located at `src/com/google/minijoe/samples/shell/MjShell.java`. Its purpose is to illustrates how to compile and run Javascript code using the Eval class.

The shell example can be built using the ant target "build-shell". To run the shell in the WTK, use the target "`run-shell`".

To run the shell and to verify that your build environment is set up correctly, change to the `minijoe` directory and type "`ant run-shell`". After compilation, the WTK emulator should pop up and display an empty text box. Type a Javascript expression in the text box, e.g. "Hello" + "World" or 4-4-4 and use the eval command to verify everything is working as expected.


### The Compiler and Runtime ###

The shell abstracts from the fact that the MiniJoe compiler and interpreter are actually two separate entities. In actual J2ME environments, one will probably want to take advantage of this separation and include only the interpreter in the client, put the compiler on a server, and send the byte code from the server to the client.

The compiler example is located at `src/com/google/minijoe/samples/compiler/MjC.java`. The ant target for building the compiler is "build-compiler". The Javascript examples in the `javascript` directory can be compiled to MiniJoe byte code using the ant target "generate-bytecode".

The runtime example is located at `src/com/google/minijoe/samples/runtime/`; the corresponding ant target is "build-runtime". The ant target builds a MIDlet that includes the byte code generated with the compiler; there is also an ant target for running the MIDlet called "run-runtime".

Note that the network example contains the Javascript byte code instead of pulling it from the net dynamically. It is accessed in the client (`MjRuntime.java`) using `getClass().getResourceAsStream(...)`. That is to keep the set up simple and everything in the examples self-contained.








