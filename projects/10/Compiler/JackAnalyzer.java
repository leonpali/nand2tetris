import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.util.LinkedList;

public class JackAnalyzer {

    public static void main(String[] args) {
        try {
            String path = args[0];
            if (path.contains(".jack")) {
                File file = new File(path);
                JackTokenizer tokenizer = new JackTokenizer(file);
                LinkedList<Token> tokens = tokenizer.tokenize();
                FileWriter writer = new FileWriter(new File(file.getName().replace(".jack", "T.xml")));
                writer.write("<tokens>\n");
                tokens.forEach(token -> {
                    try {
                        writeToXml(token, writer);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                writer.write("</tokens>");
                writer.close();

                CompilationEngine compiler = new CompilationEngine(tokens, file);
                compiler.compileClass();

            } else {
                File folder = new File(path);
                File[] files = folder.listFiles((dir, name) -> name.endsWith(".jack"));
                if(files != null) {
                    for(File f: files) {
                        JackTokenizer tokenizer = new JackTokenizer(f);
                        LinkedList<Token> tokens = tokenizer.tokenize();
                        FileWriter writer = new FileWriter(new File(f.getName().replace(".jack", "T.xml")));
                        writer.write("<tokens>\n");
                        tokens.forEach(token -> {
                            try {
                                writeToXml(token, writer);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                        writer.write("</tokens>");
                        writer.close();
                        CompilationEngine compiler = new CompilationEngine(tokens, f);
                        compiler.compileClass();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void writeToXml(Token token, FileWriter writer) throws IOException {
        switch (token.getType()) {
            case TokenType.KEYWORD:
                writer.write("<keyword> ");
                writer.write(token.getValue());
                writer.write(" </keyword>\n");
                break;
            case TokenType.SYMBOL:
                writer.write("<symbol> ");
                switch (token.getValue()) {
                    case "<":
                        writer.write("&lt;");
                        break;
                    case ">":
                        writer.write("&gt;");
                        break;
                    case "\"":
                        writer.write("&quot;");
                        break;
                    case "&":
                        writer.write("&amp;");
                        break;
                    default:
                        writer.write(token.getValue());
                        break;
                }
                writer.write(" </symbol>\n");
                break;
            case TokenType.IDENTIFIER:
                writer.write("<identifier> ");
                writer.write(token.getValue());
                writer.write(" </identifier>\n");
                break;
            case TokenType.INT_CONST:
                writer.write("<integerConstant> ");
                writer.write(token.getValue());
                writer.write(" </integerConstant>\n");
                break;
            case TokenType.STRING_CONST:
                writer.write("<stringConstant> ");
                writer.write(token.getValue().replace("\"", ""));
                writer.write(" </stringConstant>\n");
                break;
        }
    }
}