import java.util.Scanner;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        try {
            String file = args[0];
            Scanner sc = new Scanner(new File(file));
            Parser parser = new Parser();

            while(sc.hasNextLine()) {
                parser.firstParse(sc.nextLine());
            }
            sc.close();

            String code = parser.secondParse();

            String fileName = file.substring(0, 1).toUpperCase() + file.substring(1).replace("asm", "hack");
            File hackFile = new File(fileName);
            hackFile.createNewFile();

            FileWriter writer = new FileWriter(fileName);
            writer.write(code);
            
            if(code.contains("Error")) {
                System.out.println("File contains errors");
            }

            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}