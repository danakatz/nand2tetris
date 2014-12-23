import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by danakatz on 10/28/14.
 */
public class Parser {

    private int currentLineNumber;
    private String[] currentCommand;
    private ArrayList<String> instructions = new ArrayList<String>();

    public static final int C_ARITHMETIC = 1;
    public static final int C_PUSH = 2;
    public static final int C_POP = 3;
    public static final int C_LABEL = 4;
    public static final int C_GOTO = 5;
    public static final int C_IF = 6;
    public static final int C_FUNCTION = 7;
    public static final int C_CALL = 8;
    public static final int C_RETURN = 9;

    ArrayList<String> arithmeticCommands;

    public Parser(File input) throws FileNotFoundException, Exception {
        try {                                                                   // add all non-whitespace lines
            removeWhitespace(input);                                            // to an array list of instructions
            initializeArithmeticCommands();
            currentLineNumber = 0;
            if(!instructions.isEmpty()) {
                currentCommand = instructions.get(currentLineNumber).split("\\s");
            } else {
                throw new Exception("No valid instructions.");
            }
        } catch (FileNotFoundException fnf) {
            throw new FileNotFoundException(fnf.getMessage());
        }
    }

    private void initializeArithmeticCommands() {
        arithmeticCommands = new ArrayList<String>();

        arithmeticCommands.add("add");
        arithmeticCommands.add("sub");
        arithmeticCommands.add("neg");
        arithmeticCommands.add("eq");
        arithmeticCommands.add("gt");
        arithmeticCommands.add("lt");
        arithmeticCommands.add("and");
        arithmeticCommands.add("or");
        arithmeticCommands.add("not");
    }

    public boolean hasMoreCommands() {
        return (currentLineNumber < instructions.size() - 1);
    }

    public void advance() {
        currentLineNumber++;
        currentCommand = instructions.get(currentLineNumber).split("\\s");
    }

    public int commandType() {
        String type = currentCommand[0];
        if(arithmeticCommands.contains(type)) {
            return C_ARITHMETIC;
        }

        if(type.equalsIgnoreCase("push")) {
            return C_PUSH;
        }

        if(type.equalsIgnoreCase("pop")) {
            return C_POP;
        }

        if(type.equalsIgnoreCase("label")) {
            return C_LABEL;
        }

        if(type.equalsIgnoreCase("goto")) {
            return C_GOTO;
        }

        if(type.equalsIgnoreCase("if-goto")) {
            return C_IF;
        }

        if(type.equalsIgnoreCase("function")) {
            return C_FUNCTION;
        }

        if(type.equalsIgnoreCase("call")) {
            return C_CALL;
        }

        if(type.equalsIgnoreCase("return")) {
            return C_RETURN;
        }

        return 0;
    }

    public String arg1() {
        if(this.commandType() == C_ARITHMETIC) {
            return currentCommand[0];
        }
        else {
            return currentCommand[1];
        }
    }

    public int arg2() throws NumberFormatException {
        try {
            return Integer.parseInt(currentCommand[2]);
        } catch (NumberFormatException nfe) {
            throw new NumberFormatException("Invalid argument: " + nfe.getMessage());
        }
    }

    public void removeWhitespace(File input) throws FileNotFoundException {
        try {
            Scanner in = new Scanner(input);

            while(in.hasNext()) {
                String next = in.nextLine();                                // reads each line and splits into
                String[] line = next.split("\\s");                          // an array of string tokens
                String command = "";

                for(int i = 0; i < line.length; i++) {
                    if(line.length == 0) {                                  // ignores empty lines
                        break;
                    } else if(line[i].length() > 1 && line[i].substring(0, 2).equals("//") && i != 0) {
                        break;                                              // ignores inline comments
                    } else if(line[i].length() > 1 && line[i].substring(0, 2).equals("//") && i == 0) {
                        break;                                              // ignores whole-line comments
                    }  else {
                        command += line[i];
                        command += " ";                                     // preserves space between command words
                    }
                }

                if(!(command.equals("") || command.equals(" "))) {
                    instructions.add(command.trim());
                }
            }

            in.close();

        } catch(FileNotFoundException fnf) {
            throw new FileNotFoundException("File not found: " + fnf.getMessage());
        }
    }
}

