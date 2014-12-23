The virtual machine translator takes a VM program and translates it 
into the Hack assembly language.

The result is one .asm file. If the input program consists of only
one .vm file (i.e., only one class), the output .asm file will be 
in the same directory as the .vm file. If the input program is a
directory (i.e., more than one class), the output .asm file will 
be inside the directory with the .vm files.

For example, if the input file is filename.vm (or directory called 
"filename"), the program will create a file called filename.asm
in the location described above.

** If filename.asm exists already, it will be overwritten. **

All files in the VM program must have a .vm extension.

--------------------------------------------------------------

To compile the virtual machine translator from within the “src” 
directory, enter the following into the command line :

     $ javac VMTranslator.java

--------------------------------------------------------------

To run the virtual machine from within the “src” directory, 
enter the following into the command line :

     $ java VMTranslator /absolute/path/to/program

where the argument is an absolute path to your VM program.

--------------------------------------------------------------
Dana Katz — November 2014