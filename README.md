# Compiler
Compiler written in Java. BNF notation of our language, https://labouseur.com/courses/compilers/grammar.pdf


Navigate to /src and type in the command: 
```./findAndCompileAllJavaFiles.sh```

There are tests available within /input

For example, to use the program to compile source code found within "test.txt" or "easyprogram":

```java Compiler test.txt```
```java Compiler easyprogram```


This compiler executes in verbose mode by default. To run it in terse mode, append the '-t' flag: 

```java Compiler easyprogram -t```

For evaluation purposes, it is HIGHLY ADVISABLE that you choose to run the compiler in terse mode.


Alternatively, if you want even-more verbose output, try using debug mode using '-d' flag. (Note: Not fully implemented. Currently only offers slightly more verbose output.) 
