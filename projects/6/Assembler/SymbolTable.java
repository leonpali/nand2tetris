import java.util.Hashtable;

public class SymbolTable {

    private final Hashtable<String, Integer> addresses = new Hashtable<>();
    private int counter = 16;

    public SymbolTable() {
        addresses.put("R0", 0);
        addresses.put("R1", 1);
        addresses.put("R2", 2);
        addresses.put("R3", 3);
        addresses.put("R4", 4);
        addresses.put("R5", 5);
        addresses.put("R6", 6);
        addresses.put("R7", 7);
        addresses.put("R8", 8);
        addresses.put("R9", 9);
        addresses.put("R10", 10);
        addresses.put("R11", 11);
        addresses.put("R12", 12);
        addresses.put("R13", 13);
        addresses.put("R14", 14);
        addresses.put("R15", 15);
        addresses.put("SCREEN", 16384);
        addresses.put("KBD", 24576);
        addresses.put("SP", 0);
        addresses.put("LCL", 1);
        addresses.put("ARG", 2);
        addresses.put("THIS", 3);
        addresses.put("THAT", 4);
    }

    public int addSymbol(String symbol) {
        addresses.put(symbol, counter);
        counter++;
        return counter;
    }

    public int getAddress(String symbol) {
        return addresses.getOrDefault(symbol, -1);
    }
}