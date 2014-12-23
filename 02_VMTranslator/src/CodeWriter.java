import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * Created by danakatz on 10/28/14.
 */
public class CodeWriter {
    private String filename;
    private PrintWriter writeOut;
    private int currentLine;
    private int labelNumber;

    public CodeWriter(File output) throws FileNotFoundException {
        try {
            writeOut = new PrintWriter(output);
            currentLine = 0;
            labelNumber = 0;
        } catch(FileNotFoundException fnf) {
            throw new FileNotFoundException("File not found: " + fnf.getMessage());
        }
    }

    public void setFileName(String filename) {
        this.filename = filename;
    }

    public void writeInit() {
        writeOut.println("// init");
        writeLine("@256");
        writeLine("D=A");
        writeLine("@SP");
        writeLine("M=D");
        writeCall("Sys.init", 0);
    }

    public void writeArithmetic(String operation) {
        writeOut.println("// " + operation);
        if(operation.equalsIgnoreCase("add") || operation.equalsIgnoreCase("sub") || operation.equalsIgnoreCase("and")
                || operation.equalsIgnoreCase("or")) {
            writeLine("@SP");
            writeLine("AM=M-1");
            writeLine("D=M");
            writeLine("A=A-1");

            if(operation.equalsIgnoreCase("add")) {
                writeLine("M=D+M");
            } else if(operation.equalsIgnoreCase("sub")) {
                writeLine("M=M-D");
            } else if(operation.equalsIgnoreCase("and")) {
                writeLine("M=D&M");
            } else if(operation.equalsIgnoreCase("or")) {
                writeLine("M=D|M");
            }

        } else if(operation.equalsIgnoreCase("eq") || operation.equalsIgnoreCase("lt")
                || operation.equalsIgnoreCase("gt")) {
            writeLine("@SP");
            writeLine("AM=M-1");
            writeLine("D=M");
            writeLine("A=A-1");
            writeLine("D=D-M");
            writeLine("@" + (currentLine + 7));

            if(operation.equalsIgnoreCase("eq")) {
                writeLine("D;JEQ");
            } else if(operation.equalsIgnoreCase("lt")) {
                writeLine("D;JGT");
            } else if(operation.equalsIgnoreCase("gt")) {
                writeLine("D;JLT");
            }

            writeLine("@SP");
            writeLine("A=M-1");
            writeLine("M=0");
            writeLine("@" + (currentLine + 5));
            writeLine("0;JMP");
            writeLine("@SP");
            writeLine("A=M-1");
            writeLine("M=-1");

        } else if(operation.equalsIgnoreCase("not")) {
            writeLine("@SP");
            writeLine("A=M-1");
            writeLine("M=!M");

        } else if(operation.equalsIgnoreCase("neg")) {
            writeLine("D=0");
            writeLine("@SP");
            writeLine("A=M-1");
            writeLine("M=D-M");
        }
    }

    private void writeLine(String line) {
        writeOut.println(line);
        currentLine++;
    }

    public void writePushPop(int command, String segment, int index) {
        writeOut.println("// " + command + segment + index);
        if(command == Parser.C_PUSH) {
            if(segment.equalsIgnoreCase("pointer")) {
                if(index == 0) {
                    writeLine("@THIS");
                } else if(index == 1) {
                    writeLine("@THAT");
                }

                writeLine("D=M");
            } else if(segment.equalsIgnoreCase("static")) {
                writeLine("@" + filename + "." + index);
                writeLine("D=M");
            } else if(index == 0 && !segment.equalsIgnoreCase("constant")) {
                writeLine(getLabel(segment));
                writeLine("A=M");
                writeLine("D=M");
            } else if(index > 0 || segment.equalsIgnoreCase("constant")) {
                writeLine("@" + index);
                writeLine("D=A");
            }

            if(index > 0 && !segment.equalsIgnoreCase("constant") && !segment.equalsIgnoreCase("pointer")
                    && !segment.equalsIgnoreCase("static")) {
                writeLine(getLabel(segment));

                if(segment.equalsIgnoreCase("temp")) {
                    writeLine("A=D+A");
                } else {
                    writeLine("A=D+M");
                }

                writeLine("D=M");
            }

            writeLine("@SP");
            writeLine("A=M");
            writeLine("M=D");
            writeLine("@SP");
            writeLine("M=M+1");

        } else if(command == Parser.C_POP) {
            if(index == 0 || segment.equalsIgnoreCase("pointer") || segment.equalsIgnoreCase("static")) {
                writeLine("@SP");
                writeLine("AM=M-1");
                writeLine("D=M");
                if(segment.equalsIgnoreCase("pointer") && index == 0) {
                    writeLine("@THIS");
                } else if(segment.equalsIgnoreCase("pointer") && index == 1) {
                    writeLine("@THAT");
                } else if(segment.equalsIgnoreCase("static")) {
                    writeLine("@" + filename + "." + index);
                }
                else {
                    writeLine(getLabel(segment));
                }

            } else if(index > 0) {
                writeLine("@" + index);
                writeLine("D=A");
                writeLine(getLabel(segment));

                if(segment.equalsIgnoreCase("temp")) {
                    writeLine("D=D+A");
                } else {
                    writeLine("D=D+M");
                }

                writeLine("@R13");
                writeLine("M=D");
                writeLine("@SP");
                writeLine("AM=M-1");
                writeLine("D=M");
                writeLine("@R13");
            }

            if(!segment.equalsIgnoreCase("pointer") && !segment.equalsIgnoreCase("static")
                    && !segment.equalsIgnoreCase("temp")) {
                writeLine("A=M");
            }
            writeLine("M=D");
        }
    }

    public void writeLabel(String label) {
        writeOut.println("// C_LABEL " + label);
        writeOut.println("(" + label + ")");
    }

    public void writeGoto(String label) {
        writeOut.println("// C_GOTO " + label);
        writeLine("@" + label);
        writeLine("0;JMP");
    }

    public void writeIf(String label) {
        writeOut.println("// C_IF " + label);
        writeLine("@SP");
        writeLine("AM=M-1");
        writeLine("D=M");
        writeLine("@" + label);
        writeLine("D;JNE");
    }

    public void writeCall(String functionName, int numArgs) {
        writeOut.println("// call " + functionName + " " + numArgs);

        // save calling function
        writeOut.println("// save calling function");
        writeLine("@return-address" + labelNumber);
        writeLine("D=A");
        finishPush();

        writeLine("@LCL");
        writeLine("D=M");
        finishPush();

        writeLine("@ARG");
        writeLine("D=M");
        finishPush();

        writeLine("@THIS");
        writeLine("D=M");
        finishPush();

        writeLine("@THAT");
        writeLine("D=M");
        finishPush();

        // reposition ARG
        writeOut.println("// reposition ARG");
        writeLine("@SP");
        writeLine("D=M");
        writeLine("@" + numArgs);
        writeLine("D=D-A");
        writeLine("@5");
        writeLine("D=D-A");
        writeLine("@ARG");
        writeLine("M=D");

        // reposition LCL
        writeOut.println("// reposition LCL");
        writeLine("@SP");
        writeLine("D=M");
        writeLine("@LCL");
        writeLine("M=D");

        // transfer control
        writeOut.println("// transfer control");
        writeGoto(functionName);

        // declare return address label
        writeOut.println("// declare return address label");
        writeLabel("return-address" + labelNumber);

        labelNumber++;

    }

    private void finishPush() {
        writeLine("@SP");
        writeLine("A=M");
        writeLine("M=D");
        writeLine("@SP");
        writeLine("M=M+1");
    }

    public void writeReturn() {
        writeOut.println("// return");

        // set FRAME = LCL
        writeOut.println("// set FRAME = LCL");
        writeLine("@LCL");
        writeLine("D=M");
        writeLine("@FRAME");
        writeLine("M=D");

        // set RET = FRAME - 5
        writeOut.println("// set RET = FRAME - 5");
        writeLine("@5");
        writeLine("A=D-A");
        writeLine("D=M");
        writeLine("@RET");
        writeLine("M=D");

        // set ARG = pop()
        writeOut.println("// set ARG = pop()");
        writeLine("@SP");
        writeLine("AM=M-1");
        writeLine("D=M");
        writeLine("@ARG");
        writeLine("A=M");
        writeLine("M=D");

        // restore SP of the caller
        writeOut.println("// restore SP of the caller");
        writeLine("@ARG");
        writeLine("D=M+1");
        writeLine("@SP");
        writeLine("M=D");

        // restore THAT of the caller
        writeOut.println("// restore THAT of the caller");
        writeLine("@FRAME");
        writeLine("A=M-1");
        writeLine("D=M");
        writeLine("@THAT");
        writeLine("M=D");

        // restore THIS of the caller
        writeOut.println("// restore THIS of the caller");
        writeLine("@FRAME");
        writeLine("D=M");
        writeLine("@2");
        writeLine("A=D-A");
        writeLine("D=M");
        writeLine("@THIS");
        writeLine("M=D");

        // restore ARG of the caller
        writeOut.println("// restore ARG of the caller");
        writeLine("@FRAME");
        writeLine("D=M");
        writeLine("@3");
        writeLine("A=D-A");
        writeLine("D=M");
        writeLine("@ARG");
        writeLine("M=D");

        // restore LCL of the caller
        writeOut.println("// restore LCL of the caller");
        writeLine("@FRAME");
        writeLine("D=M");
        writeLine("@4");
        writeLine("A=D-A");
        writeLine("D=M");
        writeLine("@LCL");
        writeLine("M=D");

        // goto RET
        writeOut.println("// goto RET");
        writeLine("@RET");
        writeLine("A=M");
        writeLine("0;JMP");
    }

    public void writeFunction(String functionName, int numLocals) {
        writeOut.println("// function " + functionName + numLocals);

        // declare label for function entry
        writeLabel(functionName);

        // initialize local variables to 0
        for(int i = 0; i < numLocals; i++) {
            writePushPop(Parser.C_PUSH, "constant", 0);
        }
    }

    private String getLabel(String segment) {
        if(segment.equalsIgnoreCase("local")) {
            return "@LCL";
        } else if(segment.equalsIgnoreCase("argument")) {
            return "@ARG";
        } else if(segment.equalsIgnoreCase("this")) {
            return "@THIS";
        } else if(segment.equalsIgnoreCase("that")) {
            return "@THAT";
        } else if(segment.equalsIgnoreCase("temp")) {
            return "@R5";
        } else return null;
    }

    public void close() {
        writeOut.close();
    }
}
