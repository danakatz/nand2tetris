The Jack compiler parses a directory (or single .jack file) and 
outputs a compiled .vm file for each .jack file.

For example, if the input file is a directory called "directory"
(or file called "filename.jack"), the program will create a .vm
file for each .jack file, with the same name, inside the directory
(or a single "filename.vm" file in the same directory as the 
.jack file).

All files in the Jack program must have a .jack extension.

--------------------------------------------------------------

To compile the Jack compiler from the directory containing 
the .java files, enter the following into the command line :

     $ javac JackCompiler.java

--------------------------------------------------------------

To run the Jack analyzer from the directory containing
the .class files, enter the following into the command line :

     $ java JackCompiler /absolute/path/to/program

where the argument is an absolute path to your Jack program.

--------------------------------------------------------------
Dana Katz — November 2014