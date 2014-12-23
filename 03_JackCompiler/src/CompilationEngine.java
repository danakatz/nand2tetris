import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * Created by danakatz on 11/16/14.
 */
public class CompilationEngine {
    VMWriter vm;
    JackTokenizer jt;
    SymbolTable st;
    String className;
    int whileCount;
    int ifCount;

    public CompilationEngine(File infile, File outfile) throws FileNotFoundException {
        jt = new JackTokenizer(infile);
        vm = new VMWriter(outfile);
        st = new SymbolTable();
        whileCount = -1;
        ifCount = -1;
    }

    public void close() {
        vm.close();
    }

    public void compileClass() {
        if(jt.hasMoreTokens()) jt.advance();
        // consume class keyword
        getKeyword();
        // get class name identifier
        className = getIdentifier();
        // consume opening brace
        getSymbol();
        // compile variable declarations and subroutine declarations
        compileClassVarDec();
        while(!(jt.tokenType() == JackTokenizer.SYMBOL && jt.symbol() == '}')) {
            compileSubroutine();
        }
        // consume closing brace
        getSymbol();
    }

    private void compileClassVarDec() {
        while(jt.tokenType() == JackTokenizer.KEYWORD
                && (jt.keyword().equalsIgnoreCase("static") || jt.keyword().equalsIgnoreCase("field"))) {
            // get static or field keyword
            String kind = getKeyword();
            // get variable type
            String type = getType();
            // get static and field variable names
            ArrayList<String> varNames = getVarNames();
            // populate symbol table
            for(String varName : varNames) {
                st.define(varName, type, kind);
            }
        }
    }

    private void compileSubroutine() {
        st.startSubroutine();
        // get constructor, function, or method keyword
        String funcType = getKeyword();
        // get return type
        String returnType = getType();
        // get subroutine name
        String funcName = getIdentifier();
        // consume open parenthesis of parameter list
        getSymbol();
        // get parameters and add to symbol table
        ArrayList<String[]> params = compileParameterList();
        if(funcType.equals("method")) {
            st.addImplicitArg();
        }
        for(String[] param : params) {
            st.define(param[0], param[1], "arg");
        }
        // consume close parenthesis of parameter list
        getSymbol();
        // consume opening brace
        getSymbol();
        // get local variables and add to symbol table
        ArrayList<String[]> localVars = compileVarDec();
        for(String[] localVar : localVars) {
            st.define(localVar[0], localVar[1], "var");
        }
        vm.writeFunction(className + "." + funcName, st.varCount("var"));
        if(funcType.equals("constructor")) {
            vm.writePush("constant", st.varCount("field"));
            vm.writeCall("Memory.alloc", 1);
            vm.writePop("pointer", 0);
        } else if(funcType.equals("method")) {
            vm.writePush("argument", 0);
            vm.writePop("pointer", 0);
        }
        compileStatements();
        // consume closing brace
        getSymbol();
    }

    private ArrayList<String[]> compileParameterList() {
        ArrayList<String[]> params = new ArrayList<String[]>();
        String type;
        String name;
        // check for close paren, which means no parameters
        while(!(jt.tokenType() == JackTokenizer.SYMBOL && jt.symbol() == ')')) {
            // consume comma if not first in list
            if(jt.tokenType() == JackTokenizer.SYMBOL && jt.symbol() == ',') getSymbol();
            // get param type
            type = getType();
            // get param name
            name = getIdentifier();
            String[] temp = new String[2];
            temp[0] = name;
            temp[1] = type;
            params.add(temp);
        }
        return params;
    }

    private ArrayList<String[]> compileVarDec() {
        String type;
        ArrayList<String> names;
        ArrayList<String[]> vars = new ArrayList<String[]>();
        while(jt.tokenType() == JackTokenizer.KEYWORD && jt.keyword().equalsIgnoreCase("var")) {
            // consume var keyword
            getKeyword();
            type = getType();
            names = getVarNames();
            for(String name : names) {
                String[] temp = new String[2];
                temp[0] = name;
                temp[1] = type;
                vars.add(temp);
            }
        }
        return vars;
    }

    private void compileStatements() {
        while(!(jt.tokenType() == JackTokenizer.SYMBOL && jt.symbol() == '}')) {
            if(jt.tokenType() == JackTokenizer.KEYWORD && jt.keyword().equalsIgnoreCase("let")) {
                compileLet();
            } else if(jt.tokenType() == JackTokenizer.KEYWORD && jt.keyword().equalsIgnoreCase("if")) {
                compileIf();
            } else if(jt.tokenType() == JackTokenizer.KEYWORD && jt.keyword().equalsIgnoreCase("while")) {
                compileWhile();
            } else if(jt.tokenType() == JackTokenizer.KEYWORD && jt.keyword().equalsIgnoreCase("do")) {
                compileDo();
            } else if(jt.tokenType() == JackTokenizer.KEYWORD && jt.keyword().equalsIgnoreCase("return")) {
                compileReturn();
            }
        }
    }

    private void compileDo() {
        // consume do keyword
        getKeyword();
        compileTerm();
        // consume ;
        getSymbol();
        vm.writePop("temp", 0);
    }

    private void compileLet() {
        // consume let keyword
        getKeyword();
        // get variable
        String var = getIdentifier();
        // parse index and assignment
        if(jt.tokenType() == JackTokenizer.SYMBOL && jt.symbol() == '[') {
            vm.writePush(st.kindOf(var), st.indexOf(var));
            // consume opening bracket
            getSymbol();
            compileExpression();
            // consume closing bracket
            getSymbol();
            vm.writeArithmetic("add");
            vm.writePop("pointer", 1);
            // consume =
            getSymbol();
            compileExpression();
            // consume ;
            getSymbol();
            vm.writePop("that", 0);
        // parse assignment only
        } else if(jt.tokenType() == JackTokenizer.SYMBOL && jt.symbol() == '=') {
            // consume =
            getSymbol();
            compileExpression();
            // consume ;
            getSymbol();
            vm.writePop(st.kindOf(var), st.indexOf(var));
        }
    }

    private void compileWhile() {
        // increment while loop counter
        whileCount++;
        int thisWhileCount = whileCount;
        // consume while keyword
        getKeyword();
        // label loop
        vm.writeLabel("WHILE_BEGIN" + thisWhileCount);
        // parse condition
        // consume open paren
        getSymbol();
        compileExpression();
        // consume close paren
        getSymbol();
        vm.writeArithmetic("not");
        vm.writeIf("WHILE_END" + thisWhileCount);
        // parse loop statements
        // consume opening brace
        getSymbol();
        compileStatements();
        // consume closing brace
        getSymbol();
        vm.writeGoto("WHILE_BEGIN" + thisWhileCount);
        vm.writeLabel("WHILE_END" + thisWhileCount);
    }

    private void compileReturn() {
        // consume return keyword
        getKeyword();
        if(jt.tokenType() == JackTokenizer.SYMBOL && jt.symbol() == ';') {
            vm.writePush("constant", 0);
            vm.writeReturn();
            // consume ;
            getSymbol();
        } else {
            compileExpression();
            vm.writeReturn();
            // consume ;
            getSymbol();
        }
    }

    private void compileIf() {
        ifCount++;
        int thisIfCount = ifCount;
        // consume if keyword
        getKeyword();
        // consume open paren
        getSymbol();
        compileExpression();
        // consume close paren
        getSymbol();
        // consume opening brace
        getSymbol();
        vm.writeIf("IF_TRUE" + thisIfCount);
        vm.writeGoto("IF_FALSE" + thisIfCount);
        vm.writeLabel("IF_TRUE" + thisIfCount);
        compileStatements();
        // consume closing brace
        getSymbol();
        vm.writeGoto("IF_END" + thisIfCount);
        vm.writeLabel("IF_FALSE" + thisIfCount);
        if(jt.tokenType() == JackTokenizer.KEYWORD && jt.keyword().equalsIgnoreCase("else")) {
            // consume else keyword
            getKeyword();
            // consume opening brace
            getSymbol();
            compileStatements();
            // consume closing brace
            getSymbol();
        }
        vm.writeLabel("IF_END" + thisIfCount);
    }

    private int compileExpression() {                   // prints compiled expression and
        if(isStartOfTerm()) {                           //returns 1 if an expression was compiled, 0 otherwise
            compileTerm();
            while(isOperator()) {
                    char op = getSymbol();
                    compileTerm();
                    if(op == '+') vm.writeArithmetic("add");
                    else if(op == '-') vm.writeArithmetic("sub");
                    else if(op == '*') vm.writeCall("Math.multiply", 2);
                    else if(op == '/') vm.writeCall("Math.divide", 2);
                    else if(op == '&') vm.writeArithmetic("and");
                    else if(op == '|') vm.writeArithmetic("or");
                    else if(op == '<') vm.writeArithmetic("lt");
                    else if(op == '>') vm.writeArithmetic("gt");
                    else if(op == '=') vm.writeArithmetic("eq");
            }
            return 1;
        }
        return 0;
    }

    private void compileTerm() {
        if(jt.tokenType() == JackTokenizer.INT_CONST) {
            int i = getIntConstant();
            vm.writePush("constant", i);
        } else if(jt.tokenType() == JackTokenizer.STRING_CONST) {
            String str = getStrConstant();
            vm.writePush("constant", str.length());
            vm.writeCall("String.new", 1);
            for(int c = 0; c < str.length(); c++) {
                vm.writePush("constant", str.charAt(c));
                vm.writeCall("String.appendChar", 2);
            }
        } else if(isKeywordConstant()) {
            String keyword = getKeyword();
            if(keyword.equals("null") || keyword.equals("false")) {
                vm.writePush("constant", 0);
            } else if(keyword.equals("true")) {
                vm.writePush("constant", 0);
                vm.writeArithmetic("not");
            } else {
                vm.writePush("pointer", 0);
            }
        } else if(jt.tokenType() == JackTokenizer.IDENTIFIER) {
            String first = getIdentifier();
            if(jt.tokenType() == JackTokenizer.SYMBOL && jt.symbol() == '[') {
                vm.writePush(st.kindOf(first), st.indexOf(first));
                // consume opening bracket
                getSymbol();
                compileExpression();
                // consume closing bracket
                getSymbol();
                vm.writeArithmetic("add");
                vm.writePop("pointer", 1);
                vm.writePush("that", 0);
            } else if(jt.tokenType() == JackTokenizer.SYMBOL && jt.symbol() == '(') {   // must be method call on this
                vm.writePush("pointer", 0);                                             // class object
                // consume opening paren
                getSymbol();
                int nArgs = compileExpressionList();
                // consume closing paren
                getSymbol();
                vm.writeCall(className + "." + first, nArgs + 1);
            } else if(jt.tokenType() == JackTokenizer.SYMBOL && jt.symbol() == '.') {
                // consume dot
                getSymbol();
                String second = getIdentifier();
                if(st.indexOf(first) < 0) { // if pre-dot is not a symbol, must be function call from another class
                    // consume open paren
                    getSymbol();
                    int nArgs = compileExpressionList();
                    // consume close paren
                    getSymbol();
                    vm.writeCall(first + "." + second, nArgs);
                } else {    // if pre-dot is a symbol, must be a method call on an object
                    vm.writePush(st.kindOf(first), st.indexOf(first));
                    // consume open paren
                    getSymbol();
                    int nArgs = compileExpressionList();
                    // consume close paren
                    getSymbol();
                    vm.writeCall(st.typeOf(first) + "." + second, nArgs + 1);
                }
            } else {
                vm.writePush(st.kindOf(first), st.indexOf(first));
            }
        } else if(jt.tokenType() == JackTokenizer.SYMBOL && jt.symbol() == '(') {
            // consume open paren
            getSymbol();
            compileExpression();
            // consume close paren
            getSymbol();
        } else if(isUnaryOp()) {
            char op = getSymbol();
            compileTerm();
            if(op == '-') vm.writeArithmetic("neg");
            else if(op == '~') vm.writeArithmetic("not");
        }
    }

    private int compileExpressionList() {   // prints compiled expression list and returns
        int numExps = 0;                    // how many expressions were compiled
        numExps += compileExpression();
        while(jt.tokenType() == JackTokenizer.SYMBOL && jt.symbol() == ',') {
                // consume comma
                getSymbol();
                numExps += compileExpression();
        }
        return numExps;
    }

    private String getKeyword() {
        String keyword = jt.keyword();
        if(jt.hasMoreTokens()) jt.advance();
        return keyword;
    }

    private String getIdentifier() {
        String id = jt.identifier();
        if(jt.hasMoreTokens()) jt.advance();
        return id;
    }

    private char getSymbol() {
        char symbol = jt.symbol();
        if(jt.hasMoreTokens()) jt.advance();
        return symbol;
    }

    private int getIntConstant() {
        int intConst = jt.intVal();
        if(jt.hasMoreTokens()) jt.advance();
        return intConst;
    }

    private String getStrConstant() {
        String strConst = jt.stringVal();
        if(jt.hasMoreTokens()) jt.advance();
        return strConst;
    }

    private String getType() {
        if(jt.tokenType() == JackTokenizer.KEYWORD && (jt.keyword().equalsIgnoreCase("int")
                || jt.keyword().equalsIgnoreCase("char") || jt.keyword().equalsIgnoreCase("boolean")
                || jt.keyword().equalsIgnoreCase("void"))) {
            return getKeyword();
        } else if(jt.tokenType() == JackTokenizer.IDENTIFIER) {
            return getIdentifier();
        }
        return null;
    }

    private ArrayList<String> getVarNames() {
        ArrayList<String> varNames = new ArrayList<String>();
        varNames.add(getIdentifier());
        while(!(jt.tokenType() == JackTokenizer.SYMBOL && jt.symbol() == ';')) {
            getSymbol();
            varNames.add(getIdentifier());
        }
        getSymbol();
        for(String name : varNames) {
        }
        return varNames;
    }

    private boolean isOperator() {
        if(jt.tokenType() == JackTokenizer.SYMBOL) {
            return (jt.symbol() == '+' || jt.symbol() == '-' || jt.symbol() == '*'
                    || jt.symbol() == '/' || jt.symbol() == '&' || jt.symbol() == '|' || jt.symbol() == '<'
                    || jt.symbol() == '>' || jt.symbol() == '=');
        } else {
            return false;
        }

    }

    private boolean isKeywordConstant() {
        if(jt.tokenType() == JackTokenizer.KEYWORD) {
            return jt.keyword().equalsIgnoreCase("true")
                    || jt.keyword().equalsIgnoreCase("false") || jt.keyword().equalsIgnoreCase("null")
                    || jt.keyword().equalsIgnoreCase("this");
        } else {
            return false;
        }

    }

    private boolean isUnaryOp() {
        return (jt.tokenType() == JackTokenizer.SYMBOL && (jt.symbol() == '-' || jt.symbol() == '~'));
    }

    private boolean isStartOfTerm() {
        if(jt.tokenType() == JackTokenizer.INT_CONST) return true;
        if(jt.tokenType() == JackTokenizer.STRING_CONST) return true;
        if(isKeywordConstant()) return true;
        if(jt.tokenType() == JackTokenizer.IDENTIFIER) return true;
        if(jt.tokenType() == JackTokenizer.SYMBOL && jt.symbol() == '(') return true;
        if(isUnaryOp()) return true;
        return false;
    }

    private boolean isPrimitive(String type) {
        if(type.equals("int")) return true;
        if(type.equals("char")) return true;
        if(type.equals("boolean")) return true;
        return false;
    }
 }
