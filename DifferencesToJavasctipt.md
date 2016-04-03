# Introduction #

This page describes the differences between MiniJoe and the ECMA-262 Standard


# Details #


  * Regular Expressions are currently not supported in MiniJoe
  * In MiniJoe, arrays are not sparse. That means that assigning values to very high array indices may result in an out of memory exception
  * MiniJoe may enforce the Javascript Syntax stricter than other ECMA-262 implementations
  * The methods for handling URLs are missing. This should be simple to fix, though.

If there are other differences to ECMA 262 in MiniJoe, they should be filed as issues.