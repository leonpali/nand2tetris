import java.io.File;
import java.util.Scanner;
import java.io.IOException;
import java.util.LinkedList;

public class Parser {

    private Scanner sc;
    private LinkedList<String> codeAfterFirstParse = new LinkedList<>();
    private CodeGenerator codeGen;

    public Parser(String file) throws IOException {
        File vmFile = new File(file);
        this.sc = new Scanner(vmFile);
        this.codeGen = new CodeGenerator(file);
        this.codeGen.setCurrentVMFile(file);
    }

    public Parser(String file, CodeGenerator sharedCodeGen) throws IOException {
        this.sc = new Scanner(new File(file));
        this.codeGen = sharedCodeGen;
    }

    public void start() throws IOException {
        firstParse();
        secondParse();
    }

    private void firstParse() {
        while(sc.hasNextLine()) {
            String line = sc.nextLine();
            String withoutComments = line.replaceAll("//.*", "").trim();
            if(!withoutComments.isEmpty()) codeAfterFirstParse.add(withoutComments);
        }
        sc.close();
    }

    private void secondParse() throws IOException {
        while(hasMoreCommands()) {
            String currentCommand = codeAfterFirstParse.pop();
            switch (currentCommand.split(" ")[0]) {
                case "push":
                    codeGen.writePushPop("C_PUSH", currentCommand.split(" ")[1], Integer.parseInt(currentCommand.split(" ")[2]));
                    break;
                case "pop":
                    codeGen.writePushPop("C_POP", currentCommand.split(" ")[1], Integer.parseInt(currentCommand.split(" ")[2]));
                    break;
                case "label":
                    codeGen.writeLabel(currentCommand.split(" ")[1]);
                    break;
                case "goto":
                    codeGen.writeGoto(currentCommand.split(" ")[1]);
                    break;
                case "if-goto":
                    codeGen.writeIf(currentCommand.split(" ")[1]);
                    break;
                case "function":
                    codeGen.writeFunction(currentCommand.split(" ")[1], Integer.parseInt(currentCommand.split(" ")[2]));
                    break;
                case "call":
                    codeGen.writeCall(currentCommand.split(" ")[1], Integer.parseInt(currentCommand.split(" ")[2]));
                    break;
                case "return":
                    codeGen.writeReturn();
                    break;
                default:
                    codeGen.writeArithmetic(currentCommand);
            }
        }
    }

    private boolean hasMoreCommands() {
        return codeAfterFirstParse.size() > 0;
    }

    public void closeCodeGen() throws IOException {
        codeGen.close();
    }
}