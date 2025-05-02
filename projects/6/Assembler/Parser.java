import java.util.LinkedList;

public class Parser {

    private final CodeGenerator codeGen = new CodeGenerator();
    private String code = "";
    private final SymbolTable symbols = new SymbolTable();
    private final LinkedList<String> codeAfterFirstParse = new LinkedList<>();

    public Parser() {}

    public void firstParse(String line) {
        String withoutComments = line.replaceAll("//.*", "").trim();
        if(withoutComments.isEmpty()) return;

        if(withoutComments.matches("\\([^)]*\\)")) {
            String symbol = withoutComments.replace("(","").trim();
            symbols.addSymbol(symbol.replace(")","").trim());
        } else {
            codeAfterFirstParse.add(withoutComments);
        }
    }

    public String secondParse() {
        for(String line: codeAfterFirstParse) {
            if(line.charAt(0) != '@') {
                if(line.contains("=")) {
                    String[] destAndComp = line.split("=");
                    if(destAndComp[1].contains(";")) {
                        String[] compAndJmp = destAndComp[1].split(";");
                        String binary = codeGen.getCode(compAndJmp[0], destAndComp[0], compAndJmp[1]);
                        code += "111" + binary + "\n";
                    } else {
                        String binary = codeGen.getCode(destAndComp[1], destAndComp[0], "null");
                        code += "111" + binary + "\n";
                    }
                } else {
                    String[] compAndJmp = line.split(";");
                    String binary = codeGen.getCode(compAndJmp[0], "null", compAndJmp[1]);
                    code += "111" + binary + "\n";
                }
            } else {
                String inst = line.replace("@","");
                if(inst.matches("^[0-9][0-9]*$")) {
                    int num = Integer.parseInt(inst);
                    String binary = String.format("%15s", Integer.toBinaryString(num)).replace(" ", "0");
                    code += "0" + binary + "\n";
                } else {
                    if(symbols.getAddress(inst) != -1) {
                        String binary = String.format("%15s", Integer.toBinaryString(symbols.getAddress(inst))).replace(" ", "0");
                        code += "0" + binary + "\n";
                    } else {
                        int newAddress = symbols.addSymbol(inst);
                        String binary = String.format("%15s", Integer.toBinaryString(newAddress)).replace(" ", "0");
                        code += "0" + binary + "\n";
                    }
                }
            }
        }

        return code;
    }
}