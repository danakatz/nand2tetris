import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by danakatz on 11/12/14.
 */
public class JackTokenizer {
    private ArrayList<String> tokens;
    private int currentToken;
    private String symbols;
    private String[] keywords;

    public static final int KEYWORD = 0;
    public static final int SYMBOL = 1;
    public static final int IDENTIFIER = 2;
    public static final int INT_CONST = 3;
    public static final int STRING_CONST = 4;

    public JackTokenizer(File input) throws FileNotFoundException {
        symbols = "{}()[].,;+-*/&|<>=~";
        initializeKeywords();

        tokens = new ArrayList<String>();
        removeWhitespace(input);

        currentToken = -1;
    }

    public boolean hasMoreTokens() {
        return currentToken < tokens.size() - 1;
    }

    public void advance() {
        currentToken++;
    }

    public int tokenType() {
        String t = tokens.get(currentToken);
        try {
            Integer.parseInt(t);
            return INT_CONST;
        } catch(Exception e) {
            if(t.length() == 1) {
                for(int i = 0; i < symbols.length(); i++) {
                    for(int j = 0; j < t.length(); j++) {
                        if(symbols.charAt(i) == t.charAt(j)) {
                            return SYMBOL;
                        }
                    }
                }
                return IDENTIFIER;
            } else {
                for(String kw : keywords) {
                    if(t.equals(kw)) {
                        return KEYWORD;
                    }
                }
                if((t.charAt(0) == '"') && (t.charAt(t.length() - 1) == '"')) {
                    return STRING_CONST;
                }
                return IDENTIFIER;
            }
        }
    }

    public String keyword() {
        return tokens.get(currentToken);
    }

    public char symbol() {
        return tokens.get(currentToken).charAt(0);
    }

    public String identifier() {
        return tokens.get(currentToken);
    }

    public int intVal() {
        return Integer.parseInt(tokens.get(currentToken));
    }

    public String stringVal() {
        return tokens.get(currentToken).substring(1, tokens.get(currentToken).length() - 1);
    }

    private void removeWhitespace(File input) throws FileNotFoundException {
        try {
            Scanner in = new Scanner(input);

            while(in.hasNext()) {
                String next = in.nextLine();

                int doubleSlash = next.indexOf("//");
                int openComment = next.indexOf("/*");

                if(doubleSlash < 0 && openComment < 0) {
                    parse(next.trim());
                } else if(doubleSlash >= 0 && openComment < 0) {
                    parse(next.substring(0, doubleSlash).trim());
                } else if(doubleSlash >= 0 && openComment >= 0) {
                    if(doubleSlash < openComment) {
                        parse(next.substring(0, doubleSlash).trim());
                    } else if(openComment < doubleSlash) {
                        parse(next.substring(0, openComment).trim());
                        while(next.indexOf("*/") < 0) {
                            next = in.nextLine();
                        }
                        int closeComment = next.indexOf("*/");
                        if(closeComment < next.length() - 2) {
                            parse(next.substring(closeComment + 2).trim());
                        }
                    }
                } else if(doubleSlash < 0 && openComment >= 0) {
                    parse(next.substring(0, openComment).trim());
                    while(next.indexOf("*/") < 0) {
                        next = in.nextLine();
                    }
                    int closeComment = next.indexOf("*/");
                    if(closeComment < next.length() - 2) {
                        parse(next.substring(closeComment + 2).trim());
                    }
                }
            }

            in.close();
        } catch(FileNotFoundException fnf) {
            throw fnf;
        }
    }

    private void parse(String s) {
        if(s.length() == 0) {
            return;
        } else if(s.length() == 1) {
            tokens.add(s);
            return;
        } else if(s.split("\\s").length == 1) {
            int firstSymbolIndex = -1;
            // find index of first-occurring symbol
            for(int i = 0; i < symbols.length(); i++) {
                int temp = s.indexOf(symbols.charAt(i));
                if(temp >= 0 && firstSymbolIndex < 0) {
                    firstSymbolIndex = temp;
                } else if(temp >= 0 && firstSymbolIndex >= 0) {
                    if(temp < firstSymbolIndex) firstSymbolIndex = temp;
                }
            }
            // if s contains a symbol, add everything before symbol,
            // add symbol, parse everything after symbol
            if(firstSymbolIndex >= 0) {
                if(firstSymbolIndex != 0) {
                    tokens.add(s.substring(0, firstSymbolIndex));
                }
                tokens.add(s.substring(firstSymbolIndex, firstSymbolIndex + 1));
                parse(s.substring(firstSymbolIndex + 1).trim());
            } else {
                tokens.add(s);
            }
        // check to see if s has a string constant.
        // if not, parse each individual token
        } else if(s.indexOf('"') < 0) {
            String[] tks = s.split("\\s");
            for(String tk : tks) {
                parse(tk.trim());
            }
        // if s has a string constant, parse everything before opening quotes
        // add string token (including spaces)
        // parse everything after closing quotes
        } else {
            int openQ = s.indexOf('"');
            int closeQ = s.indexOf('"', openQ + 1);
            if(openQ >= 0 && closeQ > openQ) {
                parse(s.substring(0, openQ).trim());
                tokens.add(s.substring(openQ, closeQ + 1));
                if(closeQ < s.length() - 1) {
                    parse(s.substring(closeQ + 1).trim());
                }
            } else tokens.add("$ QUOTE SYNTAX ERROR $");
        }
    }

    private void initializeKeywords() {
        keywords = new String[21];
        keywords[0] = "class";
        keywords[1] = "constructor";
        keywords[2] = "function";
        keywords[3] = "method";
        keywords[4] = "field";
        keywords[5] = "static";
        keywords[6] = "var";
        keywords[7] = "int";
        keywords[8] = "char";
        keywords[9] = "boolean";
        keywords[10] = "void";
        keywords[11] = "true";
        keywords[12] = "false";
        keywords[13] = "null";
        keywords[14] = "this";
        keywords[15] = "let";
        keywords[16] = "do";
        keywords[17] = "if";
        keywords[18] = "else";
        keywords[19] = "while";
        keywords[20] = "return";
    }
}
