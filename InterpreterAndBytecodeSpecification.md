# Introduction #

This Wiki Page specifies the MiniJoe byte code and interpreter.

# Details #

The byte code consists of two types of opcodes:

  1. Simple one-byte opcodes (opcodes > 0)
  1. Special operations with immediate data such as GO, IF, PUSH\_FLOAT etc. (opcodes < 0)


Opcodes with variable-length immediate data may be two or three bytes long, with the last bit encoding the length. If the bit is set, the data length is two bytes, otherwise it is a byte


| **Symbol** | **Description** |
|:-----------|:----------------|
| stack      | Denotes the operand stack (Implemented internally as a single Javascript array) |
| sp         | The current stack pointer |
| imm        | Immediate data (one or two bytes following the instruction opcode, as described above). TODO: distinguish signed/unsigned -Stefan Haustein 10/18/07 4:09 PM |
| context    | Denotes the current evaluation context / scope chain (= activation, variables) |
| bp         | The base pointer; pointing to the location of the first parameter / local variable on the stack. |
| tmp        | A temporary variable. |
| pc         | The program counter. |
| this       | this pointer    |
| strings    | Table with string constants |
| fuctions   | Table with function literals |
| numbers    | Table with double literals |


| **Opcode** | **Name**    | **Semantics** | **Description** |
|:-----------|:------------|:--------------|:----------------|
| 0x00       | NOP         |	- 	           | No operation - does nothing  |
| 0x01       | ADD         | `stack[sp-2] += stack[sp-1]; sp--` | Addition        |
| 0x02       | AND         | `stack[sp-2] &= stack[sp-1]; sp--` | Binary AND      |
| 0x03       | APPEND      |	`tmp = stack[sp-2]; tmp[tmp.length] = stack[sp-1]; sp--`  | Append to array. Used in literal array initializers. |
| 0x04       | ASR         | `stack[sp-2] >>= stack[sp-1]; sp--` | Arithmetic shift to the right |
| 0x05       | ENUM        | `stack[sp-1] = stack[sp-1].keys()` | Puts a key enumeration on the stack |
| 0x06       | IN          | `stack[sp-2] = stack[sp-2] in stack[sp-1]; sp--` | determines whether the element at sp-2 is contained in the element at sp-1` |
| 0x07       | DIV         | `stack[sp-2] /= stack[sp-1]; sp--` | Division        |
| 0x08       | DUP         | `stack[sp] = stack[sp-1]; sp++` | 	Duplicate element at the top of the stack |
| 0x09       | EQEQ        | `stack[sp-2] = (stack[sp-2] == stack[sp-1]); sp--` | Comparison      |
| 0x0a       | CTX\_GET    | `stack[sp-1] = context[stack[sp-1]` | Read from current context. |
| 0x0b       | GET         | `stack[sp-2] = stack[sp-1][stack[sp-2]]; sp--` | Read member from environment on the stack |
| 0x0c       | CTX         | `stack[sp] = context; sp++` | put the current context on the stack. |
| 0x0d       | DEL         | `stack[sp-2] = delete stack[sp-2].stack[sp-1]; sp--` |Delete the property denoted by the two elements at the top of the stack |
| 0x0e       | GT          | `stack[sp-2] = (stack[sp-2] > stack[sp-1]); sp--` | Comparison      |
| 0x0f       | THROW       |               | throws a JsException embedding the top object on the stack and thus leaves this interpreter function call. Operation is resumed at a corresponding TRY\_CALL opcode |
| 0x10       | INC         | `stack[sp-1] = stack[sp-1]+1` |                 |
| 0x11       | DEC         | `stack[sp-1] = stack[sp-1]-1` |                 |
|0x12 	      | LT          | `stack[sp-2] = (stack[sp-2] < stack[sp-1]); sp--`  | 	Comparison     |
|0x13 	      | MOD         | `stack[sp-2] %= stack[sp-1]; sp--`  | Division remainder |
|0x14 	      | MUL         | `stack[sp-2] *= stack[sp-1]; sp--`  | Multiplication  |
|0x15 	      | NEG         | `stack[sp-1] = -stack[sp-1]`  | Negation        |
|0x16        | NEW\_ARRAY  | `stack[sp] = new Array(); sp++` | Pushes a new array on the stack. |
|0x17        | NEW\_OBJ    | `stack[sp] = new Object(); sp++` |                 |
|0x18 	      | NEW         | `stack[sp-1] = new stack[sp-1]();`  | 	Push a new object |
|0x19 	      | NOT         | `stack[sp-1] = !stack[sp-1]`  | Logical not     |
|0x1a        | 	OR         | `stack[sp-2] |= stack[sp-1]; sp--`  | Bitwise or      |
|0x1b 	      | DROP        | `sp--`        | Remove the top value from the stack |
|0x1c 	      | TRUE        | `stack[sp] = true; sp++` 	 | Push true on the stack. |
|0x1d 	      | FALSE       | `stack[sp] = false; sp++`  | Push false on the stack. |
|0x1e 	      | RET         | (1)           | Returns from a function call. |
|0x1f        | CTX\_SET    | `context[stack[sp-1]] = stack[sp-2]; sp--`  | Sets a variable in the current context |
|0x20 	      | SET\_KC     | `stack[sp-3][stack[sp-2]] = stack[sp-1]; sp-= 2`  | 	Assign a variable, keep the context on the stack. Used for literal object notation. |
|0x21        | SET         | `stack[sp-1][stack[sp-2]] = stack[sp-3]; sp-= 2` 	 | Assign a variable, keep the assigned value on the stack. Used in regular variable assignments. |
|0x22        | SHL         | `stack[sp-2] <<= stack[sp-1]; sp--`  | Bit shift to the left |
|0x23        | SHR         | `stack[sp-2] >>>= stack[sp-1]; sp--`  | Bit shift to the right without sign extension |
|0x24        | SUB         | `stack[sp-2] -= stack[sp-1]; sp--`  | 	Substraction   |
|0x25        | SWAP        | `tmp = stack[sp-2]; stack[sp-2] = stack[sp-1]; stack[sp-1] = tmp` 	 | Swaps the top two values on the stack. |
|0x26        | THIS        | `stack[sp] = this; sp++`  | Pushes this on the stack |
|0x27        | NULL        | `stack[sp] = null; sp++` | Pushes null on the stack |
|0x28        | UNDEF       | `stack[sp] = undefined; sp++` | Pushes undefined on the stack |
|0x29        | DDUP        | `stack[sp] = stack[sp-2]; stack[sp+1] = stack[sp-1]; sp = sp + 2;`  | Duplicates the top value and the value next to the top on the stack.  |
|0x2A        | ROT         | `tmp = stack[sp-1]; stack[sp-1] = stack[sp-2]; stack[sp-2] = stack[sp-3]; stack[sp-3] = tmp;` | Rotates three values on the top of the stack |
|0x2B        |  EQEQEQ     | `stack [sp-2] = stack[sp-1] === stack[sp-2]; sp = sp - 1;` | Determines whether the two values on the top of the stack are equal (===)  |
|0x2C        | XOR         | `stack[sp-1] = stack[sp-2] ^ stack[sp-1]; sp = sp - 1;`  |  Binary XOR operation |
|0x2D        | INV         | `stack[sp-1] = ~stack[sp-1]` | Binary NOT operation (invert all bits) |
|0x2E        |  WITH\_START  | `tmp = new Object(); tmp.__proto__ = stack[sp-1]; tmp.parent = ctx; ctx = tmp; sp = sp - 1;` | Changes the current context to a new Object that has the stack top object set as its prototype and the current context as next Object in its scope chain |
|0x2F        | WITH\_END   | `ctx = ctx.parent` | Changes the scope chain back to the next object in the scope chain |
|0x30        | ABOVE       | `stack[sp] = stack[sp-3]; sp = sp + 1;`  | Gets the object two below the stack top.  |
| ...        |             |               | reserved for future use |
| 0xe8..0xe9 | TRY\_CALL   |               |                 |
| 0xea..0xeb | FN          | `stack[sp] = new Function(functions[imm], context); sp++` | Pushes a new function constructed from the function literal denoted by the immediate parameter and the current context. |
| 0xec..0xed | NUM         | `stack[sp] = numbers[imm]; sp++` | Pushes the number literal with the index determined by the immediate parameter |
| 0xee..0xef 	| GO 	        | `pc += imm`	  | Add the value of the immediate operand to the program counter (Program counter position after decoding this instruction) |
| 0xf0..0xf1 	| IF 	        | `sp--; if(!stack[sp]) pc += imm;` |	Add the value of the immediate operand to the program counter (Program counter position after decoding this instruction) if the value popped from the stack is false (emulating a JS if statement that skips over a block of code if the condition does not hold). |
| 0xf2..0xf3 	| CALL        | (3) 	         | Performs a function call. The immediate parameter determines the number of parameters on the stack. |
| 0xf4..0xf5 	| LINE        |               | Sets the current line number to the immediate value |
| 0xf6..0xf7 	| LCL\_GET    | `stack[sp] = stack[bp+imm]; sp++` | Read a local variable. |
| 0xf8..0xf9 	| LCL\_SET    | `stack[bp+imm] = stack[sp-1];` | Set a local variable. |
| 0xfa..0xfb 	| NEXT        |	              |                 |
| 0xfc..0xfd 	| INT 	       | `stack[sp] = imm; sp++` | Pushes the immediate operand |
| 0xfe..0xff 	| STR         | `stack[sp] = strings[imm]; sp++` | Pushes the string literal with the index determined by the immediate parameter |

1, 2, 3): TBD