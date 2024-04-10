# Compiler (NOTE: See *semantic-analysis* branch)
Compiler written in Java.

BNF notation of our language, https://labouseur.com/courses/compilers/grammar.pdf


## Setup


- Navigate to `/src`

- Compile all files:
  - If using Linux/Unix, ```chmod +x ./findAndCompileAllJavaFiles.sh```

  - ```./findAndCompileAllJavaFiles.sh```


## Usage

Use the Java "Compiler" class, along with an input file and optional flags, to compile a source program

To use the program to compile source code found within "test.txt" or "easyprogram":

```java Compiler test.txt```
```java Compiler easyprogram```

There are tests available within /input

A good test for semantic-analysis is ```semantic-analysis-mega-test```

For example, to use the program to compile source code found within "test.txt" or "easyprogram":

```java Compiler test.txt```
```java Compiler easyprogram```


This compiler executes in verbose mode by default. To run it in terse mode, append the '-t' flag: 

```java Compiler easyprogram -t```

For evaluation purposes, it is HIGHLY ADVISABLE that you choose to run the compiler in terse mode.


Alternatively, if you want even-more verbose output, try using debug mode using '-d' flag. (Note: Not fully implemented. Currently only offers slightly more verbose output.) 

Minimum Requirements:
- Java 21
