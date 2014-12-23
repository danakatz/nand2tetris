The assembler takes an input file with assembly language code 
and converts it into machine language code.

The result is a .hack file in the same directory as the input file.

For example, if the input file is filename.asm,
the program will create a file called filename.hack.

** If filename.hack exists already, it will be overwritten. **

If the input file contains an invalid instruction, the program
terminates and prints an error description to the console.

--------------------------------------------------------------

To compile the assembler from within the “src” directory, 
enter the following into the command line :

     $ javac Assembler.java

--------------------------------------------------------------

To run the assembler from within the “src” directory, 
enter the following into the command line :

     $ java Assembler /absolute/path/to/filename.asm

where the argument is an absolute path to your assembly
language file.

--------------------------------------------------------------
Dana Katz — October 2014