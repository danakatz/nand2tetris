import java.io.File;
import java.io.PrintWriter;
import java.util.Hashtable;

/**
 * Created by danakatz on 10/19/14.
 */
public class Assembler {
    public static void main(String[] args) {
        if(args.length > 0) {
            try {
                File input = new File(args[0]);                                     // take input file
                int extension = input.getName().indexOf('.');
                String fileName = input.getName().substring(0, extension);
                File output = new File(input.getParent(), fileName + ".hack");      // create output file
                PrintWriter writeOut = new PrintWriter(output);

                Parser p = new Parser(input);                                       // create Parser

                SymbolTable st = new SymbolTable(p);                                // create SymbolTable

                int currentInstruction = 0;                                         // set current line number to 0

                // while(true) loop includes last instruction, unlike while(p.hasMoreCommands())
                while(true) {
                    if(p.commandType().equals("L_COMMAND")) {                       // ignore L_COMMANDs
                        if(p.hasMoreCommands()) {
                            p.advance();
                            continue;
                        } else {
                            break;
                        }
                    } else if(p.commandType().equals("A_COMMAND")) {                // handle A_COMMANDs
                        String machine = "0";
                        try {
                            int loc = Integer.parseInt(p.symbol());                 // get integer
                            String binary = Integer.toBinaryString(loc);            // convert to binary string
                            if(binary.length() < 15) {                              // add leading zeros if necessary
                                for(int i = 0; i < 15 - binary.length(); i++) {
                                    machine += "0";
                                }
                            }
                            machine += binary;
                        } catch (NumberFormatException nfe) {                       // if symbol is not an integer, check symbol table
                            if(st.contains(p.symbol())) {
                                String binary = Integer.toBinaryString(st.getAddress(p.symbol()));  // if in symbol table, retrieve
                                if(binary.length() < 15) {                                          // value and convert to binary string
                                    for(int i = 0; i < 15 - binary.length(); i++) {
                                        machine += "0";
                                    }
                                }
                                machine += binary;
                            } else {                                                                // if not in symbol table
                                st.addEntry(p.symbol());                                            // add current symbol (next available memory
                                String binary = Integer.toBinaryString(st.getAddress(p.symbol()));  // handled by SymbolTable module)
                                if(binary.length() < 15) {                                          // convert value to binary string
                                    for(int i = 0; i < 15 - binary.length(); i++) {
                                        machine += "0";
                                    }
                                }
                                machine += binary;
                            }
                        }
                        writeOut.println(machine);
                        currentInstruction++;
                    } else if(p.commandType().equals("C_COMMAND")) {                                // handle C_COMMANDS
                        String machine = "111";                                                     // start string with "111"
                        String comp = Code.comp(p.comp());                                          // get codes from Code module
                        String dest = Code.dest(p.dest());
                        String jump = Code.jump(p.jump());
                        if(!(dest.equals("NG") || comp.equals("NG") || jump.equals("NG"))) {        // if no invalid codes
                            machine += comp + dest + jump;                                          // add all codes to final string
                            writeOut.println(machine);
                            currentInstruction++;
                        } else {                                                                    // handles invalid codes
                            System.out.println("Error at instruction " + currentInstruction + " of .asm file.");
                            System.out.println("Resulting .hack file is incomplete.");
                            writeOut.close();
                            return;
                        }
                    } else {                                                                        // handles invalid instructions
                        System.out.println("Error at instruction " + currentInstruction + " of .asm file.");
                        System.out.println("Resulting .hack file is incomplete.");
                        writeOut.close();
                        return;
                    }

                    if(p.hasMoreCommands()) {                           // advance if more commands
                        p.advance();
                    } else {                                            // break if last command
                        break;
                    }
                }

                writeOut.close();

            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }

        } else System.out.println("No file entered.");
    }
}
