import java.util.LinkedList;
import java.util.HashMap;

public class SymbolTable {

    private HashMap<String, Symbol> classScope;
    private HashMap<String, Symbol> subroutineScope;
    private HashMap<String, Integer> indexCounters;
    private LinkedList<HashMap<String, Symbol>> scopeList;
    private LinkedList<HashMap<String, Integer>> counterList;

    public SymbolTable() {
        this.classScope = new HashMap<>();
        this.subroutineScope = new HashMap<>();
        this.indexCounters = new HashMap<>();
        this.scopeList = new LinkedList<>();
        this.counterList = new LinkedList<>();
        scopeList.add(subroutineScope);
        resetCounters();
        indexCounters.put("this", 0);
        counterList.add(indexCounters);
    }

    public void add(String name, String type, String kind) {
        String segment = kind;
        if (segment.equals("field")) segment = "this";
        int index = varCount(segment);
        Symbol symbol = new Symbol(type, segment, index);

        if (kind.equals("static") || kind.equals("field")) {
            classScope.put(name, symbol);
        } else {
            subroutineScope.put(name, symbol);
        }
        indexCounters.put(segment, index + 1);
    }

    public void startSubroutine() {
        this.subroutineScope = new HashMap<>();
        int thisCounter = indexCounters.get("this");
        int staticCounter = indexCounters.get("static");
        this.indexCounters = new HashMap<>();
        scopeList.add(subroutineScope);
        counterList.add(indexCounters);
        indexCounters.put("argument", 0);
        indexCounters.put("local", 0);
        indexCounters.put("this", thisCounter);
        indexCounters.put("static", staticCounter);
    }

    public void clearSubroutine() {
        scopeList.pop();
        counterList.pop();
        this.subroutineScope = scopeList.peekLast();
        this.indexCounters = counterList.peekLast();
    }

    public Symbol getSymbol(String name) {
        if (subroutineScope.containsKey(name)) return subroutineScope.get(name);
        return classScope.get(name);
    }

    private void resetCounters() {
        indexCounters.put("static", 0);
        indexCounters.put("argument", 0);
        indexCounters.put("local", 0);
        indexCounters.put("pointer", 0);
    } 
    
    public int varCount(String kind) {
        String segment = kind;
        if (kind.equals("field")) {
            segment = "this";
        }
        return indexCounters.getOrDefault(segment, 0);
    }

    public String getVarKind(String name) {
        Symbol symbol = subroutineScope.get(name);
        if (symbol == null) {
            symbol = classScope.get(name);
        }
        if (symbol == null) {
            throw new IllegalArgumentException("Variable not found in symbol table: " + name);
        }

        switch (symbol.getKind()) {
            case "local":
                return "local";
            case "argument":
                return "argument";
            case "static":
                return "static";
            case "field":
                return "this";
            case  "pointer":
                return "pointer";
            case "this":
                return "this";
            default:
                throw new IllegalArgumentException("Ungültiger Kind-Typ: " + symbol.getKind());
        }
    }


    public int getVarIndex(String name) {
        Symbol symbol = subroutineScope.get(name);
        if (symbol == null) {
            symbol = classScope.get(name);
        }
        if (symbol == null) {
            throw new IllegalArgumentException("Variable not found in symbol table: " + name);
        }
        return symbol.getIndex();
    }
}