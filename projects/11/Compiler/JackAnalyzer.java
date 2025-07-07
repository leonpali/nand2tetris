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
                CompilationEngine compiler = new CompilationEngine(tokens, file);
                compiler.compileClass();

            } else {
                File folder = new File(path);
                File[] files = folder.listFiles((dir, name) -> name.endsWith(".jack"));
                if(files != null) {
                    for(File f: files) {
                        JackTokenizer tokenizer = new JackTokenizer(f);
                        LinkedList<Token> tokens = tokenizer.tokenize();
                        System.out.println(tokens.size());
                        CompilationEngine compiler = new CompilationEngine(tokens, f);
                        compiler.compileClass();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}