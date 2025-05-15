import java.io.File;
import java.io.IOException;

public class VMTranslator {
    public static void main(String[] args) {
        try {
            String file = args[0];
            Parser parser = new Parser(file);
            parser.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}