import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.HashSet;

public class CompilationEngine {

    private LinkedList<Token> tokens;
    private Token currentToken;
    private HashSet<String> operators = new HashSet<>();
    private SymbolTable symbolTable;
    private VMWriter vmWriter;
    private String className;
    private int labelCounter = 0;

    public CompilationEngine(LinkedList<Token> tokens, File file) throws IOException {
        this.tokens = tokens;
        this.symbolTable = new SymbolTable();
        this.vmWriter = new VMWriter(file);
        this.className = file.getName().replace(".jack", "");
        operators.add("+");
        operators.add("-");
        operators.add("*");
        operators.add("/");
        operators.add("&");
        operators.add("|");
        operators.add("<");
        operators.add(">");
        operators.add("=");
    }

    private void advance() throws IOException {
        currentToken = tokens.removeFirst();
        //compileToken();
    }

    private void compileToken() throws IOException {
        String value = currentToken.getValue();
        String tag = "";

        switch (currentToken.getType()) {
            case TokenType.KEYWORD:
                tag = "keyword";
                break;
            case TokenType.SYMBOL:
                tag = "symbol";
                value = value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
                break;
            case TokenType.IDENTIFIER:
                tag = "identifier";
                break;
            case TokenType.INT_CONST:
                tag = "integerConstant";
                break;
            case TokenType.STRING_CONST:
                tag = "stringConstant";
                value = value.replace("\"", "");
                break;
        }
    }

    public void compileClass() throws IOException {
        
        advance(); // class
        advance(); // className
        advance(); // {

        while (tokens.peek().getValue().equals("static") ||
            tokens.peek().getValue().equals("field")
            ) {
            compileClassVarDec();
        }

        while (tokens.peek().getValue().equals("constructor") ||
            tokens.peek().getValue().equals("function") ||
            tokens.peek().getValue().equals("method") 
            ) {
            compileSubroutine();
        }

        advance(); // }
        vmWriter.close();
    }

    private void compileClassVarDec() throws IOException {
        advance(); // static/field
        String kind = currentToken.getValue();
        advance(); // type
        String type = currentToken.getValue();
        advance(); // varName
        String name = currentToken.getValue();
        symbolTable.add(name, type, kind);

        while (tokens.peek().getValue().equals(",")) {
            advance(); // ,
            advance(); // varName
            name = currentToken.getValue();
            symbolTable.add(name, type, kind);
        }

        advance(); // ;
    }

    private void compileSubroutine() throws IOException {
        symbolTable.startSubroutine();
        advance(); // cunstrucor/function/method
        String kind = currentToken.getValue();
        advance(); // return Type
        String type = currentToken.getValue();
        advance(); // subroutineName
        String name = currentToken.getValue();
        advance(); // (

        if (tokens.peek().getValue().equals("(")) {
            advance(); // ich weiß ehrlichgesagt nicht mehr wofür die Zeile ist aber ich hab Angst sie zu entfernen
        }

        if (kind.equals("method")) {
            symbolTable.add("this", className, "argument");
        }

        compileParameterList();

        advance(); // )

        compileSubroutineBody(kind, name);
        symbolTable.clearSubroutine();
    }

    private void compileParameterList() throws IOException {
        while (!tokens.peek().getValue().equals(")")) {
            advance(); // type
            String type = currentToken.getValue();
            advance(); // varName
            String name = currentToken.getValue();
            symbolTable.add(name, type, "argument");
            if (tokens.peek().getValue().equals(",")) {
                advance(); // ,
            }
        }
    }

    private void compileSubroutineBody(String subroutineKind, String subroutineName) throws IOException {
        advance(); // {

        while (tokens.peek().getValue().equals("var")) {
            compileVarDec();
        }

        int nLocals = symbolTable.varCount("local"); 
        vmWriter.writeSubroutine(subroutineName, nLocals);

        if (subroutineKind.equals("method")) {
            vmWriter.writePush("argument", 0);
            vmWriter.writePop("pointer", 0);
        } else if (subroutineKind.equals("constructor")) {
            int fieldCount = symbolTable.varCount("this");
            vmWriter.writePush("constant", fieldCount);
            vmWriter.writeCall("Memory.alloc", fieldCount);
            vmWriter.writePop("pointer", 0);
        }

        compileStatements();

        advance(); // }
    }

    private void compileVarDec() throws IOException {
        advance(); // var 
        advance(); // type
        String type = currentToken.getValue();
        advance(); // varName
        String name = currentToken.getValue();
        symbolTable.add(name, type, "local");

        while (tokens.peek().getValue().equals(",")) {
            advance(); // ,
            advance(); // varName
            name = currentToken.getValue();
            symbolTable.add(name, type, "local");
        }
        advance(); // ;
    }

    private void compileStatements() throws IOException {
        while (!tokens.peek().getValue().equals("}")) {
            switch(tokens.peek().getValue()) {
                case "let":
                    compileLet();
                    break;
                case "do":
                    compileDo();
                    break;
                case "if":
                    compileIf();
                    break;
                case "while":
                    compileWhile();
                    break;
                case "return":
                    compileReturn();
                    break;
                default:
                    System.out.println("Unbekanntes Statement: " + tokens.peek().getValue());
                    advance();
                    break;
            }
        }
    }

    private void compileDo() throws IOException {
        advance(); // do
        compileSubroutineCall();
        vmWriter.writePop("temp", 0);
        advance(); // ;
    }

    private void compileLet() throws IOException {
        advance(); // let
        advance(); // varName
        String name = currentToken.getValue();
        boolean isArray = false;

        if (tokens.peek().getValue().equals("[")) {
            isArray = true;
            advance(); // [
            compileExpression();
            advance(); // ]
            vmWriter.writePush(symbolTable.getVarKind(name), symbolTable.getVarIndex(name));
            vmWriter.writeArithmetic("add");
        }

        advance(); // =
        compileExpression();
        advance(); // ;

        if (isArray) {
            vmWriter.writePop("temp", 0);
            vmWriter.writePop("pointer", 1);
            vmWriter.writePush("temp", 0);
            vmWriter.writePop("that", 0);
        } else {
            vmWriter.writePop(symbolTable.getVarKind(name), symbolTable.getVarIndex(name));
        }
    }

    private void compileWhile() throws IOException {
        int label = labelCounter++;
        String startLabel = "WHILE_START" + label;
        String endLabel = "WHILE_END" + label;
        vmWriter.writeLabel(startLabel);

        advance(); // while
        advance(); // (
        compileExpression();
        advance(); // )
        vmWriter.writeArithmetic("not");
        vmWriter.writeIf(endLabel);
        advance(); // {
        compileStatements();
        advance(); // }
        vmWriter.writeGoto(startLabel);
        vmWriter.writeLabel(endLabel);
    }

    private void compileReturn() throws IOException {
        advance(); // return

        if(!tokens.peek().getValue().equals(";")) {
            compileExpression();
        } else {
            vmWriter.writePush("constant", 0);
        }
        advance(); // ;
        vmWriter.writeReturn();
    }

    private void compileIf() throws IOException {
        int label = labelCounter++;
        advance(); // if
        advance(); // (
        compileExpression();
        advance(); // )
        vmWriter.writeArithmetic("not");
        String elseLabel = "IF_ELSE" + label;
        String endLabel = "IF_END" + label;
        vmWriter.writeIf(elseLabel);

        advance(); // {
        compileStatements();
        advance(); // }
        vmWriter.writeGoto(endLabel);
        vmWriter.writeLabel(elseLabel);

        if (tokens.peek().getValue().equals("else")) {
            advance(); // else
            advance(); // {
            compileStatements();
            advance(); // }
        }

        vmWriter.writeLabel(endLabel);
    }

    private void compileExpression() throws IOException {
        compileTerm();
        while (operators.contains(tokens.peek().getValue())) {
            advance(); // op
            String op = currentToken.getValue();
            compileTerm();
            switch (op) {
                case "+": 
                    vmWriter.writeArithmetic("add"); 
                    break;
                case "-": 
                    vmWriter.writeArithmetic("sub"); 
                    break;
                case "*": 
                    vmWriter.writeCall("Math.multiply", 2); 
                    break;
                case "/": 
                    vmWriter.writeCall("Math.divide", 2); 
                    break;
                case "&": 
                    vmWriter.writeArithmetic("and"); 
                    break;
                case "|": 
                    vmWriter.writeArithmetic("or"); 
                    break;
                case "<": 
                    vmWriter.writeArithmetic("lt"); 
                    break;
                case ">": 
                    vmWriter.writeArithmetic("gt"); 
                    break;
                case "=": 
                    vmWriter.writeArithmetic("eq"); 
                    break;
            }
        }
    }

    private void compileTerm() throws IOException {
        Token token = tokens.peek();

        if (token.getValue().equals("(")) {
            advance(); // (
            compileExpression();
            advance(); // )
            return;
        }

        if (token.getValue().equals("-") || token.getValue().equals("~")) {
            String unaryOp = token.getValue();
            advance();
            compileTerm();
            if (unaryOp.equals("-")) {
                vmWriter.writeArithmetic("neg");
            } else {
                vmWriter.writeArithmetic("not");
            }
            return;
        }

        if (token.getType() == TokenType.INT_CONST) {
            advance(); // Zahl
            int value = Integer.parseInt(currentToken.getValue());
            vmWriter.writePush("constant", value);
            return;
        }

        if (token.getType() == TokenType.STRING_CONST) {
            advance(); // String
            String str = currentToken.getValue();
            int length = str.length();
            vmWriter.writePush("constant", length);
            vmWriter.writeCall("String.new", 1);
            for (char c : str.toCharArray()) {
                vmWriter.writePush("constant", (int) c);
                vmWriter.writeCall("String.appendChar", 2);
            }
            return;
        }

        if (token.getType() == TokenType.KEYWORD) {
            advance(); // Keyword
            switch (currentToken.getValue()) {
                case "true":
                    vmWriter.writePush("constant", 0);
                    vmWriter.writeArithmetic("not");
                    break;
                case "false":
                case "null":
                    vmWriter.writePush("constant", 0);
                    break;
                case "this":
                    vmWriter.writePush("pointer", 0);
                    break;
            }
            return;
        }

        if (token.getType() == TokenType.IDENTIFIER) {
            Token next = tokens.get(1);
            advance(); // identifier

            String name = currentToken.getValue();

            if (next.getValue().equals("[")) {
                advance(); // [
                compileExpression();
                advance(); // ]
                Symbol symbol = symbolTable.getSymbol(name);
                vmWriter.writePush(symbol.getKind(), symbol.getIndex());
                vmWriter.writeArithmetic("add");
                vmWriter.writePop("pointer", 1);
                vmWriter.writePush("that", 0);
            } else if (next.getValue().equals("(") || next.getValue().equals(".")) {
                tokens.addFirst(currentToken);
                compileSubroutineCall();
            } else {
                Symbol symbol = symbolTable.getSymbol(name);
                vmWriter.writePush(symbol.getKind(), symbol.getIndex());
            }
            return;
        }

        throw new RuntimeException("Unbekannter Term: " + token.getValue());
    }


    private int compileExpressionList() throws IOException {
        int nArgs = 0;

        if (!tokens.peek().getValue().equals(")")) {
            compileExpression();
            nArgs++;

            while (tokens.peek().getValue().equals(",")) {
                advance();
                compileExpression();
                nArgs++;
            }
        }

        return nArgs;
    }

    private void compileSubroutineCall() throws IOException {
        int nArgs = 0;
        advance(); // objectName
        String name = currentToken.getValue();

        if (tokens.peek().getValue().equals(".")) {
            advance(); // .
            advance(); // methodName
            String methodName = currentToken.getValue();

            Symbol symbol = symbolTable.getSymbol(name);
            if (symbol != null) {
                vmWriter.writePush(symbol.getKind(), symbol.getIndex());
                name = symbol.getType() + "." + methodName;
                nArgs = 1;
            } else {
                name = name + "." + methodName;
            }
        } else {
            vmWriter.writePush("pointer", 0);
            name = className + "." + name;
            nArgs = 1;
        }

        advance(); // (
        nArgs += compileExpressionList();
        advance(); // )

        vmWriter.writeCall(name, nArgs);
    }
}