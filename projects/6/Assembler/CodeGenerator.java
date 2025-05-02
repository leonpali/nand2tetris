import java.util.Hashtable;

public class CodeGenerator {

    private final Hashtable<String, String> computationTable = new Hashtable<>();
    private final Hashtable<String, String> destinationTable = new Hashtable<>();
    private final Hashtable<String, String> jumpTable = new Hashtable<>();

    public CodeGenerator() {
        computationTable.put("0", "0101010");
        computationTable.put("1", "0111111");
        computationTable.put("-1", "0111010");
        computationTable.put("D", "0001100");
        computationTable.put("A", "0110000");
        computationTable.put("M", "1110000");
        computationTable.put("!D", "0001101");
        computationTable.put("!A", "0110001");
        computationTable.put("!M", "1110001");
        computationTable.put("-D", "0001111");
        computationTable.put("-A", "0110011");
        computationTable.put("-M", "1110011");
        computationTable.put("D+1", "0011111");
        computationTable.put("A+1", "0110111");
        computationTable.put("M+1", "1110111");
        computationTable.put("D-1", "0001110");
        computationTable.put("A-1", "0110010");
        computationTable.put("M-1", "1110010");
        computationTable.put("D+A", "0000010");
        computationTable.put("D+M", "1000010");
        computationTable.put("D-A", "0010011");
        computationTable.put("D-M", "1010011");
        computationTable.put("A-D", "0000111");
        computationTable.put("M-D", "1000111");
        computationTable.put("D&A", "0000000");
        computationTable.put("D&M", "1000000");
        computationTable.put("D|A", "0010101");
        computationTable.put("D|M", "1010101");

        destinationTable.put("null", "000");
        destinationTable.put("M", "001");
        destinationTable.put("D", "010");
        destinationTable.put("MD", "011");
        destinationTable.put("DM", "011");
        destinationTable.put("A", "100");
        destinationTable.put("AM", "101");
        destinationTable.put("AD", "110");
        destinationTable.put("AMD", "111");
        destinationTable.put("ADM", "111");

        jumpTable.put("null", "000");
        jumpTable.put("JGT", "001");
        jumpTable.put("JEQ", "010");
        jumpTable.put("JGE", "011");
        jumpTable.put("JLT", "100");
        jumpTable.put("JNE", "101");
        jumpTable.put("JLE", "110");
        jumpTable.put("JMP", "111");
    }

    private String getComp(String symbol) {
        return computationTable.getOrDefault(symbol, "Error: Unknown symbol in computation part: " + symbol);
    }

    private String getDest(String symbol) {
        return destinationTable.getOrDefault(symbol, "Error: Unknown symbol in destination part: " + symbol);
    }

    private String getJmp(String symbol) {
        return jumpTable.getOrDefault(symbol, "Error: Unknown symbol in jump part: " + symbol);
    }

    public String getCode(String compSym, String destSym, String jmpSym) {
        String comp = getComp(compSym);
        String dest = getDest(destSym);
        String jmp = getJmp(jmpSym);

        return comp + dest + jmp;
    }
}