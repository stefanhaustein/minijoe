# Introduction #

This Wiki page defines the binary file format used by MiniJoe

# Details #

A MiniJoe binary file consists of a stream of bytes, 16-bit, 32-bit and 64-bit values are stored as the required number of bytes in big-endian format, this is the same format as supported by the Java java.io.DataInputStream and java.io.DataOutputStream interfaces.

A MiniJoe binary file consists of a magic number followed by one or more blocks. A block starts with an identifying byte following by the block data. Whilst most blocks have their length as an short immediately following the type byte, the interpretation of the length is block dependent and described below. Some of these blocks may contain other blocks, which in turn may contain more blocks.


| MiniJoe | Magic  Block |
|:--------|:-------------|
| Magic   | uint32       |
| Block   | Type  Data  ... |
| Type    | uint8        |
| Data    | uint8  ...   |

The following blocks are currently defined:

| **Type** | **Description** |
|:---------|:----------------|
| 0x00     | Comment         |
| 0x10     | String Table    |
| 0x20     | Double Literals |
| 0x30     | String Literals |
| 0x40     | Regex Literals  |
| 0x50     | Function Literals |
| 0x60     | Variable Names  |
| 0x80     | Byte code       |
| 0xE0     | Line number data |
| 0xF0     | Debug data      |
| 0xFF     | End Marker      |

## Magic ##

```
Magic   := "M" "i" "n" "i" "J" "o" "e" Version
Version := uint8
```

## Comment Block ##

```
CommentBlock := 0x00  Length  Data
Length       := uint16
Data         := uint8[Length]
```

This block is used for free-form comments about the MiniJoe binary, its contents are ignored (and possibly discarded) by the client-side runtime. The length field gives the number of data bytes.

(The intention is that the length and data is read by a single call to java.io.DataInputStream.readUtf8).

## Global String Table Block ##

```
StringTableBlock  :=  0x10  Count  String[Count]
Count             :=  uint16
String            :=  Length  uint8[Length]
Length            :=  uint16
```

The global string table contains the utf8 data for all strings within this program, and is referenced by the string literal block, the regex literal block, the variable name and possibly by the debug data block. The count field is the number of string entries, each string entry has a uint16 length followed by the string data, as if written by java.io.DataOutputStream.writeUtf8. The strings in this table are implicitly numbered according to their position within this block.

## Double Literal Block ##

```
DoubleLiteralBlock  :=  0x20  Count  Double[Count]
Count               :=  uint16
Double              :=  uint64
```

## String Literal Block ##

```
StringLiteralBlock  :=  0x30  Count  Index[Count]
Count               :=  uint16
Index               :=  uint16
```

The String Literal block contains indexes into the global string table.

## Regex Literal Block ##

```
RegexLiteralBlock  :=  0x40  Count  Index[Count]
Count              :=  uint16
Index              :=  uint16
```

The Regex Literal table contains indexes into the global string table.

## Function Literal Block ##

```
FunctionLiteralBlock  :=  0x50  Count  FunctionLiteral[Count]
Count                 :=  uint16
```

A Function Literal block contains one or more Function Literals, each function literal contains one or more other blocks.

```
FunctionLiteral  :=  Block  ...  0xFF
Count            :=  uint16
```

## Variable Name Block ##

```
VariableNameBlock  :=  0x60  Count  Index[Count]
Count              :=  uint16
Index              :=  uint16
```

The Local Variable Name Block contains indexes into the Global String Table and gives a mapping from local variable names to local variable slots. The variable names are implicitly numbered according to the position within this block.


## Code Block ##

```
ByteCodeBlock       :=  0x80  Locals  Parameters  Flags  Length  Code[Length]
Locals              :=  uint16    // number of local variable slots required (including parameters)
Parameters          :=  uint16    // number of declared parameters
Flags               :=  uint8     // flags, see below
Count               :=  uint16    // number of code bytes
Code                :=  uint8
```

## Flag Bits ##

| **Bit** | **Description** |
|:--------|:----------------|
| 0       | if set, the code does not contain closures or with statements and local variables may be allocated via the stack |
| 1..7    | reserved        |


If there is a Variable Name Block corresponding to this Code Block, then the number of local variable must be at least equal to the number of variable name mappings (it may be larger, in case the compiler wishes to use local variable slots to store intermediate values, i.e. the results of common subexpressions).

The parameters are mapped to the first 0 ... N local variable slots.

we should consider a flag that copies the global string table to the local string table (consider a JSON data transfer) -Stefan Haustein 10/17/07 4:47 PM

## Line Number Table ##

```
LineNumberBlock     := 0xE0  Length  [ProgramCounter, LineNumber]*
Length              := uint16
ProgramCounter      := uint16
LineNumber          := uint16
```

The Length attribute gives the number of [ProgramCounter, LineNumber] pairs in this block. The pairs occur in strictly increasing ProgramCounter order.

## End Marker ##

```
EndMarker  := 0xFF
```

Marks the end of a function literal or of the file.

## Scoping ##

A MiniJoe binary file has two levels or scopes: 'program scope' are blocks are the top level, 'function scope' are blocks within a Function Literal. Note that a Function Literal may contain a Function Literal Block which will contain more Function Literals.

At each level no more than one block of each type is allowed. Except were described below, the order of blocks within each scope doesn't matter, however the order would normally be in the order they are described above.

At the program level, the following blocks are valid:

  * Comment Block - if present this block must be first
  * Global String Table - optional, but MUST be present if there are any String Literal Blocks or Regex Literal Blocks in the file. This block must also come before any String or Regex Literal block at the program level, and would normally be the first block with in a file (or second if there exists a comment block).
  * Number Literal Block
  * String Literal Block
  * Regex Literal Block
  * Function Literal Block
  * Byte Code Block
  * Line Number Block
  * Debug Block
  * End Of File Block - MUST be present, at the end of the file


At the function level the following blocks are valid:

  * Comment Block - if present this block must be the first block in a function literal
  * Number Literal Block
  * String Literal Block
  * Regex Literal Block
  * Function Literal Block
  * Variable Name Block
  * Byte Code Block
  * Debug Block
  * End Marker - MUST be present, at the end of the function literal