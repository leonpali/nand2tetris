import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.HashSet;

public class CompilationEngine {

    private LinkedList<Token> tokens;
    private Token currentToken;
    private FileWriter writer;
    private HashSet<String> operators = new HashSet<>();

    public CompilationEngine(LinkedList<Token> tokens, File file) throws IOException {
        this.tokens = tokens;
        this.writer = new FileWriter(new File(file.getName().replace(".jack", ".xml")));
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
        compileToken();
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

        writer.write("<" + tag + "> " + value + " </" + tag + ">\n");
    }

    public void compileClass() throws IOException {
        writer.write("<class>\n");

        advance();
        advance();
        advance();

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

        advance();

        writer.write("</class>\n");

        writer.close();
    }

    private void compileClassVarDec() throws IOException {
        writer.write("<classVarDec>\n");

        advance();
        advance();
        advance();

        while (tokens.peek().getValue().equals(",")) {
            advance();
            advance();
        }

        advance();

        writer.write("</classVarDec>\n");
    }

    private void compileSubroutine() throws IOException {
        writer.write("<subroutineDec>\n");
        
        advance();
        advance();
        advance();
        advance();

        if (tokens.peek().getValue().equals("(")) {
            advance();
        }

        compileParameterList();

        advance();

        compileSubroutineBody();

        writer.write("</subroutineDec>\n");
    }

    private void compileParameterList() throws IOException {
        writer.write("<parameterList>\n");
        while (!tokens.peek().getValue().equals(")")) {
            advance();
            advance();
            if (tokens.peek().getValue().equals(",")) {
                advance();
            }
        }
        writer.write("</parameterList>\n");
    }

    private void compileSubroutineBody() throws IOException {
        writer.write("<subroutineBody>\n");

        advance();

        while (tokens.peek().getValue().equals("var")) {
            compileVarDec();
        }
        compileStatements();

        advance();

        writer.write("</subroutineBody>\n");
    }

    private void compileVarDec() throws IOException {
        writer.write("<varDec>\n");

        advance();
        advance();
        advance();

        while (tokens.peek().getValue().equals(",")) {
            advance();
            advance();
        }
        advance();

        writer.write("</varDec>\n");
    }

    private void compileStatements() throws IOException {
        writer.write("<statements>\n");

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

        writer.write("</statements>\n");
    }

    private void compileDo() throws IOException {
        writer.write("<doStatement>\n");

        advance();
        compileSubroutineCall();
        advance();

        writer.write("</doStatement>\n");
    }

    private void compileLet() throws IOException {
        writer.write("<letStatement>\n");

        advance();
        advance();

        if (tokens.peek().getValue().equals("[")) {
            advance();
            compileExpression();
            advance();
        }

        advance();
        compileExpression();
        advance();

        writer.write("</letStatement>\n");
    }

    private void compileWhile() throws IOException {
        writer.write("<whileStatement>\n");

        advance();
        advance();
        compileExpression();
        advance();
        advance();
        compileStatements();
        advance();

        writer.write("</whileStatement>\n");
    }

    private void compileReturn() throws IOException {
        writer.write("<returnStatement>\n");

        advance();

        if(!tokens.peek().getValue().equals(";")) {
            compileExpression();
        }
        advance();

        writer.write("</returnStatement>\n");
    }

    private void compileIf() throws IOException {
        writer.write("<ifStatement>\n");

        advance();
        advance();
        compileExpression();
        advance();
        advance();
        compileStatements();
        advance();
        if (tokens.peek().getValue().equals("else")) {
            advance();
            advance();
            compileStatements();
            advance();
        }

        writer.write("</ifStatement>\n");
    }

    private void compileExpression() throws IOException {
        writer.write("<expression>\n");
        compileTerm();
        while (operators.contains(tokens.peek().getValue())) {
            advance();
            compileTerm();
        }

        writer.write("</expression>\n");
    }

    private void compileTerm() throws IOException {
        writer.write("<term>\n");

        Token token = tokens.peek();

        if (token.getValue().equals("(")) {
            advance();
            compileExpression();
            advance(); 
        } else if (token.getValue().equals("-") || token.getValue().equals("~")) {
            advance();
            compileTerm();
        } else if (token.getType() == TokenType.IDENTIFIER) {
            Token next = tokens.get(1);

            if (next.getValue().equals("(") || next.getValue().equals(".")) {
                compileSubroutineCall();
            } else {
                advance(); 
                if (tokens.peek().getValue().equals("[")) {
                    advance();
                    compileExpression();
                    advance(); 
                }
            }
        } else if (token.getType() == TokenType.INT_CONST ||
                token.getType() == TokenType.STRING_CONST ||
                token.getType() == TokenType.KEYWORD) {
            advance(); 
        }

        writer.write("</term>\n");
    }

    private void compileExpressionList() throws IOException {
        writer.write("<expressionList>\n");

        while (!tokens.peek().getValue().equals(")")) {
            compileExpression();
            if (tokens.peek().getValue().equals(",")) {
                advance();
            }
        }

        writer.write("</expressionList>\n");
    }

    private void compileSubroutineCall() throws IOException {
        advance();
        if (tokens.peek().getValue().equals(".")) {
            advance();
            advance();
            advance();
            compileExpressionList();
            advance();
        } else {
            advance();
            compileExpressionList();
            advance();
        }
    }
}