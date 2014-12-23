import java.util.Hashtable;

/**
 * Created by danakatz on 10/20/14.
 */
public class SymbolTable {
    private Hashtable<String, Integer> symbols = new Hashtable<String, Integer>();
    private Parser p;
    private int nextAvailMem;
    private int currentLineNumber;

    public SymbolTable(Parser p) {          // takes Parser object from Assembler
        this.initialize();                  // adds all pre-defined symbols to table
        this.p = p;
        nextAvailMem = 16;                  // keeps track of next available memory location
        currentLineNumber = 0;              // keeps track of current line number for L_COMMANDs
        this.firstPass();                   // parses all instructions for L_COMMANDs
    }

    private void initialize() {
        symbols.put("SP", 0);
        symbols.put("LCL", 1);
        symbols.put("ARG", 2);
        symbols.put("THIS", 3);
        symbols.put("THAT", 4);
        for(int i = 0; i <= 15; i++) {
            String key = "R" + i;
            symbols.put(key, i);
        }
        symbols.put("SCREEN", 16384);
        symbols.put("KBD", 24576);
    }

    private void firstPass() {
        while(true) {
            if(p.commandType().equals("L_COMMAND")) {
                symbols.put(p.symbol(), currentLineNumber);
            } else currentLineNumber++;

            if(p.hasMoreCommands()) {
                p.advance();
            } else {
                break;
            }
        }
        p.reset();                          // resets Parser for Assembler
    }

    public void addEntry(String symbol) {
        symbols.put(symbol, nextAvailMem);
        nextAvailMem++;
    }

    public boolean contains(String symbol) {
        return symbols.containsKey(symbol);
    }

    public int getAddress(String symbol) {
        return symbols.get(symbol);
    }
}
