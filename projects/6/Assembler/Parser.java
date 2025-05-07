import java.util.LinkedList;

public class Parser {

    private final CodeGenerator codeGen = new CodeGenerator();
    private String code = "";
    private final LinkedList<String> codeAfterFirstParse = new LinkedList<>();
    private int counter = 0;

    public Parser() {}

    public void firstParse(String line) {
        String withoutComments = line.replaceAll("//.*", "").trim();
        if(withoutComments.isEmpty()) return;

        if(withoutComments.matches("\\([^)]*\\)")) {
            String label = withoutComments.replace("(","").trim();
            codeGen.addLabel(label.replace(")",""), counter);
        } else {
            counter++;
            codeAfterFirstParse.add(withoutComments);
        }
    }

    public String secondParse() {
        for(String line: codeAfterFirstParse) {
            if(line.charAt(0) != '@') {
                code += codeGen.cInstruction(line);
            } else {
                String inst = line.replace("@","");
                code += codeGen.aInstruction(inst);
            }
        }
        return code;
    }
}