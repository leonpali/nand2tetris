import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.util.Scanner;

public class VMTranslator {
    public static void main(String[] args) {
        try {
            String file = args[0];
            if (file.contains(".vm")) {
                Parser parser = new Parser(file);
                parser.start();
                parser.closeCodeGen();
            } else {
                File folder = new File(file);
                String asmFilePath = file + File.separator + folder.getName() + ".asm";
                CodeGenerator codeGen = new CodeGenerator(asmFilePath, true);

                File[] vmFiles = folder.listFiles((dir, name) -> name.endsWith(".vm"));
                if (vmFiles != null) {
                    for (File f: vmFiles) {
                        codeGen.setCurrentVMFile(f.getName());
                        Parser parser = new Parser(f.getPath(), codeGen);
                        parser.start();
                    }
                }
                codeGen.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}